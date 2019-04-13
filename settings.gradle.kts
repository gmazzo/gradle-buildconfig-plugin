includeBuild("plugin") {
    dependencySubstitution {
        substitute(module("com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin")).with(project(":"))
    }
}

include("example-generic", "example-groovy", "example-kts", "example-kts-js", "example-kts-multiplatform")
