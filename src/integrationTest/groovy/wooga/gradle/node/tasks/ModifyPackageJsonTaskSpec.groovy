package wooga.gradle.node.tasks

import groovy.json.JsonSlurper
import spock.lang.Unroll
import wooga.gradle.node.IntegrationSpec

class ModifyPackageJsonTaskSpec extends IntegrationSpec {

    def "run modifyPackageJsonTask successfully"() {

        given: "a valid defined task"

        def inputFile = createFile('package.json', projectDir)

        inputFile << """{"version":"please.replace.me"}""".stripIndent()

        def outputFile = new File(projectDir, 'package_new.json')

        buildFile << """   
        
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.ModifyPackageJsonTask) {
            inputFile = file('${escapedPath(inputFile.path)}')
            config = [version:'1.1.1']
            outputFile = file('${escapedPath(outputFile.path)}')
        }

        """.stripIndent()

        expect: "runs"
        runTasksSuccessfully("test")
    }

    @Unroll
    def "task:ModifyPackageJsonTask modifies field \"#inputKey\" to be \"#inputValue\""() {

        given: "a build.gradle with a test task of type ModifyPackageJsonTask"

        def inputFile = createFile('package.json', projectDir)

        inputFile.text = packageJsonContent([version: "please.replace.me"])

        def outputFile = new File(projectDir, 'package_new.json')

        buildFile << """   
        
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.ModifyPackageJsonTask) {
            inputFile = file('${escapedPath(inputFile.path)}')
            config = ["${inputKey}":"${inputValue}"]
            outputFile = file('${escapedPath(outputFile.path)}')
        }

        """.stripIndent()

        when: "task test is executed"
        runTasksSuccessfully("test")

        then:
        outputFile.exists()

        and:
        def outputObject = new JsonSlurper().parseText(outputFile.text)
        outputObject[inputKey] == inputValue

        where:
        inputKey  | inputValue
        "version" | "1.0.0"
        "version" | "1.0.0-SNAPSHOT"
        "name"    | "my-package-name"
    }

    @Unroll
    def "task:ModifyPackageJsonTask modifies sets fields \"#inputFields\""() {

        given: "a build.gradle with a test task of type ModifyPackageJsonTask"

        def inputFile = createFile('package.json', projectDir)

        inputFile.text = packageJsonContent(originFields)

        def outputFile = new File(projectDir, 'package_new.json')

        buildFile << """   
        
        group = 'test'
        
        task test (type:wooga.gradle.node.tasks.ModifyPackageJsonTask) {
            inputFile = file('${escapedPath(inputFile.path)}')
            config = ${inputFields}
            outputFile = file('${escapedPath(outputFile.path)}')
        }

        """.stripIndent()

        when: "task test is successfully executed"
        runTasksSuccessfully("test")

        then:
        outputFile.exists()

        and:
        def outputObject = new JsonSlurper().parseText(outputFile.text)
        outputObject == expectedFields

        where:
        originFields           | inputFields          | expectedFields
        [:]                    | [version: "'0.0.1'"] | [version: "0.0.1"]
        [version: "'0.0.1'"]   | [version: "'1.0.1'"] | [version: "1.0.1"]
        [name: "package-name"] | [version: "'0.0.1'"] | [name: "package-name", version: "0.0.1"]
    }
}
