package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
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

    private fun String.toTypeName(): TypeName {
        return when (this) {
            TypeName.BOOLEAN.toString() -> TypeName.BOOLEAN
            TypeName.BYTE.toString() -> TypeName.BYTE
            TypeName.SHORT.toString() -> TypeName.SHORT
            TypeName.CHAR.toString() -> TypeName.CHAR
            TypeName.INT.toString() -> TypeName.INT
            TypeName.LONG.toString() -> TypeName.LONG
            TypeName.FLOAT.toString() -> TypeName.FLOAT
            TypeName.DOUBLE.toString() -> TypeName.DOUBLE
            "String" -> ClassName.get(String::class.java)
            else -> try {
                ClassName.bestGuess(this)
            } catch (_: IllegalArgumentException) {
                ClassName.get(ClassUtils.getClass(this, false))
            }
        }
    }

    private fun BuildConfigField.toTypeName(): TypeName {
        return type.get().toTypeName()
            .let { rawType ->
                val typeArgs = typeArguments.getOrElse(emptyList())
                if (typeArgs.isNotEmpty()) {
                    check(rawType is ClassName) {
                        "Cannot parameterize type '$rawType'"
                    }
                    // Generic primitives must be boxed
                    ParameterizedTypeName.get(rawType, *typeArgs.map { it.toTypeName().box() }.toTypedArray())
                } else {
                    rawType
                }
        }
    }

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
            val typeName = field.toTypeName()

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
