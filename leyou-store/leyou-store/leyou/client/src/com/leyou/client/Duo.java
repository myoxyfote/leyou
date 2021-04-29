package com.leyou.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Duo {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(45654);
		while (true) {
			Socket socket = serverSocket.accept();
			ReceiveTask receiveTask = new ReceiveTask(socket);
			Thread t = new Thread(receiveTask, "接收线程");
			t.start();
		}
	}
}
