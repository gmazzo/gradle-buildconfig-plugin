package com.github.gmazzo.gradle.plugins.generators

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

object BuildConfigKotlinObjectGenerator : BuildConfigKotlinGenerator() {

    override fun FileSpec.Builder.addFields(fields: List<PropertySpec>) = addType(
        TypeSpec.objectBuilder(name)
            .addProperties(fields)
            .build()
    )

}
