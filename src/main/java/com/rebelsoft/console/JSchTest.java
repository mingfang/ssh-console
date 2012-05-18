package com.rebelsoft.console;

import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.exception.JSchException;

/**
 * Created by IntelliJ IDEA.
 * User: mingfang
 * Date: 3/24/11
 * Time: 3:44 PM
 */
public class JSchTest {
    public static void main(String[] args) throws JSchException {
        JSch jSch = JSch.getInstance();
        jSch.setKnownHosts("/tmp/knowhosts");
        Session session = jSch.createSession("mingfang", "localhost");
    }
}
