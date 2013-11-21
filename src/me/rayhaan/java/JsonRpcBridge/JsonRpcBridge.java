package me.rayhaan.java.JsonRpcBridge;

import com.google.gson.*;
import java.util.*;
import java.lang.NoSuchFieldException;
import java.lang.reflect.*;


public class JsonRpcBridge {

   /**
    * HashMap of <className, instance> of class instances that are registered on this bridge.
    */
   HashMap<String, Object> objectMap = new HashMap<String, Object>();

   /**
    * Fetches the names of classes that are registered on this bridge.
    * @return A LinkedList of class names
    */
   public LinkedList<String> getClasses() {
      LinkedList<String> result = new LinkedList<String>();
      for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
         result.add(entry.getKey());
      }
      return result;
   }

   /**
    * Add a new instance of a class to the bridge.
    */
   public void exportClass(String className, Class<?> clazz) throws Exception {
      this.objectMap.put(className, clazz.newInstance());      
   }


   /**
    * Main entry point for requests coming into the bridge, called from the Server's ClientThread
    * @param input data to be processed
    * @return final result after incoking method / packagin
    * @throws Exception
    */
   public String processRequest(String input) throws Exception {
      Gson gson = new Gson();
      JsonParser parser = new JsonParser();
      JsonObject request = parser.parse(input).getAsJsonObject();

      String fullyQualifiedMethodName = request.get("method").getAsString();
      JsonArray arguments = request.get("args").getAsJsonArray();
      return call(fullyQualifiedMethodName, arguments);
   }

   /**
    * Call a method registered on this bridge.
    * @param fullyQualifiedMethodName
    * @param arguments
    * @return
    * @throws Exception
    */
   public String call(String fullyQualifiedMethodName, JsonArray arguments) throws Exception {
      Object result;

      String className = fullyQualifiedMethodName.split("\\.")[0];
      String methodName = fullyQualifiedMethodName.split("\\.")[1];
      
      for (Map.Entry<String, Object> obj : this.objectMap.entrySet()) {
            if (className.equals(obj.getKey())) {
               result = invoke(obj.getValue(), methodName, arguments);
               return packageResult(fullyQualifiedMethodName, result);
            }
         }
      throw new Exception("Class not found / registered on bridge");
   }

   /**
    * Encapsulate the result of a method invocation in a JsonObject that can be
    * sent back to the client.
    * @param fullyQualifiedMethodName
    * @param invocationResult
    * @return
    */
   public String packageResult(String fullyQualifiedMethodName, Object invocationResult) {
      //TODO: this is a hackish implementation for testing purposes, we need to
      //actually add in the javaClass of the result
      Gson gson = new Gson();
      HashMap<String, Object> result = new HashMap<String, Object>();
      result.put("method", fullyQualifiedMethodName);
      result.put("result", invocationResult);
      return gson.toJson(result);
   }


   /**
    * Call a method registered on this JsonRpcBridge. 
    *
    * @param methodName the name of the class and method to call in the format
    * (className).(methodName)
    * @param arguments arguments to pass to the method we want to call
    *
    */
   public Object invoke(Object instance, String methodName, JsonArray arguments) throws Exception {
      Class<?> clazz = instance.getClass();
      Method m = null;

      Method[] allMethods = clazz.getDeclaredMethods();
      LinkedList<Method> candidates = new LinkedList<Method>();
   
      for (Method candidate : clazz.getDeclaredMethods()) {
         if (candidate.getName().equals(methodName)) {
            candidates.add(candidate);
         }
      }
   
      if (candidates.size() == 0) {
         throw new Exception("Method not found!");
      } else if (candidates.size() > 1) {
         // TODO: add support for overloaded methods
         //throw new Exception("Method overloading not supported yet!");
      }
      
      final Object javaArgs[] = extractJsonArgs(arguments);
      return candidates.get(0).invoke(instance, javaArgs);
   
   }


   /**
    *  This is a temporary method
    *
    */
   private static Object[] extractJsonArgs(JsonArray jsa) {
      Object[] result = new Object[jsa.size()];


      for (int i=0; i<jsa.size(); i++) {
         result[i] = jsa.getAsString();
      }

      return result;
   }

   /**
    * Convert a JsonArray to an array of Java Objects.
    * Note: we need to get the type of the array from the java class we know
    * exists already, otherwise it is difficult to detect what type a primitive
    * is.
    * @param jsa the json array to convert to a java object array
    */
   private static Object[] convertJsonArrayToJavaArray(JsonArray jsa, Class<?> type) throws Exception {

      Object[] result = new Object[jsa.size()];
      
      JsonObject obj;

      for (int i=0; i<jsa.size(); i++) {
         obj = jsa.get(i).getAsJsonObject();
         result[i] = jsonObjectToObject(obj, type.toString());
      }

      return result;
   }

   /**
    * Convert a JsonObject to a Java object.
    *
    */
   public static Object jsonObjectToObject(JsonObject JSON, String javaClass) throws Exception {
      Object instance = (Object) Class.forName(javaClass).newInstance();
      Class<?> clazz = instance.getClass();
      Field f;

      String fieldName;
      Object value;


      for (Map.Entry<String, JsonElement> entry : JSON.entrySet()) {
         fieldName = entry.getKey();
         
         f = resolveField(clazz, fieldName);
         f.setAccessible(true);
         
         Class<?> fieldType = f.getType();

         if (fieldType.isPrimitive()) {
            String type = f.getType().toString();

            switch (type) {
               case "int":
                  f.set(instance, entry.getValue().getAsInt());
                  break;
               case "double":
                  f.set(instance, entry.getValue().getAsDouble());
                  break;
               case "float":
                  f.set(instance, entry.getValue().getAsFloat());
                  break;
               case "byte":
                  f.set(instance, entry.getValue().getAsByte());
                  break;
               case "char":
                  f.set(instance, entry.getValue().getAsCharacter());
                  break;
               case "long":
                  f.set(instance, entry.getValue().getAsLong());
                  break;
            }
         } else if (fieldType.isArray()) {
            f.set(instance, convertJsonArrayToJavaArray(entry.getValue().getAsJsonArray(), fieldType));
         } else {
            // must be an object that we have serialized
            f.set(instance, jsonObjectToObject(entry.getValue().getAsJsonObject(), fieldType.toString()));
         }
      }
      
      return instance;
   }


   /**
    * Try and find a field in the class hierarchy starting from a given class
    * and working up its supercalsses.
    */
   private static Field resolveField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
      try {
         return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
         Class<?> superClass = clazz.getSuperclass();
         if (superClass != null) {
            return resolveField(superClass, fieldName);
         } else {
            throw e;
         }

      }
   }

   public static String javaClassFieldExtractor(JsonObject JSON) {
      for (Map.Entry<String, JsonElement> entrySet : JSON.entrySet()) {
         if (entrySet.getKey().equals("JavaClass")) {
            return entrySet.getValue().getAsString();
         }
      }
      return null;
   }

}
