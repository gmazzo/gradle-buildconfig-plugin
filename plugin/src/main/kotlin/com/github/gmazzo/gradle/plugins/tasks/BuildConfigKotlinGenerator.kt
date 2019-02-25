package com.github.gmazzo.gradle.plugins.tasks

import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging
import javax.annotation.Generated

internal object BuildConfigKotlinGenerator : BuildConfigGenerator {

    private val constTypes = setOf(String::class.asClassName(), BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE)

    private val logger = Logging.getLogger(javaClass)

    override fun invoke(task: BuildConfigTask) {
        logger.debug("Generating ${task.className} for fields ${task.fields}")

        val typeSpec = TypeSpec.objectBuilder(task.className)
            .addAnnotation(
                AnnotationSpec.builder(Generated::class.java)
                    .addMember("%S", javaClass.name)
                    .build()
            )

        task.distinctFields.forEach {
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

        FileSpec.builder(task.packageName, task.className)
            .addType(typeSpec.build())
            .build()
            .writeTo(task.outputDir)
    }

}
