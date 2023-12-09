package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
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

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map { field ->
        val typeName = when (field.type.get()) {
            "String" -> String::class.asClassName()
            else -> runCatching { ClassName.bestGuess(field.type.get()) }
                .getOrElse { _ -> ClassUtils.getClass(field.type.get(), false).asTypeName() }
        }.copy(nullable = field.optional.get())
            .let { typeName ->
                when (field.collectionType.getOrElse(BuildConfigField.CollectionType.NONE)) {
                    BuildConfigField.CollectionType.COLLECTION -> COLLECTION
                    BuildConfigField.CollectionType.LIST -> LIST
                    BuildConfigField.CollectionType.SET -> SET
                    BuildConfigField.CollectionType.NONE -> null
                }?.parameterizedBy(typeName) ?: typeName
            }

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
