package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input

data class BuildConfigKotlinGenerator(
    @get:Input var topLevelConstants: Boolean = false,
    @get:Input var internalVisibility: Boolean = true
) : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map {
        val typeName = when (it.type.get()) {
            "String" -> String::class.asClassName()
            else -> runCatching { ClassName.bestGuess(it.type.get()) }
                .getOrElse { _ -> ClassUtils.getClass(it.type.get(), false).asTypeName() }
        }.copy(nullable = it.optional.get())

        return@map PropertySpec.builder(it.name, typeName, kModifiers)
            .apply { if (typeName in constTypes) addModifiers(KModifier.CONST) }
            .initializer("%L", it.value.get())
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
