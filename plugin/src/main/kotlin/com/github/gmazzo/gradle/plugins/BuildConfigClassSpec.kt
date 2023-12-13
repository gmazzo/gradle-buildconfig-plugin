package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
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

    fun buildConfigField(
        type: String,
        name: String,
        value: String,
    ) = addField(nameOf(type), name, expressionOf(value))

    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<String>
    ) = addField(nameOf(type), name, value.map(::expressionOf))

    fun <Type : Any> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Type?,
    ) = addField(typeOf(type as JavaType), name, valueOf(value))

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Provider<out Type>,
    ) = addField(typeOf(type as JavaType), name, value.map(::valueOf))

    fun <Type : Serializable> buildConfigField(
        type: KClass<out Type>,
        name: String,
        value: Type?,
    ) = addField(typeOf(type.java), name, valueOf(value))

    fun <Type : Serializable> buildConfigField(
        type: KClass<out Type>,
        name: String,
        value: Provider<out Type>,
    ) = addField(typeOf(type.java), name, value.map(::valueOf))

    fun <Type : Serializable> buildConfigField(
        type: KType,
        name: String,
        value: Type?,
    ) = addField(nameOf(type), name, valueOf(value))

    fun <Type : Serializable> buildConfigField(
        type: KType,
        name: String,
        value: Provider<out Type>,
    ) = addField(nameOf(type), name, value.map(::valueOf))

}
