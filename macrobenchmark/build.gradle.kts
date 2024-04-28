/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.kotlin.android)
}

// Best articles about this:
// - [https://stefma.medium.com/sourcecompatibility-targetcompatibility-and-jvm-toolchains-in-gradle-explained-d2c17c8cff7c]
// - [https://developer.android.com/build/jdks#source-compat]
kotlin {
    jvmToolchain(jvm.versions.toolchain.get().toInt())
}

android {
    namespace = "ir.erfansn.nsmavpn.macrobenchmark"
    compileSdk = sdk.versions.compile.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = sdk.versions.target.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    testOptions {
        managedDevices {
            localDevices {
                register("pixel8Api34") {
                    device = "Pixel 8"
                    apiLevel = 34
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

baselineProfile {
    managedDevices += "pixel8Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.rules)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants { variant ->
        val artifactsLoader = variant.artifacts.getBuiltArtifactsLoader()
        variant.instrumentationRunnerArguments.put(
            "targetAppId",
            variant.testedApks.map { artifactsLoader.load(it)?.applicationId!! }
        )
    }
}
