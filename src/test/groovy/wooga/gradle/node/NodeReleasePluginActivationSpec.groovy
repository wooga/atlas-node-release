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
