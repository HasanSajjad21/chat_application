package chatapplication;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private String username;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public static void main(String[] args) {
        new Client().startClient();
    }

    public void startClient() {
        try {
            socket = new Socket("localhost", 1234);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Handle user login
            login();

            // Start separate threads for receiving and sending messages
            new Thread(this::receiveMessages).start();
            new Thread(this::sendMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        try {
            // Get username and password from the user
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            // Send login request to the server
            writer.println("LOGIN:" + username + "," + password);

            // Wait for the server's response
            String response = reader.readLine();

            // Process the server's response
            if (response.equals("LOGIN_SUCCESS")) {
                System.out.println("Login successful!");
                this.username = username;
            } else {
                System.out.println("Login failed. Invalid username or password.");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                // Process and display incoming messages
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

private void sendMessages() {
    Scanner scanner = new Scanner(System.in);
    try {
        while (true) {
            String message = scanner.nextLine();
            writer.println(message);  // Send only the message without appending the username
        if (message.equalsIgnoreCase("bye") || message.equalsIgnoreCase("exit")) {
                // Notify the server about the exit
                writer.println(username + " has left the chat.");

                // Close the client socket
                socket.close();

                // Display a message before exiting
                System.out.println("You have left the chat.");
                
                // Exit the loop and terminate the client
                break;
            }
        }
    } catch (IOException e) {
        e.printStackTrace(); 
    }finally {
        scanner.close();
    }
}

}
