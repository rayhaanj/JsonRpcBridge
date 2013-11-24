package me.rayhaan.java.JsonRpcBridge.Client;

import java.lang.reflect.*;

import me.rayhaan.java.JsonRpcBridge.Test;
import me.rayhaan.java.JsonRpcBridge.TestImpl;

public class DynamicProxy implements java.lang.reflect.InvocationHandler {
   private Object obj;

   private DynamicProxy(Object obj) {
      this.obj = obj;
   }

   public static Object newInstance( Object obj ) {
      return java.lang.reflect.Proxy.newProxyInstance(
         obj.getClass().getClassLoader(),
         obj.getClass().getInterfaces(),
         new DynamicProxy(obj)
      );
   }

   public Object invoke(Object proxy, Method m, Object[] args) throws java.lang.Throwable {
      Object result;
      try {
         System.out.println("Method " + m.getName() + " is going to be called");
         result = m.invoke(obj, args);
      } catch (InvocationTargetException e) {
        throw e.getTargetException(); 
      } catch (Exception e) {
         throw new RuntimeException("Unexpected invocation exception: " + e.getMessage());
      } finally {
         System.out.println("Method " + m.getName() + " has been run");
      }
      System.out.println(result);
      return result;
   }

   public static void main (String[] args) throws Exception  {
      Test dnmprx = (Test) DynamicProxy.newInstance( new TestImpl() );
      String result = (String) dnmprx.method("blah");
   }

}
