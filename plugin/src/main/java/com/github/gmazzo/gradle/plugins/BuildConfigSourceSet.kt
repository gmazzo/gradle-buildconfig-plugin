package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named

interface BuildConfigSourceSet : Named {

    fun buildConfigField(field: Field): Field

    fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(Field(type, name) { value })

    fun buildConfigField(type: String, name: String, value: () -> String) =
        buildConfigField(Field(type, name, value))

    data class Field(
        val type: String,
        val name: String,
        val value: () -> String
    )

}
