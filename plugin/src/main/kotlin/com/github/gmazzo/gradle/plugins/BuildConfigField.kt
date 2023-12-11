package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.Serializable
import java.lang.reflect.Type as JavaType

interface BuildConfigField : Named {

    @Input
    override fun getName(): String

    @get:Input
    val type: Property<Type>

    @get:Input
    val value: Property<Value>

    @get:Input
    val position: Property<Int>

    sealed interface Type : Serializable
    data class TypeRef(val javaType: JavaType) : Type
    data class TypeByName(val name: String, val typeParameters: List<String> = emptyList()) : Type

    sealed interface Value : Serializable
    data class Literal(val value: Serializable?) : Value
    data class Expression(val value: String) : Value

}
