package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigDsl
import com.github.gmazzo.buildconfig.BuildConfigField
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Provider
import java.io.Serializable
import kotlin.reflect.typeOf

@BuildConfigDsl
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Type?,
): NamedDomainObjectProvider<BuildConfigField> = buildConfigField(name, Action {
    it.type(typeOf<Type>())
    it.value(value)
})

@BuildConfigDsl
inline fun <reified Type : Serializable> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Type>,
) = buildConfigField(name, Action {
    it.type(typeOf<Type>())
    it.value(value)
})

@BuildConfigDsl
@JvmName("buildConfigFieldArray")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Array<Type>,
) = buildConfigField(name, Action {
    it.type(typeOf<Array<Type>>())
    it.value(value)
})

@BuildConfigDsl
@JvmName("buildConfigFieldList")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: List<Type>,
) = buildConfigField(name, Action {
    it.type(typeOf<List<Type>>())
    it.value(if (value is Serializable) value else ArrayList(value))
})

@BuildConfigDsl
@JvmName("buildConfigFieldSet")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Set<Type>,
) = buildConfigField(name, Action {
    it.type(typeOf<Set<Type>>())
    it.value(if (value is Serializable) value else LinkedHashSet(value))
})

@BuildConfigDsl
@JvmName("buildConfigFieldArray")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<Array<Type>>,
) = buildConfigField(name, Action {
    it.type(typeOf<Array<Type>>())
    it.value(value)
})

@BuildConfigDsl
@JvmName("buildConfigFieldList")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out List<Type>>,
) = buildConfigField(name, Action {
    it.type(typeOf<List<Type>>())
    it.value(value.map(::ArrayList))
})

@BuildConfigDsl
@JvmName("buildConfigFieldSet")
inline fun <reified Type : Serializable?> BuildConfigClassSpec.buildConfigField(
    name: String,
    value: Provider<out Set<Type>>,
) = buildConfigField(name, Action {
    it.type(typeOf<Set<Type>>())
    it.value(value.map(::LinkedHashSet))
})
