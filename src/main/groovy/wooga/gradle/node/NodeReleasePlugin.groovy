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

import com.moowork.gradle.node.NodePlugin
import nebula.plugin.release.ReleasePlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.node.tasks.ModifyPackageJsonTask
import wooga.gradle.node.tasks.NpmCredentialsTask

class NodeReleasePlugin implements Plugin<Project> {

    static final String NPM_CLEAN_TASK = 'npm_run_clean'
    static final String NPM_BUILD_TASK = 'npm_run_build'
    static final String NPM_TEST_TASK = 'npm_run_test'
    static final String NPM_PUBLISH_TASK = 'npm_publish'

    static final String MODIFY_PACKAGE_VERSION_TASK = 'modifyPackageJson_version'
    static final String CREATE_CREDENTIALS_TASK = 'npmCreateCredentialsTask'

    static final String PLUGIN_EXTENSION = 'nodeRelease'

    static final String TASK_GROUP = 'Node Release'

    static final String PACKAGE_JSON = 'package.json'
    static final String NPMRC = '.npmrc'

    static final String NODE_RELEASE_NPM_USER_ENV_VAR = 'NODE_RELEASE_NPM_USER'
    static final String NODE_RELEASE_NPM_PASS_ENV_VAR = 'NODE_RELEASE_NPM_PASS'
    static final String NODE_RELEASE_NPM_AUTH_URL_ENV_VAR = 'NODE_RELEASE_NPM_AUTH_URL'

    private NodeReleasePluginExtension extension

    @Override
    void apply(Project project) {

        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(NodePlugin.class)

        extension = createExtension(project)

        if (project == project.rootProject) {
            project.pluginManager.apply(ReleasePlugin.class)
            configureReleaseLifecycle(project)
            configureModifyPackageJsonVersionTask(project)
            configureNpmCredentialsTasks(project, extension)
        }

        project.tasks.create(MODIFY_PACKAGE_VERSION_TASK, ModifyPackageJsonTask.class)
        project.tasks.create(CREATE_CREDENTIALS_TASK, NpmCredentialsTask.class)
    }

    private NodeReleasePluginExtension createExtension(Project project) {
        extension = project.extensions.create(PLUGIN_EXTENSION, NodeReleasePluginExtension, project)
        extension.npmUser.set(getConfigProperty(project, 'nodeRelease.npmUser', NODE_RELEASE_NPM_USER_ENV_VAR))
        extension.npmPass.set(getConfigProperty(project, 'nodeRelease.npmPass', NODE_RELEASE_NPM_PASS_ENV_VAR))
        extension.npmAuthUrl.set(getConfigProperty(project, 'nodeRelease.npmAuthUrl', NODE_RELEASE_NPM_AUTH_URL_ENV_VAR))
        extension.npmrcFile.set(project.file(NPMRC))
        extension
    }

    private static String getConfigProperty(Project project, String name, String envName) {
        if (project.hasProperty(name)) {
            return project.properties[name].toString()
        }
        return System.getenv(envName)
    }

    private static void configureReleaseLifecycle(Project project) {
        def tasks = project.tasks

        def checkTask = tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME)
        def assembleTask = tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
        def cleanTask = tasks.getByName(BasePlugin.CLEAN_TASK_NAME)
        def postReleaseTask = tasks.getByName(ReleasePlugin.POST_RELEASE_TASK_NAME)
        def releaseTask = tasks.getByName('release')
        def publishTask = project.tasks.getByName(NPM_PUBLISH_TASK)

        def npmCleanTask = tasks.getByName(NPM_CLEAN_TASK)
        def npmTestTask = tasks.getByName(NPM_TEST_TASK)
        def npmBuildTask = tasks.getByName(NPM_BUILD_TASK)
        def npmPublishTask = tasks.getByName(NPM_PUBLISH_TASK)

        cleanTask.dependsOn npmCleanTask
        checkTask.dependsOn npmTestTask
        releaseTask.dependsOn assembleTask
        assembleTask.dependsOn npmBuildTask
        tasks.release.dependsOn assembleTask
        postReleaseTask.dependsOn npmPublishTask
        publishTask.mustRunAfter releaseTask
    }

    private static void configureModifyPackageJsonVersionTask(Project project) {
        def publishTask = project.tasks.getByName(NPM_PUBLISH_TASK)
        project.tasks.withType(ModifyPackageJsonTask, new Action<ModifyPackageJsonTask>() {

            @Override
            void execute(ModifyPackageJsonTask modifyPackageJsonTask) {
                modifyPackageJsonTask.group = TASK_GROUP
                modifyPackageJsonTask.inputFile = project.file(PACKAGE_JSON)
                modifyPackageJsonTask.outputFile = project.file(PACKAGE_JSON)
                modifyPackageJsonTask.config = [version: project.getVersion().toString()]
                modifyPackageJsonTask.description = "Set 'package.json' version based on release plugin version"
                publishTask.dependsOn modifyPackageJsonTask
            }
        })
    }

    private static void configureNpmCredentialsTasks(Project project, NodeReleasePluginExtension extension) {
        project.tasks.withType(NpmCredentialsTask, new Action<NpmCredentialsTask>() {

            @Override
            void execute(NpmCredentialsTask npmCredentialsTask) {
                npmCredentialsTask.group = TASK_GROUP
                npmCredentialsTask.description = "create ${NPMRC} file"
                npmCredentialsTask.npmUser.set(extension.npmUser)
                npmCredentialsTask.npmPass.set(extension.npmPass)
                npmCredentialsTask.npmAuthUrl.set(extension.npmAuthUrl)
                npmCredentialsTask.npmrcFile.set(extension.npmrcFile)
            }
        })
    }
}
