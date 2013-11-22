package me.rayhaan.java.JsonRpcBridge;
import java.util.LinkedList;


   public class TestImpl implements Test {

      public String method(String obj) throws Exception {
         System.out.println("Method is being run!");
         return "Hello there from TestImpl, the string you passed is " + obj;
      }

      public SomeClass getSomeClass() {
         return new SomeClass();
      }

       public LinkedList<SomeClass> getLinkedListOfSomeClass() {
           LinkedList<SomeClass> result = new LinkedList<>();
           for (int i=0; i<10; i++) result.add(new SomeClass());
           return result;
       }

      public class SomeClass {
         double pi = 3.141;
      }
   }


