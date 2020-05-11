package com.github.gmazzo.gradle.plugins.generators

@Deprecated(
    message = "use BuildConfigKotlinGenerator instead",
    replaceWith = ReplaceWith("BuildConfigKotlinGenerator(topLevelConstants = true)")
)
object BuildConfigKotlinFileGenerator : BuildConfigKotlinGenerator()
