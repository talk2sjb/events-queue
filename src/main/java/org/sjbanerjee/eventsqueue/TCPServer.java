package org.sjbanerjee.eventsqueue;

import org.sjbanerjee.eventsqueue.model.EventMessage;
import org.sjbanerjee.eventsqueue.persistence.MessagePersistence;
import org.sjbanerjee.eventsqueue.serde.SerdeUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable {
    private ServerSocket socket;
    private static Socket connectionSocket = null;

    private TCPServer(String serverAddress, int serverPort) throws Exception {
        this.socket = new ServerSocket(serverPort);
    }

    public static void startServer(String host, int port) throws Exception {
        Runnable server = new TCPServer(host, port);
        Thread thread = new Thread(server);

        thread.start();
        System.out.println("Server started at port " + port);
    }

    private void startListening() {
        EventMessage message;
        while (true) {
            try {
                connectionSocket = this.socket.accept();
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                message = SerdeUtil.getDeserializedObject(inFromClient.readLine());
                MessagePersistence.getInstance().writeSerializedObject(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        startListening();
    }
}
