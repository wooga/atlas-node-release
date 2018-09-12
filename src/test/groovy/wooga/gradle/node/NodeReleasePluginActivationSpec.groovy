package wooga.gradle.node

import nebula.test.PluginProjectSpec
import org.ajoberstar.grgit.Grgit

class NodeReleasePluginActivationSpec extends PluginProjectSpec {

    String getPluginName() {
        return 'net.wooga.node-release'
    }

    Grgit git

    def setup() {
        new File(projectDir, '.gitignore') << """
        userHome/
        """.stripIndent()

        git = Grgit.init(dir: projectDir)
        git.add(patterns: ['.gitignore'])
        git.commit(message: 'initial commit')
        git.tag.add(name: 'v0.0.1')
    }
}
