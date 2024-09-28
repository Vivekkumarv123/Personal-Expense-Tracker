import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExpenseTracker extends JFrame implements ActionListener {
    private List<Expense> expenses = new ArrayList<>();
    private DefaultListModel<String> expenseListModel = new DefaultListModel<>();
    private JList<String> expenseList;
    private JTextField amountField, dateField, miscDescField;
    private JComboBox<String> categoryCombo;
    private JTextArea summaryArea;

    // Constructor
    public ExpenseTracker() {
        setTitle("Personal Expense Tracker");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel for adding expenses
        JPanel topPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JLabel dateLabel = new JLabel("Date (dd/MM/yyyy):");
        dateField = new JTextField();
        
        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Food", "Transport", "Entertainment", "Miscellaneous"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.addActionListener(e -> toggleMiscellaneousField());

        JLabel miscDescLabel = new JLabel("Miscellaneous Description:");
        miscDescField = new JTextField();
        miscDescField.setEnabled(false);  // Initially disabled

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(this);

        topPanel.add(amountLabel);
        topPanel.add(amountField);
        topPanel.add(dateLabel);
        topPanel.add(dateField);
        topPanel.add(categoryLabel);
        topPanel.add(categoryCombo);
        topPanel.add(miscDescLabel);
        topPanel.add(miscDescField);
        topPanel.add(new JLabel(""));
        topPanel.add(addButton);

        add(topPanel, BorderLayout.NORTH);

        // Middle panel for displaying expenses
        expenseList = new JList<>(expenseListModel);
        JScrollPane listScrollPane = new JScrollPane(expenseList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Expenses"));
        
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedExpense());

        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(listScrollPane, BorderLayout.CENTER);
        middlePanel.add(deleteButton, BorderLayout.SOUTH);

        add(middlePanel, BorderLayout.CENTER);

        // Bottom panel for expense summary
        summaryArea = new JTextArea(5, 20);
        summaryArea.setEditable(false);
        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder("Expense Summary"));
        
        JButton summaryButton = new JButton("Show Summary");
        summaryButton.addActionListener(e -> showSummary());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(summaryScrollPane, BorderLayout.CENTER);
        bottomPanel.add(summaryButton, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load existing expenses
        loadExpenses();

        // Center the window
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String amountText = amountField.getText();
        String dateText = dateField.getText();
        String category = (String) categoryCombo.getSelectedItem();
        String miscDesc = miscDescField.getText();

        if (amountText.isEmpty() || category == null || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount, date, and category.");
            return;
        }

        // Parse date
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse(dateText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use dd/MM/yyyy.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            Expense expense = new Expense(amount, date, category, miscDesc);
            expenses.add(expense);
            expenseListModel.addElement(expense.toString());
            saveExpenses();
            amountField.setText("");
            dateField.setText("");
            miscDescField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.");
        }
    }

    private void toggleMiscellaneousField() {
        String category = (String) categoryCombo.getSelectedItem();
        if ("Miscellaneous".equals(category)) {
            miscDescField.setEnabled(true);
        } else {
            miscDescField.setEnabled(false);
            miscDescField.setText("");
        }
    }

    private void deleteSelectedExpense() {
        int selectedIndex = expenseList.getSelectedIndex();
        if (selectedIndex != -1) {
            expenses.remove(selectedIndex);
            expenseListModel.remove(selectedIndex);
            saveExpenses();
        } else {
            JOptionPane.showMessageDialog(this, "No expense selected.");
        }
    }

    private void showSummary() {
    double total = 0;
    double foodTotal = 0, transportTotal = 0, entertainmentTotal = 0, miscTotal = 0;

    for (Expense expense : expenses) {
        total += expense.getAmount();
        switch (expense.getCategory()) {
            case "Food":
                foodTotal += expense.getAmount();
                break;
            case "Transport":
                transportTotal += expense.getAmount();
                break;
            case "Entertainment":
                entertainmentTotal += expense.getAmount();
                break;
            case "Miscellaneous":
                miscTotal += expense.getAmount();
                break;
        }
    }

    summaryArea.setText("");
    // Show individual category totals
    if (foodTotal > 0) {
        summaryArea.append("Food: $" + foodTotal + "\n");
    }
    if (transportTotal > 0) {
        summaryArea.append("Transport: $" + transportTotal + "\n");
    }
    if (entertainmentTotal > 0) {
        summaryArea.append("Entertainment: $" + entertainmentTotal + "\n");
    }
    if (miscTotal > 0) {
        summaryArea.append("Miscellaneous: $" + miscTotal + "\n");
    }
    
    // Show grand total
    summaryArea.append("Grand Total: $" + total + "\n");
}


    private void saveExpenses() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("expenses.dat"))) {
            oos.writeObject(expenses);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadExpenses() {
        File file = new File("expenses.dat");
        
        // Check if file exists
        if (!file.exists()) {
            // If the file doesn't exist, create it
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;  // No need to load anything if file is new
        }

        // If file exists, load expenses from it
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            expenses = (List<Expense>) ois.readObject();
            for (Expense expense : expenses) {
                expenseListModel.addElement(expense.toString());
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    // Inner class to represent an expense
    static class Expense implements Serializable {
        private double amount;
        private Date date;
        private String category;
        private String description;

        public Expense(double amount, Date date, String category, String description) {
            this.amount = amount;
            this.date = date;
            this.category = category;
            this.description = description;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = formatter.format(date);
            return category.equals("Miscellaneous") ? 
                dateStr + " | " + category + ": $" + amount + " (" + description + ")" : 
                dateStr + " | " + category + ": $" + amount;
        }
    }

    public static void main(String[] args) {
        // Set the look and feel to make the UI more material-like
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the application
        SwingUtilities.invokeLater(ExpenseTracker::new);
    }
}
