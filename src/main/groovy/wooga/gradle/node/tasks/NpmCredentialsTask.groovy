/*
 * Copyright 2018 Wooga GmbH
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

import groovy.json.JsonSlurper
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

class NpmCredentialsTask extends DefaultTask {

    @Input
    final Property<String> npmUser = project.objects.property(String)

    @Input
    final Property<String> npmPass = project.objects.property(String)

    @Input
    final Property<String> npmAuthUrl = project.objects.property(String)

    @OutputFile
    final RegularFileProperty npmrcFile = newOutputFile()

    @TaskAction
    protected void exec() {
        def output = new ByteArrayOutputStream()
        project.exec(new Action<ExecSpec>() {

            @Override
            void execute(ExecSpec execSpec) {
                execSpec.commandLine 'curl', '-u', npmUser.get() + ":" + npmPass.get(), npmAuthUrl.get()
                execSpec.standardOutput = output
            }
        })
        validateResult(output.toString())
        MaybeSetCredentials(output.toString())
    }

    static def validateResult(String content) {
        try {
            def resultJson = new JsonSlurper().parseText(content)
            throw new Error("Failed npm login: ${resultJson['errors'][0]['message']}")

        } catch (Exception e) {
            //valid result is no json
        }
    }

    def MaybeSetCredentials(String content) {

        def outputFile = npmrcFile.asFile.get()
        String npmRegistryStart = content.readLines().first()

        if (outputFile.exists()) {
            def fileContentLines = outputFile.readLines()
            if (fileContentLines.contains(npmRegistryStart)) {
                println("registry already set")
                return
            }
        }

        outputFile << content
    }
}
