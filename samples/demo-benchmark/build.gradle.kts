import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.concurrent.thread

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hikage)
}

android {
    namespace = gropify.project.samples.demo.benchmark.packageName
    testNamespace = gropify.project.samples.demo.benchmark.testPackageName

    compileSdk = gropify.project.android.compileSdk

    defaultConfig {
        minSdk = gropify.project.android.minSdk
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }

    testBuildType = "release"

    buildTypes {
        release {
            isDefault = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.hikageCore)
    implementation(projects.hikageRuntimeAttribute)
    implementation(projects.hikageWidgetAndroidx)
    implementation(projects.hikageWidgetMaterial)

    implementation(platform(libs.betterandroid.android.bom))
    implementation(libs.betterandroid.ui.extension)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.material)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.benchmark.junit4)
}

val viewTreeBenchmarkOutputDirectory = layout.buildDirectory.dir("outputs/hikage_benchmark/releaseAndroidTest")
val viewTreeBenchmarkCompletionMarker = viewTreeBenchmarkOutputDirectory.map { it.file("completed.marker") }
val runViewTreeBenchmark = tasks.register<RunViewTreeBenchmarkTask>("runViewTreeBenchmark") {
    group = "verification"
    description = "Runs view-tree benchmarks with a root-safe AndroidX Benchmark instrumentation command."

    dependsOn("installReleaseAndroidTest")

    targetPackageName.set(android.namespace)
    testPackageName.set(android.testNamespace)

    instrumentationRunner.set(android.defaultConfig.testInstrumentationRunner)

    // Enable AndroidX Benchmark output and suppress errors for emulator conditions.
    // Note: Benchmark on a real device is recommended for more accurate results.
    suppressErrors.set("EMULATOR")

    deviceOutputDirectory.set(gropify.project.samples.demo.benchmark.benchmarkViewTreeReport.deviceOutputDirectory)
    localOutputDirectory.set(viewTreeBenchmarkOutputDirectory)
    completionMarkerFile.set(viewTreeBenchmarkCompletionMarker)

    suppressActivityMissing.set(gropify.project.samples.demo.benchmark.benchmarkViewTreeReport.suppressActivityMissing)
}

val generateViewTreeBenchmarkReport = tasks.register<GenerateViewTreeBenchmarkReportTask>("generateViewTreeBenchmarkReport") {
    group = "verification"
    description = "Generates a single Hikage view-tree benchmark HTML report from AndroidX Benchmark JSON outputs."

    benchmarkOutputDirectory.set(viewTreeBenchmarkOutputDirectory)
    completionMarkerFile.set(viewTreeBenchmarkCompletionMarker)
    reportFile.set(layout.buildDirectory.file(gropify.project.samples.demo.benchmark.benchmarkViewTreeReport.reportFile))
    openReport.set(gropify.project.samples.demo.benchmark.benchmarkViewTreeReport.openReport)
    mustRunAfter(runViewTreeBenchmark)
}

tasks.register("benchmarkViewTreeReport") {
    group = "verification"
    description = "Runs view-tree benchmarks and generates a merged XML vs Hikage HTML report."

    dependsOn(runViewTreeBenchmark, generateViewTreeBenchmarkReport)
}

/**
 * Runs AndroidX Benchmark without AGP's connected test wrapper.
 */
abstract class RunViewTreeBenchmarkTask : DefaultTask() {

    private companion object {

        const val SHELL_WORKAROUND_DIRECTORY = "/data/local/tmp/hikage-benchmark-bin"
        const val MIUI_BACKGROUND_ACTIVITY_OP = "10021"
        const val WRITE_EXTERNAL_STORAGE_OP = "WRITE_EXTERNAL_STORAGE"
        const val READ_EXTERNAL_STORAGE_OP = "READ_EXTERNAL_STORAGE"
        const val ROOT_COMMAND_TIMEOUT_MILLIS = 10_000L
        const val MANUAL_PERMISSION_WAIT_MILLIS = 15_000L
        const val MANUAL_PERMISSION_CHECK_INTERVAL_MILLIS = 1_000L

        val MIUI_PERMISSION_EDITOR_ACTIVITIES = listOf(
            "com.miui.permcenter.permissions.PermissionsEditorActivity",
            "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
        )
    }

    @get:Input
    abstract val targetPackageName: Property<String>

    @get:Input
    abstract val testPackageName: Property<String>

    @get:Input
    abstract val instrumentationRunner: Property<String>

    @get:Input
    abstract val suppressErrors: Property<String>

    @get:Input
    abstract val deviceOutputDirectory: Property<String>

    @get:Optional
    @get:Input
    abstract val suppressActivityMissing: Property<Boolean>

    @get:OutputDirectory
    abstract val localOutputDirectory: DirectoryProperty

    @get:OutputFile
    abstract val completionMarkerFile: RegularFileProperty

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun run() {
        val localDirectory = localOutputDirectory.asFile.get()
        val completionMarker = completionMarkerFile.asFile.get()
        val remoteDirectory = deviceOutputDirectory.get()
        val testPackage = testPackageName.get()
        val effectiveSuppressErrors = buildList {
            add(suppressErrors.get())
            if (suppressActivityMissing.getOrElse(false)) add("ACTIVITY-MISSING")
        }.joinToString(",")

        localDirectory.deleteRecursively()
        localDirectory.mkdirs()
        completionMarker.delete()

        runAdb("shell", "am", "force-stop", targetPackageName.get(), failOnError = false)
        runAdb("shell", "am", "force-stop", testPackage, failOnError = false)
        runAdb("shell", "rm", "-rf", remoteDirectory, failOnError = false)
        runAdb("shell", "mkdir", "-p", remoteDirectory)

        installShellWorkaround()
        logger.lifecycle("Running AndroidX Benchmark with a shell workaround to avoid rooted shell detection hangs.")
        ensureMiuiBackgroundActivityStartAllowed(testPackage)
        ensureExternalStoragePermissionAllowed(testPackage)

        val instrumentationArguments = listOf(
            "am",
            "instrument",
            "-w",
            "-r",
            "-e",
            "androidx.benchmark.output.enable",
            "true",
            "-e",
            "androidx.benchmark.profiling.mode",
            "none",
            "-e",
            "androidx.benchmark.suppressErrors",
            effectiveSuppressErrors,
            "-e",
            "additionalTestOutputDir",
            remoteDirectory,
            "$testPackage/${instrumentationRunner.get()}"
        )

        val instrumentationResult = runAdbWithShellWorkaround(instrumentationArguments)
        if (!instrumentationResult.output.contains("OK (")) error(
            "AndroidX Benchmark instrumentation did not finish successfully. " +
                "Exit code: ${instrumentationResult.exitCode}.\n${instrumentationResult.output}"
        )

        runAdb("pull", remoteDirectory, localDirectory.absolutePath)

        if (localDirectory.walkTopDown().none { it.isFile && it.name.endsWith("benchmarkData.json") })
            error("No AndroidX Benchmark JSON files were pulled from $remoteDirectory.")

        completionMarker.writeText("completed")
    }

    private fun installShellWorkaround() {
        runAdb(
            "shell",
            "mkdir",
            "-p",
            SHELL_WORKAROUND_DIRECTORY
        )
        runAdb(
            "shell",
            "sh",
            "-c",
            "echo '#!/system/bin/sh' > $SHELL_WORKAROUND_DIRECTORY/su && " +
                "echo 'exit 1' >> $SHELL_WORKAROUND_DIRECTORY/su && " +
                "chmod 755 $SHELL_WORKAROUND_DIRECTORY/su"
        )
    }

    private fun runAdbWithShellWorkaround(arguments: List<String>) =
        runAdb("shell", shellCommandWithWorkaround(arguments), failOnError = false)

    private fun shellCommandWithWorkaround(arguments: List<String>) =
        $$"PATH=$$SHELL_WORKAROUND_DIRECTORY:$PATH exec " + arguments.toShellCommand()

    private fun ensureMiuiBackgroundActivityStartAllowed(packageName: String) {
        if (!isMiuiDevice()) return
        if (isMiuiBackgroundActivityStartAllowed(packageName)) return

        logger.lifecycle("Detected MIUI/HyperOS. Granting benchmark background activity start permission for $packageName.")
        runAdb(
            "shell",
            "cmd",
            "appops",
            "set",
            packageName,
            MIUI_BACKGROUND_ACTIVITY_OP,
            "allow",
            failOnError = false
        )

        if (isMiuiBackgroundActivityStartAllowed(packageName)) return

        runAdb(
            "shell",
            "su",
            "-c",
            "cmd appops set $packageName $MIUI_BACKGROUND_ACTIVITY_OP allow",
            timeoutMillis = ROOT_COMMAND_TIMEOUT_MILLIS,
            failOnError = false
        )

        if (isMiuiBackgroundActivityStartAllowed(packageName)) return

        openMiuiPermissionEditor(packageName)
        logger.lifecycle(
            "MIUI/HyperOS blocked AndroidX Benchmark IsolationActivity. " +
                "Please allow background activity launch/pop-up windows for $packageName on the opened settings page."
        )

        val deadline = System.currentTimeMillis() + MANUAL_PERMISSION_WAIT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(MANUAL_PERMISSION_CHECK_INTERVAL_MILLIS)
            if (isMiuiBackgroundActivityStartAllowed(packageName)) return
        }

        error(
            "MIUI/HyperOS background activity launch permission is not allowed for $packageName.\n" +
                "AndroidX Benchmark needs to launch IsolationActivity from instrumentation. " +
                "Please enable the app permission named like \"Background pop-ups\" or \"Display pop-up windows while running in the background\", " +
                "then run benchmarkViewTreeReport again."
        )
    }

    private fun isMiuiDevice(): Boolean {
        val miuiVersion = runAdb("shell", "getprop", "ro.miui.ui.version.name", failOnError = false, logOutput = false)
            .output
            .trim()
        val hyperOsVersion = runAdb("shell", "getprop", "ro.mi.os.version.name", failOnError = false, logOutput = false)
            .output
            .trim()

        return miuiVersion.isNotEmpty() || hyperOsVersion.isNotEmpty()
    }

    private fun isMiuiBackgroundActivityStartAllowed(packageName: String): Boolean {
        val result = runAdb(
            "shell",
            "cmd",
            "appops",
            "get",
            packageName,
            MIUI_BACKGROUND_ACTIVITY_OP,
            failOnError = false,
            logOutput = false
        )

        return result.output.lines().any { line -> line.substringAfter(":").trim().startsWith("allow") }
    }

    private fun openMiuiPermissionEditor(packageName: String) {
        val opened = MIUI_PERMISSION_EDITOR_ACTIVITIES.any { activityName ->
            runAdb(
                "shell",
                "am",
                "start",
                "-a",
                "miui.intent.action.APP_PERM_EDITOR",
                "-n",
                "com.miui.securitycenter/$activityName",
                "-e",
                "extra_pkgname",
                packageName,
                failOnError = false
            ).exitCode == 0
        }

        if (!opened) runAdb(
            "shell",
            "am",
            "start",
            "-a",
            "android.settings.APPLICATION_DETAILS_SETTINGS",
            "-d",
            "package:$packageName",
            failOnError = false
        )
    }

    private fun ensureExternalStoragePermissionAllowed(packageName: String) {
        logger.lifecycle("Pre-granting legacy external storage access for AndroidX Benchmark on $packageName.")
        if (isRootShellAvailable()) {
            val rootResults = setExternalStoragePermissionAllowed(packageName) { command ->
                runAdb(
                    "shell",
                    "su",
                    "-c",
                    command.toShellCommand(),
                    timeoutMillis = ROOT_COMMAND_TIMEOUT_MILLIS,
                    failOnError = false
                )
            }
            if (rootResults.all { it.exitCode == 0 }) return
        }

        val shellResults = setExternalStoragePermissionAllowed(packageName) { command ->
            runAdb("shell", *command.toTypedArray(), failOnError = false, logOutput = false)
        }
        if (shellResults.all { it.exitCode == 0 }) return

        logger.warn(
            "Unable to pre-grant legacy external storage access for AndroidX Benchmark. " +
                "The benchmark will continue, but AndroidX Benchmark may fail before running tests on API 30+ devices."
        )
    }

    private fun isRootShellAvailable() =
        runAdb(
            "shell",
            "su",
            "-c",
            "id",
            timeoutMillis = ROOT_COMMAND_TIMEOUT_MILLIS,
            failOnError = false,
            logOutput = false
        ).output.contains("uid=0")

    private fun setExternalStoragePermissionAllowed(
        packageName: String,
        executor: (List<String>) -> CommandResult
    ): List<CommandResult> {
        val appOpsResults = listOf(WRITE_EXTERNAL_STORAGE_OP, READ_EXTERNAL_STORAGE_OP).map { operationName ->
            executor(listOf("cmd", "appops", "set", packageName, operationName, "allow"))
        }
        val permissionResults = listOf(
            "android.permission.$WRITE_EXTERNAL_STORAGE_OP",
            "android.permission.$READ_EXTERNAL_STORAGE_OP"
        ).map { permissionName ->
            // AndroidX Benchmark 1.4.1 still requests this legacy permission before each test.
            // On API 30+ it can only be force-granted from a privileged shell/root path.
            executor(listOf("pm", "grant", packageName, permissionName))
        }

        return appOpsResults + permissionResults
    }

    private fun runAdb(
        vararg arguments: String,
        prefixArguments: Array<String> = emptyArray(),
        timeoutMillis: Long = 0,
        failOnError: Boolean = true,
        logOutput: Boolean = true
    ): CommandResult {
        val command = listOf("adb") + prefixArguments + arguments
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = StringBuilder()
        val outputThread = thread(start = true, name = "adb-output-reader") {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (logOutput) logger.lifecycle(line)
                    output.appendLine(line)
                }
            }
        }
        val finished = if (timeoutMillis > 0) process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS) else {
            process.waitFor()
            true
        }

        if (!finished) {
            process.destroyForcibly()
            outputThread.join(1_000)
            if (failOnError) error("Command timed out: ${command.toShellCommand()}")
            return CommandResult(exitCode = -1, output = output.toString())
        }

        outputThread.join()

        val exitCode = process.exitValue()
        if (failOnError && exitCode != 0)
            error("Command failed with exit code $exitCode: ${command.toShellCommand()}\n$output")

        return CommandResult(exitCode, output.toString())
    }

    private fun List<String>.toShellCommand() = joinToString(" ") { it.singleQuote() }
    private fun String.singleQuote() = "'${replace("'", "'\"'\"'")}'"

    private data class CommandResult(val exitCode: Int, val output: String)
}

/**
 * Builds a human-friendly dashboard from AndroidX Benchmark JSON files.
 */
abstract class GenerateViewTreeBenchmarkReportTask : DefaultTask() {

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val benchmarkOutputDirectory: DirectoryProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @get:Internal
    abstract val completionMarkerFile: RegularFileProperty

    @get:Input
    abstract val openReport: Property<Boolean>

    @get:Internal
    private val jsonSlurper = JsonSlurper()

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generate() {
        val completionMarker = completionMarkerFile.asFile.get()
        if (!completionMarker.isFile) error("View-tree benchmark did not complete in this build. Report generation has been skipped.")

        val outputDirectory = benchmarkOutputDirectory.asFile.get()
        val benchmarkDataFiles = outputDirectory
            .takeIf(File::exists)
            ?.walkTopDown()
            ?.filter { it.isFile && it.name.endsWith("benchmarkData.json") }
            ?.toList()
            .orEmpty()

        if (benchmarkDataFiles.isEmpty())
            error("No AndroidX Benchmark JSON files found in ${outputDirectory.absolutePath}. Run benchmarkViewTreeReport first.")

        val sources = benchmarkDataFiles.mapNotNull(::parseBenchmarkSource)
        val benchmarks = sources.flatMap { source -> source.benchmarks.map { it.copy(source = source) } }
            .sortedWith(compareBy<BenchmarkEntry> { it.source.deviceName }.thenBy { it.displayOrder })

        if (benchmarks.isEmpty())
            error("No benchmark entries found in ${benchmarkDataFiles.joinToString { it.absolutePath }}.")

        val report = buildHtmlReport(sources, benchmarks)
        val targetFile = reportFile.asFile.get()
        targetFile.parentFile.mkdirs()
        targetFile.writeText(report)

        logger.lifecycle("Hikage benchmark report generated: ${targetFile.absolutePath}")
        if (openReport.get()) openReportFile(targetFile)
    }

    private fun openReportFile(file: File) {
        val reportUri = file.toURI().toString()
        val command = when {
            operatingSystemName.contains("mac") -> listOf("open", reportUri)
            operatingSystemName.contains("windows") -> listOf("rundll32", "url.dll,FileProtocolHandler", reportUri)
            else -> listOf("xdg-open", reportUri)
        }

        runCatching {
            ProcessBuilder(command).start()
        }.onSuccess {
            logger.lifecycle("Opening Hikage benchmark report in the default browser.")
        }.onFailure {
            logger.warn("Unable to open Hikage benchmark report automatically: ${it.message}")
        }
    }

    private fun parseBenchmarkSource(file: File): BenchmarkSource? {
        val root = jsonSlurper.parse(file) as? Map<*, *> ?: return null
        val context = root["context"] as? Map<*, *>
        val build = context?.get("build") as? Map<*, *>
        val version = build?.get("version") as? Map<*, *>
        val benchmarkItems = root["benchmarks"] as? List<*> ?: return null
        val source = BenchmarkSource(
            file = file,
            deviceName = listOfNotNull(build?.get("brand"), build?.get("model"))
                .joinToString(" ")
                .ifBlank { "Unknown device" },
            androidVersion = "API ${version?.get("sdk") ?: "-"}",
            benchmarkCount = benchmarkItems.size
        )

        return source.copy(
            benchmarks = benchmarkItems.mapNotNull { item ->
                val benchmark = item as? Map<*, *> ?: return@mapNotNull null
                val rawName = benchmark["name"]?.toString().orEmpty()
                val normalizedName = rawName.toBenchmarkName()
                val definition = BenchmarkDefinition.from(normalizedName) ?: return@mapNotNull null
                val metrics = benchmark["metrics"] as? Map<*, *> ?: emptyMap<Any, Any>()

                BenchmarkEntry(
                    source = source,
                    rawName = rawName,
                    name = normalizedName,
                    label = definition.label,
                    group = definition.group,
                    displayOrder = definition.displayOrder,
                    timeMetric = (metrics["timeNs"] as? Map<*, *>)?.toBenchmarkMetric(MetricUnit.NANOSECONDS),
                    allocationMetric = (metrics["allocationCount"] as? Map<*, *>)?.toBenchmarkMetric(MetricUnit.COUNT),
                    warmupIterations = (benchmark["warmupIterations"] as? Number)?.toInt(),
                    repeatIterations = (benchmark["repeatIterations"] as? Number)?.toInt()
                )
            }
        )
    }

    private fun Map<*, *>.toBenchmarkMetric(unit: MetricUnit) = BenchmarkMetric(
        unit = unit,
        minimum = (this["minimum"] as? Number)?.toDouble(),
        median = (this["median"] as? Number)?.toDouble(),
        maximum = (this["maximum"] as? Number)?.toDouble(),
        coefficientOfVariation = (this["coefficientOfVariation"] as? Number)?.toDouble()
    )

    private fun buildHtmlReport(sources: List<BenchmarkSource>, benchmarks: List<BenchmarkEntry>) = buildString {
        val generatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        appendLine("<!doctype html>")
        appendLine("<html lang=\"en\">")
        appendLine("<head>")
        appendLine("<meta charset=\"utf-8\">")
        appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
        appendLine("<title>Hikage View Tree Benchmark</title>")
        appendLine("<style>")
        appendLine(reportCss())
        appendLine("</style>")
        appendLine("</head>")
        appendLine("<body>")
        appendLine("<main>")
        appendLine("<header>")
        appendLine("<h1>XML vs Hikage View Tree Benchmark</h1>")
        appendLine("<div class=\"device-meta\">")
        appendLine("<span class=\"meta\">Merged from ${sources.size} benchmark JSON file(s)</span>")
        appendLine("<span class=\"meta-subtle\">${benchmarks.size} benchmark cases</span>")
        appendLine("</div>")
        appendLine("<div class=\"meta\">Generated at ${generatedAt.escapeHtml()}</div>")
        appendLine("</header>")
        appendLine(summaryCards(sources, benchmarks))
        appendLine(sourcePanel(sources))

        benchmarks.groupBy { it.source.file.absolutePath }.forEach { (_, entries) ->
            val source = entries.first().source
            appendLine("<h2>${source.deviceName.escapeHtml()}</h2>")
            appendLine("<div class=\"meta\">${source.androidVersion.escapeHtml()} · ${entries.size} cases</div>")
            appendLine(metricTable(entries))
            appendLine(ratioTable(entries))
        }

        appendLine("</main>")
        appendLine("</body>")
        appendLine("</html>")
    }

    private fun summaryCards(sources: List<BenchmarkSource>, benchmarks: List<BenchmarkEntry>) = buildString {
        val fastest = benchmarks.minByOrNull { it.timeMetric?.median ?: Double.MAX_VALUE }
        val slowest = benchmarks.maxByOrNull { it.timeMetric?.median ?: Double.MIN_VALUE }

        appendLine("<section class=\"summary\">")
        appendLine(summaryCard("Devices", sources.map { it.deviceName }.distinct().size.toString()))
        appendLine(summaryCard("Benchmarks", benchmarks.size.toString()))
        appendLine(summaryCard("Fastest median", fastest?.let { "${it.label}: ${it.timeMetric?.median.formatNsAsMs()} ms" } ?: "-"))
        appendLine(summaryCard("Slowest median", slowest?.let { "${it.label}: ${it.timeMetric?.median.formatNsAsMs()} ms" } ?: "-"))
        appendLine("</section>")
    }

    private fun summaryCard(label: String, value: String) =
        "<div><span>${label.escapeHtml()}</span><strong>${value.escapeHtml()}</strong></div>"

    private fun sourcePanel(sources: List<BenchmarkSource>) = buildString {
        appendLine("<section class=\"panel\">")
        appendLine("<h3>Source JSON</h3>")
        appendLine("<table>")
        appendLine("<thead><tr><th>Device</th><th>Cases</th><th>File</th></tr></thead>")
        appendLine("<tbody>")
        sources.forEach {
            appendLine(
                "<tr><td>${it.deviceName.escapeHtml()}</td><td>${it.benchmarkCount}</td><td class=\"path\">${it.file.absolutePath.escapeHtml()}</td></tr>"
            )
        }
        appendLine("</tbody>")
        appendLine("</table>")
        appendLine("</section>")
    }

    private fun metricTable(entries: List<BenchmarkEntry>) = buildString {
        appendLine("<section class=\"panel\">")
        appendLine("<h3>Metrics</h3>")
        appendLine("<table>")
        appendLine(
            "<thead><tr><th>Case</th><th>Group</th><th>Min</th><th>Median</th><th>Max</th>" +
                "<th>CV</th><th>Alloc Median</th><th>Warmup</th><th>Repeat</th></tr></thead>"
        )
        appendLine("<tbody>")
        entries.forEach {
            appendLine(
                "<tr><td>${it.label.escapeHtml()}</td><td>${it.group.label.escapeHtml()}</td>" +
                    "<td>${it.timeMetric?.minimum.formatNsAsMs()} ms</td>" +
                    "<td class=\"primary\">${it.timeMetric?.median.formatNsAsMs()} ms</td>" +
                    "<td>${it.timeMetric?.maximum.formatNsAsMs()} ms</td>" +
                    "<td>${it.timeMetric?.coefficientOfVariation.formatPercent()}</td>" +
                    "<td>${it.allocationMetric?.median.formatCount()}</td>" +
                    "<td>${it.warmupIterations ?: "-"}</td><td>${it.repeatIterations ?: "-"}</td></tr>"
            )
        }
        appendLine("</tbody>")
        appendLine("</table>")
        appendLine("</section>")
    }

    private fun ratioTable(entries: List<BenchmarkEntry>) = buildString {
        val rows = entries.groupBy { it.group }.flatMap { (group, groupEntries) ->
            val baseline = groupEntries.firstOrNull { it.groupBaselineName == group.baselineName } ?: return@flatMap emptyList()
            groupEntries.filterNot { it.name == baseline.name }.map { baseline to it }
        }

        if (rows.isEmpty()) return@buildString

        appendLine("<section class=\"panel\">")
        appendLine("<h3>XML Baseline Ratios</h3>")
        appendLine("<table>")
        appendLine(
            "<thead><tr><th>Case</th><th>Baseline</th><th>Median Time</th><th>Min Time</th>" +
                "<th>Allocations</th><th>Allocation Ratio</th></tr></thead>"
        )
        appendLine("<tbody>")
        rows.forEach { (baseline, current) ->
            appendLine(
                "<tr><td>${current.label.escapeHtml()}</td><td>${baseline.label.escapeHtml()}</td>" +
                    "<td class=\"primary\">${current.timeMetric?.median.ratioTo(baseline.timeMetric?.median)}</td>" +
                    "<td>${current.timeMetric?.minimum.ratioTo(baseline.timeMetric?.minimum)}</td>" +
                    "<td>${current.allocationMetric?.median.formatCount()}</td>" +
                    "<td class=\"primary\">${current.allocationMetric?.median.ratioTo(baseline.allocationMetric?.median)}</td></tr>"
            )
        }
        appendLine("</tbody>")
        appendLine("</table>")
        appendLine("</section>")
    }

    private val BenchmarkEntry.groupBaselineName get() = group.baselineName

    private fun String.toBenchmarkName() =
        BenchmarkDefinition.entries.firstOrNull { endsWith(it.benchmarkName) }?.benchmarkName ?: this

    private fun Double?.formatNsAsMs() = this?.let { "%.3f".format(Locale.ROOT, it / 1_000_000.0) } ?: "-"

    private fun Double?.formatCount() = this?.let { "%,.0f".format(Locale.ROOT, it) } ?: "-"

    private fun Double?.formatPercent() = this?.let { "%.2f%%".format(Locale.ROOT, it * 100) } ?: "-"

    private fun Double?.ratioTo(baseline: Double?) =
        if (this == null || baseline == null || baseline == 0.0) "-" else "%.2fx".format(Locale.ROOT, this / baseline)

    private fun String.escapeHtml() =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    private val operatingSystemName get() = System.getProperty("os.name").lowercase(Locale.ROOT)

    private fun reportCss() = """
        :root {
            color-scheme: light dark;
            --bg: #f6f7f9;
            --surface: #ffffff;
            --text: #1f2328;
            --muted: #667085;
            --line: #d9dee7;
            --accent: #006d77;
            --accent-soft: #e1f3f1;
        }
        @media (prefers-color-scheme: dark) {
            :root {
                --bg: #111418;
                --surface: #1a1f25;
                --text: #ecf0f4;
                --muted: #aab4c0;
                --line: #303844;
                --accent: #7dd3c7;
                --accent-soft: #173d3b;
            }
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            background: var(--bg);
            color: var(--text);
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            line-height: 1.5;
        }
        main {
            width: min(1180px, calc(100vw - 32px));
            margin: 0 auto;
            padding: 32px 0 48px;
        }
        header {
            margin-bottom: 24px;
        }
        h1 {
            margin: 0 0 8px;
            font-size: 30px;
            font-weight: 720;
            letter-spacing: 0;
        }
        h2 {
            margin: 28px 0 12px;
            font-size: 18px;
            letter-spacing: 0;
        }
        h3 {
            margin: 0 0 12px;
            font-size: 16px;
            letter-spacing: 0;
        }
        .meta {
            color: var(--muted);
            font-size: 14px;
        }
        .device-meta {
            display: flex;
            align-items: baseline;
            gap: 12px;
            flex-wrap: wrap;
        }
        .meta-subtle {
            color: color-mix(in srgb, var(--muted) 78%, transparent);
            font-size: 13px;
        }
        .panel {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 8px;
            padding: 18px;
            margin: 16px 0;
            overflow-x: auto;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
            gap: 12px;
        }
        .summary div {
            background: var(--accent-soft);
            border: 1px solid color-mix(in srgb, var(--accent) 35%, transparent);
            border-radius: 8px;
            padding: 12px;
        }
        .summary strong {
            display: block;
            font-size: 22px;
            color: var(--accent);
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        th, td {
            padding: 10px 12px;
            border-bottom: 1px solid var(--line);
            text-align: right;
            white-space: nowrap;
        }
        th:first-child, td:first-child { text-align: left; }
        th {
            color: var(--muted);
            font-weight: 650;
        }
        tr:last-child td { border-bottom: 0; }
        .primary { color: var(--accent); font-weight: 650; }
        .path {
            max-width: 700px;
            color: var(--muted);
            font-family: "SFMono-Regular", "Cascadia Code", monospace;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        """.trimIndent()

    private data class BenchmarkSource(
        val file: File,
        val deviceName: String,
        val androidVersion: String,
        val benchmarkCount: Int,
        val benchmarks: List<BenchmarkEntry> = emptyList()
    )

    private data class BenchmarkEntry(
        val source: BenchmarkSource,
        val rawName: String,
        val name: String,
        val label: String,
        val group: BenchmarkGroup,
        val displayOrder: Int,
        val timeMetric: BenchmarkMetric?,
        val allocationMetric: BenchmarkMetric?,
        val warmupIterations: Int?,
        val repeatIterations: Int?
    )

    private data class BenchmarkMetric(
        val unit: MetricUnit,
        val minimum: Double?,
        val median: Double?,
        val maximum: Double?,
        val coefficientOfVariation: Double?
    )

    private enum class MetricUnit {
        NANOSECONDS,
        COUNT
    }

    private enum class BenchmarkGroup(val label: String, val baselineName: String) {
        STRESS_210("210 View Tree", "xml210ViewTreeCreation"),
        DEMO("Demo Layout", "demoXmlLayoutCreation")
    }

    private enum class BenchmarkDefinition(
        val benchmarkName: String,
        val label: String,
        val group: BenchmarkGroup,
        val displayOrder: Int
    ) {
        XML_210("xml210ViewTreeCreation", "XML 210 views", BenchmarkGroup.STRESS_210, 0),
        HIKAGE_210("hikage210ViewTreeCreation", "Hikage 210 views", BenchmarkGroup.STRESS_210, 1),
        HIKAGE_210_ATTRS("hikage210ViewTreeWithAttrsCreation", "Hikage 210 views with attrs", BenchmarkGroup.STRESS_210, 2),
        HIKAGE_210_FULLY_ATTRS("hikage210ViewTreeWithFullyAttrsCreation", "Hikage 210 views fully attrs", BenchmarkGroup.STRESS_210, 3),
        HIKAGE_210_FULL_VIEW_ATTRS("hikage210ViewTreeWithFullViewAttrsCreation", "Hikage 210 views full view attrs", BenchmarkGroup.STRESS_210, 4),
        DEMO_XML("demoXmlLayoutCreation", "Demo XML layout", BenchmarkGroup.DEMO, 10),
        DEMO_HIKAGE("demoHikageLayoutCreation", "Demo Hikage layout", BenchmarkGroup.DEMO, 11);

        companion object {

            fun from(name: String) = entries.firstOrNull { it.benchmarkName == name }
        }
    }
}