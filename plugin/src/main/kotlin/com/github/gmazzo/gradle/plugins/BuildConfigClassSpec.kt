package com.github.gmazzo.gradle.plugins

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.io.Serializable

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
        value: Serializable?,
    ) = addField(nameOf(type), name, valueOf(value))

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Type?,
    ) = addField(typeOf(type), name, valueOf(castToType(value, type) as Serializable))

    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<out Serializable>
    ) = addField(nameOf(type), name, value.map { if (it is String) expressionOf(it) else valueOf(it) })

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Provider<out Type>,
    ) = addField(typeOf(type), name, value.map { valueOf(castToType(it, type) as Serializable) })

}
