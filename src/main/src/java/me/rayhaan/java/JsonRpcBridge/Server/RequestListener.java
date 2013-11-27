package me.rayhaan.java.JsonRpcBridge.Server;

import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by rayhaan on 11/26/13.
 */
public class RequestListener implements Runnable {

    JsonRpcBridge globalBridge;
    BufferedReader iStream;
    PrintWriter oStream;

    public RequestListener(JsonRpcBridge bridge, BufferedReader iStream, PrintWriter oStream) {
        this.globalBridge = bridge;
        this.iStream = iStream;
        this.oStream = oStream;
    }

    @Override
    public void run() {
        try {
            String input, output;

            /* Non-terminating */
            while (true) {
                input = readIn();
                if (input.equals("QUIT")) break;
                output = this.globalBridge.processRequest(input);
                System.out.println(output);
                this.writeString(output);
            }
            /* End Non-terminating */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String readIn() throws IOException {
        String data = iStream.readLine();
        if (data == null) throw new IOException("Connection closed!");
        return data;
    }

    private void writeString(String s) throws IOException {
        this.oStream.println(s);
        this.oStream.flush();
    }

}
