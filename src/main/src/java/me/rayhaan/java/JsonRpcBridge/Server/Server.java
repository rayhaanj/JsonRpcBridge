package me.rayhaan.java.JsonRpcBridge.Server;

import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;
import me.rayhaan.java.JsonRpcBridge.TestImpl;

import java.net.*;
import java.io.*;


public class Server implements Runnable {

   JsonRpcBridge globalBridge;

   ServerSocket socket;
   public final static int PORT = 3141;
   public final static boolean DEBUG = true;

    public Server() throws Exception {
        TestImpl testImplementation = new TestImpl();
        this.globalBridge = new JsonRpcBridge();
        globalBridge.exportClass("TestImpl", testImplementation);
    }


   public void serve_forever() throws IOException {
      
      debug("Starting server");
      this.socket = new ServerSocket(Server.PORT);

       // Non-terminating
       while (true) {
         ClientThread clientThread = new ClientThread(this.socket.accept(), globalBridge);
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
