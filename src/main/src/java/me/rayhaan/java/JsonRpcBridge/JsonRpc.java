package me.rayhaan.java.JsonRpcBridge;

import java.io.IOException;

import me.rayhaan.java.JsonRpcBridge.Server.Server;

public class JsonRpc {

    public static final String VERSION = "0.1_Alpha";

	public static void main(String[] args) {

		try {
            Server server = new Server();
            server.serve_forever();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
