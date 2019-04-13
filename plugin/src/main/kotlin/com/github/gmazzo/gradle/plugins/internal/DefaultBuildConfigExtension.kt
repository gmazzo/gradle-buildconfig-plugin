package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.NamedDomainObjectContainer

internal open class DefaultBuildConfigExtension(
    override val sourceSets: NamedDomainObjectContainer<BuildConfigSourceSetInternal>,
    defaultSourceSet: BuildConfigSourceSetInternal
) : BuildConfigExtension,
    BuildConfigSourceSetInternal by defaultSourceSet
