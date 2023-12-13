package matt.log.profile.jp

import com.jprofiler.api.controller.Controller
import com.jprofiler.api.controller.HeapDumpOptions
import com.jprofiler.api.controller.TrackingOptions
import matt.file.commons.TEMP_DIR
import matt.file.context.ProcessContext
import matt.file.toJioFile
import matt.lang.file.toJFile
import matt.lang.model.file.FsFile
import matt.lang.myPid
import matt.lang.optArray
import matt.lang.profiling.IsProfilingWithJProfiler
import matt.lang.shutdown.preaper.ProcessReaper
import matt.lang.unsafeErr
import matt.log.profile.real.GuiMode
import matt.log.profile.real.JpEnableAttachMode
import matt.log.profile.real.OfflineMode
import matt.log.profile.real.ProfilerEngine
import matt.shell.ShellVerbosity
import matt.shell.shell

private var attachedJProfilerProgrammaticallyAtRuntime = false


class JProfiler(
    private val snapshotFolder: FsFile
) : ProfilerEngine {

    companion object {
        fun defaultSnapshotFolder() = TEMP_DIR["jprofiler"]
        context(ProcessReaper)
        fun defaultSnapshotFileAction(snapshotFile: FsFile) = shell("open", snapshotFile.path)
    }

    context(ProcessReaper)
    override fun openSnapshot(file: FsFile) {
        defaultSnapshotFileAction(file)
    }

    override fun wasAttachedAtStartup(): Boolean {
        return IsProfilingWithJProfiler
    }

    override fun wasAttachedProgrammaticallyAtRuntime(): Boolean {
        return attachedJProfilerProgrammaticallyAtRuntime
    }


    context(ProcessContext, ProcessReaper)
    override fun attach(
        mode: JpEnableAttachMode,
    ): String {
        val r = shell(
            files.jpenable.path,
            "-n",
            "--pid=${myPid}" /*did not need to use pid before on heroku. But I probably should have. There is a risk of it choosing the wrong java process otherwise.*/,
            *when (mode) {
                is GuiMode -> arrayOf(
                    "--gui",
                    *optArray(mode.port) {
                        arrayOf(
                            "--port",
                            mode.port.toString()
                        )
                    }

                )

                is OfflineMode -> arrayOf(
                    "--offline",
                    "--config",
                    mode.config.path,
                    "--id",
                    mode.id.toString()
                )
            },
            verbosity = ShellVerbosity.STREAM
        )
        attachedJProfilerProgrammaticallyAtRuntime = true
        return r
    }

    override fun clearCpuDataAndStartCPURecording() {
        println("clearing CPU data...")
        println("starting CPU recording...")
        Controller.startCPURecording(
            true,
            TrackingOptions().kotlinCoroutines(true)
        )
        println("running op in CPU recording...")
    }

    override fun saveCpuSnapshot(): FsFile {
        var f: FsFile
        do {
            f = snapshotFolder["tmp-${System.currentTimeMillis()}-cpu.jps"]
        } while (f.toJioFile().exists())
        snapshotFolder.toJioFile().mkdirs()
        Controller.saveSnapshot(f.toJFile())
        return f
    }

    override fun stopCpuRecording() {
        Controller.stopCPURecording()
    }

    override fun captureMemorySnapshot(): FsFile {


        unsafeErr(
            """
            Heap dumps from the Controller Api is currently not working. See: https://stackoverflow.com/questions/77578680/how-to-save-heap-dump-from-controller-api
        """.trimIndent()
        )


        Controller.triggerHeapDump(
            HeapDumpOptions()
                .fullGc(true)
                .retainSoftReferences(false)
                .retainFinalizerReferences(false)
                .retainWeakReferences(false)
                .retainPhantomReferences(false)
                .primitiveData(true)
                .calculateRetainedSizes(true)
                .selectRecorded(true)
        )


        var f: FsFile
        do {
            f = snapshotFolder["tmp-${System.currentTimeMillis()}-mem.jps"]
        } while (f.toJioFile().exists())
        snapshotFolder.toJioFile().mkdirs()
        Controller.saveSnapshot(f.toJFile())


        return f

    }
}