plugins {
    id("publish-plugin")
}

dependencies {
    implementation("com.github.node-gradle:gradle-node-plugin:3.5.1")
}

pluginBundle {
    website = "https://github.com/mfwgenerics/markout"
    vcsUrl = "https://github.com/mfwgenerics/markout.git"
    tags = listOf("kotlin", "markout", "markdown", "jvm", "documentation")
}

gradlePlugin {
    plugins {
        create("markoutPlugin") {
            id = "io.koalaql.markout"
            displayName = "Markout Docusaurus Plugin"
            description = "Plugin Support for Markout Powered Docusaurus Sites"
            implementationClass = "io.koalaql.markout.docusaurus.GradlePlugin"
        }
    }
}