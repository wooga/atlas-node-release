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

import wooga.gradle.node.IntegrationSpec

class NpmCredentialsTaskSpec extends IntegrationSpec {

    final static String NPM_AUTH_URL = 'https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/auth/wooga'

    def "run FetchNpmCredentialsTask successfully"() {

        given: "a valid defined task"
        buildFile << """   
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmLogin = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            npmAuthUrl = '${NPM_AUTH_URL}'   
            npmrcFile = file('.npmrc')
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")
    }

    def "run FetchNpmCredentialsTask writes .npmrc file"() {

        given: "a valid defined task"
        buildFile << """   
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmLogin = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            npmAuthUrl = '${NPM_AUTH_URL}'
            npmrcFile = file('.npmrc')
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")

        and: ".npmrc file"
        file('.npmrc').exists()

        and:
        def registry = file('.npmrc').text.readLines().first()
        registry == '@wooga:registry=https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/'
    }

    def "run FetchNpmCredentialsTask writes .npmrc file only if registry doesn't exist already"() {

        def npmrcFile = file('.npmrc')
        npmrcFile << '@wooga:registry=https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/'

        given: "a valid defined task"
        buildFile << """   
        group = 'test'
        
        println("create task")
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmLogin = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            npmAuthUrl = '${NPM_AUTH_URL}'
            npmrcFile = file('.npmrc')
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")

        and: ".npmrc file"
        file('.npmrc').exists()

        and:
        file('.npmrc').text.readLines().size() == 1
    }

    def "run FetchNpmCredentialsTask successfully with default value"() {

        given: "a valid defined task"
        buildFile << """   
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            npmLogin = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            npmAuthUrl = '${NPM_AUTH_URL}'        
            npmrcFile = file('.npmrc')
        }
        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")
    }


}
