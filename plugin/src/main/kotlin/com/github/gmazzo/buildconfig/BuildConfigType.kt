package com.github.gmazzo.buildconfig

import java.io.Serializable

public data class BuildConfigType @JvmOverloads constructor(
    public val className: String,
    public val typeArguments: List<BuildConfigType> = emptyList(),
    public val nullable: Boolean = false,
    public val array: Boolean = false,
    public val arrayNullable: Boolean = false,
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
