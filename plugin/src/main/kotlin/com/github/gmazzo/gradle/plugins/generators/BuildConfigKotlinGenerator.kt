package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
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
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
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

    private fun String.toTypeName(): TypeName {
        val nonNullable = removeSuffix("?")
        val isArray = nonNullable.endsWith("[]")
        val type = when (nonNullable.removeSuffix("[]").lowercase()) {
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
            else -> ClassName.bestGuess(this)
        }
        return if (this != nonNullable) type.copy(nullable = true) else type
    }

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map { field ->
        val typeName = when (val type = field.type.get()) {
            is BuildConfigField.TypeRef -> type.javaType.asTypeName()
            is BuildConfigField.TypeByName -> type.name.toTypeName().let { resolved ->
                if (type.typeParameters.isEmpty()) resolved
                else checkNotNull(resolved as? ClassName).parameterizedBy(type.typeParameters.map { it.toTypeName() })
            }
        }

        return@map PropertySpec.builder(field.name, typeName, kModifiers)
            .apply { if (typeName in constTypes) addModifiers(KModifier.CONST) }
            .apply {
                when (val value = field.value.get()) {
                    is BuildConfigField.Literal -> initializer(
                        if (value.value is CharSequence) "%S" else "%L",
                        value.value
                    )

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

}
