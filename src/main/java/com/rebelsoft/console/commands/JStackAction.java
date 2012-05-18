package com.rebelsoft.console.commands;

/*
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
*/
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;

import java.lang.management.*;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 3/9/11
 * Time: 5:57 PM
 */
@Command(scope = "java", name = "jstack", description = "Displays thread dump")
public class JStackAction extends AbstractAction {
    @Override
    protected Object doExecute() throws Exception {
/*
        String pid = Utils.getPid();
        return threadDump(pid);
*/
        new ThreadMonitor().threadDump();
        return null;
    }

/*
    private static String threadDump(String pid) throws AttachNotSupportedException, IOException {
        StringBuilder builder = new StringBuilder();
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (int i = 0; i < list.size(); i++) {
            VirtualMachineDescriptor virtualMachineDescriptor = list.get(i);
            if (virtualMachineDescriptor.id().equals(pid)) {
                VirtualMachine vm = VirtualMachine.attach(virtualMachineDescriptor);
                InputStream in = ((HotSpotVirtualMachine) vm).remoteDataDump(new Object[0]);

                byte b[] = new byte[256];
                int n;
                do {
                    n = in.read(b);
                    if (n > 0) {
                        String s = new String(b, 0, n, "UTF-8");
                        builder.append(s);
                    }
                } while (n > 0);
                in.close();
                vm.detach();
            }

        }
        return builder.toString();
    }
*/

    public static void main(String[] args) throws Exception {
        System.out.println(new JStackAction().doExecute());
    }
}


class ThreadMonitor {

    private ThreadMXBean tmbean;

    /**
     * Constructs a ThreadMonitor object to get thread information in a remote
     * JVM.
     */
    public ThreadMonitor() {
        this.tmbean = ManagementFactory.getThreadMXBean();
    }


    /**
     * Prints the thread dump information to System.out.
     */
    public void threadDump() {
        if (tmbean.isObjectMonitorUsageSupported() && tmbean.isSynchronizerUsageSupported()) {
            // Print lock info if both object monitor usage
            // and synchronizer usage are supported.
            // This sample code can be modified to handle if
            // either monitor usage or synchronizer usage is supported.
            dumpThreadInfoWithLocks();
        } else {
            dumpThreadInfo();
        }

        if (!findDeadlock()) {
            System.out.println("No deadlocks found.");
        }

    }

    private void dumpThreadInfo() {
        System.out.println("Full Java thread dump");
        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        for (ThreadInfo ti : tinfos) {
            printThreadInfo(ti);
        }
    }

    /**
     * Prints the thread dump information with locks info to System.out.
     */
    private void dumpThreadInfoWithLocks() {
        System.out.println("Full Java thread dump with locks info");

        ThreadInfo[] tinfos = tmbean.dumpAllThreads(true, true);
        for (ThreadInfo ti : tinfos) {
            printThreadInfo(ti);
            LockInfo[] syncs = ti.getLockedSynchronizers();
            printLockInfo(syncs);
        }
        System.out.println();
    }

    private static String INDENT = "    ";

    private void printThreadInfo(ThreadInfo ti) {
        // print thread information
        printThread(ti);

        // print stack trace with locks
        StackTraceElement[] stacktrace = ti.getStackTrace();
        MonitorInfo[] monitors = ti.getLockedMonitors();
        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement ste = stacktrace[i];
            System.out.println(INDENT + "at " + ste.toString());
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == i) {
                    System.out.println(INDENT + "  - locked " + mi);
                }
            }
        }
        System.out.println();
    }

    private void printThread(ThreadInfo ti) {
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" + " Id="
                + ti.getThreadId() + " in " + ti.getThreadState());
        if (ti.getLockName() != null) {
            sb.append(" on lock=" + ti.getLockName());
        }
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        System.out.println(sb.toString());
        if (ti.getLockOwnerName() != null) {
            System.out.println(INDENT + " owned by " + ti.getLockOwnerName() + " Id="
                    + ti.getLockOwnerId());
        }
    }

    private void printMonitorInfo(ThreadInfo ti, MonitorInfo[] monitors) {
        System.out.println(INDENT + "Locked monitors: count = " + monitors.length);
        for (MonitorInfo mi : monitors) {
            System.out.println(INDENT + "  - " + mi + " locked at ");
            System.out.println(INDENT + "      " + mi.getLockedStackDepth() + " "
                    + mi.getLockedStackFrame());
        }
    }

    private void printLockInfo(LockInfo[] locks) {
        System.out.println(INDENT + "Locked synchronizers: count = " + locks.length);
        for (LockInfo li : locks) {
            System.out.println(INDENT + "  - " + li);
        }
        System.out.println();
    }

    /**
     * Checks if any threads are deadlocked. If any, print the thread dump
     * information.
     */
    public boolean findDeadlock() {
        long[] tids;
        if (tmbean.isSynchronizerUsageSupported()) {
            tids = tmbean.findDeadlockedThreads();
            if (tids == null) {
                return false;
            }

            System.out.println("Deadlock found :-");
            ThreadInfo[] infos = tmbean.getThreadInfo(tids, true, true);
            for (ThreadInfo ti : infos) {
                printThreadInfo(ti);
                printLockInfo(ti.getLockedSynchronizers());
                System.out.println();
            }
        } else {
            tids = tmbean.findMonitorDeadlockedThreads();
            if (tids == null) {
                return false;
            }
            ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
            for (ThreadInfo ti : infos) {
                // print thread information
                printThreadInfo(ti);
            }
        }

        return true;
    }

}

