package com.github.gmazzo.buildconfig.internal

import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.domainObjectContainer

internal abstract class DefaultBuildConfigExtension private constructor(
    override val sourceSets: NamedDomainObjectContainer<BuildConfigSourceSetInternal>,
    val defaultSourceSet: BuildConfigSourceSetInternal = sourceSets.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
) : BuildConfigExtensionInternal,
    BuildConfigSourceSetInternal by defaultSourceSet,
    GroovyNullValueWorkaround() {

    @Inject
    @Suppress("UNCHECKED_CAST")
    constructor(objects: ObjectFactory): this(
        objects.domainObjectContainer(DefaultBuildConfigSourceSet::class) as NamedDomainObjectContainer<BuildConfigSourceSetInternal>,
    )

}
