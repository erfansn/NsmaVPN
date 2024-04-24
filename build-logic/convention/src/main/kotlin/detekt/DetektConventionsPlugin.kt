package detekt

import ProjectPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import de.undercouch.gradle.tasks.download.Download
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import unsafeLibs

abstract class DetektConventionsPlugin : ProjectPlugin {

    override fun Project.with() {
        with(pluginManager) {
            apply("io.gitlab.arturbosch.detekt")
            apply("de.undercouch.download")
        }

        val detektComposeRulesVersion = unsafeLibs.findVersion("detektComposeRulesVersion").get().toString()
        tasks.register<DetektComposeRulesConfigTask>("writeDetektComposeRulesIdeaConfig") {
            group = "Detekt Compose Rules"
            description = "Config Detekt Compose rules to Idea if needed"

            ruleVersion.set(detektComposeRulesVersion)
            dependsOn("downloadDetektComposeRulesJarFile")
        }
        tasks.register<Download>("downloadDetektComposeRulesJarFile") {
            group = "Detekt Compose Rules"
            description = "Download the Detekt Compose rules jar file"

            src("https://github.com/mrmans0n/compose-rules/releases/download/v$detektComposeRulesVersion/detekt-compose-$detektComposeRulesVersion-all.jar")
            dest("$rootDir/detekt/detekt-compose-$detektComposeRulesVersion-all.jar")
            overwrite(false)
        }

        tasks.register("detektWrapper") {
            dependsOn(tasks.named("detekt"))
        }
        tasks.named("detekt").dependsOn(tasks.named("writeDetektComposeRulesIdeaConfig"))

        extensions.getByType<DetektExtension>().apply {
            config.setFrom("$rootDir/detekt/config.yml")
            buildUponDefaultConfig
        }

        dependencies {
            add("detektPlugins", unsafeLibs.findLibrary("detekt-compose-rules").get())
        }
    }
}