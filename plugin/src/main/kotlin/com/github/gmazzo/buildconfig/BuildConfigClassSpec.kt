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
        expression: String,
    ) = buildConfigField(name) {
        it.type(type)
        it.expression(expression)
    }

    fun buildConfigField(
        type: String,
        name: String,
        value: Serializable?,
    ) = buildConfigField(name) {
        it.type(type)
        it.value(value)
    }

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Type?,
    ) = buildConfigField(name) {
        it.type(type)
        it.value(castToType(value, type) as Serializable?)
    }

    /*
     Because of erasure types on Groovy, this method is call for two use cases:
     - when `buildConfigField('File', 'NAME', provider { 'File("aFile")' })` is called
     - when `buildConfigField('List<Int>', 'NAME', provider { [1, 2] })` is called

     So we assume that if the value is a String, it's an expression, otherwise it's a literal.
     Literal strings values has a dedicated `buildConfigField(String, 'NAME', provider { 'aValue' })` method
     */
    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<out Serializable>
    ) = buildConfigField(name) {
        it.type(type)
        it.value
            .value(value.map { v -> if (v is String) BuildConfigValue.Expression(v) else BuildConfigValue.Literal(v) })
            .disallowChanges()
    }

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Provider<out Type>,
    ) = buildConfigField(name) {
        it.type(type)
        it.value(value.map { v -> castToType(v, type) as Serializable })
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
