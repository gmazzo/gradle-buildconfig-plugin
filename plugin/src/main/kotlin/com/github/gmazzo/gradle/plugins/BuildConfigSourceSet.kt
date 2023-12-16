package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinGenerator
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

@JvmDefaultWithoutCompatibility
interface BuildConfigSourceSet : BuildConfigClassSpec {

    val generator: Property<BuildConfigGenerator>

    val generateTask: TaskProvider<BuildConfigTask>

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

    /**
     * Creates a secondary build class with the given [className] in the same package
     */
    fun forClass(className: String) = forClass(null, className)

    /**
     * Creates a secondary build class with the given [className] in the same package
     */
    fun forClass(
        className: String,
        configureAction: Action<BuildConfigClassSpec>
    ) = forClass(null, className, configureAction)

    /**
     * Creates a secondary build class with the given [className] in a new [packageName]
     */
    fun forClass(packageName: String?, className: String): BuildConfigClassSpec

    /**
     * Creates a secondary build class with the given [className] in a new [packageName]
     */
    fun forClass(
        packageName: String?,
        className: String,
        configureAction: Action<BuildConfigClassSpec>
    ) = forClass(packageName, className).apply { configureAction.execute(this) }

}
