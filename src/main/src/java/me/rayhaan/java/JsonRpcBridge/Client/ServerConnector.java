package me.rayhaan.java.JsonRpcBridge.Client;

import com.google.gson.JsonElement;

import java.io.*;
import java.util.concurrent.Future;

/**
 * Created by rayhaan on 11/21/13.
 */


public class ServerConnector {

    public static void main(String... args) throws Exception {
        ConnectorThread ct = new ConnectorThread("localhost", 3141);
        (new Thread(ct)).start();

        Future<JsonElement> result = ct.callMethod("TestImpl", "add", 1,2,3);
        JsonElement elem = result.get();
        System.out.println("Result" + elem);
    }


}
