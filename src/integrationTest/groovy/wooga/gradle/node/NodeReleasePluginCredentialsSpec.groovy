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

package wooga.gradle.node

import org.ajoberstar.grgit.Grgit
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared

class NodeReleasePluginCredentialsSpec extends GithubIntegrationSpec {

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


        git = Grgit.init(dir: projectDir)
        git.add(patterns: ['.gitignore'])
        git.commit(message: 'initial commit')
        git.tag.add(name: 'v0.0.1')
        git.remote.add(name: "origin", url: "https://github.com/${testRepositoryName}.git")
    }

    def "run task of type NpmCredentialsTask with default properties"() {

        given: "a valid defined task"
        buildFile << """                   
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) 
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")
    }

    def "run task of type NpmCredentialsTask with task properties"() {

        given: "a valid defined task"
        buildFile << """                   
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmUser = '${npmUser}'
            npmPass = '${npmPass}'
            npmAuthUrl = '${npmAuthUrl}'   
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")
    }

    def "run task :ensureNpmrc with project properties"() {

        when: "no env vars set for npm login"
        environmentVariables.clear('NODE_RELEASE_NPM_USER', 'NODE_RELEASE_NPM_PASS')

        and:
        assert (!System.getenv("NODE_RELEASE_NPM_USER"))
        assert (!System.getenv("NODE_RELEASE_NPM_PASS"))

        and: 'properties defined in properties file'
        new File(projectDir, 'gradle.properties') << """
        nodeRelease.npmUser=${npmUser}
        nodeRelease.npmPass=${npmPass}
        nodeRelease.npmAuthUrl=${npmAuthUrl}
        """.stripIndent().trim()

        then: "runs"
        runTasksSuccessfully("ensureNpmrc")
    }

    def "run task :ensureNpmrc with extension.properties"() {

        when: "no env vars set for npm login"
        environmentVariables.clear('NODE_RELEASE_NPM_USER', 'NODE_RELEASE_NPM_PASS')

        and:
        assert (!System.getenv("NODE_RELEASE_NPM_USER"))
        assert (!System.getenv("NODE_RELEASE_NPM_PASS"))
        assert runTasksWithFailure("ensureNpmrc")

        and: 'properties defined in properties file'
        buildFile << """nodeRelease {
            npmUser='${npmUser}'
            npmPass='${npmPass}'
            npmAuthUrl='${npmAuthUrl}'                                          
        }
        """.stripIndent()

        then: "runs"
        runTasksSuccessfully("ensureNpmrc")
    }

    def "task of type NpmCredentialsTask writes .npmrc file"() {

        given: "a valid defined task"
        buildFile << """    
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmUser = '${npmUser}'
            npmPass = '${npmPass}'
            npmAuthUrl = '${npmAuthUrl}'
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")

        and: ".npmrc file"
        file('.npmrc').exists()

        and:
        def registry = file('.npmrc').readLines().first()
        registry == '@wooga:registry=https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/'
    }

    def "task of type NpmCredentialsTask writes .npmrc file only if registry doesn't exist already"() {

        def npmrcFile = file('.npmrc')
        npmrcFile << '@wooga:registry=https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/'

        given: "a valid defined task"
        buildFile << """          
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmUser = '${npmUser}'
            npmPass = '${npmPass}'
            npmAuthUrl = '${npmAuthUrl}'
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")

        and: ".npmrc file"
        file('.npmrc').exists()

        and:
        file('.npmrc').readLines().size() == 1
    }

}
