package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.generators.BuildConfigGenerator
import com.github.gmazzo.buildconfig.generators.BuildConfigJavaGenerator
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import java.io.Serializable
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

@BuildConfigDsl
public interface BuildConfigClassSpec : Named {

    @Input
    override fun getName(): String

    @get:Nested
    public val generator: Property<BuildConfigGenerator>

    public fun generator(generator: BuildConfigGenerator): BuildConfigClassSpec = apply {
        this.generator.set(generator)
    }

    public fun useJavaOutput(): BuildConfigClassSpec =
        useJavaOutput {}

    public fun useJavaOutput(configure: Action<BuildConfigJavaGenerator>): BuildConfigClassSpec =
        generator(BuildConfigJavaGenerator().apply(configure::execute))

    public fun useKotlinOutput(): BuildConfigClassSpec =
        useKotlinOutput {}

    public fun useKotlinOutput(configure: Action<BuildConfigKotlinGenerator>): BuildConfigClassSpec =
        generator(BuildConfigKotlinGenerator().apply(configure::execute))

    @get:Input
    public val className: Property<String>

    @get:Input
    @get:Optional
    public val packageName: Property<String>

    @get:Nested
    public val buildConfigFields: NamedDomainObjectContainer<BuildConfigField>

    @get:Input
    @get:Optional
    public val documentation: Property<String>

    public fun className(className: String): BuildConfigClassSpec = apply {
        this.className.set(className)
    }

    public fun packageName(packageName: String): BuildConfigClassSpec = apply {
        this.packageName.set(packageName)
    }

    @Suppress("unused")
    public fun withoutPackage(): BuildConfigClassSpec = apply {
        packageName("")
    }

    public fun buildConfigField(
        type: String,
        name: String,
        expression: String,
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name) {
        it.type(type)
        it.expression(expression)
    }

    public fun buildConfigField(
        type: String,
        name: String,
        value: Serializable?,
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name) {
        it.type(type)
        it.value(value)
    }

    public fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Type?,
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name) {
        it.type(type)
        it.value(castToType(value, type) as Serializable?)
    }

    public fun buildConfigField(
        type: Class<*>,
        name: String,
        expression: BuildConfigValue.Expression
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name) {
        it.type(type)
        it.value.value(expression)
    }

    /*
     Because of erasure types on Groovy, this method is call for two use cases:
     - when `buildConfigField('File', 'NAME', provider { 'File("aFile")' })` is called
     - when `buildConfigField('List<Int>', 'NAME', provider { [1, 2] })` is called

     So we assume that if the value is a String, it's an expression, otherwise it's a literal.
     Literal strings values has a dedicated `buildConfigField(String, 'NAME', provider { 'aValue' })` method
     */
    public fun buildConfigField(
        type: String,
        name: String,
        value: Provider<out Serializable>
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name) {
        it.type(type)
        it.value
            .value(value.map { v ->
                when (v) {
                    is BuildConfigValue -> v
                    is String -> BuildConfigValue.Expression(v)
                    else -> BuildConfigValue.Literal(v)
                }
            })
            .disallowChanges()
    }

    public fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Provider<out Type>,
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name) {
        it.type(type)
        it.value.value(value.map { v ->
            when (v) {
                is BuildConfigValue -> v
                is String -> when (type) {
                    String::class.java -> BuildConfigValue.Literal(v)
                    else -> BuildConfigValue.Expression(v)
                }

                else -> BuildConfigValue.Literal(castToType(v, type) as Serializable)
            }
        })
    }

    public fun buildConfigField(
        name: String,
        configure: Action<BuildConfigField>
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigFields.size.let { position ->
        buildConfigFields.register(name) {
            it.position.convention(position)
            configure.execute(it)
        }
    }

    public fun expression(expression: String): BuildConfigValue.Expression =
        BuildConfigValue.Expression(expression)

    @Suppress("UNCHECKED_CAST")
    public fun <Type : Serializable> expect(defaultsTo: Type? = (BuildConfigValue.NoDefault as Type)): Type =
        BuildConfigValue.Expect(value = defaultsTo) as Type

}
