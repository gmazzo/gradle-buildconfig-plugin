package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigDsl
import com.github.gmazzo.buildconfig.nameOf
import com.github.gmazzo.buildconfig.valueOf
import org.gradle.api.provider.Provider
import java.io.Serializable
import kotlin.reflect.typeOf

@BuildConfigDsl
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type?,
) = buildConfigField(nameOf(typeOf<Type>()), name, valueOf(value))

@BuildConfigDsl
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Type>,
) = buildConfigField(nameOf(typeOf<Type>()), name, value.map(::valueOf))

@BuildConfigDsl
@JvmName("buildConfigFieldArray")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Array<Type>,
) = buildConfigField(nameOf(typeOf<Array<Type>>()), name, valueOf(value))

@BuildConfigDsl
@JvmName("buildConfigFieldList")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: List<Type>,
) = buildConfigField(nameOf(typeOf<List<Type>>()), name, valueOf(if (value is Serializable) value else ArrayList(value)))

@BuildConfigDsl
@JvmName("buildConfigFieldSet")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Set<Type>,
) = buildConfigField(nameOf(typeOf<Set<Type>>()), name, valueOf(if (value is Serializable) value else LinkedHashSet(value)))

@BuildConfigDsl
@JvmName("buildConfigFieldArray")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Array<Type>>,
) = buildConfigField(nameOf(typeOf<Array<Type>>()), name, value.map(::valueOf))

@BuildConfigDsl
@JvmName("buildConfigFieldList")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out List<Type>>,
) = buildConfigField(nameOf(typeOf<List<Type>>()), name, value.map { valueOf(if (it is Serializable) it else ArrayList(it)) })

@BuildConfigDsl
@JvmName("buildConfigFieldSet")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Set<Type>>,
) = buildConfigField(nameOf(typeOf<Set<Type>>()), name, value.map { valueOf(if (it is Serializable) it else LinkedHashSet(it)) })
