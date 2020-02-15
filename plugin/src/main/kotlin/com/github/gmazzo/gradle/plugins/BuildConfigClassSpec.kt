package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinFileGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinObjectGenerator
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

    fun buildConfigField(field: BuildConfigField): BuildConfigField

    fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(BuildConfigField(type, name, value))

    fun buildConfigField(type: String, name: String, value: () -> String) =
        buildConfigField(BuildConfigField(type, name, value()))

    val generateTask: BuildConfigTask

}
