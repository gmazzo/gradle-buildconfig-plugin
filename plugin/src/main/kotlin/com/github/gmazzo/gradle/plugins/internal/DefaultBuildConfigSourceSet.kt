package com.github.gmazzo.gradle.plugins.internal

import org.gradle.api.NamedDomainObjectContainer

internal open class DefaultBuildConfigSourceSet(
    override val classSpec: BuildConfigClassSpecInternal,
    override val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpecInternal>
) :
    BuildConfigSourceSetInternal,
    BuildConfigClassSpecInternal by classSpec {

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpecInternal =
        extraSpecs.maybeCreate(className).apply {
            className(className)
            packageName?.let(::packageName)
        }

}
