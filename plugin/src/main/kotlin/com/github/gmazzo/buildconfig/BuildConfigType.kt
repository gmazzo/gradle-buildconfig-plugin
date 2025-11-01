package com.github.gmazzo.buildconfig

import java.io.Serializable

data class BuildConfigType @JvmOverloads constructor(
    val className: String,
    val typeArguments: List<BuildConfigType> = emptyList(),
    val nullable: Boolean = false,
    val array: Boolean = false,
    val arrayNullable: Boolean = false,
) : Serializable {

    private val text by lazy {
        buildString {
            append(className)
            if (typeArguments.isNotEmpty()) {
                append("<")
                append(typeArguments.joinToString(", "))
                append(">")
            }
            if (nullable) append("?")
            if (array) append("[]")
            if (arrayNullable) append("?")
        }
    }

    override fun toString(): String = text

}
