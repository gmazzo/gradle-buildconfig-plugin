package org.gradle.kotlin.dsl

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigField
import org.gradle.api.provider.Provider
import java.io.Serializable
import kotlin.reflect.typeOf

inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type?,
) = buildConfigField(typeOf<Type>(), name, value)

inline fun <reified Type : Any?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Serializable>,
) = buildConfigField(typeOf<Type>(), name, value)

inline fun <reified Type : Any?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: BuildConfigField.Value,
) = buildConfigField(typeOf<Type>(), name, value)

inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: List<Type>,
) = buildConfigField(typeOf<List<Type>>(), name, if (value is Serializable) value else arrayListOf(value))

inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Set<Type>,
) = buildConfigField(typeOf<Set<Type>>(), name, if (value is Serializable) value else linkedSetOf(value))
