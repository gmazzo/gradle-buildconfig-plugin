package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinGenerator
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

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

    fun useJavaOutput(configure: Action<BuildConfigJavaGenerator>) =
        generator(BuildConfigJavaGenerator().apply(configure::execute))

    fun useKotlinOutput() =
        useKotlinOutput {}

    fun useKotlinOutput(configure: Action<BuildConfigKotlinGenerator>) =
        generator(BuildConfigKotlinGenerator().apply(configure::execute))

    fun buildConfigField(field: BuildConfigField): BuildConfigField

    fun buildConfigField(type: String, name: String, value: String)

    fun buildConfigField(type: String, name: String, value: Provider<String>)

    val generateTask: TaskProvider<BuildConfigTask>

}
