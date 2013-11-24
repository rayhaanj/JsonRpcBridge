package me.rayhaan.java.JsonRpcBridge.Client;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.google.gson.*;
import me.rayhaan.java.JsonRpcBridge.JsonUtils;


/**
 * Created by rayhaan on 11/22/13.
 */
public class ConnectorThread implements Runnable {

    Socket conn;
    BufferedReader iStream;
    PrintWriter oStream;

    private int callCount = 0;

    HashMap<String, PushHandler> pushHandlers = new HashMap<>();

    HashMap<Integer, JsonElement> serverCallResponses = new HashMap<>();

    public ConnectorThread(String host, int port) throws Exception {
        // setup the connection
        this.conn = new Socket(host, port);
        this.iStream = new BufferedReader( new InputStreamReader(this.conn.getInputStream()) );
        this.oStream = new PrintWriter(this.conn.getOutputStream());

        // Do handshake to ensure we are connected to the right type of server
        String header = readIn();
        JsonParser jParser = new JsonParser();
        JsonElement headObj = jParser.parse(header);

        // Verify server part of the handshake
        if (! headObj.getAsJsonObject().get("Server").getAsString().contains("JsonRpcBridge")) {
            throw new Exception("There was an error connecting to the server!");
        }

        // Send back our response
        HashMap<String, String> response = new HashMap<>();
        response.put("Establish_Connection", "true");

        writeString(new Gson().toJson(response));
    }

    @Override
    public void run() {
        System.out.println("[Debug] connected to server " + this.conn.getInetAddress());
        ServerEventListener eventListener = new ServerEventListener(this, this.iStream);
        (new Thread(eventListener)).start();
    }


    /** Client call */

    /**
     * record the result from a successful call to the server
     * @param serverData
     */
    public void processCallResponse(String serverData) {
        System.out.println("Response recieved!");
        JsonElement data = new JsonParser().parse(serverData);
        int requestID = data.getAsJsonObject().get("requestID").getAsInt();
        JsonElement callResult = data.getAsJsonObject().get("result");
        this.serverCallResponses.put(requestID, callResult);
    }

    /**
     * Call a method on the server
     * @param className
     * @param methodName
     * @param arguments
     * @return
     */
    public Future<JsonElement> callMethod(String className, String methodName, Object... arguments) throws IOException {
        System.out.println("Calling method " + methodName);
        final int requestID = ++callCount;
        HashMap<String, Object> request = new HashMap<>();
        request.put("requestID", requestID);
        request.put("method", className + "." + methodName);
        request.put("args", arguments);
        Gson gson = new GsonBuilder().serializeNulls().create();

        String JSON = gson.toJson(request);
        System.out.println(JSON);
        // Fire the request
        this.writeString(JSON);

        class CheckResponse implements Callable<JsonElement> {
            ConnectorThread connectorThread;
            int requestID;

            public CheckResponse(ConnectorThread ct, int requestID) {
                this.connectorThread = ct;
                this.requestID = requestID;
            }

            @Override
            public JsonElement call() throws Exception {
                while (! this.connectorThread.serverCallResponses.containsKey(requestID)) {
                    // Just wait till the serverCallResponses contains an element with our key
                    Thread.sleep(10);
                }
                return this.connectorThread.serverCallResponses.get(requestID);
            }
        }

        FutureTask futureResponse = new FutureTask<>(new CheckResponse(this, requestID));
        futureResponse.run();
        return futureResponse;
    }



    /** SERVER PUSH */

    /**
     * Add a listener that can be called from the server side
     * @param tag
     * @param instance
     * @param m
     */
    public void registerPushListener(String tag, Object instance, Method m) {
        this.pushHandlers.put(tag, new PushHandler(instance, m));
    }

    /**
     * Call the method associated with a registered push handler
     * @param json Server data
     * @throws Exception
     */
    protected void processServerPush(String json) throws Exception {
        JsonElement data = new JsonParser().parse(json);
        String tag = data.getAsJsonObject().get("tag").getAsString();
        Object[] arguments = JsonUtils.convertJsonArrayToJavaArray(data.getAsJsonObject().get("args").getAsJsonArray());

        if (!this.pushHandlers.containsKey(tag)) throw new Exception("No handler registered for this tag!");
        PushHandler handler = this.pushHandlers.get(tag);

        handler.handler.invoke(handler.instance, arguments);
    }

    /** Utility methods */
    private String readIn() throws IOException, ClassNotFoundException {
        String data = iStream.readLine();
        if (data == null) throw new IOException("Connection closed!");
        return data;
    }

    private void writeString(String s) throws IOException {
        this.oStream.println(s);
        this.oStream.flush();
    }



}