package org.sjbanerjee.eventsqueue;

import org.sjbanerjee.eventsqueue.serde.SerdeUtil;

import java.io.IOException;
import java.util.Scanner;

public class Application {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            TCPServer.startServer(SERVER_HOST, SERVER_PORT);
            while (scanner.hasNextLine()) {
                String jsonMessage = scanner.nextLine();
                if (validateMessage(jsonMessage))
                    TCPClient.startClient(SERVER_HOST, SERVER_PORT, jsonMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean validateMessage(String message) {
        try {
            SerdeUtil.getDeserializedObject(message);
            return true;
        } catch (IOException e) {
            System.out.println("Message validation failed!");
            return false;
        }
    }

}
