package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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
    data class JavaRef(val javaType: JavaType) : Type
    data class NameRef(val className: String, val typeParameters: List<NameRef> = emptyList()) : Type

    sealed interface Value : Serializable { val value: Serializable? }
    data class Literal(override val value: Serializable?) : Value {
        init {
            check(value !is Value) { "$value is already a Value" }
            check(value !is Provider<*>) { "$value is a Gradle provider" }
        }
    }
    data class Expression(override val value: String) : Value

}
