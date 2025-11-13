package com.github.gmazzo.buildconfig.internal

import org.gradle.api.NamedDomainObjectContainer

internal abstract class DefaultBuildConfigExtension(
    override val sourceSets: NamedDomainObjectContainer<BuildConfigSourceSetInternal>,
    defaultSourceSet: BuildConfigSourceSetInternal
) : BuildConfigExtensionInternal,
    BuildConfigSourceSetInternal by defaultSourceSet,
    GroovyNullValueWorkaround()
