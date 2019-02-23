# gradle-apklib-plugin
An Android-Gradle plugin for generate DEX classes and a APK from a Library module.

## Why?
Well, if you reach this page, you are very likely looking for this.
In my case, I was on the need to perform hot-code Android updates pushed from my server without forcing the users to update the app from the *PlayStore*.

## How it works
This library relies on the Android's build stack, and given a Library module:
- Forces the assemble of the AAR package
- Extracts the `classes.jar` and perform dexing on it using the `dx` tool.
- Repackages the generated `classes.dex` along with all original Java resources from `classes.jar` into an `mymodule.lib.apk` file

Note: This tool will allow you to push updates of Java (Android) code and Java resources. Android's resources (strings, drawables, etc) or any other Android specific component are not supported.

## Usage
On your `build.gradle` add:
```groovy
plugins {
    id 'com.github.gmazzo.apklib' version '0.3'
}

apply plugin: 'com.android.library'
```
Check [https://plugins.gradle.org/plugin/com.github.gmazzo.apklib](https://plugins.gradle.org/plugin/com.github.gmazzo.apklib) for other instructions

After applying the plugin, a task `bundleLibApk` per variant will be added to your build.
On a default configuration will be `bundleLibApkDebug` and `bundleLibApkRelease`.

## What's next?
Check the provided `example-app` and `example-lib` for a full working example of this library with hot-code loading.
You may easily replace the content from the asset with the response from a web endpoint.

This repository also serves as an example of:
- Building and publishing a **Gradle's plugin** (also linked to another plugin like Android's one in this case)
- Full **Kotlin** + **Dagger 2** working code
- Kotlin Android Layout Extensions working code (to **avoid findViewById()**)
- **Dagger's Android module** working code
- Android's **buildConfigField** usage
- Gradle and Android **sourceSet specific dependencies** working code
- Gradle's tasks **input** and **output** definition for proper **UP-TO-DATE** support on custom tasks
- Gradle's tasks dependency (including cross-project linking) with **dependsOn** and **evaluationDependsOn**
- Android's **assets generation** with Gradle's tasks
- Gradle's **includeBuild** and **dependencySubstitution** working code, for in-build live plugin build and testing
- Advanced ProGuard obfuscation like **-keep,allowobfuscation**, **-flattenpackagehierarchy** and **-adaptresourcefilecontents**
- Exploration of Java backend concepts of **ClassLoader** and **ServiceLoader**
