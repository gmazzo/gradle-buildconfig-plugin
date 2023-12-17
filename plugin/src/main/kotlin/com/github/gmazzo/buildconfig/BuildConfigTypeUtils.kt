package com.github.gmazzo.buildconfig

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Provider
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType

private val regEx = "([^\\[\\]?]+?)(\\?)?(\\[])?".toRegex()

internal fun String.parseTypename(): Triple<String, Boolean, Boolean> {
    val match = regEx.matchEntire(this)
    checkNotNull(match) {
        "Class name must be of one of these formats: 'ClassName', 'ClassName?', 'ClassName[]' or 'ClassName?[]'"
    }

    val (type, nullable, array) = match.destructured

    return Triple(type, nullable.isNotEmpty(), array.isNotEmpty())
}

internal fun typeOf(type: Class<*>) =
    BuildConfigType.JavaRef(type)

@PublishedApi
internal fun nameOf(type: KType): BuildConfigType.NameRef = (type.classifier!! as KClass<*>).let { kClass ->
    BuildConfigType.NameRef(
        kClass.qualifiedName!! + if (type.isMarkedNullable) "?" else "",
        if (kClass.typeParameters.isEmpty()) emptyList() else type.arguments.map { nameOf(it.type!!) }
    )
}

internal fun nameOf(className: String): BuildConfigType.NameRef {
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
    parentParameters: MutableList<BuildConfigType.NameRef>?
): BuildConfigType.NameRef {
    val parameters = mutableListOf<BuildConfigType.NameRef>()
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
    return BuildConfigType.NameRef(name, parameters)
}

@PublishedApi
internal fun expressionOf(expression: String) =
    BuildConfigValue.Expression(expression)

@PublishedApi
internal fun valueOf(value: Serializable?) =
    BuildConfigValue.Literal(value)

private fun BuildConfigClassSpec.buildConfigField(
    type: BuildConfigType<*>,
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
internal fun BuildConfigClassSpec.addField(type: BuildConfigType<*>, name: String, value: BuildConfigValue) =
    buildConfigField(type, name) { it.value.value(value).disallowChanges() }

@PublishedApi
internal fun BuildConfigClassSpec.addField(
    type: BuildConfigType<*>,
    name: String,
    value: Provider<BuildConfigValue>
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
