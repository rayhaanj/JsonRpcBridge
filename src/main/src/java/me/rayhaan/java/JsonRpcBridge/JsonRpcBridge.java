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
   @SuppressWarnings("unused")
   public LinkedList<String> getClasses() {
      LinkedList<String> result = new LinkedList<>();
      for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
         result.add(entry.getKey());
      }
      return result;
   }

   /**
    * Add a new instance of a class to the bridge.
    */
   public void exportClass(String className, Object instance) throws Exception {
      this.objectMap.put(className, instance);
   }


   /**
    * Main entry point for requests coming into the bridge, called from the Server's ClientThread
    * @param input data to be processed
    * @return final result after invoking method / packagin
    * @throws Exception
    */
   public String processRequest(String input) throws Exception {
      JsonParser parser = new JsonParser();
      JsonObject request = parser.parse(input).getAsJsonObject();

      String fullyQualifiedMethodName = request.get("method").getAsString();

      Object[] arguments;
      if (request.get("args").isJsonNull()) {
        arguments = new Object[] {};
      } else {
        arguments = JsonUtils.convertJsonArrayToJavaArray(request.get("args").getAsJsonArray());
      }

      int requestID = request.get("requestID").getAsInt();
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
                System.out.println(javaArgs.length);

                m = mInvoker.resolve(methodName, javaArgs);

                result = mInvoker.invoke(m, javaArgs);

               return packageResult(requestID, fullyQualifiedMethodName, result);
            }
         }
      throw new Exception("Class not found / registered on bridge");
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

       JsonElement serializedInvocationResult = JsonUtils.javaClassFieldInjector(invocationResult);

      Gson gson = new Gson();
      HashMap<String, Object> result = new HashMap<>();

      result.put("requestID", requestID);
      result.put("method", fullyQualifiedMethodName);
      result.put("result", serializedInvocationResult);
      return gson.toJson(result);
   }
   
   


}
