package com.github.gmazzo.gradle.plugins.generators

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec

internal object BuildConfigKotlinFileGenerator : BuildConfigKotlinGenerator() {

    override fun FileSpec.Builder.addFields(fields: List<PropertySpec>) =
        fields.fold(this) { acc, it -> acc.addProperty(it) }

}
