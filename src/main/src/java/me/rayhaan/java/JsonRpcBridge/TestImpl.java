package me.rayhaan.java.JsonRpcBridge;
import java.util.LinkedList;


   public class TestImpl implements Test {


      private String testString = "";

      public String method(String obj) throws Exception {
         System.out.println("Method is being run!");
         this.testString = testString + obj;
         return "Hello " + testString;
      }

       public int add(int a, int b, int c) {
           return a + b + c;
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


