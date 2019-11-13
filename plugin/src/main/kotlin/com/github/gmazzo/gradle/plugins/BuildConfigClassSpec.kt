package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.*
import org.gradle.api.Named

interface BuildConfigClassSpec : Named {

    var className: String?

    var packageName: String?

    var generator: BuildConfigGenerator?

    fun className(className: String) = apply {
        this.className = className
    }

    fun packageName(packageName: String) = apply {
        this.packageName = packageName
    }

    fun withoutPackage() = apply {
        packageName("")
    }

    fun generator(generator: BuildConfigGenerator) = apply {
        this.generator = generator
    }

    fun useJavaOutput() = generator(BuildConfigJavaGenerator)

    fun useKotlinOutput(topLevelConstants: Boolean = false) = generator(
        if (topLevelConstants) BuildConfigKotlinFileGenerator else BuildConfigKotlinObjectGenerator
    )

    @Deprecated("Use outputType instead", ReplaceWith("outputType(language)"))
    fun language(language: String) = generator(BuildConfigLanguage.valueOf(language))

    @Deprecated("Use outputType instead", ReplaceWith("outputType(language)"))
    fun language(language: BuildConfigGenerator) = generator(language)

    fun buildConfigField(field: BuildConfigField): BuildConfigField

    fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(BuildConfigField(type, name, value))

    fun buildConfigField(type: String, name: String, value: () -> String) =
        buildConfigField(BuildConfigField(type, name, value()))

    val generateTask: BuildConfigTask

}
