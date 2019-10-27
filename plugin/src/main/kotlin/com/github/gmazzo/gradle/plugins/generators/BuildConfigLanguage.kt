package com.github.gmazzo.gradle.plugins.generators

enum class BuildConfigLanguage(
    generator: BuildConfigGenerator
) : BuildConfigGenerator by generator {
    JAVA(BuildConfigJavaGenerator),
    KOTLIN(BuildConfigKotlinGenerator)
}
