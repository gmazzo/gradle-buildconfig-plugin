package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.asVarArg
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

    private fun String.toTypeName(): TypeName {
        val (typeName, isArray, isNullable) = parseTypename()

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
            else -> ClassName.bestGuess(this)
        }.let { if (isNullable && it.isPrimitive) it.box() else if (!isNullable && it.isBoxedPrimitive) it.unbox() else it }
        return if (isArray) ArrayTypeName.of(type) else type
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

            val value = field.value.get()
            val valueIsNull = value is BuildConfigField.Literal && value.value == null
            val sanitizedNullableTypeName = resolvePrimitiveArrayAwareType(typeName, valueIsNull)

            typeSpec.addField(
                FieldSpec.builder(
                    sanitizedNullableTypeName,
                    field.name,
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
                )
                    .apply {
                        when (value) {
                            is BuildConfigField.Literal -> initializer(
                                value.value.poetFormat(sanitizedNullableTypeName.isPrimitive),
                                *value.value.asVarArg(),
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

    private fun Any?.poetFormat(asPrimitive: Boolean): String {
        fun Iterable<*>.asArray(): String =
            joinToString(prefix = "{", separator = ", ", postfix = "}") { it.poetFormat(asPrimitive) }

        return when (this) {
            is Array<*> -> asList().asArray()
            is ByteArray -> asList().asArray()
            is ShortArray -> asList().asArray()
            is CharArray -> asList().asArray()
            is IntArray -> asList().asArray()
            is LongArray -> asList().asArray()
            is FloatArray -> asList().asArray()
            is DoubleArray -> asList().asArray()
            is BooleanArray -> asList().asArray()
            is Set<*> -> if (asPrimitive) asArray() else joinToString(
                prefix = "new java.util.HashSet(java.util.Arrays.asList(",
                separator = ", ",
                postfix = "))"
            ) { it.poetFormat(asPrimitive) }

            is Iterable<*> -> if (asPrimitive) asArray() else joinToString(
                prefix = "java.util.Arrays.asList(",
                separator = ", ",
                postfix = ")"
            ) { it.poetFormat(asPrimitive) }

            is CharSequence -> "\$S"
            is Long -> "\$LL"
            else -> "\$L"
        }
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

}
