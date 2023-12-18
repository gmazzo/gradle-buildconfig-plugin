package com.github.gmazzo.buildconfig

import org.jetbrains.annotations.VisibleForTesting
import java.io.Serializable
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

private val regEx = "([^\\[\\]?]+?)(\\?)?(\\[])?".toRegex()

@VisibleForTesting
internal fun String.parseTypename(): Triple<String, Boolean, Boolean> {
    val match = regEx.matchEntire(this)
    checkNotNull(match) {
        "Class name must be of one of these formats: 'ClassName', 'ClassName?', 'ClassName[]' or 'ClassName?[]'"
    }

    val (type, nullable, array) = match.destructured

    return Triple(type, nullable.isNotEmpty(), array.isNotEmpty())
}

@Suppress("RecursivePropertyAccessor")
private val Type.genericName: String
    get() = when (this) {
        Boolean::class.java, Boolean::class.javaObjectType, BooleanArray::class.java -> "Boolean"
        Byte::class.java, Byte::class.javaObjectType, ByteArray::class.java -> "Byte"
        Short::class.java, Short::class.javaObjectType, ShortArray::class.java -> "Short"
        Char::class.java, Char::class.javaObjectType, CharArray::class.java -> "Char"
        Int::class.java, Int::class.javaObjectType, IntArray::class.java -> "Int"
        Long::class.java, Long::class.javaObjectType, LongArray::class.java -> "Long"
        Float::class.java, Float::class.javaObjectType, FloatArray::class.java -> "Float"
        Double::class.java, Double::class.javaObjectType, DoubleArray::class.java -> "Double"
        String::class.java -> "String"
        List::class.java -> "List"
        Set::class.java -> "Set"
        is GenericArrayType -> genericComponentType.genericName
        is ParameterizedType -> rawType.genericName
        is Class<*> -> if (isArray) componentType.genericName else name
        else -> error("Unsupported type: $this")
    }

private val Type.isArray get() = when (this) {
    is Class<*> -> isArray
    is GenericArrayType -> true
    else -> false
}

internal fun nameOf(type: Type): BuildConfigType = when(type) {
    is Class<*> -> BuildConfigType(
        className = type.genericName,
        typeArguments = type.typeParameters.map { nameOf(it.genericDeclaration) },
        nullable = false,
        array = type.isArray
    )
    is ParameterizedType -> BuildConfigType(
        className = type.rawType.genericName,
        typeArguments = type.actualTypeArguments.map { nameOf(it) },
        nullable = false,
        array = false
    )
    is GenericArrayType -> BuildConfigType(
        className = type.genericComponentType.genericName,
        typeArguments = checkNotNull(type.genericComponentType as? ParameterizedType) {
            "Unsupported type: $type"
        }.actualTypeArguments.map { nameOf(it) },
        nullable = false,
        array = true
    )
    else -> error("Unsupported type: $type")
}

@PublishedApi
internal fun nameOf(type: KType): BuildConfigType {
    val isArray = type.javaType.isArray
    val targetType = type.takeIf { isArray }?.arguments?.singleOrNull()?.type ?: type

    return BuildConfigType(
        className = type.javaType.genericName,
        typeArguments = targetType.arguments.map { nameOf(it.type!!) },
        nullable = targetType.isMarkedNullable,
        array = isArray
    )
}

internal fun nameOf(className: String): BuildConfigType {
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
    parentParameters: MutableList<BuildConfigType>?
): BuildConfigType {
    val parameters = mutableListOf<BuildConfigType>()
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

    val (typeName, nullable, array) = name.parseTypename()
    return BuildConfigType(typeName, parameters, nullable, array)
}

@PublishedApi
internal fun expressionOf(expression: String) =
    BuildConfigValue.Expression(expression)

@PublishedApi
internal fun valueOf(value: Serializable?) =
    BuildConfigValue.Literal(value)

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
