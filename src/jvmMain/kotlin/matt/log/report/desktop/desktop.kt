package matt.log.report.desktop

import matt.lang.common.disabledCode
import matt.lang.j.NUM_LOGICAL_CORES
import matt.lang.j.RUNTIME
import matt.lang.j.myPid
import matt.lang.platform.arch.ARCH
import matt.lang.platform.os.OS
import matt.log.report.VersionGetterService
import matt.model.code.errreport.common.Report
import matt.model.code.errreport.j.ThrowReport
import matt.model.data.byte.ByteSize
import matt.prim.str.mybuild.api.lineDelimitedString
import matt.prim.str.mybuild.api.string
import matt.service.j.loadServiceOrNull
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage


class BugReport(
    t: Thread?,
    e: Throwable?
) : Report() {

    private val memReport = MemReport()
    private val throwReport =
        ThrowReport(
            t, e
        )
    private val sysReport = SystemReport()
    override val text by lazy {
        disabledCode {
            @Suppress("UNUSED_VARIABLE") val v = loadServiceOrNull<VersionGetterService>()?.getTheVersion()
        }
        val v = "Not getting version because my VersionGetterService implementation now needs an argument with a type that the module that contains VersionGetterService cannot resolve... the whole thing needs refactoring"
        string {
            lineDelimited {
                +"VERSION: $v"

                +"PID: $myPid"
                blankLine()
                +"SYSTEM REPORT"
                +sysReport
                blankLine()
                +"RAM REPORT"
                +memReport
                blankLine()
                +"THROW REPORT"
                +throwReport
            }
        }
    }
}

class SystemReport {

    override fun toString(): String = text

    val text by lazy {
        """
        OS: $OS
        ARCH: $ARCH
        CPUS: $NUM_LOGICAL_CORES
        """.trimIndent()
    }
}

class MemReport : Report() {

    val total = ByteSize(RUNTIME.totalMemory())
    val max = ByteSize(RUNTIME.maxMemory())
    val free = ByteSize(RUNTIME.freeMemory())

    private val memBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()

    private val heap: MemoryUsage = memBean.heapMemoryUsage
    private val nonHeap: MemoryUsage = memBean.nonHeapMemoryUsage

    val used by lazy {
        total - free
    }
    val spaceToGrow by lazy {
        max - used
    }

    override val text by lazy {
        lineDelimitedString {
            +"heapsize:\t$total"
            +"heapmaxsize:\t$max"
            +"heapFreesize:\t$free"
            +"heap:\t$heap"
            +"nonHeap:\t$nonHeap"
        }
    }
}




