package io.koalaql.markout

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import java.util.concurrent.Callable

class GradlePlugin: Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        target.extensions.create("markout", MarkoutConfig::class.java)

        fun execTask(name: String, builder: (JavaExec) -> Unit) = tasks
            .register(name, JavaExec::class.java) {
                val ext = target.extensions.getByType(MarkoutConfig::class.java)

                it.group = "markout"

                it.classpath = project
                    .files()
                    .from(Callable { project
                        .extensions
                        .getByType(JavaPluginExtension::class.java)
                        .sourceSets.getByName("main")
                        .runtimeClasspath
                    })

                it.environment("MARKOUT_PATH", (ext.rootDir ?: rootDir).absolutePath)

                if (ext.mainClass == null) throw GradleException("markout.mainClass was not configured")

                it.mainClass.set(ext.mainClass)

                builder(it)
            }

        val checkTask = execTask("markoutCheck") {
            it.description = "Check that Markout generated files are up-to-date."
            it.environment("MARKOUT_MODE", "expect")
        }

        execTask("markout") {
            it.description = "Generate and clean Markout files."
            it.environment("MARKOUT_MODE", "apply")
        }

        tasks
            .matching { it.name == "check" }
            .configureEach { it.dependsOn(checkTask) }
    }
}