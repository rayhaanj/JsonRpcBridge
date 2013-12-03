package me.rayhaan.java.JsonRpcBridge.Server;

import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;

import java.net.*;
import java.io.*;
import java.util.LinkedList;

public class Server implements Runnable {

    public JsonRpcBridge globalBridge;
    public LinkedList<ClientThread> clients;

    ServerSocket socket;
    public final static int PORT = 3141;
    public final static boolean DEBUG = true;

    public Server() throws Exception {
        this.globalBridge = new JsonRpcBridge();
        this.clients = new LinkedList<>();
    }

    public JsonRpcBridge getGlobalBridge() {
        return this.globalBridge;
    }

    public boolean pushAllClients(String s) {
        boolean success = true;
        for (ClientThread ct : clients) {
            try {
                ct.push(s);
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public void registerClient(ClientThread ct) {
        this.clients.add(ct);
    }

    public void unregisterClient(ClientThread ct) {
        this.clients.remove(ct);
    }
    public void serve_forever() throws IOException {

        debug("Starting server");
        this.socket = new ServerSocket(Server.PORT);

        // Non-terminating
        while (true) {
            ClientThread clientThread = new ClientThread(this, this.socket.accept(), globalBridge);
            debug("New client connected: " + clientThread.serverSocket.getRemoteSocketAddress().toString());
            (new Thread(clientThread)).start();
        }
      /* End Non-terminating */

    }

    @Override
    public void run() {
        try {
            serve_forever();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void debug(String msg) {
        if (Server.DEBUG) System.out.println("[Debug] " + msg);
    }


}
