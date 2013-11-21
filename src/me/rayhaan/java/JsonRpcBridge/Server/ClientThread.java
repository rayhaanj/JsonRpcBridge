package me.rayhaan.java.JsonRpcBridge.Server;

import java.io.*;
import java.net.*;

import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;
import me.rayhaan.java.JsonRpcBridge.TestImpl;


public class ClientThread implements Runnable {

   public Socket serverSocket;
   private BufferedReader istream;
   private PrintWriter ostream;

   public ClientThread(Socket serverSocket) {
      this.serverSocket = serverSocket;
   }

   @Override
   public void run() {
      try {
         this.ostream =  new PrintWriter(this.serverSocket.getOutputStream());
         this.ostream.flush();
         this.istream = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
         this.writeString("Hello there");
         String resp = this.readIn();
         System.out.println(resp);

         JsonRpcBridge jRpcBridge = new JsonRpcBridge();
         jRpcBridge.exportClass("TestImpl", TestImpl.class);

         String input, output;
         /* Non-terminating */
         while(true) {
            input = readIn();
            if (input.equals("QUIT")) break;
            try {
               output = jRpcBridge.processRequest(input);
               System.out.println(output);
               this.writeString(output);
            } catch (Exception e) {
               e.printStackTrace();
            }

         }
         /* End Non-terminating */
      
      } catch(IOException  ioe) {
         ioe.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private String readIn() throws IOException, ClassNotFoundException {
      String data = null;
      while (data == null) {
         data = this.istream.readLine();
      }
      return data;
   }

   private void writeString(String s) throws IOException {
      this.ostream.println(s);
      this.ostream.flush();
      return;
   }


}
