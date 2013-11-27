package me.rayhaan.java.JsonRpcBridge;

import com.google.gson.*;

import java.lang.reflect.Method;
import java.util.*;

import me.rayhaan.java.JsonRpcBridge.Reflect.MethodInvoker;

public class JsonRpcBridge {

   /**
    * HashMap of <className, instance> of class instances that are registered on this bridge.
    */
   HashMap<String, Object> objectMap = new HashMap<>();

   /**
    * Fetches the names of classes that are registered on this bridge.
    * @return A LinkedList of class names
    */
   public String getBridgeMethods(int requestID) {
       HashMap<String, LinkedList<String>> methodMap = new HashMap<>();

      String className;
      Class<?> clazz;
      for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
         className = entry.getKey();
         clazz = entry.getValue().getClass();

          LinkedList<String> methodList = new LinkedList<>();
         for (Method m : clazz.getDeclaredMethods()) {
             methodList.add(m.getName());
         }
         methodMap.put(className, methodList);
      }

       HashMap<String, Object> result = new HashMap<>();
       result.put("requestID", requestID);
       result.put("result", methodMap);
      return new Gson().toJson(result);
   }

   /**
    * Add a new instance of a class to the bridge.
    */
   public void export(String className, Object instance) throws Exception {
      this.objectMap.put(className, instance);
   }

   /**
    * Main entry point for requests coming into the bridge, called from the Server's ClientThread
    * @param input data to be processed
    * @return final result after invoking method / packaging
    * @throws Exception
    */
   public String processRequest(String input) throws Exception {
      JsonParser parser = new JsonParser();
      JsonObject request = parser.parse(input).getAsJsonObject();
      int requestID = request.get("requestID").getAsInt();

      String fullyQualifiedMethodName = request.get("method").getAsString();

       if (fullyQualifiedMethodName.equals("Bridge.listMethods")) {
           return getBridgeMethods(requestID);
       }

      Object[] arguments;
      if (request.get("args").isJsonNull()) {
        arguments = new Object[] {};
      } else {
        arguments = JsonUtils.convertJsonArrayToJavaArray(request.get("args").getAsJsonArray());
      }


      return call(requestID, fullyQualifiedMethodName, arguments);
   }

   /**
    * Call a method registered on this bridge.
    * @param fullyQualifiedMethodName name of class & method in format ClassName.MethodName
    * @param javaArgs Formal arguments to pass to the method
    * @return A JSON formatted string contianing the result of the method call
    * @throws Exception If the invocation went wrong
    */
   public String call(int requestID, String fullyQualifiedMethodName, Object[] javaArgs) throws Exception {
      Object result;
      
      
      String className = fullyQualifiedMethodName.split("\\.")[0];
      String methodName = fullyQualifiedMethodName.split("\\.")[1];
      
      MethodInvoker mInvoker;
      Method m;

      for (Map.Entry<String, Object> obj : this.objectMap.entrySet()) {
            if (className.equals(obj.getKey())) {

                mInvoker = new MethodInvoker(obj.getValue());

                m = mInvoker.resolve(methodName, javaArgs);
                try {
                    result = mInvoker.invoke(m, javaArgs);
                } catch (Exception e) {
                    return packageException(requestID, fullyQualifiedMethodName, e);
                }
               return packageResult(requestID, fullyQualifiedMethodName, result);
            }
         }
      return packageException(requestID, fullyQualifiedMethodName, new Exception("Class not found / registered on bridge"));
   }




   /**
    * Encapsulate the result of a method invocation in a JsonObject that can be
    * sent back to the client.
    * @param fullyQualifiedMethodName name of class & method in format ClassName.MethodName
    * @param invocationResult Result of calling the method
    * @return Json formatted string
    */
   public String packageResult(int requestID, String fullyQualifiedMethodName, Object invocationResult) throws Exception {

       // Insert the javaClass of the invocationResult into the JsonObject
       if (invocationResult instanceof Collection) {
           throw new Exception("Collections are unsupported at this time");
       } else if (invocationResult instanceof Map) {
           throw new Exception("Maps are not supported at this time");
       }

       JsonElement serializedInvocationResult;
       if (invocationResult != null) {
            serializedInvocationResult = JsonUtils.javaClassFieldInjector(invocationResult);
       } else {
           // FIXME: Add support for null returns!
           serializedInvocationResult = (new Gson()).toJsonTree("Null Response!");
       }

      Gson gson = new Gson();
      HashMap<String, Object> result = new HashMap<>();

      result.put("requestID", requestID);
      result.put("method", fullyQualifiedMethodName);
      result.put("result", serializedInvocationResult);
      return gson.toJson(result);
   }

    /**
     * Package an exception for sending back to the client
     * @param requestID
     * @param fullyQualifiedMethodName
     * @param e
     * @return
     */
   public String packageException(int requestID, String fullyQualifiedMethodName, Exception e) {
       Gson gson = new Gson();
       HashMap<String, Object> result = new HashMap<>();

       result.put("requestID", requestID);
       result.put("method", fullyQualifiedMethodName);
       result.put("isException", true);
       System.out.println("Exception message" + e.getMessage());
       result.put("exception", e.getMessage());
       return gson.toJson(result);
   }

}
