package com.rebelsoft.console.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;

import java.lang.management.ManagementFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 3/9/11
 * Time: 4:59 PM
 */
@Command(scope = "*", name = "pid", description = "Displays this pid")
public class PidAction extends AbstractAction {
    @Override
    protected Object doExecute() throws Exception {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.substring(0, name.indexOf("@"));
        return pid;
    }

}
