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

    Server server;

    public ClientThread(Server server, Socket serverSocket, JsonRpcBridge globalBridge) {
        this.serverSocket = serverSocket;
        this.globalBridge = globalBridge;
        this.server = server;
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

    public void push(String data) throws IOException{
        this.writeString(data);
    }

    @Override
    public void run() {
        try {

            this.openConnection();

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

            // start a new thread to listen for client requests
            RequestListener listener = new RequestListener(this.globalBridge, this.istream, this.ostream);
            (new Thread(listener)).start();

        } catch (Exception e) {
            e.printStackTrace();
            try { this.closeConnection(); } catch (IOException closeErr) { closeErr.printStackTrace(); }
        }

    }

    /**
     * Initialize the connection to the client
     * @throws IOException
     */
    private void openConnection() throws IOException {
        this.ostream = new PrintWriter(this.serverSocket.getOutputStream());
        this.ostream.flush();
        this.istream = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
        this.server.registerClient(this);
    }

    /**
     * Close the connection to the client
     * @throws IOException
     */
    private void closeConnection() throws IOException {
        this.istream.close();
        this.ostream.close();
        this.server.unregisterClient(this);
    }


    /**
     * Read in a string fron the network connection
     * @return String read in from the client
     * @throws IOException
     */
    private String readIn() throws IOException {
        String data = istream.readLine();
        if (data == null) throw new IOException("Connection closed!");
        return data;
    }

    /**
     * Send a string to the client
     * @param s The string to send to the client
     * @throws IOException
     */
    private void writeString(String s) throws IOException {
        this.ostream.println(s);
        this.ostream.flush();
    }



}
