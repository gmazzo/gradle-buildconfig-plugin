package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import groovy.lang.GroovyObjectSupport

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

    fun buildConfigField(
        type: Class<*>,
        name: String,
        value: Any?,
    ) = check(value == null) { "Only `null` values expected here, please fill a bug" }.run {
        buildConfigField(name) {
            it.type(type)
            it.value(null)
        }
    }

}
