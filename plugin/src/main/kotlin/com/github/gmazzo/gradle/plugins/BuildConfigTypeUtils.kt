package com.github.gmazzo.gradle.plugins

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Provider
import java.io.Serializable
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType

private val regEx = "(.*?)(\\[])?(\\?)?".toRegex()

internal fun String.parseTypename(): Triple<String, Boolean, Boolean> = regEx.matchEntire(this)?.let {
    val (type, array, nullable) = it.destructured

    Triple(type, array.isNotEmpty(), nullable.isNotEmpty())
} ?: Triple(this, false, false)

internal fun typeOf(type: Type) =
    BuildConfigField.JavaRef(type)

@PublishedApi
internal fun nameOf(type: KType): BuildConfigField.NameRef = (type.classifier!! as KClass<*>).let { kClass ->
    BuildConfigField.NameRef(
        kClass.qualifiedName!! + if (type.isMarkedNullable) "?" else "",
        if (kClass.typeParameters.isEmpty()) emptyList() else type.arguments.map { nameOf(it.type!!) }
    )
}

internal fun nameOf(className: String): BuildConfigField.NameRef {
    val iterator = className.toList().listIterator()
    val nameRef = parseName(iterator, null)
    check(!iterator.hasNext()) {
        "Failed to parse '$className', input remaining: ${
            iterator.asSequence().joinToString("")
        }"
    }
    return nameRef
}

private fun parseName(
    iterator: ListIterator<Char>,
    parentParameters: MutableList<BuildConfigField.NameRef>?
): BuildConfigField.NameRef {
    val parameters = mutableListOf<BuildConfigField.NameRef>()
    val name = buildString {
        while (iterator.hasNext()) {
            when (val ch = iterator.next()) {
                ' ' -> continue
                '<' -> {
                    parameters.add(0, parseName(iterator, parameters))
                }

                ',' -> {
                    parentParameters!!.add(0, parseName(iterator, parentParameters))
                    iterator.previous()
                }

                '>' -> break
                else -> append(ch)
            }
        }
    }
    return BuildConfigField.NameRef(name, parameters)
}

@PublishedApi
internal fun expressionOf(expression: String) =
    BuildConfigField.Expression(expression)

@PublishedApi
internal fun valueOf(value: Any?) = check(value is Serializable?) { "Value must be a Serializable: $value" }.let {
    BuildConfigField.Literal(value)
}

private fun BuildConfigClassSpec.buildConfigField(
    type: BuildConfigField.Type,
    name: String,
    action: (BuildConfigField) -> Unit,
): NamedDomainObjectProvider<BuildConfigField> = buildConfigFields.size.let { position ->
    buildConfigFields.register(name) {
        it.type.value(type).disallowChanges()
        it.position.convention(position)
        action(it)
    }
}

@PublishedApi
internal fun BuildConfigClassSpec.addField(type: BuildConfigField.Type, name: String, value: BuildConfigField.Value) =
    buildConfigField(type, name) { it.value.value(value).disallowChanges() }

@PublishedApi
internal fun BuildConfigClassSpec.addField(
    type: BuildConfigField.Type,
    name: String,
    value: Provider<BuildConfigField.Value>
) =
    buildConfigField(type, name) { it.value.value(value).disallowChanges() }

internal val Any?.elements: List<Any?>
    get() = when (this) {
        null -> listOf(null)
        is Array<*> -> toList()
        is ByteArray -> toList()
        is ShortArray -> toList()
        is CharArray -> toList()
        is IntArray -> toList()
        is LongArray -> toList()
        is FloatArray -> toList()
        is DoubleArray -> toList()
        is BooleanArray -> toList()
        is Collection<*> -> toList()
        else -> listOf(this)
    }

internal fun Any?.asVarArg(): Array<*> = when (this) {
    is Array<*> -> this
    is ByteArray -> toTypedArray()
    is ShortArray -> toTypedArray()
    is CharArray -> toTypedArray()
    is IntArray -> toTypedArray()
    is LongArray -> toTypedArray()
    is FloatArray -> toTypedArray()
    is DoubleArray -> toTypedArray()
    is BooleanArray -> toTypedArray()
    is List<*> -> toTypedArray()
    is Iterable<*> -> toList().toTypedArray()
    else -> arrayOf(this)
}
