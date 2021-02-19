package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinGenerator
import groovy.lang.Closure
import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.ConfigureUtil

interface BuildConfigClassSpec : Named {

    val className: Property<String>

    val packageName: Property<String>

    val generator: Property<BuildConfigGenerator>

    fun className(className: String) = apply {
        this.className.set(className)
    }

    fun packageName(packageName: String) = apply {
        this.packageName.set(packageName)
    }

    fun withoutPackage() = apply {
        packageName("")
    }

    fun generator(generator: BuildConfigGenerator) = apply {
        this.generator.set(generator)
    }

    fun useJavaOutput() =
        useJavaOutput {}

    fun useJavaOutput(configure: (BuildConfigJavaGenerator).() -> Unit) =
        generator(BuildConfigJavaGenerator().apply(configure))

    fun useJavaOutput(configure: Closure<*>) =
        useJavaOutput { ConfigureUtil.configure(configure, this) }

    fun useKotlinOutput() =
        useKotlinOutput {}

    fun useKotlinOutput(configure: (BuildConfigKotlinGenerator).() -> Unit) =
        generator(BuildConfigKotlinGenerator().apply(configure))

    fun useKotlinOutput(configure: Closure<*>) =
        useKotlinOutput { ConfigureUtil.configure(configure, this) }

    fun buildConfigField(field: BuildConfigField): BuildConfigField

    fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(BuildConfigField(type, name, value))

    fun buildConfigField(type: String, name: String, value: () -> String) =
        buildConfigField(BuildConfigField(type, name, value()))

    val generateTask: TaskProvider<BuildConfigTask>

}
