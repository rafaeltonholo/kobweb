package com.varabyte.kobweb.gradle.application

import com.varabyte.kobweb.gradle.application.buildservices.KobwebTaskListener
import com.varabyte.kobweb.gradle.application.extensions.createAppBlock
import com.varabyte.kobweb.gradle.application.extensions.createExportBlock
import com.varabyte.kobweb.gradle.application.tasks.KobwebBrowserCacheIdTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebCopyDependencyResourcesTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebCreateServerScriptsTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebExportTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebGenerateApisFactoryTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebGenerateSiteEntryTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebGenerateSiteIndexTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebStartTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebStopTask
import com.varabyte.kobweb.gradle.application.tasks.KobwebUnpackServerJarTask
import com.varabyte.kobweb.gradle.application.util.kebabCaseToTitleCamelCase
import com.varabyte.kobweb.gradle.core.KobwebCorePlugin
import com.varabyte.kobweb.gradle.core.extensions.KobwebBlock
import com.varabyte.kobweb.gradle.core.kmp.JsTarget
import com.varabyte.kobweb.gradle.core.kmp.JvmTarget
import com.varabyte.kobweb.gradle.core.kmp.buildTargets
import com.varabyte.kobweb.gradle.core.kmp.kotlin
import com.varabyte.kobweb.gradle.core.ksp.setupKsp
import com.varabyte.kobweb.gradle.core.kspBackendFile
import com.varabyte.kobweb.gradle.core.kspFrontendFile
import com.varabyte.kobweb.gradle.core.tasks.KobwebTask
import com.varabyte.kobweb.gradle.core.util.namedOrNull
import com.varabyte.kobweb.project.KobwebFolder
import com.varabyte.kobweb.project.conf.KobwebConfFile
import com.varabyte.kobweb.server.api.ServerEnvironment
import com.varabyte.kobweb.server.api.ServerRequest
import com.varabyte.kobweb.server.api.ServerRequestsFile
import com.varabyte.kobweb.server.api.ServerStateFile
import com.varabyte.kobweb.server.api.SiteLayout
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.tooling.events.FailureResult
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import javax.inject.Inject
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

val Project.kobwebFolder: KobwebFolder
    get() = KobwebFolder.fromChildPath(layout.projectDirectory.asFile.toPath())
        ?: throw GradleException("This project is not a Kobweb project but is applying the Kobweb plugin.")

@Suppress("unused") // KobwebApplicationPlugin is found by Gradle via reflection
class KobwebApplicationPlugin @Inject constructor(
    private val buildEventsListenerRegistry: BuildEventsListenerRegistry
) : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(KobwebCorePlugin::class.java)

        val kobwebFolder = project.kobwebFolder
        val kobwebConf = with(KobwebConfFile(kobwebFolder)) {
            // TODO(#310): Remove this hack before Kobweb v1.0. At that point, users are very likely to have picked up
            //  this update by then (or created a new app, where templates will have been updated).
            if (!path.exists()) {
                throw GradleException("Missing conf.yaml file from Kobweb folder")
            }

            val originalText = path.readText()
            val updatedText = originalText
                .replace("build/developmentExecutable", "build/dist/js/developmentExecutable")
                .replace("build/distributions", "build/dist/js/productionExecutable")

            if (originalText != updatedText) {
                project.logger.warn(
                    "The Compose HTML team changed the location of some output files in 1.5.1. In order to keep you working, we have automatically updated your `.kobweb/conf.yaml` file to reflect these changes."
                )
                path.writeText(updatedText)
            }
            content!!
        }

        val kobwebBlock = ((project as ExtensionAware).extensions["kobweb"] as KobwebBlock).apply {
            createAppBlock(kobwebConf)
            createExportBlock()
        }

        setupKsp(project, kobwebBlock, includeAppData = true)

        val env =
            project.findProperty("kobwebEnv")?.let { ServerEnvironment.valueOf(it.toString()) } ?: ServerEnvironment.DEV
        val runLayout =
            project.findProperty("kobwebRunLayout")?.let { SiteLayout.valueOf(it.toString()) } ?: SiteLayout.KOBWEB
        val exportLayout =
            project.findProperty("kobwebExportLayout")?.let { SiteLayout.valueOf(it.toString()) } ?: SiteLayout.KOBWEB

        project.extra["kobwebBuildTarget"] =
            project.findProperty("kobwebBuildTarget")?.let { BuildTarget.valueOf(it.toString()) }
                ?: if (env == ServerEnvironment.DEV) BuildTarget.DEBUG else BuildTarget.RELEASE
        val buildTarget = project.kobwebBuildTarget

        val kobwebCopyDependencyResourcesTask = project.tasks
            .register<KobwebCopyDependencyResourcesTask>("kobwebCopyDepResources", kobwebBlock)
        val kobwebGenSiteIndexTask = project.tasks
            .register<KobwebGenerateSiteIndexTask>("kobwebGenSiteIndex", kobwebConf, kobwebBlock, buildTarget)

        kobwebGenSiteIndexTask.configure {
            // Make sure copy resources occurs first, so that our index.html file doesn't get overwritten
            dependsOn(kobwebCopyDependencyResourcesTask)
        }

        val kobwebUnpackServerJarTask = project.tasks.register<KobwebUnpackServerJarTask>("kobwebUnpackServerJar")
        val kobwebCreateServerScriptsTask = project.tasks
            .register<KobwebCreateServerScriptsTask>("kobwebCreateServerScripts")
        val kobwebStartTask = run {
            val reuseServer = project.findProperty("kobwebReuseServer")?.let { it.toString().toBoolean() } ?: true
            project.tasks.register<KobwebStartTask>("kobwebStart", env, runLayout, reuseServer)
        }
        kobwebStartTask.configure {
            dependsOn(kobwebUnpackServerJarTask)
            doLast {
                val devScript = kobwebConf.server.files.dev.script
                if (!project.file(devScript).exists()) {
                    throw GradleException(
                        "e: Your .kobweb/conf.yaml dev script (\"$devScript\") could not be found. This will cause " +
                            "the page to fail to load with a 500 error. Perhaps search your build/ directory for " +
                            "\"${devScript.substringAfterLast('/')}\" to find the right path."
                    )
                }
            }
        }
        project.tasks.register<KobwebStopTask>("kobwebStop")

        val kobwebCleanSiteTask = project.tasks.register<KobwebTask>(
            "kobwebCleanSite",
            "Cleans all site artifacts generated by a previous export"
        )
        kobwebCleanSiteTask.configure {
            doLast {
                project.delete(kobwebConf.server.files.prod.siteRoot)
            }
        }
        val kobwebCleanFolderTask = project.tasks.register<KobwebTask>(
            "kobwebCleanFolder",
            "Cleans all transient files that live in the .kobweb folder"
        )
        kobwebCleanFolderTask.configure {
            dependsOn(kobwebCleanSiteTask)
            doLast {
                project.delete(kobwebFolder.resolve("server"))
            }
        }
        val kobwebExportTask = project.tasks
            .register<KobwebExportTask>("kobwebExport", kobwebConf, kobwebBlock, exportLayout)

        project.tasks.register<KobwebBrowserCacheIdTask>("kobwebBrowserCacheId", kobwebBlock)

        // Note: I'm pretty sure I'm abusing build service tasks by adding a listener to it directly but I'm not sure
        // how else I'm supposed to do this
        val taskListenerService = project.gradle.sharedServices
            .registerIfAbsent("kobweb-task-listener", KobwebTaskListener::class.java) {}
        run {
            var isBuilding = false
            var isServerRunning = run {
                val stateFile = ServerStateFile(kobwebFolder)
                stateFile.content?.let { serverState ->
                    ProcessHandle.of(serverState.pid).isPresent
                }
            } ?: false

            taskListenerService.get().onFinishCallbacks.add { event ->
                if (kobwebStartTask.name !in project.gradle.startParameter.taskNames) return@add

                val taskName = event.descriptor.name.substringAfterLast(":")
                val serverRequestsFile = ServerRequestsFile(kobwebFolder)
                val taskFailed = event.result is FailureResult

                if (isServerRunning) {
                    if (taskFailed) {
                        serverRequestsFile.enqueueRequest(
                            ServerRequest.SetStatus(
                                "Failed.",
                                isError = true,
                                timeoutMs = 500
                            )
                        )
                    } else {
                        if (taskName == kobwebStartTask.name) {
                            serverRequestsFile.enqueueRequest(ServerRequest.ClearStatus())
                            serverRequestsFile.enqueueRequest(ServerRequest.IncrementVersion())
                        } else if (!isBuilding) {
                            serverRequestsFile.enqueueRequest(ServerRequest.SetStatus("Building..."))
                            isBuilding = true
                        }
                    }
                } else {
                    if (!taskFailed && taskName == kobwebStartTask.name) {
                        isServerRunning = true
                    }
                }
            }
        }
        buildEventsListenerRegistry.onTaskCompletion(taskListenerService)

        // TODO: below we add generated kobweb sources to the generatedByKsp... source sets. This is done to prevent KSP
        //  from processing them, but ideally we should probably create our own source set for this instead?
        // Note: we use `matching` instead of `named` for the ksp source sets because they don't exist yet at this point
        project.buildTargets.withType<KotlinJsIrTarget>().configureEach {
            val jsTarget = JsTarget(this)
            project.hackWorkaroundSinceWebpackTaskIsBrokenInContinuousMode()

            val kobwebGenSiteEntryTask = project.tasks
                .register<KobwebGenerateSiteEntryTask>("kobwebGenSiteEntry", kobwebConf, kobwebBlock, buildTarget)

            kobwebGenSiteEntryTask.configure {
                kspGenFile = project.kspFrontendFile
            }

            val jsRunTasks = listOf(
                jsTarget.browserDevelopmentRun, jsTarget.browserProductionRun,
                jsTarget.browserRun, jsTarget.run,
            )
            // Users should be using Kobweb tasks instead of the standard multiplatform tasks, but they
            // probably don't know that. We do our best to work even in those cases, but warn the user to prefer
            // the Kobweb commands instead.
            jsRunTasks.forEach { taskName ->
                project.tasks.namedOrNull(taskName)?.configure {
                    doFirst {
                        logger.error("With Kobweb, you should run `gradlew kobwebStart` instead. Some site behavior may not work.")
                    }
                }
            }

            project.kotlin.sourceSets.matching { it.name == "generatedByKspKotlinJs" }.configureEach {
                kotlin.srcDir(kobwebGenSiteEntryTask)
            }

            project.kotlin.sourceSets.named(jsTarget.mainSourceSet) {
                // TODO: kobwebCopyDependencyResourcesTask is not included as it already has the same output dir
                //  but ideally they would have different output dirs and then we'd include it here too
                resources.srcDirs(kobwebGenSiteIndexTask)
            }

            // When exporting, both dev + production webpack actions are triggered - dev for the temporary server
            // that runs, and production for generating the final JS file for the site. However, these tasks share some
            // output directories (see https://youtrack.jetbrains.com/issue/KT-56305), so the following order
            // declaration is needed for gradle to be happy. Note also that we don't configure the task directly by its
            // name, as it may not yet exist (for some reason). Pending https://github.com/gradle/gradle/issues/16543,
            // we simply match it by its name amongst all tasks of its type.
            project.tasks
                .matching { it.name == jsTarget.compileProductionExecutableKotlin }
                .configureEach { mustRunAfter(kobwebStartTask) }

            kobwebStartTask.configure {
                // PROD env uses files copied over into a site folder by the export task, so it doesn't need to trigger
                // much.
                if (env == ServerEnvironment.DEV) {
                    val webpackTask = project.tasks.named(jsTarget.browserDevelopmentWebpack)
                    dependsOn(webpackTask)
                }
            }

            kobwebExportTask.configure {
                appFrontendMetadataFile = project.kspFrontendFile
                // Exporting ALWAYS spins up a dev server, so that way it loads the files it needs from dev locations
                // before outputting them into a final prod folder.
                check(env == ServerEnvironment.DEV)

                dependsOn(kobwebCleanSiteTask)
                dependsOn(kobwebCreateServerScriptsTask)
                dependsOn(kobwebStartTask)
                dependsOn(project.tasks.namedOrNull(jsTarget.browserProductionWebpack))
            }
        }
        project.buildTargets.withType<KotlinJvmTarget>().configureEach {
            val jvmTarget = JvmTarget(this)

            project.tasks.namedOrNull<Copy>(jvmTarget.processResources)?.configure {
                // TODO: are we doing something wrong or is this fine - (also in library)
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }

            // PROD env uses files copied over into a site folder by the export task, so it doesn't need to trigger
            // much.
            kobwebStartTask.configure {
                if (env == ServerEnvironment.DEV) {
                    // If this site has server routes, make sure we built the jar that our servers can load
                    dependsOn(project.tasks.namedOrNull(jvmTarget.jar))
                }
            }

            val kobwebGenApisFactoryTask = project.tasks
                .register<KobwebGenerateApisFactoryTask>("kobwebGenApisFactory", kobwebBlock)

            kobwebGenApisFactoryTask.configure {
                kspGenFile = project.kspBackendFile!! // exists when jvm target exists
            }

            project.kotlin.sourceSets.matching { it.name == "generatedByKspKotlinJvm" }.configureEach {
                kotlin.srcDir(kobwebGenApisFactoryTask)
            }
        }
    }
}

/**
 * Method provided for users to call if they generate their own Gradle task that generates some JS (frontend) code.
 *
 * Calling this ensures that their task will be triggered before the relevant Kobweb compilation task.
 */
@Deprecated(
    "Add the task outputs to the source set directly instead. Note that you may have to adjust the task to output a directory instead of a file.",
    ReplaceWith("kotlin.sourceSets.getByName(\"jsMain\").kotlin.srcDir(task)"),
)
fun Project.notifyKobwebAboutFrontendCodeGeneratingTask(task: Task) {
    tasks.matching { it.name == "kspKotlinJs" }.configureEach { dependsOn(task) }
}

@Deprecated(
    "Add the task outputs to the source set directly instead. Note that you may have to adjust the task to output a directory instead of a file.",
    ReplaceWith("kotlin.sourceSets.getByName(\"jsMain\").kotlin.srcDir(task)"),
)
fun Project.notifyKobwebAboutFrontendCodeGeneratingTask(task: TaskProvider<*>) {
    tasks.matching { it.name == "kspKotlinJs" }.configureEach { dependsOn(task) }
}

/**
 * Method provided for users to call if they generate their own Gradle task that generates some JVM (server) code.
 *
 * Calling this ensures that their task will be triggered before the relevant Kobweb compilation task.
 */
@Deprecated(
    "Add the task outputs to the source set directly instead. Note that you may have to adjust the task to output a directory instead of a file.",
    ReplaceWith("kotlin.sourceSets.getByName(\"jvmMain\").kotlin.srcDir(task)"),
)
fun Project.notifyKobwebAboutBackendCodeGeneratingTask(task: Task) {
    tasks.matching { it.name == "kspKotlinJvm" }.configureEach { dependsOn(task) }
}

@Deprecated(
    "Add the task outputs to the source set directly instead. Note that you may have to adjust the task to output a directory instead of a file.",
    ReplaceWith("kotlin.sourceSets.getByName(\"jvmMain\").kotlin.srcDir(task)"),
)
fun Project.notifyKobwebAboutBackendCodeGeneratingTask(task: TaskProvider<*>) {
    tasks.matching { it.name == "kspKotlinJvm" }.configureEach { dependsOn(task) }
}

/**
 * Inform Kobweb about a task that generates a jar that should be copied into the server's plugins directory.
 *
 * Users can always manually copy over server jars into their .kobweb/server/plugins directory, but this method
 * automates a common use case where the user is developing the plugin locally, and it would be frustrating to remember
 * to copy the new jar over each time something has changed.
 *
 * This method will create an intermediate task that copies the jar into the plugins directory, and then hooks up task
 * dependencies so that it will be called automatically before the Kobweb server runs.
 *
 * @param name An optional name you can provide for the intermediate "copy jar to plugins dir" task. You normally don't
 *   have to provide a name, but providing your own may be slightly more performant (since the task name I generate
 *   requires realizing the task, which may slightly slow down the configuration phase).
 */
fun Project.notifyKobwebAboutServerPluginTask(
    jarTask: TaskProvider<Jar>,
    name: String = "copy${project.name.kebabCaseToTitleCamelCase()}JarToKobwebServerPluginsDir"
) {
    val copyKobwebServerPluginTask = tasks.register<Copy>(name) {
        from(jarTask)
        destinationDir = project.projectDir.resolve(".kobweb/server/plugins")
    }

    tasks.named("kobwebStart") {
        dependsOn(copyKobwebServerPluginTask)
    }
}

val Project.kobwebBuildTarget get() = project.extra["kobwebBuildTarget"] as BuildTarget

// For context, see: https://youtrack.jetbrains.com/issue/KT-55820/jsBrowserDevelopmentWebpack-in-continuous-mode-doesnt-keep-outputs-up-to-date
// It seems like the webpack tasks are broken when run in continuous mode (it has a special branch of logic for handling
// `isContinuous` mode and I guess it just needs more time to bake).
// Unfortunately, `kobweb run` lives and dies on its live reloading behavior. So in order to allow it to support
// webpack, we need to get a little dirty here, using reflection to basically force the webpack task to always take the
// non-continuous logic branch.
// Basically, we're setting this value to always be false:
// https://github.com/JetBrains/kotlin/blob/4af0f110c7053d753c92fd9caafb4be138fdafba/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/js/webpack/KotlinWebpack.kt#L276
private fun Project.hackWorkaroundSinceWebpackTaskIsBrokenInContinuousMode() {
    tasks.withType<KotlinWebpack>().configureEach {
        // Gradle generates subclasses via bytecode generation magic. Here, we need to grab the superclass to find
        // the private field we want.
        this::class.java.superclass.declaredFields
            // Note: Isn't ever null for now but checking protects us against future changes to KotlinWebpack
            .firstOrNull { it.name == "isContinuous" }
            ?.let { isContinuousField ->
                isContinuousField.isAccessible = true
                isContinuousField.setBoolean(this, false)
            }
    }
}
