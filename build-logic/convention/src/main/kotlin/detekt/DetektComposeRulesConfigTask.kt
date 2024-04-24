package detekt

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

abstract class DetektComposeRulesConfigTask : DefaultTask() {

    @get:InputFile
    val detektIdeaConfigFile = File("${project.rootDir}/.idea", "detekt.xml")

    @get:Input
    abstract val ruleVersion: Property<String>

    @TaskAction
    fun taskAction() {
        if (!detektIdeaConfigFile.exists()) {
            println("Warning: To use Detekt in Android Studio while coding, download and enable Detekt plugin.")
            return
        }

        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = FileInputStream(detektIdeaConfigFile).use {
            db.parse(it)
        }

        val component = doc.getElementsByTagName("component").item(0)
        component.checkEnableDetekt()
        doc.configPluginJars(component = component)
        doc.configBuildUponDefault(component = component)
        doc.configConfigurationsFiles(component = component)
        doc.configTreatsAsError(component = component)

        FileOutputStream(detektIdeaConfigFile).use {
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no")

            val source = DOMSource(doc)
            val result = StreamResult(it)

            transformer.transform(source, result)
        }
    }

    private fun Node.checkEnableDetekt() {
        val optionName = "enableDetekt"
        val targetOption: Node? = childNodes.findOptionOrNull(optionName)

        targetOption?.takeUnless {
            it.attributes.getNamedItem("value").nodeValue == "true"
        }?.let {
            println("Please enable the Detekt plugin to use it")
        }
    }

    private fun Document.configPluginJars(component: Node) {
        val optionName = "pluginJars"
        val targetOption: Node? = component.childNodes.findOptionOrNull(optionName)

        val detektRuleJarFilePath =
            "\$PROJECT_DIR\$/detekt/detekt-compose-${ruleVersion.get()}-all.jar"
        targetOption?.let {
            val list = it.childNodes
            var hasDefinedBefore = false
            for (i in 0 until list.length) {
                val option = list.item(i)
                if (option.nodeType == Node.ELEMENT_NODE && option.hasAttributes() &&
                    option.attributes.getNamedItem("value")?.nodeValue == detektRuleJarFilePath
                ) {
                    hasDefinedBefore = true
                }
            }
            if (hasDefinedBefore) return
            component.removeChild(it)
        }

        val rootOption = createElement("option")
        rootOption.setAttribute("name", optionName)
        val optionList = createElement("list").apply {
            val option = createElement("option")
            option.setAttribute("value", detektRuleJarFilePath)
            appendChild(option)
        }
        rootOption.appendChild(optionList)
        component.appendChild(rootOption)
    }

    private fun Document.configBuildUponDefault(component: Node) {
        val optionName = "buildUponDefaultConfig"
        val targetOption: Node? = component.childNodes.findOptionOrNull(optionName)

        targetOption?.let {
            if (it.attributes.getNamedItem("value").nodeValue == "false") return
            component.removeChild(it)
        }

        val rootOption = createElement("option")
        rootOption.setAttribute("name", optionName)
        rootOption.setAttribute("value", "false")
        component.appendChild(rootOption)
    }

    private fun Document.configConfigurationsFiles(component: Node) {
        val optionName = "configurationFiles"
        val targetOption: Node? = component.childNodes.findOptionOrNull(optionName)

        val detektRuleConfigFilePath =
            "\$PROJECT_DIR\$/detekt/config.yml"
        targetOption?.let {
            val list = it.childNodes
            var hasDefinedBefore = false
            for (i in 0 until list.length) {
                val option = list.item(i)
                if (option.nodeType == Node.ELEMENT_NODE && option.hasAttributes() &&
                    option.attributes.getNamedItem("value")?.nodeValue == detektRuleConfigFilePath
                ) {
                    hasDefinedBefore = true
                }
            }
            if (hasDefinedBefore) return
            component.removeChild(it)
        }

        val rootOption = createElement("option")
        rootOption.setAttribute("name", optionName)
        val optionList = createElement("list").apply {
            val option = createElement("option")
            option.setAttribute("value", detektRuleConfigFilePath)
            appendChild(option)
        }
        rootOption.appendChild(optionList)
        component.appendChild(rootOption)
    }

    private fun Document.configTreatsAsError(component: Node) {
        val optionName = "treatAsErrors"
        val targetOption: Node? = component.childNodes.findOptionOrNull(optionName)

        targetOption?.let {
            if (it.attributes.getNamedItem("value").nodeValue == "true") return
            component.removeChild(it)
        }

        val rootOption = createElement("option")
        rootOption.setAttribute("name", optionName)
        rootOption.setAttribute("value", "true")
        component.appendChild(rootOption)
    }

    private fun NodeList.findOptionOrNull(name: String): Node? {
        var targetOption: Node? = null
        for (i in 0 until length) {
            val option = item(i)
            if (option?.nodeType == Node.ELEMENT_NODE &&
                option.attributes?.getNamedItem("name")?.nodeValue == name
            ) {
                targetOption = option
                break
            }
        }
        return targetOption
    }
}
