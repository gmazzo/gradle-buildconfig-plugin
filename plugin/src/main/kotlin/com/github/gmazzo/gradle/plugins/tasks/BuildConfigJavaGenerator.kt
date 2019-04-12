package com.github.gmazzo.gradle.plugins.tasks

import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging
import javax.annotation.Generated
import javax.lang.model.element.Modifier

internal object BuildConfigJavaGenerator : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    override fun invoke(spec: BuildConfigTaskSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val typeSpec = TypeSpec.classBuilder(spec.className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        if (spec.addGeneratedAnnotation) {
            typeSpec.addAnnotation(
                AnnotationSpec.builder(Generated::class.java)
                    .addMember("value", "\$S", javaClass.name)
                    .build()
            )
        }

        spec.fields.forEach {
            val typeName = when (it.type) {
                "String" -> TypeName.get(String::class.java)
                else -> try {
                    ClassName.bestGuess(it.type)
                } catch (_: IllegalArgumentException) {
                    TypeName.get(ClassUtils.getClass(it.type, false))
                }
            }

            typeSpec.addField(
                FieldSpec.builder(typeName, it.name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(CodeBlock.of(it.value))
                    .build()
            )
        }

        JavaFile
            .builder(
                spec.packageName, typeSpec
                    .addMethod(
                        MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PRIVATE)
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(spec.outputDir)
    }

}
