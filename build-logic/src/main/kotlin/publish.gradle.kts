plugins {
    id("conventions")

    `java-library`
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("markout") {
            from(components["java"])

            artifactId = project.name

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set("Markout")
                description.set("Source code capture plugin")
                url.set("https://markout.koalaql.io")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        name.set("Damien O'Hara")
                        url.set("https://github.com/mfwgenerics")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:mfwgenerics/markout.git")
                    developerConnection.set("scm:git@github.com:mfwgenerics/markout.git")
                    url.set("https://github.com/mfwgenerics/markout")
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_PASSWORD")
        )

        sign(publishing.publications["markout"])
    }

    repositories {
        maven {
            val repoId = System.getenv("REPOSITORY_ID")

            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$repoId/")

            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}