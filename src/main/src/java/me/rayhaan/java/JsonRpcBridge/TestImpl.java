package me.rayhaan.java.JsonRpcBridge;

   public class TestImpl implements Test {

      public String method(String obj) throws Exception {
         System.out.println("Method is being run!");
         return "Hello there from TestImpl, the string you passed is " + obj;
      }


      public SomeClass getSomeClass() {
         return new SomeClass();
      }

      public class SomeClass {
         double pi = 3.141;
      }
   }


