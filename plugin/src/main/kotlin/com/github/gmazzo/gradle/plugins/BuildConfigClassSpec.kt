package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigOutputType.Companion.asOutputType
import org.gradle.api.Named

interface BuildConfigClassSpec : Named {

    val generateTask: BuildConfigTask

    fun className(className: String)

    fun packageName(packageName: String)

    @Deprecated("Use outputType instead", ReplaceWith("outputType(language)"))
    fun language(language: String) = outputType(language)

    @Deprecated("Use outputType instead", ReplaceWith("outputType(language)"))
    fun language(language: BuildConfigGenerator) = outputType(language)

    fun outputType(outputType: String) =
        outputType(outputType.asOutputType())

    fun outputType(outputType: BuildConfigGenerator)

    fun buildConfigField(field: BuildConfigField): BuildConfigField

    fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(BuildConfigField(type, name, value))

    fun buildConfigField(type: String, name: String, value: () -> String) =
        buildConfigField(BuildConfigField(type, name, value()))

}
