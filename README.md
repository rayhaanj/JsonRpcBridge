JsonRpcBridge
=============

Aim
---
The aim of this project is to allow the remote invocation of methods on a host application and to pass objects in a JSON serialized form between client and server.

This package is designed to be loaded into an existing java application, from which you can export objects onto the JsonRpcBridge, and makes those methods available to the clients.

Using the Bridge
----------------
You can create a server and export a class using the following procedure:

```java
import me.rayhaan.java.JsonRpcBridge.Server.Server;
import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;

Server jsonBridgeServer = new Server();
JsonRpcBridge bridge = jsonBridgeServer.getGlobalBridge();

SomeClass myClass = new SomeClass();
bridge.export("SomeClass", myClass); // Export the object myCLass under the handle SomeClass

jsonBridgeServer.serve_forever(); // runs the server loop listening for connections
```

Connecting to the server as a client

``` java
import me.rayhaan.java.JsonRpcBridge.Client.ConnectorThread;

ConnectorThread ct = new ConnectorThread("localhost", 3141);
(new Thread(ct)).start();

Future<JsonElement> res2 = ct.callMethod("TestImpl", "customObjectTest", new Point(1,2));

try {
    JsonElement elem = res2.get();
    System.out.println("Result" + elem);
} catch (Exception e) {
    e.printStackTrace();
}
```


Using server-side push handling
-------------------------------


```java

// To send data to all the clients
Server.push("<Json serialized data with a tag, method , and arguments>");
```

``` java
// To listen for server sent pushes on the client side
ConnectorThread ct = new Connectorthread(...); // fill in host/port

// Register a method to be called when a push request comes in
ct.registerPushListener("push_handle_name", MyClass, Myclass.class.getDeclaredMethod("processBounce"));

...

public void processBounce(String ss) {
    System.out.println("Got Bounce!" + ss);
}

```
