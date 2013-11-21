package me.rayhaan.java.JsonRpcBridge.Server;

import java.net.*;
import java.io.*;


public class Server {

   ServerSocket socket;
   public final static int PORT = 3141;
   public final static boolean DEBUG = true;


   public static void main(String... args) {
      Server server = new Server();
      try {
         server.serve_forever();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void serve_forever() throws IOException {
      
      debug("Starting server");
      this.socket = new ServerSocket(Server.PORT);

      /* Non-terminating */
      while (true) {
         ClientThread clientThread = new ClientThread(this.socket.accept());
         debug("New client connected: " + clientThread.serverSocket.getRemoteSocketAddress().toString());
         (new Thread(clientThread)).start();
      }
      /* End Non-terminating */

   }


   public static void debug(String msg) {
      if (Server.DEBUG) System.out.println("[Debug] " + msg);
   }


}
