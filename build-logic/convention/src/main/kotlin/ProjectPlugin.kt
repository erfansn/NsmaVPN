import org.gradle.api.Plugin
import org.gradle.api.Project

interface ProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.with()
    }

    fun Project.with()
}