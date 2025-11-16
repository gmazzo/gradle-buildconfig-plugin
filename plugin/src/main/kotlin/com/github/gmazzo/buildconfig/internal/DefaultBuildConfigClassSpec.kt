package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigField
import javax.inject.Inject
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance

internal abstract class DefaultBuildConfigClassSpec @Inject constructor(
    name: String,
    private val description: String,
    private val objects: ObjectFactory,
) :
    Named by (Named { name }),
    BuildConfigClassSpec,
    GroovyNullValueWorkaround() {

    override val buildConfigFields: NamedDomainObjectContainer<BuildConfigField> =
        objects.domainObjectContainer(BuildConfigField::class) { name ->
            objects.newInstance<BuildConfigField>(name).apply {
                position.convention(buildConfigFields.size)
            }
        }

    override fun toString() = description

}
