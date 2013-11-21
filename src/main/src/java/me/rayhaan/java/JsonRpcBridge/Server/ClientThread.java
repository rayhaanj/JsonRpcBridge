package me.rayhaan.java.JsonRpcBridge.Server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

import me.rayhaan.java.JsonRpcBridge.JsonRpc;
import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;
import me.rayhaan.java.JsonRpcBridge.TestImpl;

import com.google.gson.*;


public class ClientThread implements Runnable {

   public Socket serverSocket;
   private BufferedReader istream;
   private PrintWriter ostream;

   public ClientThread(Socket serverSocket) {
      this.serverSocket = serverSocket;
   }

    public String generateHandshake(String version) {
        HashMap<String, String> data = new HashMap<String,String>();
        data.put("Server", "JsonRpcBridge Version " + version);
        Gson gson = new Gson();
        return gson.toJson(data);
    }

    public boolean confirmHandshake(String response) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement responseData = parser.parse(response);
            if (responseData.getAsJsonObject().get("Establish_Connection").getAsString().equals("true")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

   @Override
   public void run() {
      try {
         this.ostream =  new PrintWriter(this.serverSocket.getOutputStream());
         this.ostream.flush();
         this.istream = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
         this.writeString(generateHandshake(JsonRpc.VERSION));

         String resp = this.readIn();
         if (!confirmHandshake(resp)) {
            System.out.println("Error negotiating connection");
             this.writeString("There was an error negotiating a connection");
             closeConnection();
             throw new Exception("Error negotiating connection");
         }

          System.out.println("Successfully negotiated connection");

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

    private void closeConnection() throws IOException {
        this.istream.close();
        this.ostream.close();
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
