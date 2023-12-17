package com.github.gmazzo.buildconfig

import java.io.Serializable

sealed class BuildConfigType<RefType : Serializable> : Serializable {

    abstract val ref: RefType

    abstract val typeParameters: List<BuildConfigType<*>>

    data class JavaRef(
        override val ref: Class<*>,
        override val typeParameters: List<BuildConfigType<*>> = emptyList(),
    ) : BuildConfigType<Class<*>>()

    data class NameRef(
        override val ref: String,
        override val typeParameters: List<BuildConfigType<*>> = emptyList(),
    ) : BuildConfigType<String>()

    override fun toString(): String = when(typeParameters.size) {
        0 -> ref.toString()
        else -> "$ref<${typeParameters.joinToString(", ")}>"
    }

}
