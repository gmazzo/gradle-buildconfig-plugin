package org.gradle.kotlin.dsl

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import java.io.Serializable
import kotlin.reflect.typeOf

inline fun <reified Type : Serializable> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type?,
) = buildConfigField(Type::class, name, value)

inline fun <reified Type : Serializable> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: List<Type>,
) = buildConfigField(typeOf<List<Type>>(), name, if (value is Serializable) value else arrayListOf(value))

inline fun <reified Type : Serializable> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Set<Type>,
) = buildConfigField(typeOf<Set<Type>>(), name, if (value is Serializable) value else linkedSetOf(value))
