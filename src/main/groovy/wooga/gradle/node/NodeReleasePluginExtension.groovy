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

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

class NodeReleasePluginExtension {

    final Property<String> npmUser
    final Property<String> npmPass
    final Property<String> npmAuthUrl
    final RegularFileProperty npmrcFile

    NodeReleasePluginExtension(Project project) {
        npmUser = project.objects.property(String)
        npmPass = project.objects.property(String)
        npmAuthUrl = project.objects.property(String)
        npmrcFile = project.layout.fileProperty()
    }
}
