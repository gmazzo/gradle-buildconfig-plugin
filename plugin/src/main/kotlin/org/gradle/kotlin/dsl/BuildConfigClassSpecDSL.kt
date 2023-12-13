package org.gradle.kotlin.dsl

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.addField
import com.github.gmazzo.gradle.plugins.nameOf
import com.github.gmazzo.gradle.plugins.valueOf
import org.gradle.api.provider.Provider
import java.io.Serializable
import kotlin.reflect.typeOf

inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type?,
) = addField(nameOf(typeOf<Type>()), name, valueOf(value))

inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Type>,
) = addField(nameOf(typeOf<Type>()), name, value.map(::valueOf))

@JvmName("buildConfigFieldList")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: List<Type>,
) = buildConfigField(typeOf<List<Type>>(), name, if (value is Serializable) value else ArrayList(value))

@JvmName("buildConfigFieldList")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out List<Type>>,
) = buildConfigField(typeOf<List<Type>>(), name, value.map { if (it is Serializable) it else ArrayList(it) })

@JvmName("buildConfigFieldSet")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Set<Type>,
) = buildConfigField(typeOf<Set<Type>>(), name, if (value is Serializable) value else LinkedHashSet(value))

@JvmName("buildConfigFieldSet")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Set<Type>>,
) = buildConfigField(typeOf<Set<Type>>(), name, value.map { if (it is Serializable) it else LinkedHashSet(it) })
