package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.asVarArg
import com.github.gmazzo.gradle.plugins.collectionSize
import com.github.gmazzo.gradle.plugins.parseTypename
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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input

data class BuildConfigKotlinGenerator(
    @get:Input var topLevelConstants: Boolean = false,
    @get:Input var internalVisibility: Boolean = true
) : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    private fun BuildConfigField.Type.toTypeName(): TypeName = when (this) {
        is BuildConfigField.JavaRef -> javaType.asTypeName()
        is BuildConfigField.NameRef -> {
            val (typeName, isArray, isNullable) = className.parseTypename()

            val type = when (typeName.lowercase()) {
                "boolean" -> if (isArray) BOOLEAN_ARRAY else BOOLEAN
                "byte" -> if (isArray) BYTE_ARRAY else BYTE
                "short" -> if (isArray) SHORT_ARRAY else SHORT
                "char" -> if (isArray) CHAR_ARRAY else CHAR
                "int" -> if (isArray) INT_ARRAY else INT
                "integer" -> if (isArray) INT_ARRAY else INT
                "long" -> if (isArray) LONG_ARRAY else LONG
                "float" -> if (isArray) FLOAT_ARRAY else FLOAT
                "double" -> if (isArray) DOUBLE_ARRAY else DOUBLE
                "string" -> STRING
                else -> ClassName.bestGuess(typeName)
            }
            val genericType =
                if (typeParameters.isEmpty()) type
                else checkNotNull(type as? ClassName).parameterizedBy(typeParameters.map { it.toTypeName() })

            if (isNullable) genericType.copy(nullable = true) else genericType
        }
    }

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map { field ->
        val typeName = when (val type = field.type.get()) {
            is BuildConfigField.JavaRef -> type.javaType.asTypeName()
            is BuildConfigField.NameRef -> type.toTypeName()
        }

        val value = field.value.get()
        val nullableAwareType = if (value.value != null) typeName else typeName.copy(nullable = true)

        return@map PropertySpec.builder(field.name, nullableAwareType, kModifiers)
            .apply { if (value.value != null && typeName in constTypes) addModifiers(KModifier.CONST) }
            .apply {
                when (value) {
                    is BuildConfigField.Literal ->{
                        val (format, count) = nullableAwareType.format(value.value)
                        val args = value.value.asVarArg()

                        check(count == args.size) {
                            "Invalid number of arguments for ${field.name} of type ${nullableAwareType}: " +
                                    "expected $count, got ${args.size}: ${args.joinToString()}"
                        }
                        initializer(format, *args)
                    }
                    is BuildConfigField.Expression -> initializer("%L", value.value)
                }
            }
            .build()
    }

    override fun execute(spec: BuildConfigGeneratorSpec) {
        logger.debug("Generating {} for fields {}", spec.className, spec.fields)

        val fields = spec.fields.asPropertiesSpec()

        FileSpec.builder(spec.packageName, spec.className)
            .addFields(fields, spec.documentation)
            .build()
            .writeTo(spec.outputDir)
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
        val count by lazy { forValue.collectionSize }
        val arrayFormat by lazy { format(count, "arrayOf") }
        val listFormat by lazy { format(count, "listOf") }
        val setFormat by lazy { format(count, "setOf") }

        return when (this) {
            LONG -> "%LL" to 1
            STRING -> "%S" to 1
            ARRAY -> arrayFormat to count
            BYTE_ARRAY -> format(count, "byteArrayOf") to count
            SHORT_ARRAY -> format(count, "shortArrayOf") to count
            CHAR_ARRAY -> format(count, "charArrayOf") to count
            INT_ARRAY -> format(count, "intArrayOf") to count
            LONG_ARRAY -> format(count, "longArrayOf") to count
            FLOAT_ARRAY -> format(count, "floatArrayOf") to count
            DOUBLE_ARRAY -> format(count, "doubleArrayOf") to count
            BOOLEAN_ARRAY -> format(count, "booleanArrayOf") to count
            LIST -> listFormat to count
            SET -> setFormat to count
            is ParameterizedTypeName -> when (rawType) {
                ARRAY -> arrayFormat to count
                LIST -> listFormat to count
                SET -> setFormat to count
                else -> "%L" to 1
            }

            else -> "%L" to 1
        }
    }

    private fun format(count: Int, function: String) = (1..count).joinToString(
        prefix = "$function(",
        separator = ", ",
        postfix = ")",
        transform = { "%L" }
    )

}
