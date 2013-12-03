package me.rayhaan.java.JsonRpcBridge;

import com.google.gson.Gson;
import me.rayhaan.java.JsonRpcBridge.Server.Server;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;


public class TestImpl implements Test {

    Server server;

    public TestImpl(Server server) {
        this.server = server;
    }

    private String testString = "";

    public String method(String obj) throws Exception {
        System.out.println("Method is being run!");
        this.testString = testString + obj;
        return "Hello " + testString;
    }

    @SuppressWarnings("unused")
    public int add(int a, int b, int c) {
        return a + b + c;
    }

    @SuppressWarnings("unused")
    public void throwException() throws Exception {
        throw new Exception("Testing exceptions");
    }

    public void bounce(String tag, String ss) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("tag", tag);
        result.put("args", new Object[] {ss});
        String jsonData = (new Gson()).toJson(result);
        this.server.pushAllClients(jsonData);
    }

    public String customObjectTest(Point p) {
        String ss = "X coord: " + p.x + " y coord " + p.y;
        System.out.println(ss);
        return ss;
    }


    public SomeClass getSomeClass() {
        return new SomeClass();
    }

    public LinkedList<SomeClass> getLinkedListOfSomeClass() {
        LinkedList<SomeClass> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) result.add(new SomeClass());
        return result;
    }

    public class SomeClass {
        double pi = 3.141;
    }
}


