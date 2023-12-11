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

    @Deprecated("Kept for backward compatibility, use typesafe overloads instead",
        ReplaceWith("buildConfigField(type(type), name, expression(value))")
    )
    fun buildConfigField(
        type: String,
        name: String,
        value: String,
    ) = buildConfigField(type(type), name, expression(value))

    @Deprecated("Kept for backward compatibility, use typesafe overloads instead",
        ReplaceWith("buildConfigField(type(type), name, value.map(::expression))")
    )
    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<String>
    ) = buildConfigField(type(type), name, value.map(::expression))

    fun <Type : Serializable> BuildConfigClassSpec.buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Type,
    ) = buildConfigField(type(type), name, literal(value))

    fun <Type : Serializable> BuildConfigClassSpec.buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Provider<out Type>,
    ) = buildConfigField(type(type), name, value.map(::literal))

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

    fun type(className: CharSequence, vararg typeParameters: String) =
        BuildConfigField.TypeByName(className.toString(), typeParameters.toList())

    fun type(javaType: JavaType) =
        BuildConfigField.TypeRef(javaType)

    fun literal(value: Serializable) =
        BuildConfigField.Literal(value)

    fun expression(expression: CharSequence) =
        BuildConfigField.Expression(expression.toString())

}
