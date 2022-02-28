/*
 * Copyright 2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
        config.each { inputConfig[it.key] = it.value }
        def content = new JsonBuilder(inputConfig).toPrettyString()
        outputFile.text = content
    }

}
