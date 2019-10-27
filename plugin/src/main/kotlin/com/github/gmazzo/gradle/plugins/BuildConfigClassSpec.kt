package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigLanguage
import org.gradle.api.Named

interface BuildConfigClassSpec : Named {

    val generateTask: BuildConfigTask

    fun className(className: String)

    fun packageName(packageName: String)

    fun language(language: String) =
        language(BuildConfigLanguage.valueOf(language.toUpperCase()))

    fun language(language: BuildConfigGenerator)

    fun buildConfigField(field: BuildConfigField): BuildConfigField

    fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(BuildConfigField(type, name, value))

    fun buildConfigField(type: String, name: String, value: () -> String) =
        buildConfigField(BuildConfigField(type, name, value()))

}
