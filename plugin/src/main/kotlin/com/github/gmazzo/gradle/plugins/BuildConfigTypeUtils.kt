package com.github.gmazzo.gradle.plugins

import java.io.Serializable

private val regEx = "(.*?)(\\[])?(\\?)?".toRegex()

internal fun String.parseTypename(): Triple<String, Boolean, Boolean> = regEx.matchEntire(this)?.let {
    val (type, array, nullable) = it.destructured

    Triple(type, array.isNotEmpty(), nullable.isNotEmpty())
} ?: Triple(this, false, false)

internal val Any?.collectionSize
    get() = when (this) {
        null -> 1
        is Array<*> -> size
        is ByteArray -> size
        is ShortArray -> size
        is CharArray -> size
        is IntArray -> size
        is LongArray -> size
        is FloatArray -> size
        is DoubleArray -> size
        is BooleanArray -> size
        is Collection<*> -> size
        else -> error("Value is not a collection: $javaClass: $this")
    }

internal fun Any?.asVarArg(): Array<*> = when (this) {
    is Array<*> -> this
    is ByteArray -> toTypedArray()
    is ShortArray-> toTypedArray()
    is CharArray-> toTypedArray()
    is IntArray-> toTypedArray()
    is LongArray-> toTypedArray()
    is FloatArray-> toTypedArray()
    is DoubleArray -> toTypedArray()
    is BooleanArray -> toTypedArray()
    is List<*> -> toTypedArray()
    is Iterable<*> -> toList().toTypedArray()
    else -> arrayOf(this)
}

internal val Serializable?.value
    get() = (this as? BuildConfigField.Value) ?: BuildConfigField.Literal(this)
