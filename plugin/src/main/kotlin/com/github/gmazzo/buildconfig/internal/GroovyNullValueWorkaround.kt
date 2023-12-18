package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.nameOf
import com.github.gmazzo.buildconfig.valueOf
import groovy.lang.GroovyObjectSupport
import java.io.Serializable

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
    ) = check(value == null) { "Only `null` values expected here, please fill a bug" }
        .run { buildConfigField(nameOf(type), name, valueOf(null)) }

}
