package com.github.gmazzo.gradle.plugins

import java.io.Serializable

/**
 * A simple representation of a type for a [BuildConfigField].
 *
 * @property rawType the raw type, such as "String".
 * @property typeArguments optional list of type arguments if this is a generic type.
 */
data class FieldType internal constructor(
    val rawType: String,
    val typeArguments: List<String>,
): Serializable {
  companion object {
    @JvmName("create")
    @JvmStatic
    operator fun invoke(rawType: String, typeArguments: List<String>): FieldType = FieldType(rawType, typeArguments)
    
    @JvmStatic
    fun create(rawType: String, vararg typeArguments: String): FieldType = invoke(rawType, typeArguments.toList())
  }
}