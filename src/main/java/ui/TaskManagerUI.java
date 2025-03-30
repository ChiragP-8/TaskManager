package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import client.Client;

public class TaskManagerUI {
    private final JFrame frame;
    private final DefaultTableModel model;
    private final Client client;
    private final String username;

    public TaskManagerUI(String username) {
        this.username = username;
        this.client = new Client();
        this.frame = new JFrame("Task Manager - " + username);
        this.model = new DefaultTableModel(new String[]{"Task", "Due Date", "Priority", "Status"}, 0);
        initializeUI();
        loadTasks();
    }

    private void initializeUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);

        // Table setup
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Input components
        JTextField taskField = new JTextField(20);
        JTextField dueDateField = new JTextField(10);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});

        // Buttons
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");

        // Add button action
        addButton.addActionListener(e -> {
            String task = taskField.getText().trim();
            String dueDate = dueDateField.getText().trim();

            if (task.isEmpty() || dueDate.isEmpty()) {
                showError("Input Error", "Task and Due Date cannot be empty!");
                return;
            }

            try {
                client.sendTask(
                        username,
                        task,
                        dueDate,
                        (String) priorityBox.getSelectedItem(),
                        "Pending"
                );
                taskField.setText("");
                dueDateField.setText("");
                loadTasks();
            } catch (IOException ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    showError("Duplicate Task", "Task '" + task + "' already exists!");
                } else {
                    showError("Connection Error", ex.getMessage());
                }
            }
        });

        // Delete button action
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                showError("Selection Error", "Please select a task to delete!");
                return;
            }

            try {
                String task = (String) model.getValueAt(selectedRow, 0);
                String dueDate = (String) model.getValueAt(selectedRow, 1);
                String priority = (String) model.getValueAt(selectedRow, 2);
                String status = (String) model.getValueAt(selectedRow, 3);

                client.deleteTask(username, task, dueDate, priority, status);
                loadTasks();
            } catch (IOException ex) {
                showError("Deletion Error", "Failed to delete task: " + ex.getMessage());
            }
        });

        // Input panel layout
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("Due Date:"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityBox);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);

        // Main layout
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Window closing handler
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.close();
            }
        });

        frame.setVisible(true);
    }

    private void loadTasks() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    String tasks = client.getTasks(username);
                    SwingUtilities.invokeLater(() -> updateTaskTable(tasks));
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            showError("Load Error", "Failed to load tasks: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    private void updateTaskTable(String tasks) {
        model.setRowCount(0);
        if (tasks != null && !tasks.isEmpty()) {
            for (String line : tasks.split("\n")) {
                if (!line.trim().isEmpty()) {
                    model.addRow(line.split(";"));
                }
            }
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String username = JOptionPane.showInputDialog("Enter your username:");
            if (username != null && !username.trim().isEmpty()) {
                new TaskManagerUI(username.trim());
            }
        });
    }
}