package com.rebelsoft.console.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer;

import java.lang.management.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import static com.rebelsoft.console.commands.Formatter.*;


/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 3/9/11
 * Time: 8:10 PM
 */
@Command(scope = "java", name = "vmsummary", description = "Displays VM summary")
public class VmSummaryAction extends AbstractAction {

    public static void main(String[] args1) {
        AnsiConsole.systemInstall();
        System.out.println(getVmSummary());
    }

    private static String getVmSummary() {
        StringBuilder result = new StringBuilder(AnsiRenderer.render("@|bold VM Summary\n|@"));
        seperator(result);

        RuntimeMXBean rmBean = ManagementFactory.getRuntimeMXBean();
        CompilationMXBean cmpMBean = ManagementFactory.getCompilationMXBean();
        ThreadMXBean tmBean = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean clMBean = ManagementFactory.getClassLoadingMXBean();
        OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunOSMBean = null;
        if (osMBean instanceof com.sun.management.OperatingSystemMXBean) {
            sunOSMBean = (com.sun.management.OperatingSystemMXBean) osMBean;
        }
        Utils.append(result, "Virtual Machine", rmBean.getVmName(), "version", rmBean.getVmVersion());
        Utils.append(result, "Vendor", rmBean.getVmVendor());
        Utils.append(result, "Name", rmBean.getName());
        Utils.append(result, "Uptime", formatTime(rmBean.getUptime()));
        if (sunOSMBean != null) {
            long processCpuTime = sunOSMBean.getProcessCpuTime();
            Utils.append(result, "Process CPU time", formatNanoTime(processCpuTime));
        }
        Utils.append(result, "JIT compiler", cmpMBean.getName());
        Utils.append(result, "Total compile time", cmpMBean.isCompilationTimeMonitoringSupported()
                ? formatTime(cmpMBean.getTotalCompilationTime()) : "Unavailable");

        seperator(result);

        Utils.append(result, "Live Threads", tmBean.getThreadCount());
        Utils.append(result, "Peak", tmBean.getPeakThreadCount());
        Utils.append(result, "Daemon threads", tmBean.getDaemonThreadCount());
        Utils.append(result, "Total threads started", tmBean.getTotalStartedThreadCount());

        Utils.append(result, "Current classes loaded", clMBean.getLoadedClassCount());
        Utils.append(result, "Total classes loaded", clMBean.getTotalLoadedClassCount());
        Utils.append(result, "Total classes unloaded", clMBean.getUnloadedClassCount());

        seperator(result);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage u = memoryBean.getHeapMemoryUsage();
        String[] strings1 = formatKByteStrings(u.getUsed(), u.getMax());
        Utils.append(result, "Current heap size", strings1[0]);
        Utils.append(result, "Maximum heap size", strings1[1]);
        Utils.append(result, "Committed memory", formatKByteStrings(u.getCommitted())[0]);
        Utils.append(result, "Pending finalization", memoryBean.getObjectPendingFinalizationCount());

        Collection<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbageCollectorMBean : garbageCollectors) {
            String gcName = garbageCollectorMBean.getName();
            long gcCount = garbageCollectorMBean.getCollectionCount();
            long gcTime = garbageCollectorMBean.getCollectionTime();
            Utils.append(result, "Garbage collector", "Name =", gcName, ", Collections =", gcCount, ", Total time spent =",
                    (gcTime >= 0) ? formatTime(gcTime) : "Unavailable");
        }

        seperator(result);

        String osName = osMBean.getName();
        String osVersion = osMBean.getVersion();
        String osArch = osMBean.getArch();
        int nCPUs = osMBean.getAvailableProcessors();
        Utils.append(result, "Operating System", osName + " " + osVersion);
        Utils.append(result, "Architecture", osArch);
        Utils.append(result, "Number of processors", nCPUs);

        if (sunOSMBean != null) {
            String[] kbStrings1 = formatKByteStrings(sunOSMBean.getCommittedVirtualMemorySize());

            String[] kbStrings2 = formatKByteStrings(
                    sunOSMBean.getTotalPhysicalMemorySize(),
                    sunOSMBean.getFreePhysicalMemorySize(),
                    sunOSMBean.getTotalSwapSpaceSize(),
                    sunOSMBean.getFreeSwapSpaceSize()
            );

            Utils.append(result, "Committed virtual memory", kbStrings1[0]);
            Utils.append(result, "Total physical memory", kbStrings2[0]);
            Utils.append(result, "Free physical memory", kbStrings2[1]);
            Utils.append(result, "Total swap space", kbStrings2[2]);
            Utils.append(result, "Free swap space", kbStrings2[3]);
        }
        seperator(result);

        StringBuilder args = new StringBuilder("");
        java.util.List<String> inputArguments = rmBean.getInputArguments();
        for (String arg : inputArguments) {
            args.append(arg).append(" ");
        }
        Utils.append(result, "VM arguments", args.toString());
        seperator(result);

        Utils.append(result, "Class path", rmBean.getClassPath());
        seperator(result);

        Utils.append(result, "Library path", rmBean.getLibraryPath(), 4);
        seperator(result);

        Utils.append(result, "Boot class path",
                rmBean.isBootClassPathSupported() ?
                        rmBean.getBootClassPath() : "Unavailable");

        return result.toString();
    }

    private static void seperator(StringBuilder result) {
//        result.append(AnsiRenderer.render("@|underline                                            |@"));
        result.append("\n");
    }

    static DateFormat dateFormat = new SimpleDateFormat();


    @Override
    protected Object doExecute() throws Exception {
        return getVmSummary();
    }
}
