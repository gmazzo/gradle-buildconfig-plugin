package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.tasks.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigKotlinGenerator

enum class BuildConfigLanguage(
    generator: BuildConfigGenerator
) : BuildConfigGenerator by generator {
    JAVA(BuildConfigJavaGenerator),
    KOTLIN(BuildConfigKotlinGenerator)
}
