package me.rayhaan.java.JsonRpcBridge.Server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

import me.rayhaan.java.JsonRpcBridge.JsonRpc;
import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;

import com.google.gson.*;

/**
 * Handles the communication between an individual client and the server.
 */
public class ClientThread implements Runnable {

    private JsonRpcBridge globalBridge;
    public Socket serverSocket;
    private BufferedReader istream;
    private PrintWriter ostream;

    public ClientThread(Socket serverSocket, JsonRpcBridge globalBridge) {
        this.serverSocket = serverSocket;
        this.globalBridge = globalBridge;
    }

    /**
     * Generate the Json string that is sent to the client as soon as a connection is established.
     *
     * @param version The version number of this JsonRpcBridge
     * @return A json string to send to the client with server information
     */
    public String generateHandshake(String version) {
        HashMap<String, String> data = new HashMap<>();
        data.put("Server", "JsonRpcBridge Version " + version);
        Gson gson = new Gson();
        return gson.toJson(data);
    }

    /**
     * Verify that the response handshake from the client is valid.
     *
     * @param response The string that was read in from the client
     * @return true if valid, false otherwise
     */
    public boolean confirmHandshake(String response) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement responseData = parser.parse(response);
            if (responseData.getAsJsonObject().get("Establish_Connection").getAsString().equals("true")) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void run() {
        try {
            this.ostream = new PrintWriter(this.serverSocket.getOutputStream());
            this.ostream.flush();
            this.istream = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));

            /* Handshake */
            this.writeString(generateHandshake(JsonRpc.VERSION));

            String resp = this.readIn();
            if (!confirmHandshake(resp)) {
                System.out.println("Error negotiating connection");
                this.writeString("There was an error negotiating a connection");
                closeConnection();
                throw new Exception("Error negotiating connection");
            }
            System.out.println("Successfully negotiated connection");

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

    private void closeConnection() throws IOException {
        this.istream.close();
        this.ostream.close();
    }


    private String readIn() throws IOException, ClassNotFoundException {
        String data = istream.readLine();
        if (data == null) throw new IOException("Connection closed!");
        return data;
    }

    private void writeString(String s) throws IOException {
        this.ostream.println(s);
        this.ostream.flush();
    }



}
