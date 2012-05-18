package com.rebelsoft.console;

import org.apache.log4j.BasicConfigurator;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.ConnectFuture;

public class SshClientTest {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();

        String host = "localhost";
        int port = 22;
        String user = "ming";
        String password = "";

        SshClient client = null;
        try {
            client = SshClient.setUpDefaultClient();
            client.start();
            ConnectFuture future = client.connect(host, port);
            future.await();
            ClientSession session = future.getSession();

            //auth
            session.authPassword(user, password);
            int ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
            if ((ret & ClientSession.WAIT_AUTH) != 0) {
                System.err.println("login failed");
                System.exit(-1);
            }
            if ((ret & ClientSession.CLOSED) != 0) {
                System.err.println("error");
                System.exit(-1);
            }

            ClientChannel channel = session.createChannel("shell");
            channel.setIn(System.in);
            channel.setOut(System.out);
            channel.setErr(System.err);
            channel.open();
            channel.waitFor(ClientChannel.CLOSED, 0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        } finally {
            try {
                client.stop();
            } catch (Throwable t) {
            }
        }
        System.exit(0);
    }

}