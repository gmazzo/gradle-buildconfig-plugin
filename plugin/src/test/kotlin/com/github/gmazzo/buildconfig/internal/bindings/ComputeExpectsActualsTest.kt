package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.BuildConfigTask
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.DefaultBuildConfigExtension
import com.github.gmazzo.buildconfig.internal.capitalized
import org.gradle.kotlin.dsl.buildConfigField
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComputeExpectsActualsTest {

    private val project = ProjectBuilder.builder().build()

    @TestFactory
    fun findEffectiveSpec() = listOf(

        // rare case, all are empty
        findEffectiveSpecCase(
            "all empty", layout(kmpAndroidLegacy = true), mapOf(
                "androidBarProdDebug" to "androidBarProdDebug",
                "androidBarProdRelease" to "androidBarProdRelease",
                "androidBarStageDebug" to "androidBarStageDebug",
                "androidBarStageRelease" to "androidBarStageRelease",
                "androidFooProdDebug" to "androidFooProdDebug",
                "androidFooProdRelease" to "androidFooProdRelease",
                "androidFooStageDebug" to "androidFooStageDebug",
                "androidFooStageRelease" to "androidFooStageRelease",
                "androidUnitTest" to "androidUnitTest",
                "androidUnitTestDebug" to "androidUnitTest",
                "androidUnitTestRelease" to "androidUnitTest",
                "androidInstrumentationTest" to "androidInstrumentationTest",
                "androidInstrumentationTestDebug" to "androidInstrumentationTestDebug",
                "androidInstrumentationTestRelease" to "androidInstrumentationTestRelease",
                "iosArm64Main" to "nativeMain",
                "iosArm64Test" to "nativeTest",
                "iosSimulatorArm64Main" to "nativeMain",
                "iosSimulatorArm64Test" to "nativeTest",
                "jvmMain" to "jvmMain",
                "jvmTest" to "jvmTest",
                "jsMain" to "webMain",
                "jsTest" to "webTest",
                "wasmJsMain" to "webMain",
                "wasmJsTest" to "webTest",
            )
        ),

        // typical case, only main has fields
        findEffectiveSpecCase(
            "single field", layout(kmpAndroidLegacy = true) {
                buildConfigField("COMMON", "aCommonValue")

            }, mapOf(
                "androidBarProdDebug" to "androidBarProdDebug",
                "androidBarProdRelease" to "androidBarProdRelease",
                "androidBarStageDebug" to "androidBarStageDebug",
                "androidBarStageRelease" to "androidBarStageRelease",
                "androidFooProdDebug" to "androidFooProdDebug",
                "androidFooProdRelease" to "androidFooProdRelease",
                "androidFooStageDebug" to "androidFooStageDebug",
                "androidFooStageRelease" to "androidFooStageRelease",
                "androidUnitTest" to "androidUnitTest",
                "androidUnitTestDebug" to "androidUnitTest",
                "androidUnitTestRelease" to "androidUnitTest",
                "androidInstrumentationTest" to "androidInstrumentationTest",
                "androidInstrumentationTestDebug" to "androidInstrumentationTestDebug",
                "androidInstrumentationTestRelease" to "androidInstrumentationTestRelease",
                "iosArm64Main" to "nativeMain",
                "iosArm64Test" to "nativeTest",
                "iosSimulatorArm64Main" to "nativeMain",
                "iosSimulatorArm64Test" to "nativeTest",
                "jvmMain" to "jvmMain",
                "jvmTest" to "jvmTest",
                "jsMain" to "webMain",
                "jsTest" to "webTest",
                "wasmJsMain" to "webMain",
                "wasmJsTest" to "webTest",
            )
        ),

        // mobile flag case
        findEffectiveSpecCase(
            "mobile flag field", layout(kmpAndroidLegacy = true) {
                buildConfigField("IS_MOBILE", false)

                sourceSet("androidMain") {
                    buildConfigField("IS_MOBILE", true)
                }
                sourceSet("iosMain") {
                    buildConfigField("IS_MOBILE", true)
                }

            }, mapOf(
                "androidBarProdDebug" to "androidBarProdDebug",
                "androidBarProdRelease" to "androidBarProdRelease",
                "androidBarStageDebug" to "androidBarStageDebug",
                "androidBarStageRelease" to "androidBarStageRelease",
                "androidFooProdDebug" to "androidFooProdDebug",
                "androidFooProdRelease" to "androidFooProdRelease",
                "androidFooStageDebug" to "androidFooStageDebug",
                "androidFooStageRelease" to "androidFooStageRelease",
                "androidUnitTest" to "androidUnitTest",
                "androidUnitTestDebug" to "androidUnitTest",
                "androidUnitTestRelease" to "androidUnitTest",
                "androidInstrumentationTest" to "androidInstrumentationTest",
                "androidInstrumentationTestDebug" to "androidInstrumentationTestDebug",
                "androidInstrumentationTestRelease" to "androidInstrumentationTestRelease",
                "iosArm64Main" to "iosMain",
                "iosArm64Test" to "nativeTest",
                "iosSimulatorArm64Main" to "iosMain",
                "iosSimulatorArm64Test" to "nativeTest",
                "jvmMain" to "jvmMain",
                "jvmTest" to "jvmTest",
                "jsMain" to "webMain",
                "jsTest" to "webTest",
                "wasmJsMain" to "webMain",
                "wasmJsTest" to "webTest",
            )
        ),

        // kmp typical case, platform field
        findEffectiveSpecCase(
            "platform field", layout(kmpAndroidLegacy = true) {
                buildConfigField("COMMON", "aCommonValue")
                buildConfigField("PLATFORM", expect<String>())

                sourceSet("androidMain") {
                    buildConfigField("PLATFORM", "android")
                }
                sourceSet("jvmMain") {
                    buildConfigField("PLATFORM", "jvm")
                }
                sourceSet("iosMain") {
                    buildConfigField("PLATFORM", "ios")
                }
                sourceSet("webMain") {
                    buildConfigField("PLATFORM", "web")
                }

            }, mapOf(
                "androidBarProdDebug" to "androidBarProdDebug",
                "androidBarProdRelease" to "androidBarProdRelease",
                "androidBarStageDebug" to "androidBarStageDebug",
                "androidBarStageRelease" to "androidBarStageRelease",
                "androidFooProdDebug" to "androidFooProdDebug",
                "androidFooProdRelease" to "androidFooProdRelease",
                "androidFooStageDebug" to "androidFooStageDebug",
                "androidFooStageRelease" to "androidFooStageRelease",
                "androidUnitTest" to "androidUnitTest",
                "androidUnitTestDebug" to "androidUnitTest",
                "androidUnitTestRelease" to "androidUnitTest",
                "androidInstrumentationTest" to "androidInstrumentationTest",
                "androidInstrumentationTestDebug" to "androidInstrumentationTestDebug",
                "androidInstrumentationTestRelease" to "androidInstrumentationTestRelease",
                "iosArm64Main" to "iosMain",
                "iosArm64Test" to "nativeTest",
                "iosSimulatorArm64Main" to "iosMain",
                "iosSimulatorArm64Test" to "nativeTest",
                "jvmMain" to "jvmMain",
                "jvmTest" to "jvmTest",
                "jsMain" to "webMain",
                "jsTest" to "webTest",
                "wasmJsMain" to "webMain",
                "wasmJsTest" to "webTest",
            )
        ),

        // complex case, fields at different flavors
        findEffectiveSpecCase(
            "complex", layout(kmpAndroidLegacy = true) {
                buildConfigField("COMMON", "aCommonValue")
                buildConfigField("PLATFORM", expect<String>())

                sourceSet("androidMain") {
                    buildConfigField("PLATFORM", "android")
                    buildConfigField("DEBUG", expect(false))
                }
                sourceSet("androidDebug") {
                    buildConfigField("DEBUG", true)
                }
                sourceSet("jvmMain") {
                    buildConfigField("PLATFORM", "jvm")
                }
                sourceSet("iosMain") {
                    buildConfigField("PLATFORM", "ios")
                    buildConfigField("IOS_VALUE", "anIOSValue")
                    buildConfigField("SIMULATOR", false)
                }
                sourceSet("iosSimulatorArm64Main") {
                    buildConfigField("SIMULATOR", true)
                }
                sourceSet("webMain") {
                    buildConfigField("PLATFORM", "web")
                }
                sourceSet("jsMain") {
                    buildConfigField("JS_VALUE", "aJsValue")
                }

            }, mapOf(
                "androidBarProdDebug" to "androidBarProdDebug",
                "androidBarProdRelease" to "androidBarProdRelease",
                "androidBarStageDebug" to "androidBarStageDebug",
                "androidBarStageRelease" to "androidBarStageRelease",
                "androidFooProdDebug" to "androidFooProdDebug",
                "androidFooProdRelease" to "androidFooProdRelease",
                "androidFooStageDebug" to "androidFooStageDebug",
                "androidFooStageRelease" to "androidFooStageRelease",
                "androidUnitTest" to "androidUnitTest",
                "androidUnitTestDebug" to "androidUnitTest",
                "androidUnitTestRelease" to "androidUnitTest",
                "androidInstrumentationTest" to "androidInstrumentationTest",
                "androidInstrumentationTestDebug" to "androidInstrumentationTestDebug",
                "androidInstrumentationTestRelease" to "androidInstrumentationTestRelease",
                "iosArm64Main" to "iosArm64Main",
                "iosArm64Test" to "nativeTest",
                "iosSimulatorArm64Main" to "iosSimulatorArm64Main",
                "iosSimulatorArm64Test" to "nativeTest",
                "jvmMain" to "jvmMain",
                "jvmTest" to "jvmTest",
                "jsMain" to "jsMain",
                "jsTest" to "webTest",
                "wasmJsMain" to "wasmJsMain",
                "wasmJsTest" to "webTest",
            )
        ),

        // simple with classes case
        // TODO does sub classes may have different effective specs? it's not covered with tests now
        findEffectiveSpecCase(
            "simple with classes", layout(kmpAndroidLegacy = true) {
                buildConfigField("API_URL", expect("\"https://api.example.com\""))
                buildConfigField("TIMEOUT", 30)
                buildConfigField("FEATURE_ENABLED", expect(true))
                buildConfigField("PROVIDED", project.provider { "computedValue" })

                subclass("Extra1") {
                    buildConfigField("EXTRA_FIELD", "aValue")
                }

                subclass("Extra2") {
                    buildConfigField("EXTRA_FIELD", 42)
                }

            },
            mapOf(
                "androidBarProdDebug" to "androidBarProdDebug",
                "androidBarProdRelease" to "androidBarProdRelease",
                "androidBarStageDebug" to "androidBarStageDebug",
                "androidBarStageRelease" to "androidBarStageRelease",
                "androidFooProdDebug" to "androidFooProdDebug",
                "androidFooProdRelease" to "androidFooProdRelease",
                "androidFooStageDebug" to "androidFooStageDebug",
                "androidFooStageRelease" to "androidFooStageRelease",
                "androidUnitTest" to "androidUnitTest",
                "androidUnitTestDebug" to "androidUnitTest",
                "androidUnitTestRelease" to "androidUnitTest",
                "androidInstrumentationTest" to "androidInstrumentationTest",
                "androidInstrumentationTestDebug" to "androidInstrumentationTestDebug",
                "androidInstrumentationTestRelease" to "androidInstrumentationTestRelease",
                "iosArm64Main" to "nativeMain",
                "iosArm64Test" to "nativeTest",
                "iosSimulatorArm64Main" to "nativeMain",
                "iosSimulatorArm64Test" to "nativeTest",
                "jvmMain" to "jvmMain",
                "jvmTest" to "jvmTest",
                "jsMain" to "webMain",
                "jsTest" to "webTest",
                "wasmJsMain" to "webMain",
                "wasmJsTest" to "webTest",
            )
        ),
    )

    private fun findEffectiveSpecCase(
        name: String,
        layout: BuildConfigExtensionInternal,
        entries: Map<String, String>,
    ) = dynamicContainer(
        name, entries
            .map { findEffectiveSpecCase(layout, it.key, it.value) } +
            layout.extraSpecs.names.map { className ->
                dynamicContainer(
                    className, entries
                        .map { findEffectiveSpecCase(layout, it.key, it.value) { ss -> ss.forClass(className) } })
            })

    private fun findEffectiveSpecCase(
        layout: BuildConfigExtensionInternal,
        targetSourceSet: String,
        expectedSourceSet: String,
        selector: (BuildConfigSourceSetInternal) -> BuildConfigClassSpec = { it }
    ) = dynamicTest("$targetSourceSet -> $expectedSourceSet") {
        val actual = findEffectiveSpec(layout.sourceSets.getByName(targetSourceSet), selector).name

        assertEquals(expectedSourceSet, actual)
    }

    @TestFactory
    fun computeExpectsActuals() = listOf(

        // Simple case: actual matches expected
        computeExpectsActualsCase("single", layout(kmpAndroidLegacy = false) {
            buildConfigField("COMMON", "aCommonValue")

        }) {
            buildConfigField("COMMON", "aCommonValue")
        },

        // Legacy case: complex setup without expect/actual
        computeExpectsActualsCase("complex setup without expect/actual", layout(kmpAndroidLegacy = false) {
            buildConfigField("API_URL", "\"https://api.example.com\"")
            buildConfigField("TIMEOUT", 30)
            buildConfigField("FEATURE_ENABLED", true)
            buildConfigField("PROVIDED", project.provider { "computedValue" })

            subclass("Extra1") {
                buildConfigField("EXTRA_FIELD", "aValue")
            }

            subclass("Extra2") {
                buildConfigField("EXTRA_FIELD", 42)
            }

        }) {
            val providedValue = project.provider { "computedValue" }

            buildConfigField("API_URL", "\"https://api.example.com\"")
            buildConfigField("TIMEOUT", 30)
            buildConfigField("FEATURE_ENABLED", true)
            buildConfigField("PROVIDED", providedValue)

            subclass("Extra1") {
                buildConfigField("EXTRA_FIELD", "aValue")
            }

            subclass("Extra2") {
                buildConfigField("EXTRA_FIELD", 42)
            }
        },

        // single mobile field with explicit expect
        computeExpectsActualsCase("single mobile field", layout(kmpAndroidLegacy = false) {
            buildConfigField("IS_MOBILE", expect(false))

            sourceSet("androidMain") {
                buildConfigField("IS_MOBILE", true)
            }
            sourceSet("iosMain") {
                buildConfigField("IS_MOBILE", true)
            }

        }) {
            buildConfigField("IS_MOBILE", false).shouldBeExpect()

            sourceSet("androidMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("IS_MOBILE", true).shouldBeActual()
            }
            sourceSet("iosMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("IS_MOBILE", true).shouldBeActual()
            }
            sourceSet("jvmMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("IS_MOBILE", false).shouldBeActual()
            }
            sourceSet("webMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("IS_MOBILE", false).shouldBeActual()
            }
        },

        // complex scenarios
        computeExpectsActualsCase("complex", layout(kmpAndroidLegacy = false) {
            buildConfigField("API_URL", expect("\"https://api.example.com\""))
            buildConfigField("TIMEOUT", expect(30))
            buildConfigField("FEATURE_ENABLED", expect(true))

            subclass("Extra1") {
                buildConfigField("EXTRA_FIELD", "aValue")
                buildConfigField("PROVIDED", project.provider { "computedValue" })
            }

            subclass("Extra2") {
                buildConfigField("EXTRA_FIELD", expect(42))
            }

            sourceSet("jvmMain") {
                buildConfigField("TIMEOUT", 60)

                subclass("Extra2") {
                    buildConfigField("EXTRA_FIELD", -100)
                }

                subclass("Extra3") {
                    buildConfigField("EXTRA_FIELD", "anotherValue")
                }
            }
            sourceSet("androidMain") {
                buildConfigField("FEATURE_ENABLED", false)
            }

        }) {
            buildConfigField("API_URL", "\"https://api.example.com\"").shouldBeExpect()
            buildConfigField("TIMEOUT", 30).shouldBeExpect()
            buildConfigField("FEATURE_ENABLED", true).shouldBeExpect()

            subclass("Extra1") {
                buildConfigField("EXTRA_FIELD", "aValue")
                buildConfigField("PROVIDED", project.provider { "computedValue" })
            }

            subclass("Extra2") {
                buildConfigField("EXTRA_FIELD", expect(42)).shouldBeExpect()
            }

            sourceSet("jvmMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("API_URL", "\"https://api.example.com\"").shouldBeActual()
                buildConfigField("TIMEOUT", 60).shouldBeActual()
                buildConfigField("FEATURE_ENABLED", true).shouldBeActual()

                subclass("Extra2") {
                    buildConfigField("EXTRA_FIELD", -100).shouldBeActual()
                }

                subclass("Extra3") {
                    buildConfigField("EXTRA_FIELD", "anotherValue")
                }
            }
            sourceSet("androidMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("API_URL", "\"https://api.example.com\"").shouldBeActual()
                buildConfigField("TIMEOUT", 30).shouldBeActual()
                buildConfigField("FEATURE_ENABLED", false).shouldBeActual()
            }
            sourceSet("nativeMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("API_URL", "\"https://api.example.com\"").shouldBeActual()
                buildConfigField("TIMEOUT", 30).shouldBeActual()
                buildConfigField("FEATURE_ENABLED", true).shouldBeActual()
            }
            sourceSet("webMain") {
                className("BuildConfig") // should match the root one

                buildConfigField("API_URL", "\"https://api.example.com\"").shouldBeActual()
                buildConfigField("TIMEOUT", 30).shouldBeActual()
                buildConfigField("FEATURE_ENABLED", true).shouldBeActual()
            }
        }
    )

    private fun computeExpectsActualsCase(
        name: String,
        input: BuildConfigExtensionInternal,
        expectsBlock: BuildConfigExtensionInternal.() -> Unit
    ): DynamicContainer {
        val expects = create(expectsBlock)
        val actual by lazy { input.computeExpectsActuals(); input }

        fun BuildConfigClassSpec.classCases(actual: Lazy<BuildConfigClassSpec?>): List<DynamicTest> =
            listOf(dynamicTest("class name match") {
                val expectedNames = fullClassName
                val actualNames = actual.value?.fullClassName

                assertEquals(expectedNames, actualNames)
            }) +
                buildConfigFields.map { field ->
                    dynamicTest("field: ${field.name}") {
                        val actualField = actual.value?.buildConfigFields?.findByName(field.name)

                        assertEquals(field.asMap(), actualField?.asMap())
                    }

                } + dynamicTest("field names match") {
                val expectedNames = buildConfigFields.names
                val actualNames = actual.value?.buildConfigFields?.names

                assertEquals(expectedNames, actualNames)
            }

        fun BuildConfigSourceSetInternal.sourceSetCases(): List<DynamicNode> {
            val actualSourceSet = lazy { actual.sourceSets.findByName(this@sourceSetCases.name) }

            return classCases(actualSourceSet) +
                extraSpecs.map {
                    dynamicContainer(
                        "class: ${it.name}",
                        it.classCases(lazy { actualSourceSet.value?.extraSpecs?.findByName(it.name) })
                    )
                } +
                dynamicTest("extra names match") {
                    val expectedNames = extraSpecs.names
                    val actualNames = actualSourceSet.value.extraSpecs.names

                    assertEquals(expectedNames, actualNames)
                }
        }


        val otherSourceSetsMatch = expects.sourceSets
            .filter { it.name != "main" }
            .map { it.name to it.sourceSetCases() }
            .filter { (_, it) -> it.isNotEmpty() }
            .map { dynamicContainer("sourceSet: ${it.first}", it.second) }

        return dynamicContainer(
            name,
            expects.sourceSetCases() +
                otherSourceSetsMatch +
                dynamicTest("source set names match") {
                    val expectedNames = expects.sourceNamesWithContent
                    val actualNames = actual.sourceNamesWithContent

                    assertEquals(expectedNames, actualNames)
                })
    }

    private val BuildConfigExtensionInternal.sourceNamesWithContent
        get() = sourceSets.asSequence()
            .filter { it.buildConfigFields.isNotEmpty() || it.extraSpecs.isNotEmpty() }
            .mapTo(sortedSetOf()) { it.name }

    @Suppress("unused", "UnusedVariable")
    private fun layout(kmpAndroidLegacy: Boolean, block: BuildConfigExtensionInternal.() -> Unit = {}) = create {
        val main by sourceSets.getting
        val test by sourceSets.creating
        val androidMain by sourceSets.creating { dependsOn(main) }

        if (kmpAndroidLegacy) {
            val androidDebug by sourceSets.creating { dependsOn(androidMain); androidMain.supersededBy(this) }
            val androidRelease by sourceSets.creating { dependsOn(androidMain); androidMain.supersededBy(this) }
            val androidFoo by sourceSets.creating { dependsOn(androidMain); androidMain.supersededBy(this) }
            val androidBar by sourceSets.creating { dependsOn(androidMain); androidMain.supersededBy(this) }
            val androidStage by sourceSets.creating { dependsOn(androidMain); androidMain.supersededBy(this) }
            val androidProd by sourceSets.creating { dependsOn(androidMain); androidMain.supersededBy(this) }
            val androidFooStage by sourceSets.creating {
                dependsOn(androidFoo); androidFoo.supersededBy(this)
                dependsOn(androidStage); androidStage.supersededBy(this)
            }
            val androidBarStage by sourceSets.creating {
                dependsOn(androidBar); androidBar.supersededBy(this)
                dependsOn(androidStage); androidStage.supersededBy(this)
            }
            val androidFooProd by sourceSets.creating {
                dependsOn(androidFoo); androidFoo.supersededBy(this)
                dependsOn(androidProd); androidProd.supersededBy(this)
            }
            val androidBarProd by sourceSets.creating {
                dependsOn(androidBar); androidBar.supersededBy(this)
                dependsOn(androidProd); androidProd.supersededBy(this)
            }
            val androidFooStageDebug by sourceSets.creating {
                dependsOn(androidFooStage); androidFooStage.supersededBy(this); markAsKMPTarget()
                dependsOn(androidDebug); androidDebug.supersededBy(this); markAsKMPTarget()
            }
            val androidFooStageRelease by sourceSets.creating {
                dependsOn(androidFooStage); androidFooStage.supersededBy(this); markAsKMPTarget()
                dependsOn(androidRelease); androidRelease.supersededBy(this); markAsKMPTarget()
            }
            val androidFooProdDebug by sourceSets.creating {
                dependsOn(androidFooProd); androidFooProd.supersededBy(this); markAsKMPTarget()
                dependsOn(androidDebug); androidDebug.supersededBy(this); markAsKMPTarget()
            }
            val androidFooProdRelease by sourceSets.creating {
                dependsOn(androidFooProd); androidFooProd.supersededBy(this); markAsKMPTarget()
                dependsOn(androidRelease); androidRelease.supersededBy(this); markAsKMPTarget()
            }
            val androidBarStageDebug by sourceSets.creating {
                dependsOn(androidBarStage); androidBarStage.supersededBy(this); markAsKMPTarget()
                dependsOn(androidDebug); androidDebug.supersededBy(this); markAsKMPTarget()
            }
            val androidBarStageRelease by sourceSets.creating {
                dependsOn(androidBarStage); androidBarStage.supersededBy(this); markAsKMPTarget()
                dependsOn(androidRelease); androidRelease.supersededBy(this); markAsKMPTarget()
            }
            val androidBarProdDebug by sourceSets.creating {
                dependsOn(androidBarProd); androidBarProd.supersededBy(this); markAsKMPTarget()
                dependsOn(androidDebug); androidDebug.supersededBy(this); markAsKMPTarget()
            }
            val androidBarProdRelease by sourceSets.creating {
                dependsOn(androidBarProd); androidBarProd.supersededBy(this); markAsKMPTarget()
                dependsOn(androidRelease); androidRelease.supersededBy(this); markAsKMPTarget()
            }
            val androidUnitTest by sourceSets.creating { dependsOn(test) }
            val androidUnitTestDebug by sourceSets.creating { dependsOn(androidUnitTest); markAsKMPTarget() }
            val androidUnitTestRelease by sourceSets.creating { dependsOn(androidUnitTest); markAsKMPTarget() }
            val androidInstrumentationTest by sourceSets.creating
            val androidInstrumentationTestDebug by sourceSets.creating { dependsOn(androidInstrumentationTest); markAsKMPTarget() }
            val androidInstrumentationTestRelease by sourceSets.creating { dependsOn(androidInstrumentationTest); markAsKMPTarget() }

        } else {
            androidMain.markAsKMPTarget()
            val androidHostTest by sourceSets.creating { dependsOn(test); markAsKMPTarget() }
            val androidDeviceTest by sourceSets.creating { dependsOn(test); markAsKMPTarget() }
        }

        val nativeMain by sourceSets.creating { dependsOn(main) }
        val nativeTest by sourceSets.creating { dependsOn(test) }
        val iosMain by sourceSets.creating { dependsOn(nativeMain) }
        val iosTest by sourceSets.creating { dependsOn(nativeTest) }
        val iosArm64Main by sourceSets.creating { dependsOn(iosMain); markAsKMPTarget() }
        val iosArm64Test by sourceSets.creating { dependsOn(iosTest); markAsKMPTarget() }
        val iosSimulatorArm64Main by sourceSets.creating { dependsOn(iosMain); markAsKMPTarget() }
        val iosSimulatorArm64Test by sourceSets.creating { dependsOn(iosTest); markAsKMPTarget() }
        val jvmMain by sourceSets.creating { dependsOn(main); markAsKMPTarget() }
        val jvmTest by sourceSets.creating { dependsOn(test); markAsKMPTarget() }
        val webMain by sourceSets.creating { dependsOn(main) }
        val webTest by sourceSets.creating { dependsOn(test) }
        val jsMain by sourceSets.creating { dependsOn(webMain); markAsKMPTarget() }
        val jsTest by sourceSets.creating { dependsOn(webTest); markAsKMPTarget() }
        val wasmJsMain by sourceSets.creating { dependsOn(webMain); markAsKMPTarget() }
        val wasmJsTest by sourceSets.creating { dependsOn(webTest); markAsKMPTarget() }

        block()
    }

    private fun create(block: BuildConfigExtensionInternal.() -> Unit) =
        project.objects.newInstance<DefaultBuildConfigExtension>().also {
            it.sourceSets.configureEach { spec ->
                val prefix = when (val name = spec.name) {
                    "main" -> ""
                    else -> name.capitalized
                }
                val taskName = "generate${prefix}BuildConfig"

                spec.packageName.convention("org.test")
                spec.className.convention("${prefix}BuildConfig")
                spec.generateTask =
                    if (project.tasks.names.contains(taskName)) project.tasks.named<BuildConfigTask>(taskName)
                    else project.tasks.register<BuildConfigTask>(taskName)
            }

            it.block()
        }

    private fun BuildConfigExtensionInternal.sourceSet(name: String, block: BuildConfigSourceSetInternal.() -> Unit) =
        sourceSets.maybeCreate(name).apply { block() }

    private fun BuildConfigSourceSetInternal.subclass(name: String, block: BuildConfigClassSpec.() -> Unit) =
        forClass(name).apply { block() }

    private fun BuildConfigField.shouldBeExpect() = apply { tags.add(BuildConfigField.IsExpect) }

    private fun BuildConfigField.shouldBeActual() = apply { tags.add(BuildConfigField.IsActual) }

    private fun BuildConfigField.asMap() = mapOf(
        "name" to name,
        "type" to type.orNull,
        "value" to value.orNull,
        "tags" to tags.get(),
    )

    private val BuildConfigClassSpec.fullClassName
        get() = when (val packageName = packageName.orNull) {
            null -> className.get()
            else -> "$packageName.${className.get()}"
        }

}
