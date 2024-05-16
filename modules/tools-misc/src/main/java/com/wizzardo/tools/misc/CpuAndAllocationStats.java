package com.wizzardo.tools.misc;

import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;

public class CpuAndAllocationStats {

    public static final boolean CPU_TIME_TRACING_SUPPORTED;
    public static final boolean ALLOCATION_TRACING_SUPPORTED;
    protected static final ThreadLocal<CpuAndAllocationStats> THREAD_LOCAL = ThreadLocal.withInitial(CpuAndAllocationStats::new);

    static {
        ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        CPU_TIME_TRACING_SUPPORTED = threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled();
        ALLOCATION_TRACING_SUPPORTED = threadMXBean.isThreadAllocatedMemorySupported() && threadMXBean.isThreadAllocatedMemoryEnabled();
    }

    protected final long threadId;
    protected long cpuTime;
    protected long cpuUserTime;
    protected long allocation;
    protected ThreadMXBean threadMXBean;

    public static CpuAndAllocationStats get() {
        return THREAD_LOCAL.get();
    }

    public CpuAndAllocationStats() {
        this(Thread.currentThread());
    }

    public CpuAndAllocationStats(Thread thread) {
        threadId = thread.getId();
        threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    }

    public void reset() {
        if (CPU_TIME_TRACING_SUPPORTED) {
            cpuTime = getTotalCpuTime();
            cpuUserTime = getTotalCpuUserTime();
        }
        if (ALLOCATION_TRACING_SUPPORTED) {
            allocation = getTotalAllocation();
        }
    }

    public long getCountedCpuTime() {
        return getTotalCpuTime() - cpuTime;
    }

    public long getTotalCpuTime() {
        if (!CPU_TIME_TRACING_SUPPORTED)
            return 0;

        return threadMXBean.getThreadCpuTime(threadId);
    }

    public long getCountedCpuUserTime() {
        return getTotalCpuUserTime() - cpuUserTime;
    }

    public long getTotalCpuUserTime() {
        if (!CPU_TIME_TRACING_SUPPORTED)
            return 0;

        return threadMXBean.getThreadUserTime(threadId);
    }

    public long getCountedAllocation() {
        return getTotalAllocation() - allocation;
    }

    public long getTotalAllocation() {
        if (!ALLOCATION_TRACING_SUPPORTED)
            return 0;

        return threadMXBean.getThreadAllocatedBytes(threadId);
    }
}
