plugins {
    `kotlin-dsl`
}

group = "ir.erfansn.nsmavpn.buildlogic"

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("detekt") {
            id = "nsmavpn.detekt"
            implementationClass = "detekt.DetektConventionsPlugin"
        }
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.undertouch.download.gradle.plugin)
}
