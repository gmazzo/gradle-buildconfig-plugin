package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.internal.DefaultNamedDomainObjectSet
import org.gradle.internal.reflect.Instantiator

internal open class DefaultBuildConfigSourceSet(
    private val name: String,
    instantiator: Instantiator
) : BuildConfigSourceSet {

    internal val fields = DefaultNamedDomainObjectSet(
        BuildConfigField::class.java, instantiator, BuildConfigField::name
    )

    override fun getName() = name

    override fun buildConfigField(field: BuildConfigField) = field.also {
        fields.remove(it)
        fields.add(it)
    }


}
