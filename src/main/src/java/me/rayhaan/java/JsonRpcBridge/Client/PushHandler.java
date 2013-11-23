package me.rayhaan.java.JsonRpcBridge.Client;

import java.lang.reflect.Method;

/**
 * Created by rayhaan on 11/23/13.
 */
public class PushHandler {

    public Object instance;
    public Method handler;

    public PushHandler(Object instance, Method handler) {
        this.instance = instance;
        this.handler = handler;
    }

}
