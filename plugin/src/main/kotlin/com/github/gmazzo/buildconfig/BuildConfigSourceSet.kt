package com.github.gmazzo.buildconfig

import org.gradle.api.Action
import org.gradle.api.tasks.TaskProvider

public interface BuildConfigSourceSet : BuildConfigClassSpec {

    public val generateTask: TaskProvider<BuildConfigTask>

    /**
     * Creates a secondary build class with the given [className] in the same package
     */
    @Suppress("unused")
    public fun forClass(className: String): BuildConfigClassSpec = forClass(null, className)

    /**
     * Creates a secondary build class with the given [className] in the same package
     */
    public fun forClass(
        className: String,
        configureAction: Action<BuildConfigClassSpec>
    ): BuildConfigClassSpec = forClass(null, className, configureAction)

    /**
     * Creates a secondary build class with the given [className] in a new [packageName]
     */
    public fun forClass(packageName: String?, className: String): BuildConfigClassSpec

    /**
     * Creates a secondary build class with the given [className] in a new [packageName]
     */
    public fun forClass(
        packageName: String?,
        className: String,
        configureAction: Action<BuildConfigClassSpec>
    ): BuildConfigClassSpec = forClass(packageName, className).apply { configureAction.execute(this) }

}
