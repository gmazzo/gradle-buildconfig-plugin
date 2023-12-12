package com.github.gmazzo.gradle.plugins

private val regEx = "(.*?)(\\[])?(\\?)?".toRegex()

internal fun String.parseTypename(): Triple<String, Boolean, Boolean> = regEx.matchEntire(this)?.let {
    val (type, array, nullable) = it.destructured

    Triple(type, array.isNotEmpty(), nullable.isNotEmpty())
} ?: Triple(this, false, false)

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
