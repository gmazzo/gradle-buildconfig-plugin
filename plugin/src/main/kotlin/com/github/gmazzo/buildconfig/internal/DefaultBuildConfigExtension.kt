package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.NamedDomainObjectContainer

internal abstract class DefaultBuildConfigExtension(
    override val sourceSets: NamedDomainObjectContainer<BuildConfigSourceSetInternal>,
    defaultSourceSet: BuildConfigSourceSetInternal
) : BuildConfigExtension,
    BuildConfigSourceSetInternal by defaultSourceSet,
    GroovyNullValueWorkaround()
