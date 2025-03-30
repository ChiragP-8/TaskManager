package client;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 5000;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public synchronized void ensureConnected() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(SERVER_IP, PORT);
            socket.setSoTimeout(5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
    }

    public synchronized void sendTask(String username, String task, String dueDate,
                                      String priority, String status) throws IOException {
        ensureConnected();
        out.println("ADD;" + username + ";" + task + ";" + dueDate + ";" + priority + ";" + status);
        handleAck();
    }

    public synchronized String getTasks(String username) throws IOException {
        ensureConnected();
        out.println("GET;" + username);
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.equals("END")) {
            response.append(line).append("\n");
        }
        return response.toString().trim();
    }

    public synchronized void deleteTask(String username, String task, String dueDate,
                                        String priority, String status) throws IOException {
        ensureConnected();
        out.println("DELETE;" + username + ";" + task + ";" + dueDate + ";" + priority + ";" + status);
        handleAck();
    }

    private void handleAck() throws IOException {
        String response = in.readLine();
        if (!"ACK".equals(response)) {
            throw new IOException("Server didn't acknowledge");
        }
    }

    public synchronized void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}