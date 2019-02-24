package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.internal.DefaultDomainObjectSet

internal open class DefaultBuildConfigSourceSet(
    private val name: String
) : BuildConfigSourceSet {

    internal val fields = DefaultDomainObjectSet(BuildConfigField::class.java)

    override fun getName() = name

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields.add(it) }

}
