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

    def "set default engine to npm"() {
        given:
        !project.file('package-lock.json')
        !project.file('yarn.lock')

        when:
        project.plugins.apply(NodeReleasePlugin)

        then:
        project.tasks.getByName('npm_run_test')
    }
}
