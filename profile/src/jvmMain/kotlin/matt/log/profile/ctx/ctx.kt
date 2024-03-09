package matt.log.profile.ctx

import matt.log.profile.real.Profiler


interface ProfilingContext {
    val profiler: Profiler
}

class ProfilingContextImpl(override val profiler: Profiler): ProfilingContext

