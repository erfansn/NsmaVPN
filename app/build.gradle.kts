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
@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.sentry.android)
    alias(libs.plugins.sentry.kotlin.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.ksp)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.kotlin.plugin.parcelize)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.nsmavpn.detekt)
    alias(libs.plugins.gms.oss.licenses)
}

// Best articles about this:
// - [https://stefma.medium.com/sourcecompatibility-targetcompatibility-and-jvm-toolchains-in-gradle-explained-d2c17c8cff7c]
// - [https://developer.android.com/build/jdks#source-compat]
kotlin {
    jvmToolchain(jvm.versions.toolchain.get().toInt())
}

android {
    namespace = "ir.erfansn.nsmavpn"
    compileSdk = sdk.versions.compile.get().toInt()

    defaultConfig {
        applicationId = "ir.erfansn.nsmavpn"
        minSdk = 23
        targetSdk = sdk.versions.target.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "ir.erfansn.nsmavpn.HiltTestRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("RELEASE_STORE_FILE_PATH"))
            storePassword = System.getenv("RELEASE_STORE_PASS")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASS")
        }
    }
    buildTypes {
        debug {
            // This value isn't secret: https://github.com/cli/oauth/issues/1
            resValue("string", "web_client_id", "870845865908-4qde8iut5e3j76tmtn54rcthqjh4lcg8.apps.googleusercontent.com")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            // How to create one: https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid#get_your_google_api_client_id
            resValue("string", "web_client_id", "870845865908-gc4r0i630d55tujd4555f6p8hjbg2fjc.apps.googleusercontent.com")
        }
    }

    androidResources {
        generateLocaleConfig = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompilerVersion.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES,INDEX.LIST}"
            excludes += "/mozilla/public-suffix-list.txt"
            excludes += "DebugProbesKt.bin"
        }
    }
    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

baselineProfile {
    dexLayoutOptimization = true
    automaticGenerationDuringBuild = true
    saveInSrc = false
}

androidComponents {
    registerSourceType("proto")

    // Temporarily solution about problem with generated classes of Protobuf [https://github.com/google/ksp/issues/1590]
    onVariants { variant ->
        afterEvaluate {
            val capName = variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            tasks.named<KotlinCompile>("ksp${capName}Kotlin") {
                setSource(tasks.getByName("generate${capName}Proto").outputs)
            }
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.compiler.get().toString()
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.macrobenchmark)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3.asProvider())
    implementation(libs.androidx.compose.material3.windowsize)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.runtime.tracing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.accompanist.drawablepainter)

    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.play.services.oss.licenses)
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.gmail)

    implementation(libs.skrapeit)
    implementation(libs.coil.compose)
    implementation(libs.cache4k)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.coil.test)
    testImplementation(libs.robolectric)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
