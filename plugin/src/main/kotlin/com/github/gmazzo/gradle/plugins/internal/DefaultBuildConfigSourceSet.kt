package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.NamedDomainObjectContainer

internal open class DefaultBuildConfigSourceSet(
    internal val classSpec: DefaultBuildConfigClassSpec,
    internal val extraSpecs: NamedDomainObjectContainer<DefaultBuildConfigClassSpec>
) :
    BuildConfigSourceSet,
    BuildConfigClassSpec by classSpec {

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpec =
        extraSpecs.maybeCreate(className).apply {
            className(className)
            packageName?.let(::packageName)
        }

}
