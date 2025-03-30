package server;

import java.io.*;
import java.net.*;
import java.sql.*;
import database.DatabaseManager;

public class Server {
    private static final int PORT = 5000;
    private DatabaseManager dbManager;

    public Server() throws Exception {
        dbManager = new DatabaseManager();
        start();
    }

    private void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket, dbManager).start();
        }
    }

    public static void main(String[] args) throws Exception {
        new Server();
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private DatabaseManager dbManager;

        public ClientHandler(Socket socket, DatabaseManager dbManager) {
            this.socket = socket;
            this.dbManager = dbManager;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                socket.setSoTimeout(30000);
                String request;

                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(";");
                    String command = parts[0];
                    String username = parts[1];

                    switch (command) {
                        case "ADD":
                            handleAdd(out, username, parts);
                            break;
                        case "GET":
                            handleGet(out, username);
                            break;
                        case "DELETE":
                            handleDelete(out, username, parts);
                            break;
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Client timeout");
            } catch (Exception e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        // In ClientHandler class
        private void handleAdd(PrintWriter out, String username, String[] parts) {
            try {
                dbManager.addTask(username, parts[2], parts[3], parts[4], parts[5]);
                out.println("ACK");
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate entry")) {
                    out.println("ERROR;Duplicate task name");
                } else {
                    out.println("ERROR;Database error");
                }
            }
        }

        private void handleGet(PrintWriter out, String username) throws SQLException {
            String tasks = dbManager.getTasks(username);
            out.println(tasks);
            out.println("END");
        }

        private void handleDelete(PrintWriter out, String username, String[] parts) throws SQLException {
            dbManager.deleteTask(username, parts[2], parts[3], parts[4], parts[5]);
            out.println("ACK");
        }
    }
}