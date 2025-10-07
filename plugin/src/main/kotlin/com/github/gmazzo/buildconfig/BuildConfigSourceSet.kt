package com.github.gmazzo.buildconfig

import org.gradle.api.Action
import org.gradle.api.tasks.TaskProvider

@JvmDefaultWithoutCompatibility
interface BuildConfigSourceSet : BuildConfigClassSpec {

    val generateTask: TaskProvider<BuildConfigTask>

    /**
     * Creates a secondary build class with the given [className] in the same package
     */
    @Suppress("unused")
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
