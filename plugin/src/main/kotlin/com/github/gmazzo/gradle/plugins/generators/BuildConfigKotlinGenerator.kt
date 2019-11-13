package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging

abstract class BuildConfigKotlinGenerator : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    abstract fun FileSpec.Builder.addFields(fields: List<PropertySpec>): FileSpec.Builder

    private fun Iterable<BuildConfigField>.asPropertiesSpec() = map {
        val typeName = when (it.type) {
            "String" -> String::class.asClassName()
            else -> try {
                ClassName.bestGuess(it.type)
            } catch (_: IllegalArgumentException) {
                ClassUtils.getClass(it.type, false).asTypeName()
            }
        }

        return@map PropertySpec.builder(it.name, typeName, KModifier.PUBLIC)
            .addModifiers(*(if (typeName in constTypes) arrayOf(KModifier.CONST) else emptyArray()))
            .initializer(CodeBlock.of(it.value))
            .build()
    }

    override fun execute(spec: BuildConfigTaskSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val fields = spec.fields.asPropertiesSpec()

        FileSpec.builder(spec.packageName, spec.className)
            .addFields(fields)
            .build()
            .writeTo(spec.outputDir)
    }

}
