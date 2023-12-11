package org.gradle.kotlin.dsl

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import java.io.Serializable

inline fun <reified Type : Serializable> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type,
) = buildConfigField(type(Type::class.java), name, literal(value))
