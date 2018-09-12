package wooga.gradle.node

import nebula.test.ProjectSpec
import org.ajoberstar.grgit.Grgit
import spock.lang.Unroll

class NodeReleasePluginSpec extends ProjectSpec {

    Grgit git

    def setup() {
        new File(projectDir, '.gitignore') << """
        userHome/
        """.stripIndent()

        new File(projectDir, 'package.json') << """{"version":"please.replace.me"}""".stripIndent()

        git = Grgit.init(dir: projectDir)
        git.add(patterns: ['.gitignore'])
        git.commit(message: 'initial commit')
        git.tag.add(name: 'v0.0.1')
    }

    @Unroll('verify task creation of task #taskName')
    def "creates helper tasks"() {
        given:
        assert !project.tasks.hasProperty(taskName)

        when:
        project.plugins.apply(NodeReleasePlugin)

        then:
        project.tasks.getByName(taskName)

        where:
        taskName << [NodeReleasePlugin.NPM_TEST_TASK, NodeReleasePlugin.NPM_BUILD_TASK, NodeReleasePlugin.NPM_CLEAN_TASK]
    }
}
