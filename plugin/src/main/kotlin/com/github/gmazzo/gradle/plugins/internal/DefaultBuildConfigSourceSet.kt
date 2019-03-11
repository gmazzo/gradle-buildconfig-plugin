package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.internal.FactoryNamedDomainObjectContainer
import org.gradle.internal.reflect.Instantiator

internal open class DefaultBuildConfigSourceSet(
    internal val classSpec: DefaultBuildConfigClassSpec,
    instantiator: Instantiator
) : FactoryNamedDomainObjectContainer<DefaultBuildConfigClassSpec>(
    DefaultBuildConfigClassSpec::class.java,
    instantiator
),
    BuildConfigSourceSet,
    BuildConfigClassSpec by classSpec {

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpec =
        maybeCreate(className).apply {
            className(className)
            packageName?.let(::packageName)
        }

}
