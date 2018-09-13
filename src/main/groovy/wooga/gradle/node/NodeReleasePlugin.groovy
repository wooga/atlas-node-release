package wooga.gradle.node

import com.moowork.gradle.node.NodePlugin
import nebula.plugin.release.ReleasePlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.node.tasks.ModifyPackageJsonTask

class NodeReleasePlugin implements Plugin<Project> {

    static final String NPM_CLEAN_TASK = 'npm_run_clean'
    static final String NPM_BUILD_TASK = 'npm_run_build'
    static final String NPM_TEST_TASK = 'npm_run_test'
    static final String NPM_PUBLISH_TASK = 'npm_publish'

    static final String TASK_GROUP = 'Node Release'

    static final String PACKAGE_JSON = 'package.json'

    @Override
    void apply(Project project) {
        applyBase(project)
        applyGradleNode(project)

        if (project == project.rootProject) {
            applyNebularRelease(project)
            configureReleaseLifecycle(project)
            configureModifyPackageJsonTask(project)
        }

        project.afterEvaluate {
            def task = project.tasks.create('ModifyPackageJson_version', ModifyPackageJsonTask.class)
            configureModifyPackageJsonVersionTask(task, project)
        }
    }

    private static void applyBase(Project project) {
        project.pluginManager.apply(BasePlugin.class)
    }

    private static void applyGradleNode(Project project) {
        project.pluginManager.apply(NodePlugin.class)
    }

    private static void applyNebularRelease(Project project) {
        project.pluginManager.apply(ReleasePlugin.class)
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

    private static void configureModifyPackageJsonTask(Project project) {
        def publishTask = project.tasks.getByName(NPM_PUBLISH_TASK)
        project.tasks.withType(ModifyPackageJsonTask, new Action<ModifyPackageJsonTask>() {

            @Override
            void execute(ModifyPackageJsonTask modifyPackageJsonTask) {
                configureModifyPackageJsonVersionTask(modifyPackageJsonTask, project)
                publishTask.dependsOn modifyPackageJsonTask
            }
        })
    }

    private static void configureModifyPackageJsonVersionTask(ModifyPackageJsonTask task, Project project) {
        task.group = TASK_GROUP
        task.inputFile = project.file(PACKAGE_JSON)
        task.outputFile = project.file(PACKAGE_JSON)
        task.config = [version: project.getVersion().toString()]
        task.description = "Set 'package.json' version based on release plugin version"
    }
}
