package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.asVarArg
import com.github.gmazzo.gradle.plugins.elements
import com.github.gmazzo.gradle.plugins.parseTypename
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

    private fun BuildConfigField.Type.toTypeName(): TypeName = when (this) {
        is BuildConfigField.JavaRef -> TypeName.get(javaType)
        is BuildConfigField.NameRef -> {
            val (typeName, isArray, isNullable) = className.parseTypename()

            val type = when (typeName) {
                "boolean" -> TypeName.BOOLEAN
                "Boolean" -> TypeName.BOOLEAN.box()
                "byte" -> TypeName.BYTE
                "Byte" -> TypeName.BYTE.box()
                "short" -> TypeName.SHORT
                "Short" -> TypeName.SHORT.box()
                "char" -> TypeName.CHAR
                "Char" -> TypeName.CHAR.box()
                "int" -> TypeName.INT
                "Integer" -> TypeName.INT.box()
                "long" -> TypeName.LONG
                "Long" -> TypeName.LONG.box()
                "float" -> TypeName.FLOAT
                "Float" -> TypeName.FLOAT.box()
                "double" -> TypeName.DOUBLE
                "Double" -> TypeName.DOUBLE.box()
                "String" -> ClassName.get(String::class.java)
                else -> ClassName.bestGuess(typeName)
            }.let { if (isNullable && it.isPrimitive) it.box() else it }

            val genericType =
                if (typeParameters.isEmpty()) type
                else ParameterizedTypeName.get(
                    type as ClassName,
                    *typeParameters.map { it.toTypeName() }.toTypedArray()
                )

            if (isArray) ArrayTypeName.of(genericType) else genericType
        }
    }

    override fun execute(spec: BuildConfigGeneratorSpec) {
        logger.debug("Generating {} for fields {}", spec.className, spec.fields)

        val typeSpec = TypeSpec.classBuilder(spec.className)
            .addModifiers(Modifier.FINAL)

        if (!defaultVisibility) {
            typeSpec.addModifiers(Modifier.PUBLIC)
        }

        if (spec.documentation != null) {
            typeSpec.addJavadoc("\$L", spec.documentation)
        }

        spec.fields.forEach { field ->
            try {
                val typeName = when (val type = field.type.get()) {
                    is BuildConfigField.JavaRef -> TypeName.get(type.javaType)
                    is BuildConfigField.NameRef -> type.toTypeName()
                }
                val value = field.value.get()
                val nullableAwareType = if (value.value == null && typeName.isPrimitive) typeName.box() else typeName

                typeSpec.addField(
                    FieldSpec.builder(
                        nullableAwareType,
                        field.name,
                        Modifier.PUBLIC,
                        Modifier.STATIC,
                        Modifier.FINAL
                    ).apply {
                        when (value) {
                            is BuildConfigField.Literal -> {
                                val (format, count) = nullableAwareType.format(value.value)
                                val args = value.value.asVarArg()

                                check(count == args.size) {
                                    "Invalid number of arguments for ${field.name} of type ${nullableAwareType}: " +
                                            "expected $count, got ${args.size}: ${args.joinToString()}"
                                }
                                initializer(format, *args)
                            }

                            is BuildConfigField.Expression -> initializer("\$L", value.value)
                        }
                    }.build()
                )
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to generate field ${field.name} of type ${field.value.get()}, " +
                            "with value: ${field.value.get().value}", e
                )
            }
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

    private fun resolvePrimitiveArrayAwareType(typeName: TypeName, valueIsNull: Boolean): TypeName {
        val innerType = when (typeName) {
            is ArrayTypeName -> typeName.componentType
            else -> typeName
        }

        val nullableAwareType =
            if (valueIsNull && innerType.isPrimitive) innerType.box()
            else if (!valueIsNull && innerType.isBoxedPrimitive) innerType.unbox()
            else innerType

        return if (innerType == typeName) nullableAwareType
        else ArrayTypeName.of(nullableAwareType)
    }

    private fun TypeName.format(forValue: Any?): Pair<String, Int> {
        fun Any?.format() = when (this) {
            is Byte -> "(byte) \$L"
            is Char -> "'\$L'"
            is Long -> "\$LL"
            is Float -> "\$Lf"
            is String -> "\$S"
            else -> "\$L"
        }

        fun List<Any?>.format(prefix: String, postfix: String) = joinToString(
            prefix = prefix,
            separator = ", ",
            postfix = postfix,
            transform = { it.format() }
        ) to size

        val elements = forValue.elements
        val singleFormat by lazy { elements.single().format() to 1 }
        val arrayFormat by lazy { elements.format("{", "}") }
        val listFormat by lazy { elements.format("java.util.Arrays.asList(", ")") }
        val setFormat by lazy { elements.format("new java.util.LinkedHashSet(java.util.Arrays.asList(", "))") }

        return when (this) {
            TypeName.LONG, ClassName.get(String::class.java) -> singleFormat
            is ArrayTypeName -> arrayFormat
            ClassName.get(List::class.java) -> listFormat
            ClassName.get(Set::class.java) -> setFormat
            is ParameterizedTypeName -> when (rawType) {
                ClassName.get(List::class.java) -> listFormat
                ClassName.get(Set::class.java) -> setFormat
                else -> singleFormat
            }

            else -> singleFormat
        }
    }

}
