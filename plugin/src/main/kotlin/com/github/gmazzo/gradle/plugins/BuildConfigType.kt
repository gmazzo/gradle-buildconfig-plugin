package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import java.io.Serializable
import java.lang.reflect.Type as JavaType

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
