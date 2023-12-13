package matt.log.profile.jp.testprogram

import com.jprofiler.api.controller.Controller
import com.jprofiler.api.controller.HeapDumpOptions
import matt.lang.file.toJFile
import matt.lang.model.file.FsFile
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

/*
Official Test Program. Modified only in a couple minor ways:
1. Minor refactoring that have no functional affect
2. Allow specifying a root folder for saved snapshots
*/

// This class shows how to use the offline profiling API in the Controller class.
// Please see the "Offline profiling" topic in the manual for a systematic discussion of this feature.
class TestProgram(
    val triggerHeapDumps: Boolean,
    val folder: FsFile,
) {
    companion object {
        private const val COUNT = 100000
    }


    // These lists hold objects to illustrate memory profiling
    private val sines: MutableList<Double> = ArrayList(COUNT)
    private val squareRoots: MutableList<Double> = ArrayList(COUNT)
    private val logs: MutableList<Double> = ArrayList(COUNT)
    fun main() {


        // On startup, JProfiler does not record any data. The various recording subsystems have to be
        // switched on programatically.
        Controller.startCPURecording(true)
        Controller.startAllocRecording(true)
        Controller.startThreadProfiling()
        Controller.startVMTelemetryRecording()

        // This is observer method
        calculateStuff()

        // You can switch off recording at any point. Recording can be switched on again.
        Controller.stopCPURecording()
        Controller.stopAllocRecording()
        Controller.stopThreadProfiling()
        Controller.stopVMTelemetryRecording()
    }

    private fun calculateStuff() {

        // Bookmarks can be added with the API.
        Controller.addBookmark("Start calculating sines")
        calculateSines()
        if (triggerHeapDumps) {
            // If you would like to use the heap walker for saved snapshots, you have to trigger a heap dump at some point.
            // The last heap dump will be saved in the snapshot file. This makes snapshot files much larger and creates
            // significant memory overhead.
            Controller.triggerHeapDump(HeapDumpOptions.SELECT_RECORDED)
        }
        // Now we save a snapshot with all recorded profiling data
        Controller.saveSnapshot(folder["after_sines.jps"].toJFile())

        // The same sequence with a different method
        Controller.addBookmark("Start calculating square roots")
        calculateSquareRoots()
        if (triggerHeapDumps) {
            Controller.triggerHeapDump(HeapDumpOptions.SELECT_RECORDED)
        }
        Controller.saveSnapshot(folder["after_square_roots.jps"].toJFile())

        // And a third time
        Controller.addBookmark("Start calculating logs")
        calculateLogs()
        if (triggerHeapDumps) {
            Controller.triggerHeapDump(HeapDumpOptions.SELECT_RECORDED)
        }
        Controller.saveSnapshot(folder["after_logs.jps"].toJFile())
    }

    private fun calculateSines() {
        val increment = Math.PI / 2 / COUNT
        for (i in 0..<COUNT) {
            sines.add(sin(increment * i))
        }
    }

    private fun calculateSquareRoots() {
        for (i in 0..<COUNT) {
            squareRoots.add(sqrt(i.toDouble()))
        }
    }

    private fun calculateLogs() {
        for (i in 0..<COUNT) {
            logs.add(ln((i + 1).toDouble()))
        }
    }
}
