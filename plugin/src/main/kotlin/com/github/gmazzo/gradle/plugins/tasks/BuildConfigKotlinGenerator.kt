package com.github.gmazzo.gradle.plugins.tasks

import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging

internal object BuildConfigKotlinGenerator : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    override fun execute(spec: BuildConfigTaskSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val typeSpec = TypeSpec.objectBuilder(spec.className)

        spec.fields.forEach {
            val typeName = when (it.type) {
                "String" -> String::class.asClassName()
                else -> try {
                    ClassName.bestGuess(it.type)
                } catch (_: IllegalArgumentException) {
                    ClassUtils.getClass(it.type, false).asTypeName()
                }
            }

            typeSpec.addProperty(
                PropertySpec.builder(
                    it.name,
                    typeName,
                    KModifier.PUBLIC
                )
                    .addModifiers(*(if (typeName in constTypes) arrayOf(KModifier.CONST) else emptyArray()))
                    .initializer(CodeBlock.of(it.value))
                    .build()
            )
        }

        FileSpec.builder(spec.packageName, spec.className)
            .addType(typeSpec.build())
            .build()
            .writeTo(spec.outputDir)
    }

}
