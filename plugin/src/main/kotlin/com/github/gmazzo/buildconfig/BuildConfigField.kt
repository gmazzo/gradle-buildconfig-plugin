package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.internal.nameOf
import java.io.Serializable
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

public interface BuildConfigField : Named, Comparable<BuildConfigField> {

    @Input
    override fun getName(): String

    @get:Input
    public val type: Property<BuildConfigType>

    @get:Input
    public val value: Property<BuildConfigValue>

    @get:Input
    @get:Optional
    public val position: Property<Int>

    @get:Input
    @get:Optional
    public val tags: SetProperty<Serializable>

    public fun type(classLiteral: String): BuildConfigField = apply {
        this.type.value(nameOf(classLiteral)).disallowChanges()
    }

    public fun type(className: String, vararg typeArguments: String): BuildConfigField = apply {
        this.type.value(BuildConfigType(className, typeArguments.map(::nameOf).toList())).disallowChanges()
    }

    public fun type(className: String, vararg typeArguments: BuildConfigType): BuildConfigField = apply {
        this.type.value(BuildConfigType(className, typeArguments.toList())).disallowChanges()
    }

    public fun type(type: Type): BuildConfigField = apply {
        this.type.value(nameOf(type)).disallowChanges()
    }

    public fun type(type: KClass<*>): BuildConfigField = apply {
        type(type.java)
    }

    public fun type(type: KType): BuildConfigField = apply {
        this.type.value(nameOf(type)).disallowChanges()
    }

    public fun value(literal: Serializable?): BuildConfigField = apply {
        value.value(
            when (literal) {
                is BuildConfigValue -> literal
                else -> BuildConfigValue.Literal(literal)
            }
        ).disallowChanges()
    }

    public fun <Type : Serializable> value(literal: Provider<out Type>): BuildConfigField = apply {
        value.value(literal.map(BuildConfigValue::Literal)).disallowChanges()
    }

    public fun expression(expression: String): BuildConfigField = apply {
        value.value(BuildConfigValue.Expression(expression)).disallowChanges()
    }

    public fun expression(expression: Provider<String>): BuildConfigField = apply {
        value.value(expression.map(BuildConfigValue::Expression)).disallowChanges()
    }

    override fun compareTo(other: BuildConfigField): Int =
        when (val cmp = position.getOrElse(0).compareTo(other.position.getOrElse(0))) {
            0 -> name.compareTo(other.name)
            else -> cmp
        }

}
