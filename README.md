JsonRpcBridge
=============

Aim
---
The aim of this project is to allow the remote invocaiton of methods on a host application and to pass objects in a JSON serialized form between client and server.

This package is designed to be loaded into an existing java application, from which you can export objects onto the JsonRpcBridge, and makes those methods available to the clients.

Using the Bridge
----------------

```java
import me.rayhaan.java.JsonRpcBridge.Server.Server;
import me.rayhaan.java.JsonRpcBridge.JsonRpcBridge;

Server jsonBridgeServer = new Server();
JsonRpcBridge bridge = jsonBridgeServer.getGlobalBridge();

SomeClass myClass = new SomeClass();
bridge.export("SomeClass", myClass);

jsonBridgeServer.serve_forever();
```
