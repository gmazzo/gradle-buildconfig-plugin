package com.github.gmazzo.gradle.plugins.tasks

import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.squareup.javapoet.*
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging
import javax.lang.model.element.Modifier

internal object BuildConfigJavaGenerator : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    override fun execute(spec: BuildConfigTaskSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val typeSpec = TypeSpec.classBuilder(spec.className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

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
