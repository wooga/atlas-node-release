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

package wooga.gradle.node

import org.ajoberstar.grgit.Grgit
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared
import spock.lang.Unroll

class NodeReleasePluginEngineSpec extends GithubIntegrationSpec {

    @Shared
    def version = "1.0.0"

    @Shared
    def npmUser = System.getenv("NODE_RELEASE_NPM_USER_TEST")

    @Shared
    def npmPass = System.getenv("NODE_RELEASE_NPM_PASS_TEST")

    @Shared
    def npmAuthUrl = System.getenv("NODE_RELEASE_NPM_AUTH_URL_TEST")

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    Grgit git

    @Shared
    File packageJsonFile

    def setup() {

        environmentVariables.set("GRGIT_USER", testUserName)
        environmentVariables.set("GRGIT_PASS", testUserToken)
        environmentVariables.set('NODE_RELEASE_NPM_USER', npmUser)
        environmentVariables.set('NODE_RELEASE_NPM_PASS', npmPass)
        environmentVariables.set('NODE_RELEASE_NPM_AUTH_URL', npmAuthUrl)

        new File(projectDir, '.gitignore') << """
        userHome/
        .gradle
        .gradle-test-kit
        .npmrc
        """.stripIndent()

        buildFile << """
            group = 'test'
            version = "$version"
            ${applyPlugin(NodeReleasePlugin)}    
            node.version = '10.5.0'
            node.download = true
        """.stripIndent()

        packageJsonFile = createFile('package.json')
        packageJsonFile.text = packageJsonContent([
                "scripts"        : ["clean": "shx echo \"clean\"", "test": "shx echo \"test\"", "build": "shx echo \"build\""],
                "devDependencies": ["shx": "^0.3.2"],
        ])

        git = Grgit.init(dir: projectDir)
        git.add(patterns: ['.gitignore'])
        git.commit(message: 'initial commit')
    }

    @Unroll
    def "detect engine #engine"() {

        given:
        "a lock file of type ${lockfile}"
        if (lockfile) {
            createFile(lockfile)
        }

        //and: "dependencies are installed"
        //runTasksSuccessfully('npmSetup')

        when:
        "run task ${task}"
        def result = runTasks(task)

        println(result)

        then:
        "successfully executed task ${expectedTask}"
        result.wasExecuted(expectedTask)

        where:
        engine | task             | expectedTask     | lockfile
        "npm"  | "node_run_clean" | "npm_run_clean"  | null
        "npm"  | "node_run_clean" | "npm_run_clean"  | "package-lock.json"
        "yarn" | "node_run_clean" | "yarn_run_clean" | "yarn.lock"
    }
}
