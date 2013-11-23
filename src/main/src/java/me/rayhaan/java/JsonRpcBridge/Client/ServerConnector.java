package me.rayhaan.java.JsonRpcBridge.Client;

import java.io.*;
import java.util.concurrent.Future;

/**
 * Created by rayhaan on 11/21/13.
 */


public class ServerConnector {

    public static void main(String... args) throws Exception {
        ConnectorThread ct = new ConnectorThread("localhost", 3141);
        (new Thread(ct)).start();
        Future result = ct.callMethod("TestImpl", "method", new Object[] {"TEST"});
        System.out.println("Result" + result.get());
        System.exit(0);
    }


}
