package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import java.lang.reflect.Type as JavaType

@BuildConfigDsl
@JvmDefaultWithoutCompatibility
interface BuildConfigClassSpec : Named {

    @Input
    override fun getName(): String

    @get:Input
    val className: Property<String>

    @get:Input
    @get:Optional
    val packageName: Property<String>

    @get:Nested
    val buildConfigFields: NamedDomainObjectContainer<BuildConfigField>

    @get:Input
    @get:Optional
    val documentation: Property<String>

    fun className(className: String) = apply {
        this.className.set(className)
    }

    fun packageName(packageName: String) = apply {
        this.packageName.set(packageName)
    }

    fun withoutPackage() = apply {
        packageName("")
    }

    @Deprecated(
        "Kept for backward compatibility, use typesafe overloads instead",
        ReplaceWith("buildConfigField(type(type), name, expression(value))")
    )
    fun buildConfigField(
        type: String,
        name: String,
        value: String,
    ) = buildConfigField(typeOf(type), name, expression(value))

    @Deprecated(
        "Kept for backward compatibility, use typesafe overloads instead",
        ReplaceWith("buildConfigField(type(type), name, value.map(::expression))")
    )
    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<String>
    ) = buildConfigField(typeOf(type), name, value.map(::expression))

    fun buildConfigField(
        type: JavaType,
        name: String,
        value: Serializable?,
    ) = buildConfigField(typeOf(type), name, literal(value))

    fun buildConfigField(
        type: JavaType,
        name: String,
        value: Provider<out Serializable>,
    ) = buildConfigField(typeOf(type), name, value.map(::literal))

    fun buildConfigField(
        type: KClass<*>,
        name: String,
        value: Serializable?,
    ) = buildConfigField(typeOf(type), name, literal(value))

    fun buildConfigField(
        type: KClass<*>,
        name: String,
        value: Provider<out Serializable>,
    ) = buildConfigField(typeOf(type), name, value.map(::literal))

    fun buildConfigField(
        type: KType,
        name: String,
        value: Serializable?,
    ) = buildConfigField(typeOf(type), name, literal(value))

    fun buildConfigField(
        type: KType,
        name: String,
        value: Provider<out Serializable>,
    ) = buildConfigField(typeOf(type), name, value.map(::literal))

    fun buildConfigField(
        type: BuildConfigField.Type,
        name: String,
        value: BuildConfigField.Value,
    ): NamedDomainObjectProvider<BuildConfigField>

    fun buildConfigField(
        type: BuildConfigField.Type,
        name: String,
        value: Provider<BuildConfigField.Value>
    ): NamedDomainObjectProvider<BuildConfigField>

    fun typeOf(className: CharSequence, vararg typeParameters: String): BuildConfigField.Type =
        BuildConfigField.NameRef(className.toString(), typeParameters.map(::typeOf))

    fun typeOf(type: JavaType) =
        BuildConfigField.JavaRef(type)

    fun typeOf(type: KClass<*>): BuildConfigField.Type =
        typeOf(type.qualifiedName!!)

    fun typeOf(type: KType): BuildConfigField.Type =
        BuildConfigField.NameRef(
            (type.classifier!! as KClass<*>).qualifiedName!!,
            type.arguments.map { typeOf(it.type!!) }
        )

    fun literal(value: Serializable?) =
        BuildConfigField.Literal(value)

    fun expression(expression: CharSequence) =
        BuildConfigField.Expression(expression.toString())

}
