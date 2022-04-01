/*
 * Copyright 2020-2022 Wooga GmbH
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

import com.github.gradle.node.NodePlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.specs.Spec
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.github.publish.GithubPublishPlugin
import wooga.gradle.github.publish.tasks.GithubPublish
import wooga.gradle.node.tasks.ModifyPackageJsonTask
import wooga.gradle.node.tasks.NpmCredentialsTask
import wooga.gradle.version.VersionPlugin
import wooga.gradle.version.VersionPluginExtension
import wooga.gradle.version.VersionScheme

class NodeReleasePlugin implements Plugin<Project> {

    static final String NODE_CLEAN_TASK = 'node_run_clean'
    static final String NODE_BUILD_TASK = 'node_run_build'
    static final String NODE_TEST_TASK = 'node_run_test'
    static final String NODE_PUBLISH_TASK = 'node_publish'

    public static final String RC_TASK_NAME = "rc"
    private static final String DEPRECATED_CANDIDATE_TASK_NAME = "candidate"
    public static final String FINAL_TASK_NAME = "final"
    public static final String SNAPSHOT_TASK_NAME = "snapshot"

    static final String MODIFY_PACKAGE_VERSION_TASK = 'modifyPackageJson_version'
    static final String CREATE_CREDENTIALS_TASK = 'ensureNpmrc'

    static final String PLUGIN_EXTENSION = 'nodeRelease'
    static final String TASK_GROUP = 'Node Release'

    static final String PACKAGE_JSON = 'package.json'
    static final String PACKAGE_LOCK_JSON = 'package-lock.json'
    static final String YARN_LOCK_JSON = 'yarn.lock'
    static final String NPMRC = '.npmrc'

    private NodeReleasePluginExtension extension
    private static Engine engine

    @Override
    void apply(Project project) {

        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(NodePlugin.class)
        project.pluginManager.apply(GithubPublishPlugin.class)

        extension = createExtension(project)

        if (project == project.rootProject) {
            detectEngine(project)
            aliasCandidateTasksToRc(project)
            applyVersionPlugin(project)
            configureReleaseLifecycle(project)
            configureModifyPackageJsonVersionTask(project)
            configureNpmCredentialsTasks(project, extension)
            configureGithubPublish(project)
        }


        project.tasks.create(MODIFY_PACKAGE_VERSION_TASK, ModifyPackageJsonTask.class)
        project.tasks.create(CREATE_CREDENTIALS_TASK, NpmCredentialsTask.class)
    }

    private static void applyVersionPlugin(Project project) {
        project.pluginManager.apply(VersionPlugin)
        VersionPluginExtension versionExtension = project.extensions.findByType(VersionPluginExtension)
        def p = project.providers.provider({
            project.gradle.startParameter.taskNames.contains("final")
            if (project.gradle.taskGraph.allTasks.contains(":final")) {
                return "final"
            }
            null
        })
        versionExtension.versionScheme(VersionScheme.semver2)
    }

    private NodeReleasePluginExtension createExtension(Project project) {
        extension = project.extensions.create(PLUGIN_EXTENSION, NodeReleasePluginExtension, project)
        extension.npmUser.convention(NodeReleasePluginConventions.npmUser.getStringValueProvider(project))
        extension.npmPass.convention(NodeReleasePluginConventions.npmPassword.getStringValueProvider(project))
        extension.npmAuthUrl.convention(NodeReleasePluginConventions.npmAuthUrl.getStringValueProvider(project))
        extension.npmrcFile.set(project.file(NPMRC))
        extension
    }

    private static void configureModifyPackageJsonVersionTask(Project project) {
        def publishTask = project.tasks.getByName(NODE_PUBLISH_TASK)
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

    /**
     * Hook the various node tasks into gradle's release lifecycle tasks.
     * The engine scoped tasks are....
     */
    private static void configureReleaseLifecycle(Project project) {
        def tasks = project.tasks

        def checkTask = tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME)
        def assembleTask = tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
        def cleanTask = tasks.getByName(BasePlugin.CLEAN_TASK_NAME)
        def publishTask = tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)

        def githubPublishTask = project.tasks.getByName(GithubPublishPlugin.PUBLISH_TASK_NAME)

        def nodeCleanTask = tasks.create(NODE_CLEAN_TASK)
        def nodeTestTask = tasks.create(NODE_TEST_TASK)
        def nodeBuildTask = tasks.create(NODE_BUILD_TASK)
        def nodePublishTask = tasks.create(NODE_PUBLISH_TASK)

        def engineScopedCleanTask = tasks.getByName(engineScopedTaskName(NODE_CLEAN_TASK))
        def engineScopedTestTask = tasks.getByName(engineScopedTaskName(NODE_TEST_TASK))
        def engineScopedBuildTask = tasks.getByName(engineScopedTaskName(NODE_BUILD_TASK))
        def engineScopedPublishTask = tasks.getByName(engineScopedTaskName(NODE_PUBLISH_TASK))

        nodeCleanTask.dependsOn engineScopedCleanTask
        nodeTestTask.dependsOn engineScopedTestTask

        // Set up assemble lifecycle dependencies
        nodeBuildTask.dependsOn engineScopedBuildTask
        assembleTask.dependsOn nodeBuildTask
        // Set up publish lifecycle dependencies
        nodePublishTask.dependsOn nodeBuildTask, engineScopedPublishTask
        publishTask.dependsOn nodePublishTask
        githubPublishTask.mustRunAfter nodePublishTask

        // TODO: Add custom tasks previously provided by the nebula release plugin
        Task finalTask = project.tasks.create(FINAL_TASK_NAME)
        Task rcTask = project.tasks.create(RC_TASK_NAME)
        Task snapshotTask = project.tasks.create(SNAPSHOT_TASK_NAME)

        [finalTask, rcTask, snapshotTask].each {
            it.dependsOn publishTask
        }

        cleanTask.dependsOn nodeCleanTask
        checkTask.dependsOn nodeTestTask
    }

    private static void configureGithubPublish(Project project) {
        VersionPluginExtension versionExtension = project.extensions.findByType(VersionPluginExtension)

        //TODO Clean me up and fix me in net.wooga.release
        def logger = project.logger
        def predicate = new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                def propertyName = "versionBuilder.stage"
                def value = versionExtension.stage.getOrNull()
                def validValues = ["rc", "final"]

                Boolean satisfied = false
                String messagePrefix = "Predicate property '${propertyName}' for task '${task.name}'"
                if (value) {
                    satisfied = value in validValues
                    if (satisfied) {
                        logger.info("${messagePrefix} satisfies the condition as it has a valid value of '${value}'")
                    } else {
                        logger.info("${messagePrefix} did not satisfy the condition as its value of '${value}' was not among those valid: ${validValues}")
                    }
                } else {
                    logger.warn("${messagePrefix} did not satisfy the condition as it was not found among the project's properties")
                }
                return satisfied
            }
        }

        project.tasks.withType(GithubPublish, new Action<GithubPublish>() {
            @Override
            void execute(GithubPublish githubPublishTask) {
                githubPublishTask.onlyIf(predicate)
                githubPublishTask.tagName.set("v${project.version}")
                githubPublishTask.releaseName.set(project.version.toString())
                githubPublishTask.body = null
                githubPublishTask.draft.set(false)
                githubPublishTask.prerelease.set(versionExtension.stage.map({
                    it != "final"
                }))
            }
        })
    }

    private static detectEngine(Project project) {
        engine = project.file(YARN_LOCK_JSON).exists() ? Engine.yarn : Engine.npm
    }

    private static engineScopedTaskName(String taskName) {
        return "${engine}_${(taskName - "node_")}"
    }

    /**
     * Older versions of this plugin used the {@code candidate} task name for what we consider the {@code rc} task.
     * We need to replace any mentions of {@code candidate} with {@code rc} for our newer API.
     */
    protected static void aliasCandidateTasksToRc(Project project) {

        // Rename the 'candidate' task, if present, to 'rc'
        List<String> cliTasks = project.rootProject.gradle.startParameter.taskNames
        if (cliTasks.contains(DEPRECATED_CANDIDATE_TASK_NAME)) {
            cliTasks.remove(DEPRECATED_CANDIDATE_TASK_NAME)
            cliTasks.add(RC_TASK_NAME)
            project.rootProject.gradle.startParameter.setTaskNames(cliTasks)
        }

        def releaseStagePropertyName = "release.stage"

        // Also rename 'candidate' to 'rc' for the release stage
        if (project.properties.containsKey(releaseStagePropertyName)
                && project.properties[releaseStagePropertyName] == "candidate") {
            project.allprojects.each {
                it.extensions.getByType(ExtraPropertiesExtension).set(releaseStagePropertyName, "rc")
            }
        }
    }
}
