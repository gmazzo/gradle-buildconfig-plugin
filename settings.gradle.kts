includeBuild("plugin") {
    dependencySubstitution {
        substitute(module("com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin")).with(project(":"))
    }
}

include("example-groovy", "example-kts")
