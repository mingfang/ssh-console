package com.rebelsoft.console.commands.log4j;

import com.rebelsoft.console.commands.Utils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.CompleterValues;
import org.apache.karaf.shell.console.AbstractAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mingfang
 * Date: 3/10/11
 * Time: 11:45 AM
 */
@Command(scope = "log4j", name = "loggers", description = "Displays all loggers")
public class ListLoggersAction extends AbstractAction {

    @Argument(index = 0, name = "logger", description = "logger name to list", required = false, multiValued = false)
    String name;

    @Override
    protected Object doExecute() throws Exception {
        return listLoggers();
    }

    private String listLoggers() {
        StringBuilder builder = new StringBuilder("");
        Enumeration currentLoggers = LogManager.getCurrentLoggers();
        while (currentLoggers.hasMoreElements()) {
            Logger logger = (Logger) currentLoggers.nextElement();
            if (name != null && !name.equals(logger.getName())) {
                continue;
            }
            builder.append(Utils.ansi("bold", logger.getName()))
                    .append(" Level: ").append(Utils.ansi("bold", String.valueOf(logger.getLevel())))
                    .append(" Effective Level: ").append(Utils.ansi("bold", String.valueOf(logger.getEffectiveLevel())))
                    .append("\n");
        }
        return builder.toString();
    }

    @CompleterValues(index = 0)
    public String[] loggers(){
        List<String> loggers = new ArrayList<String>();
        Enumeration currentLoggers = LogManager.getCurrentLoggers();
        while (currentLoggers.hasMoreElements()) {
            Logger logger = (Logger) currentLoggers.nextElement();
            loggers.add(logger.getName());
        }
        return loggers.toArray(new String[loggers.size()]);
    }
}

