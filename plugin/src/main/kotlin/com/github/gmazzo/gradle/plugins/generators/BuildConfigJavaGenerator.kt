package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import javax.lang.model.element.Modifier

data class BuildConfigJavaGenerator(
    @get:Input var defaultVisibility: Boolean = false
) : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    private fun String.toTypeName(): TypeName {
        val nonNullable = removeSuffix("?")
        val type = when (nonNullable.removeSuffix("[]").lowercase()) {
            "boolean" -> TypeName.BOOLEAN
            "byte" -> TypeName.BYTE
            "short" -> TypeName.SHORT
            "char" -> TypeName.CHAR
            "int" -> TypeName.INT
            "integer" -> TypeName.INT
            "long" -> TypeName.LONG
            "float" -> TypeName.FLOAT
            "double" -> TypeName.DOUBLE
            "string" -> ClassName.get(String::class.java)
            else -> ClassName.bestGuess(this)
        }.let { if (this != nonNullable) it.box() else it }
        return if (nonNullable.endsWith("[]")) ArrayTypeName.of(type) else type
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
            val typeName = when (val type = field.type.get()) {
                is BuildConfigField.TypeRef -> TypeName.get(type.javaType)
                is BuildConfigField.TypeByName -> type.name.toTypeName().let { resolved ->
                    if (type.typeParameters.isEmpty()) resolved
                    else ParameterizedTypeName.get(
                        checkNotNull(resolved as? ClassName),
                        *type.typeParameters.map { it.toTypeName() }.toTypedArray()
                    )
                }
            }

            typeSpec.addField(
                FieldSpec.builder(typeName, field.name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .apply {
                        when (val value = field.value.get()) {
                            is BuildConfigField.Literal -> initializer(
                                if (value.value is CharSequence) "\$S" else "\$L",
                                value.value
                            )

                            is BuildConfigField.Expression -> initializer("\$L", value.value)
                        }
                    }
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
