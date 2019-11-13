package com.github.gmazzo.gradle.plugins.generators

@Deprecated("Use `BuildConfigJavaGenerator` or `BuildConfigKotlinObjectGenerator` instead")
enum class BuildConfigLanguage(
    generator: BuildConfigGenerator
) : BuildConfigGenerator by generator {
    JAVA(BuildConfigJavaGenerator),
    KOTLIN(BuildConfigKotlinObjectGenerator)
}
