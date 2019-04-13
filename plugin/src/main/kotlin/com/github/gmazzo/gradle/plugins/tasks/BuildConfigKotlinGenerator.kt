package com.github.gmazzo.gradle.plugins.tasks

import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
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
import javax.annotation.Generated

internal object BuildConfigKotlinGenerator : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    override fun execute(spec: BuildConfigTaskSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val typeSpec = TypeSpec.objectBuilder(spec.className)

        if (spec.addGeneratedAnnotation) {
            typeSpec.addAnnotation(
                AnnotationSpec.builder(Generated::class.java)
                    .addMember("%S", javaClass.name)
                    .build()
            )
        }

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
