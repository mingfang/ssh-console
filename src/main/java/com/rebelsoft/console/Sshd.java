package com.rebelsoft.console;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.basic.AbstractCommand;
import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.gogo.runtime.threadio.ThreadIOImpl;
import org.apache.felix.service.command.Function;
import org.apache.karaf.shell.commands.ClearAction;
import org.apache.karaf.shell.commands.HistoryAction;
import org.apache.karaf.shell.commands.LogoutAction;
import org.apache.karaf.shell.console.HelpAction;
import org.apache.karaf.shell.ssh.ShellCommandFactory;
import org.apache.karaf.shell.ssh.ShellFactoryImpl;
import org.apache.karaf.shell.ssh.SshServerFactory;
import org.apache.log4j.Logger;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: mingfang
 * Date: 3/8/11
 * Time: 1:19 PM
 */
public class Sshd {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = Logger.getLogger(Sshd.class);

    private static final int DEFAULT_PORT = 8222;
    private int port = DEFAULT_PORT;

    private CommandProcessorImpl commandProcessor;

// --------------------------- CONSTRUCTORS ---------------------------

    public Sshd() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "unknown";
        }
        System.setProperty("karaf.name", hostName);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

// -------------------------- OTHER METHODS --------------------------

    public void autoRegister(String pkg) throws IOException, ClassNotFoundException {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(pkg)))
                        .setUrls(ClasspathHelper.forClassLoader())
                        .setScanners(new TypeAnnotationsScanner())
        );
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Command.class);
        for (Class<?> aClass : annotated) {
            registerActionClass((Class<? extends Action>) aClass);
        }
    }

    public void registerActionInstance(final Action action) {
        final Class actionClass = action.getClass();
        Command cmd = (Command) actionClass.getAnnotation(Command.class);
        Function function = new AbstractCommand() {
            @Override
            public Action createNewAction() {
                return action;
            }
        };
        commandProcessor.addCommand(cmd.scope(), function, cmd.name());
    }

    public void start() throws IOException, ClassNotFoundException {
        ThreadIOImpl threadIO = new ThreadIOImpl();
        threadIO.start();
        commandProcessor = new CommandProcessorImpl(threadIO);

        SshServer defaultSshServer = SshServer.setUpDefaultServer();
        defaultSshServer.setPort(port);

        //auth
        List<NamedFactory<UserAuth>> factories = new ArrayList<NamedFactory<UserAuth>>();
        factories.add(new UserAuthNone.Factory());
        defaultSshServer.setUserAuthFactories(factories);
        defaultSshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("/tmp/keys"));

        ShellCommandFactory commandFactory = new ShellCommandFactory();
        commandFactory.setCommandProcessor(commandProcessor);
        defaultSshServer.setCommandFactory(commandFactory);

        ShellFactoryImpl shellFactory = new ShellFactoryImpl();
        shellFactory.setCommandProcessor(commandProcessor);
        defaultSshServer.setShellFactory(shellFactory);

        SshServerFactory sshServerFactory = new SshServerFactory(defaultSshServer);
        sshServerFactory.setStart(true);
        sshServerFactory.start();
        registerDefaultActions();
    }

    private void registerDefaultActions() throws IOException, ClassNotFoundException {
        registerActionClass(HelpAction.class);
        registerActionClass(ClearAction.class);
        registerActionClass(HistoryAction.class);
        registerActionClass(LogoutAction.class);
        registerActionClass(SshAction.class);
    }

    public void registerActionClass(final Class<? extends Action> actionClass) {
        LOGGER.info("Registering action:" + actionClass.getSimpleName());
        Command cmd = actionClass.getAnnotation(Command.class);
        Function function = new AbstractCommand() {
            @Override
            public Action createNewAction() {
                try {
                    return ((Class<? extends Action>) actionClass).newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        commandProcessor.addCommand(cmd.scope(), function, cmd.name());
    }

    public void stop() {

    }
}
