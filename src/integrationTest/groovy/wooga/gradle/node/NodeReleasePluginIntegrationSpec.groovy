package wooga.gradle.node

import spock.lang.Unroll

class NodeReleasePluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << """
            ${applyPlugin(NodeReleasePlugin)}    
        """.stripIndent()
    }
    
    @Unroll
    def "converts #propertyName property value of #candidateName to #rcName in command line"() {
        when:
        def result = runTasksSuccessfully("properties", "-P", "release.stage=${candidateName}")

        then:
        result.standardOutput.contains("${propertyName}: ${rcName}")

        where:
        propertyName = "release.stage"
        candidateName = "candidate"
        rcName = "rc"
    }

    @Unroll
    def "converts #propertyName property value of #candidateName to #rcName in properties file"() {
        given: "a gradle.properties file"
        def gradleProperties = createFile("gradle.properties", projectDir)
        gradleProperties << "${propertyName}=${candidateName}"

        when:
        def result = runTasksSuccessfully("properties")

        then:
        result.standardOutput.contains("${propertyName}: ${rcName}")

        where:
        propertyName = "release.stage"
        candidateName = "candidate"
        rcName = "rc"
    }

    @Unroll
    def "renames task name from #candidateTaskName to #rcTaskName"() {
        when:
        // We expect the task to fail
        def result = runTasks(candidateTaskName)

        then:
        !result.standardOutput.contains("task ':${candidateTaskName}'")
        result.standardOutput.contains("task ':${rcTaskName}'")

        where:
        candidateTaskName = "candidate"
        rcTaskName = "rc"
    }
}
