package wooga.gradle.node.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

class ModifyPackageJsonTask extends DefaultTask {

    @InputFile
    File inputFile

    @Input
    Map<String, Object> config

    @OutputFiles
    FileCollection getOutputFiles() {
        project.files(outputFile)
    }

    public File outputFile

    @TaskAction
    protected void modify() {
        def inputConfig = new JsonSlurper().parseText(inputFile.text)
        config.each {
            inputConfig[it.key] = it.value
            println("Set " + it.key + ":" + it.value)
        }
        def content = new JsonBuilder(inputConfig).toPrettyString()
        outputFile.text = content
    }

}
