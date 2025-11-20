package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigDsl
import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.BuildConfigValue
import java.io.Serializable
import kotlin.reflect.typeOf
import org.gradle.api.Action
import org.gradle.api.provider.Provider

public operator fun BuildConfigSourceSet.invoke(action: Action<BuildConfigSourceSet>): Unit =
    action.execute(this)

@BuildConfigDsl
public inline fun <reified Type : Any> BuildConfigClassSpec.buildConfigField(
    name: String,
    crossinline configure: BuildConfigField.() -> Unit
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Type>())
    configure.invoke(it)
}

@BuildConfigDsl
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Type>())
    it.value(value)
}

@BuildConfigDsl
public inline fun <reified Type : Serializable> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Type>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Type>())
    it.value(value)
}

@BuildConfigDsl
@JvmName("buildConfigFieldExpression")
public inline fun <reified Type : Any?> BuildConfigClassSpec.buildConfigField(
    name: String,
    expression: BuildConfigValue.Expression,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Type>())
    it.value.set(expression)
}

@BuildConfigDsl
@JvmName("buildConfigFieldExpression")
public inline fun <reified Type : Any?> BuildConfigClassSpec.buildConfigField(
    name: String,
    expression: Provider<BuildConfigValue.Expression>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Type>())
    it.value.set(expression)
}

@BuildConfigDsl
@JvmName("buildConfigFieldArray")
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Array<Type>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Array<Type>>())
    it.value(value)
}

@BuildConfigDsl
@JvmName("buildConfigFieldList")
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: List<Type>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<List<Type>>())
    it.value(ArrayList(value))
}

@BuildConfigDsl
@JvmName("buildConfigFieldSet")
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Set<Type>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Set<Type>>())
    it.value(LinkedHashSet(value))
}

@BuildConfigDsl
@JvmName("buildConfigFieldMap")
public inline fun <reified Key : Serializable?, reified Value : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Map<Key, Value>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Map<Key, Value>>())
    it.value(LinkedHashMap(value))
}

@BuildConfigDsl
@JvmName("buildConfigFieldArray")
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<Array<Type>>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Array<Type>>())
    it.value(value)
}

@BuildConfigDsl
@JvmName("buildConfigFieldList")
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out List<Type>>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<List<Type>>())
    it.value(value.map(::ArrayList))
}

@BuildConfigDsl
@JvmName("buildConfigFieldSet")
public inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Set<Type>>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Set<Type>>())
    it.value(value.map(::LinkedHashSet))
}

@BuildConfigDsl
@JvmName("buildConfigFieldMap")
public inline fun <reified Key : Serializable?, reified Value : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Map<Key, Value>>,
): BuildConfigField = buildConfigField(name) {
    it.type(typeOf<Map<Key, Value>>())
    it.value(value.map(::LinkedHashMap))
}
