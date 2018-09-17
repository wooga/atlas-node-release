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

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

class NpmCredentialsTask extends DefaultTask {

    @Input
    String credentials

    @Input
    String authenticationUrl

    @OutputFile
    File npmrcFile

    @TaskAction
    protected void exec() {
        def output = new ByteArrayOutputStream()
        project.exec(new Action<ExecSpec>() {

            @Override
            void execute(ExecSpec execSpec) {
                execSpec.commandLine 'curl', '-u', credentials, authenticationUrl
                execSpec.standardOutput = output
            }
        })

        MaybeSetCredentials(output.toString())
    }

    def MaybeSetCredentials(String content) {

        def contentLines = content.readLines()

        if (npmrcFile.exists()) {
            def fileContentLines = npmrcFile.text.readLines()
            if (fileContentLines.contains(contentLines.first())) {
                println("registry already set")
                return
            }
        }

        npmrcFile << content
    }
}
