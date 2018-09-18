package wooga.gradle.node

import org.ajoberstar.grgit.Grgit
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared

class NodeReleasePluginCredentialsSpec extends GithubIntegrationSpec {

    final static String NPM_AUTH_URL = 'https://wooga.artifactoryonline.com/wooga/api/npm/atlas-node/auth/wooga'

    @Shared
    def version = "1.0.0"

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    Grgit git

    def setup() {
        
        environmentVariables.set("GRGIT_USER", testUserName)
        environmentVariables.set("GRGIT_PASS", testUserToken)
        environmentVariables.set("NODE_RELEASE_NPM_LOGIN", System.getenv('ATLAS_NPM_CREDENTIALS'))
        environmentVariables.set("NODE_RELEASE_NPM_AUTH_URL", NPM_AUTH_URL)

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


    def "Plugin creates npmCredentialsTask"() {

        given: "a build file with plugin"
        
        when: "run the credentials task"
        def result = runTasks("${NodeReleasePlugin.CREATE_CREDENTIALS_TASK}")

        then:
        println(result.standardOutput)
        result.success
    }

}
