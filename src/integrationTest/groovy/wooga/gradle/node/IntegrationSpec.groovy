package wooga.gradle.node

import groovy.json.JsonBuilder
import groovy.json.StringEscapeUtils

class IntegrationSpec extends nebula.test.IntegrationSpec {

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    String packageJsonContent(Map<String, Object> content) {
        new JsonBuilder(content).toPrettyString()
    }
}
