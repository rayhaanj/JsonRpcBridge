package me.rayhaan.java.JsonRpcBridge.Client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by rayhaan on 11/23/13.
 */
public class ServerEventListener implements Runnable {

    ConnectorThread connectorThread;
    BufferedReader iStream;

    public ServerEventListener(ConnectorThread connectorThread, BufferedReader iStream) {
        this.connectorThread = connectorThread;
        this.iStream = iStream;
    }

    @Override
    public void run() {
        try {
            String input;
            while(this.connectorThread.conn.isConnected()) {
                input = readIn();
                processServerEvent(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processServerEvent(String serverData) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonElement data = jsonParser.parse(serverData);
        if (data.getAsJsonObject().has("requestID")) {
            // This is data for an operation we called on the server
            connectorThread.processCallResponse(serverData);
        } else {
            connectorThread.processServerPush(serverData);
        }
    }

    private String readIn() throws IOException, ClassNotFoundException {
        String data = this.iStream.readLine();
        if (data == null) throw new IOException("Connection closed!");
        return data;
    }
}
