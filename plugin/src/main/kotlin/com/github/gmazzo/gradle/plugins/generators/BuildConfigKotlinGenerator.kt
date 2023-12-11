package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.squareup.javapoet.TypeName as JTypeName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input

data class BuildConfigKotlinGenerator(
    @get:Input var topLevelConstants: Boolean = false,
    @get:Input var internalVisibility: Boolean = true
) : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    private fun String.toTypeName(): TypeName {
        val cleanedType = removeSuffix("?")
        return when (cleanedType) {
            JTypeName.BOOLEAN.toString(), BOOLEAN.toString() -> BOOLEAN
            JTypeName.BYTE.toString(), BYTE.toString() -> BYTE
            JTypeName.SHORT.toString(), SHORT.toString() -> SHORT
            JTypeName.CHAR.toString(), CHAR.toString() -> CHAR
            JTypeName.INT.toString(), INT.toString() -> INT
            JTypeName.LONG.toString(), LONG.toString() -> LONG
            JTypeName.FLOAT.toString(), FLOAT.toString() -> FLOAT
            JTypeName.DOUBLE.toString(), DOUBLE.toString() -> DOUBLE
            "String" -> STRING
            else -> runCatching { ClassName.bestGuess(cleanedType) }
                .getOrElse { ClassUtils.getClass(cleanedType, false).asTypeName() }
        }.copy(nullable = endsWith('?')) as ClassName
    }

    private fun BuildConfigField.toTypeName(): TypeName {
        return type.get().toTypeName()
            .let { rawType ->
                val typeArgs = typeArguments.getOrElse(emptyList())
                if (typeArgs.isNotEmpty()) {
                    check(rawType is ClassName) {
                        "Cannot parameterize type '$rawType'"
                    }
                    rawType.parameterizedBy(typeArgs.map { it.toTypeName() }).copy(nullable = rawType.isNullable)
                } else {
                    rawType
                }
            }
    }

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map { field ->
        val typeName = field.toTypeName()

        return@map PropertySpec.builder(field.name, typeName, kModifiers)
            .apply { if (typeName in constTypes) addModifiers(KModifier.CONST) }
            .initializer("%L", field.value.get())
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
