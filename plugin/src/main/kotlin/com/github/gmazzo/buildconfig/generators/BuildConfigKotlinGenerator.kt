package com.github.gmazzo.buildconfig.generators

import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.BuildConfigType
import com.github.gmazzo.buildconfig.BuildConfigValue
import com.github.gmazzo.buildconfig.asVarArg
import com.github.gmazzo.buildconfig.elements
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import java.io.File
import java.net.URI as JavaURI

data class BuildConfigKotlinGenerator(
    @get:Input var topLevelConstants: Boolean = false,
    @get:Input var internalVisibility: Boolean = true
) : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    override fun execute(spec: BuildConfigGeneratorSpec) {
        logger.debug("Generating {} for fields {}", spec.className, spec.fields)

        val fields = spec.fields.asPropertiesSpec()

        FileSpec.builder(spec.packageName, spec.className)
            .addFields(fields, spec.documentation)
            .build()
            .writeTo(spec.outputDir)
    }

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map { field ->
        try {
            val typeName = field.type.get().toTypeName()

            val value = field.value.get()
            val nullableAwareType = if (value.value != null) typeName else typeName.copy(nullable = true)

            return@map PropertySpec.builder(field.name, nullableAwareType, kModifiers)
                .apply { if (value.value != null && typeName in CONST_TYPES) addModifiers(KModifier.CONST) }
                .apply {
                    when (value) {
                        is BuildConfigValue.Literal -> {
                            val (format, count) = nullableAwareType.format(value.value)
                            val args = value.value.asVarArg()

                            check(count == args.size) {
                                "Invalid number of arguments for ${field.name} of type ${nullableAwareType}: " +
                                        "expected $count, got ${args.size}: ${args.joinToString()}"
                            }
                            initializer(format, *args)
                        }

                        is BuildConfigValue.Expression -> initializer("%L", value.value)
                    }

                }
                .build()
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to generate field '${field.name}' of type '${field.type.get()}', " +
                        "with value: ${field.value.get().value} (of type '${field.value.get().value?.javaClass}')", e
            )
        }
    }

    private fun BuildConfigType.toTypeName(): TypeName {
        val kotlinClassName = runCatching { Class.forName(className).kotlin.qualifiedName!! }.getOrDefault(className)
        var type: TypeName = when (kotlinClassName.lowercase()) {
            "boolean" -> if (array && !nullable) BOOLEAN_ARRAY else BOOLEAN
            "byte" -> if (array && !nullable) BYTE_ARRAY else BYTE
            "short" -> if (array && !nullable) SHORT_ARRAY else SHORT
            "char" -> if (array && !nullable) CHAR_ARRAY else CHAR
            "int" -> if (array && !nullable) INT_ARRAY else INT
            "integer" -> if (array && !nullable) INT_ARRAY else INT
            "long" -> if (array && !nullable) LONG_ARRAY else LONG
            "float" -> if (array && !nullable) FLOAT_ARRAY else FLOAT
            "double" -> if (array && !nullable) DOUBLE_ARRAY else DOUBLE
            "string" -> STRING
            "list" -> LIST
            "set" -> SET
            "*", "?" -> STAR
            else -> ClassName.bestGuess(kotlinClassName)
        }
        if (typeArguments.isNotEmpty())
            type = (type as ClassName).parameterizedBy(*typeArguments.map { it.toTypeName() }.toTypedArray())
        if (nullable) type = type.copy(nullable = true)
        if (array && !type.isPrimitiveArray) type = ARRAY.parameterizedBy(type)
        return type
    }

    private fun FileSpec.Builder.addFields(fields: List<PropertySpec>, kdoc: String?): FileSpec.Builder = when {
        topLevelConstants -> {
            if (kdoc != null) addFileComment("%L", kdoc)
            fields.fold(this, FileSpec.Builder::addProperty)
        }

        else -> addType(
            TypeSpec.objectBuilder(name)
                .apply { if (kdoc != null) addKdoc("%L", kdoc) }
                .addModifiers(kModifiers)
                .addProperties(fields)
                .build()
        )
    }

    private val kModifiers
        get() = if (internalVisibility) KModifier.INTERNAL else KModifier.PUBLIC


    private fun TypeName.format(forValue: Any?): Pair<String, Int> {
        fun TypeName?.format() = when (this?.copy(nullable = false)) {
            CHAR -> "'%L'"
            LONG -> "%LL"
            FLOAT -> "%Lf"
            STRING -> "%S"
            FILE -> "java.io.File(%S)"
            URI -> "java.net.URI.create(%S)"
            else -> "%L"
        }

        fun List<Any?>.format(function: String, elementType: TypeName?) = joinToString(
            prefix = "$function(",
            separator = ", ",
            postfix = ")",
            transform = { it?.let { elementType ?: it::class.asTypeName() }.format() }
        ) to size

        val elements = forValue.elements

        fun singleFormat() =
            elements.single()?.let { it::class.asTypeName() }.format() to 1

        fun arrayFormat(elementType: TypeName?) =
            elements.format("arrayOf", elementType)

        fun listFormat(elementType: TypeName?) =
            elements.format("listOf", elementType)

        fun setFormat(elementType: TypeName?) =
            elements.format("setOf", elementType)

        fun mapFormat(keyType: TypeName?, valueType: TypeName?) =
            elements.joinToString(
                prefix = "mapOf(",
                separator = ", ",
                postfix = ")",
                transform = {
                    val (key, value) = (it as Map.Entry<Any?, Any?>)
                    val keyFormat = (keyType ?: key?.let { key::class.asTypeName() }).format()
                    val valueFormat = (valueType ?: value?.let { value::class.asTypeName() }).format()

                    "$keyFormat to $valueFormat"
                }
            ) to elements.size * 2

        return when (val nonNullable = copy(nullable = false)) {
            LONG, STRING -> singleFormat()
            ARRAY -> arrayFormat(null)
            BYTE_ARRAY -> elements.format("byteArrayOf", BYTE)
            SHORT_ARRAY -> elements.format("shortArrayOf", SHORT)
            CHAR_ARRAY -> elements.format("charArrayOf", CHAR)
            INT_ARRAY -> elements.format("intArrayOf", INT)
            LONG_ARRAY -> elements.format("longArrayOf", LONG)
            FLOAT_ARRAY -> elements.format("floatArrayOf", FLOAT)
            DOUBLE_ARRAY -> elements.format("doubleArrayOf", DOUBLE)
            BOOLEAN_ARRAY -> elements.format("booleanArrayOf", BOOLEAN)
            LIST, GENERIC_LIST -> listFormat(null)
            SET, GENERIC_SET -> setFormat(null)
            MAP, GENERIC_MAP -> mapFormat(null, null)
            is ParameterizedTypeName -> when (nonNullable.rawType) {
                ARRAY -> arrayFormat(nonNullable.typeArguments[0])
                LIST, GENERIC_LIST -> listFormat(nonNullable.typeArguments[0])
                SET, GENERIC_SET -> setFormat(nonNullable.typeArguments[0])
                MAP, GENERIC_MAP -> mapFormat(nonNullable.typeArguments[0], nonNullable.typeArguments[1])
                else -> singleFormat()
            }

            else -> singleFormat()
        }
    }

    private val TypeName.isPrimitiveArray
        get() = when (copy(nullable = false)) {
            BYTE_ARRAY, SHORT_ARRAY, CHAR_ARRAY, INT_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY, BOOLEAN_ARRAY -> true
            else -> false
        }

    companion object {
        private val CONST_TYPES = setOf(STRING, BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)
        private val GENERIC_LIST = ClassName("", "List")
        private val GENERIC_SET = ClassName("", "Set")
        private val GENERIC_MAP = ClassName("", "Map")
        private val FILE = File::class.asClassName()
        private val URI = JavaURI::class.asClassName()
    }

}
