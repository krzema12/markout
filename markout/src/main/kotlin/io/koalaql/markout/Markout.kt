package io.koalaql.markout

import io.koalaql.markout.output.Output
import io.koalaql.markout.output.OutputDirectory
import io.koalaql.markout.output.OutputFile
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

@MarkoutDsl
interface Markout {
    @MarkoutDsl
    fun directory(name: String, builder: Markout.() -> Unit)
    @MarkoutDsl
    fun file(name: String, contents: String)
}

private fun buildOutput(builder: Markout.() -> Unit): OutputDirectory = OutputDirectory {
    val entries = hashMapOf<String, Output>()

    object : Markout {
        override fun directory(name: String, builder: Markout.() -> Unit) {
            entries[name] = buildOutput(builder)
        }

        override fun file(name: String, contents: String) {
            entries[name] = OutputFile { out ->
                out.writer().use { it.append(contents) }
            }
        }
    }.builder()

    entries
}

private val METADATA_FILE_NAME = Path(".markout")

private fun isEmpty(dir: Path) =
    Files.newDirectoryStream(dir).use { directory -> !directory.iterator().hasNext() }

private fun validMetadataPath(dir: Path, path: String): Path? {
    if (path.isBlank()) return null

    return dir
        .resolve(path)
        .normalize()
        .takeIf { it.parent == dir }
}

/* order is important here: metadata path should be the last to be deleted to allow crash recovery */
private fun metadataPaths(dir: Path): Sequence<Path> =
    try {
        val metadata = dir.resolve(METADATA_FILE_NAME)

        metadata
            .readText()
            .splitToSequence("\n")
            .mapNotNull { validMetadataPath(dir, it) }
            .plusElement(metadata) /* plusElement rather than plus bc Path : Iterable<Path> */
    } catch (ex: NoSuchFileException) {
        emptySequence()
    }

private fun cleanDirectory(dir: Path) {
    metadataPaths(dir).forEach { path ->
        if (Files.isDirectory(path)) {
            cleanDirectory(path)

            if (isEmpty(path)) Files.delete(path)
        } else {
            Files.deleteIfExists(path)
        }
    }
}

private fun Output.write(path: Path) {
    when (this) {
        is OutputDirectory -> {
            if (!Files.isDirectory(path)) {
                Files.createDirectory(path)
            }

            val entries = entries()

            /* write metadata first for graceful crash recovery */
            path.resolve(METADATA_FILE_NAME).writeText(entries.keys.joinToString(
                separator = "\n",
                postfix = "\n"
            ))

            entries.forEach { (name, output) ->
                output.write(path.resolve(name))
            }
        }
        is OutputFile -> {
            check (Files.notExists(path)) {
                "$path already exists as a user created file"
            }

            writeTo(Files.newOutputStream(path))
        }
    }
}

fun markout(
    path: Path,
    builder: Markout.() -> Unit
) {
    cleanDirectory(path)
    buildOutput(builder).write(path)
}