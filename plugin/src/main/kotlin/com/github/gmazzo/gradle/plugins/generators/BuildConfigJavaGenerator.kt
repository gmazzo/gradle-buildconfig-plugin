package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.asVarArg
import com.github.gmazzo.gradle.plugins.collectionSize
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

            val type = when (typeName.lowercase()) {
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
                else -> ClassName.bestGuess(typeName)
            }.let { if (isNullable && it.isPrimitive) it.box() else if (!isNullable && it.isBoxedPrimitive) it.unbox() else it }

            val genericType =
                if (typeParameters.isEmpty()) type
                else ParameterizedTypeName.get(
                    checkNotNull(type as? ClassName),
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
            val typeName = when (val type = field.type.get()) {
                is BuildConfigField.JavaRef -> TypeName.get(type.javaType)
                is BuildConfigField.NameRef -> type.toTypeName()
            }

            val value = field.value.get()
            val valueIsNull = value is BuildConfigField.Literal && value.value == null
            val nullableAwareType = resolvePrimitiveArrayAwareType(typeName, valueIsNull)

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
        val count by lazy { forValue.collectionSize }
        val listFormat by lazy { format(count, "java.util.List.of(", ")") }
        val setFormat by lazy { format(count, "java.util.Set.of(", ")") }

        return when (this) {
            TypeName.LONG -> "\$LL" to 1
            ClassName.get(String::class.java) -> "\$S" to 1
            is ArrayTypeName -> format(count, "{", "}") to count
            ClassName.get(List::class.java) -> listFormat to count
            ClassName.get(Set::class.java) -> setFormat to count
            is ParameterizedTypeName -> when (rawType) {
                ClassName.get(List::class.java) -> listFormat to count
                ClassName.get(Set::class.java) -> setFormat to count
                else -> "\$L" to 1
            }

            else -> "\$L" to 1
        }
    }

    private fun format(count: Int, prefix: String, postfix: String) = (1..count).joinToString(
        prefix = prefix,
        separator = ", ",
        postfix = postfix,
        transform = { "\$L" }
    )

}
