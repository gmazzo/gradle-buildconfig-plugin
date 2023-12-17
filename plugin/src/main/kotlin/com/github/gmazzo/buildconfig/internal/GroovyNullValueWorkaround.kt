package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.addField
import com.github.gmazzo.buildconfig.typeOf
import com.github.gmazzo.buildconfig.valueOf
import groovy.lang.GroovyObjectSupport
import java.io.Serializable
import java.lang.reflect.Type

/**
 * Workaround for Groovy's `null` value issue when calling overloaded methods limitation:
 * ```
 * > Ambiguous method overloading for method com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension_Decorated#buildConfigField.
 *   Cannot resolve which method to invoke for [class java.lang.Class, class java.lang.String, null] due to overlapping prototypes between:
 *   	[class java.lang.Class, class java.lang.String, interface java.io.Serializable]
 *   	[class java.lang.Class, class java.lang.String, interface org.gradle.api.provider.Provider]
 * ```
 */
internal abstract class GroovyNullValueWorkaround : BuildConfigClassSpec, GroovyObjectSupport() {

    fun <Type : Serializable> buildConfigField(
        type: Class<out Type>,
        name: String,
        value: Any?, // this should be `Serializable?` but Groovy fails to resolve the overloading when `null as Serializable` is passed as value
    ) = check(value is Serializable?) { "Value is not a Serializable: $value (${value!!::class.java.name})" }
        .run { addField(typeOf(type), name, valueOf(value)) }

}
