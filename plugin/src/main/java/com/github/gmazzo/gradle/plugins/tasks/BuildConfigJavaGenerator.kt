package com.github.gmazzo.gradle.plugins.tasks

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.api.logging.Logging
import java.lang.reflect.Modifier.FINAL
import java.lang.reflect.Modifier.PUBLIC
import java.lang.reflect.Modifier.STATIC
import javax.annotation.Generated
import javax.lang.model.element.Modifier

internal object BuildConfigJavaGenerator : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    private val knownTypes by lazy {
        val constantModifiers = PUBLIC or STATIC or FINAL

        TypeName::class.java.declaredFields
            .filter { it.modifiers and constantModifiers == constantModifiers }
            .filter { TypeName::class.java.isAssignableFrom(it.type) }
            .map { it.get(null) as TypeName }
            .map { it.toString() to it }
            .toMap() + ("String" to TypeName.get(String::class.java))
    }

    override fun invoke(task: BuildConfigTask) {
        logger.debug("Generating ${task.className} for fields ${task.fields}")

        val typeSpec = TypeSpec.classBuilder(task.className)
            .addAnnotation(
                AnnotationSpec.builder(Generated::class.java)
                    .addMember("value", "\$S", javaClass.name)
                    .build()
            )

        task.distintFields.forEach {
            val typeName = knownTypes.getOrElse(it.type) {
                ClassName.bestGuess(it.type)
            }

            typeSpec.addField(
                FieldSpec.builder(
                    typeName,
                    it.name,
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
                )
                    .initializer(CodeBlock.of(it.value))
                    .build()
            )
        }

        JavaFile.builder(task.packageName, typeSpec.build())
            .build()
            .writeTo(task.outputDir)
    }

}
