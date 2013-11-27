package me.rayhaan.java.JsonRpcBridge.Client;

import com.google.gson.JsonElement;

import java.io.*;
import java.util.concurrent.Future;

/**
 * Created by rayhaan on 11/21/13.
 */


public class ServerConnector {

    public void processBounce(String ss) {
        System.out.println("Got Bounce!" + ss);
    }

    public void testServerConnecion() throws Exception {
        ConnectorThread ct = new ConnectorThread("localhost", 3141);
        (new Thread(ct)).start();

        ct.registerPushListener("push", this, this.getClass().getDeclaredMethod("managePush", String.class));
        Future<JsonElement> result = ct.callMethod("TestImpl", "throwException");

        ct.registerPushListener("bounce", this, this.getClass().getDeclaredMethod("processBounce", String.class));

        Future<JsonElement> res2 = ct.callMethod("TestImpl", "bounce", "bounce", "Lorem ipsum dolor");
        try {
            JsonElement elem = result.get();
            System.out.println("Result" + elem);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String... args) throws Exception {
        (new ServerConnector()).testServerConnecion();
    }


}
