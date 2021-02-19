package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging

open class BuildConfigKotlinGenerator(
    var topLevelConstants: Boolean = false,
    var internalVisibility: Boolean = false
) : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map {
        val typeName = when (it.type) {
            "String" -> String::class.asClassName()
            else -> try {
                ClassName.bestGuess(it.type)
            } catch (_: IllegalArgumentException) {
                ClassUtils.getClass(it.type, false).asTypeName()
            }
        }

        return@map PropertySpec.builder(it.name, typeName, kModifiers)
            .addModifiers(*(if (typeName in constTypes) arrayOf(KModifier.CONST) else emptyArray()))
            .initializer(CodeBlock.of(it.value))
            .build()
    }

    override fun execute(spec: BuildConfigGeneratorSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val fields = spec.fields.asPropertiesSpec()

        FileSpec.builder(spec.packageName, spec.className)
            .addFields(fields)
            .build()
            .writeTo(spec.outputDir)
    }

    private fun FileSpec.Builder.addFields(fields: List<PropertySpec>): FileSpec.Builder = when {
        topLevelConstants -> fields.fold(this) { acc, it -> acc.addProperty(it) }
        else -> addType(
            TypeSpec.objectBuilder(name)
                .addModifiers(kModifiers)
                .addProperties(fields)
                .build()
        )
    }

    private val kModifiers
        get() = if (internalVisibility) KModifier.INTERNAL else KModifier.PUBLIC

}
