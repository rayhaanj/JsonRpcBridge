package me.rayhaan.java.JsonRpcBridge.Client;

import com.google.gson.JsonElement;

import java.awt.*;
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

        ct.registerPushListener("bounce", this, this.getClass().getDeclaredMethod("processBounce", String.class));

        Future<JsonElement> res2 = ct.callMethod("TestImpl", "customObjectTest", new Point(1,2));

        try {
            JsonElement elem = res2.get();
            System.out.println("Result" + elem);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void main(String... args) throws Exception {
        (new ServerConnector()).testServerConnecion();
    }


}
