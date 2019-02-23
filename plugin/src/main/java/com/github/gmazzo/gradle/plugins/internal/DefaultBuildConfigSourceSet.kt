package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

internal open class DefaultBuildConfigSourceSet(
    private val name: String
) : BuildConfigSourceSet {

    private val fields = mutableListOf<BuildConfigSourceSet.Field>()

    override fun getName() = name

    override fun buildConfigField(field: BuildConfigSourceSet.Field) =
        field.also { fields.add(it) }

}
