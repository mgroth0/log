package matt.log.report

import matt.lang.NUM_LOGICAL_CORES
import matt.lang.RUNTIME
import matt.lang.myPid
import matt.lang.platform.ARCH
import matt.lang.platform.OS
import matt.model.code.errreport.Report
import matt.model.code.errreport.ThrowReport
import matt.model.data.byte.ByteSize
import matt.prim.str.mybuild.string
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface VersionGetterService {

    fun getTheVersion(): String
}

class BugReport(
    t: Thread?,
    e: Throwable?
) : Report() {

    private val memReport = MemReport()
    private val throwReport = ThrowReport(
        t,
        e
    )
    private val sysReport = SystemReport()
    override val text by lazy {
        val serviceLoader = ServiceLoader.load(VersionGetterService::class.java)
        val v = serviceLoader.findFirst().map { it.getTheVersion() }.getOrNull()
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

    override fun toString(): String {
        return text
    }

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

    val used by lazy {
        total - free
    }
    val spaceToGrow by lazy {
        max - used
    }

    override val text by lazy {
        var s = ""
        s += "heapsize:${total}\n"
        s += "heapmaxsize:${max}\n"
        s += "heapFreesize:${free}"
        s
    }
}


