package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import org.jetbrains.annotations.VisibleForTesting

private typealias Selector = (BuildConfigSourceSetInternal) -> BuildConfigClassSpec?

/**
 * Helper method to extract the complexity out of [KotlinBinder.Multiplatform] for handling expect/actual fields.
 */
internal fun BuildConfigExtensionInternal.computeExpectsActuals() {
    val seenSpecs = linkedSetOf<BuildConfigClassSpec>()
    val expectSpecs = linkedSetOf<Pair<BuildConfigSourceSetInternal, String?>>()
    val propagationCandidates = mutableMapOf<BuildConfigSourceSetInternal, MutableSet<BuildConfigSourceSetInternal>>()

    // first we infer all the expect/actuals based on matching names in dependsOns
    for (spec in sourceSets.filter { it.isKMPTarget }) {
        computeExpectsActuals(seenSpecs, expectSpecs, propagationCandidates, spec)

        for (extra in spec.extraSpecs) {
            computeExpectsActuals(seenSpecs, expectSpecs, propagationCandidates, spec, extra.name)
        }
    }

    // finally, we propagate any missing expect with default into its target actuals
    for ((expectSpec, extraName) in expectSpecs) {
        val selector: Selector = when (extraName) {
            null -> { it -> it }
            else -> { it -> it.extraSpecs.maybeCreate(extraName) }
        }
        val selectedExpectSpec = selector(expectSpec)

        for (expectField in selectedExpectSpec!!.buildConfigFields) {
            expectField.tags.add(BuildConfigField.IsExpect)

            for (actualSpec in propagationCandidates[expectSpec]!!) {
                val selectedActualSpec = selector(actualSpec)!!

                val actualField =
                    selectedActualSpec.buildConfigFields.findByName(expectField.name) ?:
                    selectedActualSpec.buildConfigFields.create(expectField.name) {
                        it.type.convention(expectField.type)
                        it.value.convention(expectField.value)
                    }

                actualField.tags.add(BuildConfigField.IsActual)

                // also makes sure that the actual class matches the expect declaration
                selectedActualSpec.defaultsFrom(selectedExpectSpec)
            }
        }
    }
}

private fun computeExpectsActuals(
    seenSpecs: MutableSet<BuildConfigClassSpec>,
    expectSpecs: MutableSet<Pair<BuildConfigSourceSetInternal, String?>>,
    propagationCandidates: MutableMap<BuildConfigSourceSetInternal, MutableSet<BuildConfigSourceSetInternal>>,
    targetSpec: BuildConfigSourceSetInternal,
    extraName: String? = null,
) {
    val selector: Selector = when (extraName) {
        null -> { it -> it }
        else -> { it -> it.extraSpecs.findByName(extraName) }
    }
    val spec = findEffectiveSpec(targetSpec, selector)
    val selectedSpec = selector(spec)!!

    if (!seenSpecs.add(selectedSpec)) return

    for (dependsOn in spec.allDependsOn.filter { !it.isSuperseded }) {
        propagationCandidates.getOrPut(dependsOn, ::linkedSetOf).add(spec)

        val selectedDependsOn = selector(dependsOn) ?: continue
        for (field in selectedSpec.buildConfigFields) {
            if (field.name !in selectedDependsOn.buildConfigFields.names) continue

            expectSpecs.add(dependsOn to extraName)
            field.tags.add(BuildConfigField.IsActual)

            // also makes sure that the actual class matches the expect declaration
            selectedSpec.defaultsFrom(selectedDependsOn)
        }
    }
}

/**
 * Returns staring from this [spec] and going down on its [BuildConfigSourceSetInternal.dependsOn] graph,
 * the "effective spec" is defined as the first one that:
 * - Its [BuildConfigSourceSetInternal.dependents] (if any) are empty
 * - Has any declared field or depends on directly on a root one
 *
 * If no such spec is found, returns [spec].
 * The returned can be the same as [spec] but never its root one (`main` or `test`)
 */
@VisibleForTesting
internal fun findEffectiveSpec(
    spec: BuildConfigSourceSetInternal,
    selector: Selector = { it },
): BuildConfigSourceSetInternal {
    if (selector(spec)?.hasFields == true) return spec

    var candidate = spec
    for (dep in spec.allDependsOn.filter { !it.isSuperseded && !it.isRoot }) {
        if (dep.allDependents.any { d -> selector(d)?.hasFields == true }) return candidate
        candidate = dep
    }
    return candidate
}

private val BuildConfigSourceSetInternal.isRoot: Boolean
    get() = dependsOn.isEmpty()

private val BuildConfigClassSpec.hasFields: Boolean
    get() = buildConfigFields.isNotEmpty()
