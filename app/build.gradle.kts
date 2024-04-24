@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

import com.android.build.gradle.internal.tasks.factory.dependsOn
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
    id("nsmavpn.detekt")
}

// Best articles about this:
// - [https://stefma.medium.com/sourcecompatibility-targetcompatibility-and-jvm-toolchains-in-gradle-explained-d2c17c8cff7c]
// - [https://developer.android.com/build/jdks#source-compat]
kotlin {
    jvmToolchain(17)
}

android {
    namespace = "ir.erfansn.nsmavpn"
    compileSdk = sdk.versions.compile.get().toInt()

    defaultConfig {
        applicationId = "ir.erfansn.nsmavpn"
        minSdk = 23
        targetSdk = sdk.versions.target.get().toInt()
        versionCode = 1
        // TODO: Change to Calender versioning
        versionName = "1.0"

        testInstrumentationRunner = "ir.erfansn.nsmavpn.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
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
            excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            excludes += "/mozilla/public-suffix-list.txt"
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

tasks.preBuild.dependsOn(tasks.detektWrapper)

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

    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.accompanist.drawablepainter)

    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)

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
