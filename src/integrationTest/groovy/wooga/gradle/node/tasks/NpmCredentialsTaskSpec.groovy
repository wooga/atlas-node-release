package wooga.gradle.node.tasks

import wooga.gradle.node.IntegrationSpec

class NpmCredentialsTaskSpec extends IntegrationSpec {

    def "run FetchNpmCredentialsTask successfully"() {

        given: "a valid defined task"
        buildFile << """   
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            credentials = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            authenticationUrl = 'https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/auth/wooga'
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
            credentials = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            authenticationUrl = 'https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/auth/wooga'
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
        
        task test (type:wooga.gradle.node.tasks.NpmCredentialsTask) {
            credentials = '${System.env['ATLAS_NPM_CREDENTIALS']}'
            authenticationUrl = 'https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/auth/wooga'
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

}
