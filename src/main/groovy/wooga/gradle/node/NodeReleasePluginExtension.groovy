package wooga.gradle.node

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

class NodeReleasePluginExtension {

    final Property<String> npmLogin
    final Property<String> npmAuthUrl
    final RegularFileProperty npmrcFile

    NodeReleasePluginExtension(Project project) {
        npmLogin = project.objects.property(String)
        npmAuthUrl = project.objects.property(String)
        npmrcFile = project.layout.fileProperty()
    }
}
