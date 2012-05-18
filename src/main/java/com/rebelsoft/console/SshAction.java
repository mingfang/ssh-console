/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.rebelsoft.console;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.common.util.NoCloseInputStream;
import org.apache.sshd.common.util.NoCloseOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Connect to a SSH server.
 *
 */
@Command(scope = "ssh", name = "ssh", description = "Connects to a remote SSH server")
public class SshAction extends AbstractAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(name="-l", aliases={"--username"}, description = "The user name for remote login", required = false, multiValued = false)
    private String username;

    @Option(name="-P", aliases={"--password"}, description = "The password for remote login", required = false, multiValued = false)
    private String password;

    @Option(name="-p", aliases={"--port"}, description = "The port to use for SSH connection", required = false, multiValued = false)
    private int port = 22;

    @Argument(index = 0, name = "hostname", description = "The host name to connect to via SSH", required = true, multiValued = false)
    private String hostname;

    @Argument(index = 1, name = "command", description = "Optional command to execute", required = false, multiValued = true)
    private List<String> command;

	private ClientSession session;
    private String sshClientId;


    public void setSshClientId(String sshClientId) {
        this.sshClientId = sshClientId;
    }

    @Override
    protected Object doExecute() throws Exception {

        //
        // TODO: Parse hostname for <username>@<hostname>
        //

        System.out.println("Connecting to host " + hostname + " on port " + port);

        username = "ming";
        password = "";
        
        // If the username/password was not configured via cli, then prompt the user for the values
        if (username == null || password == null) {
            log.debug("Prompting user for credentials");
            if (username == null) {
                username = readLine("Login: ");
            }
            if (password == null) {
                password = readLine("Password: ");
            }
        }

        // Create the client from prototype
        SshClient client = SshClient.setUpDefaultClient();
        log.debug("Created client: {}", client);
        client.start();

        try {
            ConnectFuture future = client.connect(hostname, port);
            future.await();
            session = future.getSession();
            try {
                System.out.println("Connected");

                session.authPassword(username, password);
                int ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
                if ((ret & ClientSession.CLOSED) != 0) {
                    System.err.println("error");
                    System.exit(-1);
                }
                if ((ret & ClientSession.AUTHED) == 0) {
                    System.err.println("Authentication failed");
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                if (command != null) {
                    for (String cmd : command) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append(cmd);
                    }
                }

                ClientChannel channel = session.createChannel("shell");
                if (sb.length() > 0) {
                    channel = session.createChannel("exec", sb.append("\n").toString());
                    channel.setIn(new ByteArrayInputStream(new byte[0]));
                } else {
                    channel = session.createChannel("shell");
                    channel.setIn(new NoCloseInputStream(System.in));
                    ((ChannelShell) channel).setupSensibleDefaultPty();
                }
                channel.setOut(new NoCloseOutputStream(System.out));
                channel.setErr(new NoCloseOutputStream(System.err));
                channel.open();
                channel.waitFor(ClientChannel.CLOSED, 0);
            } finally {
                session.close(false);
            }
        } finally {
            client.stop();
        }

        return null;
    }

    public String readLine(String msg) throws IOException {
        StringBuffer sb = new StringBuffer();
        System.err.print(msg);
        System.err.flush();
        for (;;) {
            int c = super.session.getKeyboard().read();
            if (c < 0) {
                return null;
            }
            System.err.print((char) c);
            if (c == '\r' || c == '\n') {
                break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

}
