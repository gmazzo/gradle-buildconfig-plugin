package com.github.gmazzo.gradle.plugins

import org.gradle.api.Action

interface BuildConfigSourceSet : BuildConfigClassSpec {

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
