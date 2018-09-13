package wooga.gradle.node

import groovy.json.JsonSlurper
import org.ajoberstar.grgit.Grgit
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder
import org.jfrog.artifactory.client.model.RepoPath
import spock.lang.Shared

class NodeReleasePluginPublishSpec extends IntegrationSpec {

    @Shared
    File packageJsonFile

    @Shared
    def version = "1.0.0"

    def uniquePackagePostfix() {
        String key = "TRAVIS_JOB_NUMBER"
        def env = System.getenv()
        if (env.containsKey(key)) {
            return env.get(key)
        }
        return "UNKNOWN-" + UUID.randomUUID().toString()
    }

    @Shared
    def tag = "@wooga-test"

    @Shared
    def packageID = "integration-test-" + uniquePackagePostfix()


    @Shared
    def artifactoryUrl = "https://wooga.jfrog.io/wooga/"

    @Shared
    Artifactory artifactory

    def artifactoryRepoName = "atlas-node-integrationTest"
    //def repoUrl = "$artifactoryUrl/api/npm/$artifactoryRepoName"

    def setupSpec() {
        String artifactoryCredentials = System.getenv("artifactoryCredentials")
        assert artifactoryCredentials
        def credentials = artifactoryCredentials.split(':')
        artifactory = ArtifactoryClientBuilder.create()
                .setUrl(artifactoryUrl)
                .setUsername(credentials[0])
                .setPassword(credentials[1])
                .build()
    }

    Grgit git

    def setup() {

        new File(projectDir, '.gitignore') << """
        userHome/
        """.stripIndent()

        buildFile << """
            group = 'test'
            version = "$version"
            ${applyPlugin(NodeReleasePlugin)}
        """.stripIndent()

        packageJsonFile = createFile("package.json")
        packageJsonFile.text = packageJsonContent([
                "name"           : "$tag/$packageID",
                "version"        : "0.0.0",
                "scripts"        : ["clean": "shx echo \"clean\"", "test": "shx echo \"test\"", "build": "shx echo \"build\""],
                "devDependencies": ["shx": "^0.3.2"],
                "publishConfig"  : ["registry": "https://wooga.jfrog.io/wooga/api/npm/" + artifactoryRepoName + "/"]
        ])

        def npmrcFile = createFile(".npmrc")
        npmrcFile << "$tag:registry=https://wooga.jfrog.io/wooga/api/npm/" + artifactoryRepoName + "/"
        npmrcFile << "\n//wooga.jfrog.io/wooga/api/npm/" + artifactoryRepoName + "/:_authToken=" + System.getenv('artifactory_npm_token')

        git = Grgit.init(dir: projectDir)
        git.add(patterns: ['.gitignore'])
        git.commit(message: 'initial commit')
        git.tag.add(name: 'v0.0.1')
    }

    def cleanup() {
        def config = new JsonSlurper().parseText(packageJsonFile.text)
        String artifactName = "$packageID-$config.version"
        cleanupArtifactory(artifactoryRepoName, artifactName)
    }

    def cleanupArtifactory(String repoName, String artifactName) {
        List<RepoPath> searchItems = artifactory.searches()
                .repositories(repoName)
                .artifactsByName(artifactName)
                .doSearch()

        for (RepoPath searchItem : searchItems) {
            String repoKey = searchItem.getRepoKey()
            println(repoKey)
            String itemPath = searchItem.getItemPath()
            artifactory.repository(repoName).delete(itemPath)
        }
    }

    def hasPackageOnArtifactory(String repoName, String artifactName) {
        List<RepoPath> packages = artifactory.searches()
                .repositories(repoName)
                .artifactsByName(artifactName)
                .doSearch()

        assert packages.size() == 1
        true
    }

    def 'builds package and publish a snapshot when running task snapshot'() {
        given: "the future npm artifact"
        packageJsonFile.exists()

        and: "some content to publish"
        def content = createFile("content.json")
        content.text = "hello world"

        and:

        when: "run the publish task"
        def result = runTasksSuccessfully("snapshot")

        then:
        result.wasExecuted("npm_publish")
    }
}
