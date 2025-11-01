package com.github.gmazzo.buildconfig

import java.io.Serializable
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface BuildConfigField : Named {

    @Input
    override fun getName(): String

    @get:Input
    val type: Property<BuildConfigType>

    @get:Input
    @get:Optional
    val value: Property<BuildConfigValue>

    @get:Input
    @get:Optional
    val position: Property<Int>

    fun type(classLiteral: String) = apply {
        this.type.value(nameOf(classLiteral)).disallowChanges()
    }

    fun type(className: String, vararg typeArguments: String) = apply {
        this.type.value(BuildConfigType(className, typeArguments.map(::nameOf).toList())).disallowChanges()
    }

    fun type(className: String, vararg typeArguments: BuildConfigType) = apply {
        this.type.value(BuildConfigType(className, typeArguments.toList())).disallowChanges()
    }

    fun type(type: Type) = apply {
        this.type.value(nameOf(type)).disallowChanges()
    }

    fun type(type: KClass<*>) = apply {
        type(type.java)
    }

    fun type(type: KType) = apply {
        this.type.value(nameOf(type)).disallowChanges()
    }

    fun value(literal: Serializable?) = apply {
        value.value(BuildConfigValue.Literal(literal)).disallowChanges()
    }

    fun <Type : Serializable> value(literal: Provider<out Type>) = apply {
        value.value(literal.map(BuildConfigValue::Literal)).disallowChanges()
    }

    fun expression(expression: String) = apply {
        value.value(BuildConfigValue.Expression(expression)).disallowChanges()
    }

    fun expression(expression: Provider<String>) = apply {
        value.value(expression.map(BuildConfigValue::Expression)).disallowChanges()
    }

}
