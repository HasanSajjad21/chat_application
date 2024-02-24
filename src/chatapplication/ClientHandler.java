package chatapplication;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server1 server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket clientSocket, Server1 server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            server.appendLog("Error setting up input/output streams for client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            handleLogin();
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("bye") || message.equalsIgnoreCase("exit")) {
                    break;
                } else {
                    server.broadcastMessage(username + ": " + message, this);
                    server.logMessage(username, message);
                }
            }
        } catch (IOException e) {
            server.appendLog(username + " has disconnected.");
        } finally {
            closeConnection();
        }
    }

    private void handleLogin() throws IOException {
        String loginRequest = reader.readLine();
        if (loginRequest.startsWith("LOGIN:")) {
            String[] credentials = loginRequest.split("LOGIN:")[1].split(",");
            String username = credentials[0];
            String password = credentials[1];

            if (server.isValidUser(username, password)) {
                writer.println("LOGIN_SUCCESS");
                this.username = username;
                server.appendLog(username + " has joined the chat.");
            } else {
                writer.println("LOGIN_FAIL");
                closeConnection();
            }
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void closeConnection() {
        try {
            server.removeClient(this);
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            server.appendLog(username + " connection closed.");
        } catch (IOException e) {
            server.appendLog("Error closing connection for " + username + ": " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }
}
