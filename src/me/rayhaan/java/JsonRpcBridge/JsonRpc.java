package me.rayhaan.java.JsonRpcBridge;

import java.io.IOException;

import me.rayhaan.java.JsonRpcBridge.Server.Server;

public class JsonRpc {

	public static void main(String[] args) {
		Server server = new Server();
		try {
			server.serve_forever();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
