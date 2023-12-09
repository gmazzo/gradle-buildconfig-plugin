package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.apache.commons.lang3.ClassUtils
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import javax.lang.model.element.Modifier

data class BuildConfigJavaGenerator(
    @get:Input var defaultVisibility: Boolean = false
) : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    override fun execute(spec: BuildConfigGeneratorSpec) {
        logger.debug("Generating ${spec.className} for fields ${spec.fields}")

        val typeSpec = TypeSpec.classBuilder(spec.className)
            .addModifiers(Modifier.FINAL)

        if (!defaultVisibility) {
            typeSpec.addModifiers(Modifier.PUBLIC)
        }

        if (spec.documentation != null) {
            typeSpec.addJavadoc("\$L", spec.documentation)
        }

        spec.fields.forEach { field ->
            val typeName = when (field.type.get()) {
                "String" -> TypeName.get(String::class.java)
                else -> try {
                    ClassName.bestGuess(field.type.get())
                } catch (_: IllegalArgumentException) {
                    TypeName.get(ClassUtils.getClass(field.type.get(), false))
                }
            }.let { typeName ->
                when (field.collectionType.getOrElse(BuildConfigField.CollectionType.NONE)) {
                    BuildConfigField.CollectionType.COLLECTION -> ClassName.get(Collection::class.java)
                    BuildConfigField.CollectionType.LIST -> ClassName.get(List::class.java)
                    BuildConfigField.CollectionType.SET -> ClassName.get(Set::class.java)
                    BuildConfigField.CollectionType.NONE -> null
                }?.let {
                    ParameterizedTypeName.get(it, typeName)
                } ?: typeName
            }

            typeSpec.addField(
                FieldSpec.builder(typeName, field.name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$L", field.value.get())
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
