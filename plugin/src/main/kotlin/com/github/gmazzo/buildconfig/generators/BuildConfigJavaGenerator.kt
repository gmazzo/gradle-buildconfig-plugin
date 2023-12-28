package com.github.gmazzo.buildconfig.generators

import com.github.gmazzo.buildconfig.BuildConfigType
import com.github.gmazzo.buildconfig.BuildConfigValue
import com.github.gmazzo.buildconfig.asVarArg
import com.github.gmazzo.buildconfig.elements
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import javax.lang.model.element.Modifier

data class BuildConfigJavaGenerator(
    @get:Input var defaultVisibility: Boolean = false
) : BuildConfigGenerator {

    private val logger = Logging.getLogger(javaClass)

    private fun BuildConfigType.toTypeName(): TypeName {
        var type = when (className.lowercase()) {
            "boolean" -> if (nullable) TypeName.BOOLEAN.box() else TypeName.BOOLEAN
            "byte" -> if (nullable) TypeName.BYTE.box() else TypeName.BYTE
            "short" -> if (nullable) TypeName.SHORT.box() else TypeName.SHORT
            "char", "character" -> if (nullable) TypeName.CHAR.box() else TypeName.CHAR
            "int", "integer" -> if (nullable) TypeName.INT.box() else TypeName.INT
            "long" -> if (nullable) TypeName.LONG.box() else TypeName.LONG
            "float" -> if (nullable) TypeName.FLOAT.box() else TypeName.FLOAT
            "double" -> if (nullable) TypeName.DOUBLE.box() else TypeName.DOUBLE
            "string" -> STRING
            "list" -> LIST
            "set" -> SET
            "*", "?" -> WildcardTypeName.get(Object::class.java)
            else -> ClassName.bestGuess(className)
        }
        if (typeArguments.isNotEmpty()) type = ParameterizedTypeName.get(
            type as ClassName,
            *typeArguments.map { it.copy(nullable = true).toTypeName() }.toTypedArray()
        )
        if (array) type = ArrayTypeName.of(type)
        return type
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
                val typeName = field.type.get().toTypeName()
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
                            is BuildConfigValue.Literal -> {
                                val (format, count) = nullableAwareType.format(value.value)
                                val args = value.value.asVarArg()

                                check(count == args.size) {
                                    "Invalid number of arguments for ${field.name} of type ${nullableAwareType}: " +
                                            "expected $count, got ${args.size}: ${args.joinToString()}"
                                }
                                initializer(format, *args)
                            }

                            is BuildConfigValue.Expression -> initializer("\$L", value.value)
                        }
                    }.build()
                )
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to generate field '${field.name}' of type '${field.type.get()}', " +
                            "with value: ${field.value.get().value} (of type '${field.value.get().value?.javaClass}')",
                    e
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

    private fun TypeName.format(forValue: Any?): Pair<String, Int> {
        fun TypeName?.format() = when (if (this?.isBoxedPrimitive == true) unbox() else this) {
            TypeName.BYTE -> "(byte) \$L"
            TypeName.CHAR -> "'\$L'"
            TypeName.SHORT -> "(short) \$L"
            TypeName.LONG -> "\$LL"
            TypeName.FLOAT -> "\$Lf"
            STRING -> "\$S"
            else -> "\$L"
        }

        fun List<Any?>.format(prefix: String, postfix: String, item: (Any) -> TypeName) = joinToString(
            prefix = prefix,
            separator = ", ",
            postfix = postfix,
            transform = { it?.let(item).format() }
        ) to size

        val elements = forValue.elements

        fun singleFormat() =
            elements.single()?.let { TypeName.get(it::class.java) }.format() to 1

        fun arrayFormat(item: (Any) -> TypeName) =
            elements.format("{", "}", item)

        fun listFormat(item: (Any) -> TypeName) =
            elements.format("java.util.Arrays.asList(", ")", item)

        fun setFormat(item: (Any) -> TypeName) =
            elements.format("new java.util.LinkedHashSet<>(java.util.Arrays.asList(", "))", item)

        return when (this) {
            TypeName.LONG, ClassName.get(String::class.java) -> singleFormat()
            is ArrayTypeName -> arrayFormat { componentType }
            LIST, GENERIC_LIST -> listFormat { TypeName.get(it::class.java) }
            SET, GENERIC_SET -> setFormat { TypeName.get(it::class.java) }
            is ParameterizedTypeName -> when (rawType) {
                LIST, GENERIC_LIST -> listFormat { typeArguments.first() }
                SET, GENERIC_SET -> setFormat { typeArguments.first() }
                else -> singleFormat()
            }

            else -> singleFormat()
        }
    }

    private companion object {
        private val STRING = ClassName.get(String::class.java)
        private val LIST = ClassName.get(List::class.java)
        private val SET = ClassName.get(Set::class.java)
        private val GENERIC_LIST = ClassName.get("", "List")
        private val GENERIC_SET = ClassName.get("", "Set")
    }

}
