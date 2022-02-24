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

import groovy.json.JsonSlurper
import org.ajoberstar.grgit.Grgit
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder
import org.jfrog.artifactory.client.model.RepoPath
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared
import spock.lang.Unroll

class NodeReleasePluginPublishSpec extends GithubIntegrationSpec {

    @Shared
    File packageJsonFile

    @Shared
    def version = "1.0.0"

    String uniquePostfix() {
        String key = "TRAVIS_JOB_NUMBER"
        def env = System.getenv()
        if (env.containsKey(key)) {
            return env.get(key)
        }
        return "UNKNOWN"
    }

    @Shared
    def scope = "@wooga-test"

    @Shared
    def packageID = "integration-test-" + uniquePostfix()

    @Shared
    def artifactoryUrl = "https://wooga.jfrog.io/wooga/"

    @Shared
    Artifactory artifactory

    def artifactoryRepoName = "atlas-node-integrationTest"

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

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

        environmentVariables.set("GRGIT_USER", testUserName)
        environmentVariables.set("GRGIT_PASS", testUserToken)
        environmentVariables.set("GITHUB_LOGIN", testUserName)
        environmentVariables.set("GITHUB_PASSWORD", testUserToken)

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
            
            github.repositoryName = '$testRepositoryName'
        """.stripIndent()

        packageJsonFile = createFile("package.json")
        packageJsonFile.text = packageJsonContent([
                "name"           : "$scope/$packageID",
                "version"        : "0.0.0",
                "scripts"        : ["clean": "shx echo \"clean\"", "test": "shx echo \"test\"", "build": "shx echo \"build\""],
                "devDependencies": ["shx": "^0.3.2"],
                "publishConfig"  : ["registry": "https://wooga.jfrog.io/wooga/api/npm/" + artifactoryRepoName + "/"]
        ])

        def npmrcFile = createFile(".npmrc")
        npmrcFile << "$scope:registry=https://wooga.jfrog.io/wooga/api/npm/" + artifactoryRepoName + "/"
        npmrcFile << "\n//wooga.jfrog.io/wooga/api/npm/" + artifactoryRepoName + "/:_authToken=" + System.getenv('artifactory_npm_token')

        git = Grgit.init(dir: projectDir)
        git.add(patterns: ['.gitignore'])
        git.commit(message: 'initial commit')
        git.tag.add(name: 'v0.0.1')
        git.remote.add(name: "origin", url: "https://github.com/${testRepositoryName}.git")

        cleanupArtifactory(artifactoryRepoName, packageNameForPackageJson())
    }

    def cleanup() {
        cleanupArtifactory(artifactoryRepoName, packageNameForPackageJson())
    }

    def cleanupArtifactory(String repoName, String artifactName) {
        List<RepoPath> searchItems = artifactory.searches()
                .repositories(repoName)
                .artifactsByName(artifactName.replaceAll(/\+.*$/, ''))
                .doSearch()

        for (RepoPath searchItem : searchItems) {
            String repoKey = searchItem.getRepoKey()
            println(repoKey)
            String itemPath = searchItem.getItemPath()
            try {
                artifactory.repository(repoName).delete(itemPath)
            } catch (e) {
                println("error while deleting ${itemPath}")
            }
            finally {
                println("delete done")
            }
        }
    }

    def hasPackageOnArtifactory(String repoName, String artifactName) {
        List<RepoPath> packages = artifactory.searches()
                .repositories(repoName)
                .artifactsByName(artifactName)
                .doSearch()

        packages.size() >= 2
    }

    def packageNameForPackageJson() {
        def config = new JsonSlurper().parseText(packageJsonFile.text)
        "$packageID-$config.version"
    }

    @Unroll
    def 'builds and publish a package running task publish with #stage with version #version'() {
        given: "the future npm artifact"
        packageJsonFile.exists()

        and: "some content to publish"
        def content = createFile("content.json")
        content.text = "hello world"

        and:
        git.add(patterns: ['.'])
        git.commit(message: 'add files')

        when: "run the publish task"
        environmentVariables.set("VERSION_BUILDER_STAGE", stage)
        def result = runTasks("publish")
        def config = new JsonSlurper().parseText(packageJsonFile.text)

        then:
        print(result.standardOutput)
        result.success
        result.wasExecuted("npm_publish")
        hasPackageOnArtifactory(artifactoryRepoName, packageNameForPackageJson())
        config.version == version

        where:
        stage      | version
        "snapshot" | "0.1.0-master.1"
        "rc"       | "0.1.0-rc.1"
        "final"    | "0.1.0"
    }

    @Unroll
    def 'build and creates #stage version #version with github release #expectRelease as prerelease #prerelease'() {
        given: "the future npm artifact"
        packageJsonFile.exists()

        and: "some content to publish"
        def content = createFile("content.json")
        content.text = "hello world"

        and:
        git.add(patterns: ['.'])
        git.commit(message: 'add files')

        and: "ensure artifact is not located on artifactory"
        environmentVariables.set("VERSION_BUILDER_STAGE", stage)
        cleanupArtifactory(artifactoryRepoName, "${packageID}-${version}")
        assert !hasPackageOnArtifactory(artifactoryRepoName, "${packageID}-${version}")

        when: "run the publish task"
        def result = runTasks("publish")

        then:
        result.success
        result.wasExecuted("node_publish")
        result.wasExecuted("npm_publish")
        result.wasSkipped("githubPublish") != expectRelease

        sleep(2000)
        hasReleaseByName(version) == expectRelease
        hasPackageOnArtifactory(artifactoryRepoName, "${packageID}-${version}")

        if (expectRelease) {
            getReleaseByName(version).prerelease == prerelease
        }

        where:
        stage      | version          | expectRelease | prerelease
        "snapshot" | "0.1.0-master.1" | false         | true
        "rc"       | "0.1.0-rc.1"     | true          | true
        "final"    | "0.1.0"          | true          | false
    }
}
