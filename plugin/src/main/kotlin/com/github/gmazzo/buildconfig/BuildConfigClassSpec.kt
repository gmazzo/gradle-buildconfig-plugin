package com.github.gmazzo.buildconfig

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
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
    ) = buildConfigField(nameOf(type), name, expressionOf(value))

    fun buildConfigField(
        type: String,
        name: String,
        value: Serializable?,
    ) = buildConfigField(nameOf(type), name, valueOf(value))

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Type?,
    ) = buildConfigField(nameOf(type), name, valueOf(castToType(value, type) as Serializable))

    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<out Serializable>
    ) = buildConfigField(nameOf(type), name, value.map { if (it is String) expressionOf(it) else valueOf(it) })

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Provider<out Type>,
    ) = buildConfigField(nameOf(type), name, value.map { valueOf(castToType(it, type) as Serializable) })

    fun buildConfigField(
        type: BuildConfigType,
        name: String,
        value: BuildConfigValue,
    ) = buildConfigField(name) {
        it.type.value(type).disallowChanges()
        it.value.value(value).disallowChanges()
    }

    fun buildConfigField(
        type: BuildConfigType,
        name: String,
        value: Provider<BuildConfigValue>
    ) = buildConfigField(name) {
        it.type.value(type).disallowChanges()
        it.value.value(value).disallowChanges()
    }

    fun buildConfigField(
        name: String,
        configure: Action<BuildConfigField>
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigFields.size.let { position ->
        buildConfigFields.register(name) {
            it.position.convention(position)
            configure.execute(it)
        }
    }

}
