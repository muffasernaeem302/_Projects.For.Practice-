import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Single-file Expense Tracker Application
 * A complete standalone application with GUI and data persistence
 */
public class ExpenseTrackerApp extends JFrame {

    // Model Classes
    enum TransactionType {
        INCOME, EXPENSE
    }

    static class Transaction {
        private String id;
        private LocalDate date;
        private String category;
        private TransactionType type;
        private double amount;
        private String note;
        private String person;

        public Transaction(String id, LocalDate date, String category, TransactionType type, double amount, String note,
                String person) {
            this.id = id;
            this.date = date;
            this.category = category;
            this.type = type;
            this.amount = amount;
            this.note = note;
            this.person = person;
        }

        public Transaction(LocalDate date, String category, TransactionType type, double amount, String note,
                String person) {
            this(UUID.randomUUID().toString(), date, category, type, amount, note, person);
        }

        public String getId() {
            return id;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public TransactionType getType() {
            return type;
        }

        public double getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }

        public String getPerson() {
            return person;
        }
    }

    static class Budget {
        private double targetIncome;
        private double targetExpense;
        private double targetSavings;

        public Budget(double targetIncome, double targetExpense, double targetSavings) {
            this.targetIncome = targetIncome;
            this.targetExpense = targetExpense;
            this.targetSavings = targetSavings;
        }

        public double getTargetIncome() {
            return targetIncome;
        }

        public double getTargetExpense() {
            return targetExpense;
        }

        public double getTargetSavings() {
            return targetSavings;
        }
    }

    // Repository Classes
    static class TransactionRepository {
        private static final String FILE_PATH = "data/transactions.csv";
        private final Path path;

        public TransactionRepository() {
            this.path = Paths.get(FILE_PATH);
            try {
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                if (!Files.exists(path)) {
                    Files.createFile(path);
                    Files.writeString(path, "id,date,category,type,amount,note,person\n", StandardOpenOption.APPEND);
                }
            } catch (IOException e) {
                System.err.println("Error initializing TransactionRepository: " + e.getMessage());
            }
        }

        public void save(Transaction transaction) {
            String line = String.format(Locale.US, "%s,%s,%s,%s,%.2f,%s,%s\n",
                    transaction.getId(),
                    transaction.getDate(),
                    transaction.getCategory(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getNote(),
                    transaction.getPerson());
            try {
                Files.writeString(path, line, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Error saving transaction: " + e.getMessage());
            }
        }

        public List<Transaction> findAll() {
            List<Transaction> transactions = new ArrayList<>();
            try {
                List<String> lines = Files.readAllLines(path);
                if (lines.size() <= 1)
                    return transactions;

                transactions = lines.stream()
                        .skip(1)
                        .filter(line -> !line.trim().isEmpty())
                        .map(line -> {
                            String[] parts = line.split(",", 7);
                            if (parts.length >= 5) {
                                return new Transaction(
                                        parts[0],
                                        LocalDate.parse(parts[1]),
                                        parts[2],
                                        TransactionType.valueOf(parts[3]),
                                        Double.parseDouble(parts[4]),
                                        parts.length >= 6 ? parts[5] : "",
                                        parts.length == 7 ? parts[6] : "None");
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                System.err.println("Error reading transactions: " + e.getMessage());
            }
            return transactions;
        }

        public void deleteById(String id) {
            List<Transaction> all = findAll();
            try {
                Files.writeString(path, "id,date,category,type,amount,note,person\n",
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                for (Transaction t : all) {
                    if (!t.getId().equals(id)) {
                        save(t);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error deleting transaction: " + e.getMessage());
            }
        }
    }

    static class BudgetRepository {
        private static final String FILE_PATH = "data/budget.csv";
        private final Path path;

        public BudgetRepository() {
            this.path = Paths.get(FILE_PATH);
            try {
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                if (!Files.exists(path)) {
                    Files.createFile(path);
                    save(new Budget(5000.0, 3000.0, 1000.0));
                }
            } catch (IOException e) {
                System.err.println("Error initializing BudgetRepository: " + e.getMessage());
            }
        }

        public void save(Budget budget) {
            String line = String.format(Locale.US, "%.2f,%.2f,%.2f\n",
                    budget.getTargetIncome(),
                    budget.getTargetExpense(),
                    budget.getTargetSavings());
            try {
                Files.writeString(path, line, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                System.err.println("Error saving budget: " + e.getMessage());
            }
        }

        public Budget load() {
            try {
                List<String> lines = Files.readAllLines(path);
                if (!lines.isEmpty()) {
                    String[] parts = lines.get(0).split(",");
                    if (parts.length == 3) {
                        return new Budget(
                                Double.parseDouble(parts[0]),
                                Double.parseDouble(parts[1]),
                                Double.parseDouble(parts[2]));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error reading budget, using defaults: " + e.getMessage());
            }
            return new Budget(5000.0, 3000.0, 1000.0);
        }
    }

    // Service Classes
    static class FinanceAnalyzer {
        public double calculateTotalIncome(List<Transaction> transactions, LocalDate month) {
            return transactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .filter(t -> t.getDate().getMonth() == month.getMonth() && t.getDate().getYear() == month.getYear())
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        }

        public double calculateTotalExpense(List<Transaction> transactions, LocalDate month) {
            return transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getDate().getMonth() == month.getMonth() && t.getDate().getYear() == month.getYear())
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        }

        public Map<String, Double> getExpensesByCategory(List<Transaction> transactions, LocalDate month) {
            return transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getDate().getMonth() == month.getMonth() && t.getDate().getYear() == month.getYear())
                    .collect(Collectors.groupingBy(Transaction::getCategory,
                            Collectors.summingDouble(Transaction::getAmount)));
        }

        public Map.Entry<String, Double> getTopSpendingCategory(List<Transaction> transactions, LocalDate month) {
            return getExpensesByCategory(transactions, month).entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
        }

        public double getAverageDailySpend(List<Transaction> transactions, LocalDate month) {
            double totalExpense = calculateTotalExpense(transactions, month);
            int daysInMonth = month.lengthOfMonth();
            int currentDay = month.getMonth() == LocalDate.now().getMonth()
                    && month.getYear() == LocalDate.now().getYear()
                            ? LocalDate.now().getDayOfMonth()
                            : daysInMonth;

            return currentDay > 0 ? totalExpense / currentDay : 0;
        }
    }

    static class ExportService {
        public void exportToCsv(String filename, List<Transaction> transactions) {
            Path path = Paths.get(filename);
            try {
                if (!Files.exists(path.getParent()) && path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                StringBuilder csv = new StringBuilder();
                csv.append("Date,Category,Type,Amount,Note,Person\n");
                for (Transaction t : transactions) {
                    csv.append(String.format(Locale.US, "%s,\"%s\",%s,%.2f,\"%s\",\"%s\"\n",
                            t.getDate(), t.getCategory(), t.getType(), t.getAmount(), t.getNote(), t.getPerson()));
                }
                Files.writeString(path, csv.toString(), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("CSV successfully exported to: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error exporting CSV: " + e.getMessage());
            }
        }
    }

    // GUI Components
    private TransactionRepository transactionRepository;
    private BudgetRepository budgetRepository;
    private FinanceAnalyzer financeAnalyzer;
    private ExportService exportService;

    private JProgressBar incomeBar, expenseBar, savingsBar;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel analyticsLabel;
    private JTextField searchField;

    public ExpenseTrackerApp() {
        transactionRepository = new TransactionRepository();
        budgetRepository = new BudgetRepository();
        financeAnalyzer = new FinanceAnalyzer();
        exportService = new ExportService();

        setTitle("FINANCE_CLI // EXPENSE TRACKER");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(30, 30, 30));
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Panel.background", new Color(30, 30, 30));
        UIManager.put("Button.background", new Color(50, 50, 50));
        UIManager.put("Button.foreground", Color.WHITE);
        Font monospace = new Font("Consolas", Font.PLAIN, 14);

        setLayout(new BorderLayout(10, 10));

        JLabel headerLabel = new JLabel(
                "<html><b><font color='#e5c07b'>FINANCE_CLI // EXPENSE TRACKER</font></b></html>");
        headerLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerLabel, BorderLayout.NORTH);
        topPanel.add(createDashboardPanel(monospace), BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        centerPanel.add(createMenuPanel(monospace), BorderLayout.WEST);
        centerPanel.add(createTablePanel(monospace), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        analyticsLabel = new JLabel("Loading...");
        analyticsLabel.setFont(monospace);
        analyticsLabel.setForeground(new Color(86, 182, 194));
        analyticsLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        add(analyticsLabel, BorderLayout.SOUTH);

        updateDashboard();
    }

    private JPanel createDashboardPanel(Font font) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        incomeBar = createBar(new Color(152, 195, 121));
        expenseBar = createBar(new Color(224, 108, 117));
        savingsBar = createBar(new Color(229, 192, 123));

        panel.add(createBarPanel("INCOME", incomeBar, font));
        panel.add(createBarPanel("EXPENSES", expenseBar, font));
        panel.add(createBarPanel("TOTAL SAVINGS", savingsBar, font));
        return panel;
    }

    private JProgressBar createBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setForeground(color);
        bar.setBackground(new Color(50, 50, 50));
        return bar;
    }

    private JPanel createBarPanel(String title, JProgressBar bar, Font font) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        JLabel l = new JLabel(title);
        l.setFont(font);
        p.add(l, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    private JPanel createMenuPanel(Font font) {
        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 0, 10));
        TitledBorder border = BorderFactory.createTitledBorder("MAIN MENU:");
        border.setTitleColor(new Color(229, 192, 123));
        border.setTitleFont(font.deriveFont(Font.BOLD));
        menuPanel.setBorder(border);
        menuPanel.setPreferredSize(new Dimension(250, 0));

        String[] options = { "1. ADD TRANSACTION [I/O]", "2. ADD BUDGET LIMIT [GOALS]", "3. REFRESH DATA",
                "4. EXPORT TO CSV", "5. EXIT APP" };
        for (int i = 0; i < options.length; i++) {
            JButton btn = new JButton(options[i]);
            btn.setFont(font);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            final int index = i;
            btn.addActionListener(e -> handleMenu(index));
            menuPanel.add(btn);
        }
        return menuPanel;
    }

    private void handleMenu(int index) {
        if (index == 0) {
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.add(new JLabel("Category:"));
            JTextField catField = new JTextField();
            panel.add(catField);

            panel.add(new JLabel("Type:"));
            JComboBox<String> typeBox = new JComboBox<>(new String[] { "EXPENSE", "INCOME" });
            panel.add(typeBox);

            panel.add(new JLabel("Amount:"));
            JTextField amtField = new JTextField();
            panel.add(amtField);

            panel.add(new JLabel("Note:"));
            JTextField noteField = new JTextField();
            panel.add(noteField);

            panel.add(new JLabel("Person:"));
            JTextField personField = new JTextField();
            panel.add(personField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Add Transaction", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String cat = catField.getText().trim();
                    TransactionType type = TransactionType.valueOf((String) typeBox.getSelectedItem());
                    double amt = Double.parseDouble(amtField.getText().trim().replace(",", "."));
                    String note = noteField.getText().trim();
                    String person = personField.getText().trim();
                    if (cat.isEmpty())
                        throw new Exception("Category cannot be empty");

                    transactionRepository.save(new Transaction(LocalDate.now(), cat, type, amt,
                            note.isEmpty() ? "GUI" : note, person.isEmpty() ? "None" : person));
                    updateDashboard();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid format! " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (index == 1) {
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            panel.add(new JLabel("Income Goal:"));
            JTextField incField = new JTextField();
            panel.add(incField);
            panel.add(new JLabel("Expense Limit:"));
            JTextField expField = new JTextField();
            panel.add(expField);
            panel.add(new JLabel("Savings Goal:"));
            JTextField savField = new JTextField();
            panel.add(savField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Add Budget Limits", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double inc = Double.parseDouble(incField.getText().trim().replace(",", "."));
                    double exp = Double.parseDouble(expField.getText().trim().replace(",", "."));
                    double sav = Double.parseDouble(savField.getText().trim().replace(",", "."));
                    budgetRepository.save(new Budget(inc, exp, sav));
                    updateDashboard();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid format! Limits must be numbers.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (index == 2) {
            updateDashboard();
        } else if (index == 3) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export to CSV");
            fileChooser.setSelectedFile(new File("finance_export.csv"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                exportService.exportToCsv(fileToSave.getAbsolutePath(), transactionRepository.findAll());
                JOptionPane.showMessageDialog(this, "Exported successfully to " + fileToSave.getName());
            }
        } else if (index == 4) {
            System.exit(0);
        }
    }

    private JPanel createTablePanel(Font font) {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        TitledBorder border = BorderFactory.createTitledBorder("> ALL ACTIVITY:");
        border.setTitleColor(new Color(229, 192, 123));
        border.setTitleFont(font.deriveFont(Font.BOLD));
        tablePanel.setBorder(border);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(font);
        searchField = new JTextField();
        searchField.setBackground(new Color(50, 50, 50));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(font);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateDashboard();
            }

            public void removeUpdate(DocumentEvent e) {
                updateDashboard();
            }

            public void changedUpdate(DocumentEvent e) {
                updateDashboard();
            }
        });
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] cols = { "DATE", "CATEGORY", "TYPE", "AMOUNT", "PERSON", "NOTE", "ID" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel) {
            public String getToolTipText(java.awt.event.MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    if (rowIndex >= 0 && colIndex >= 0) {
                        Object value = getValueAt(rowIndex, colIndex);
                        if (value != null)
                            tip = value.toString();
                    }
                } catch (RuntimeException e1) {
                }
                return tip;
            }
        };
        table.setFont(font);
        table.setBackground(new Color(40, 40, 40));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 60));
        table.getTableHeader().setFont(font.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // DATE
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // CATEGORY
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // TYPE
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // AMOUNT
        table.getColumnModel().getColumn(4).setPreferredWidth(180); // PERSON
        table.getColumnModel().getColumn(5).setPreferredWidth(250); // NOTE

        // Hide ID column
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setWidth(0);

        JScrollPane scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(new Color(40, 40, 40));
        tablePanel.add(scroll, BorderLayout.CENTER);

        // Delete Button
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setFont(font);
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String id = (String) tableModel.getValueAt(selectedRow, 6);
                transactionRepository.deleteById(id);
                updateDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            }
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(deleteBtn);
        tablePanel.add(bottomPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private void updateDashboard() {
        List<Transaction> transactions = transactionRepository.findAll();
        Budget budget = budgetRepository.load();
        LocalDate now = LocalDate.now();

        double totalInc = financeAnalyzer.calculateTotalIncome(transactions, now);
        double totalExp = financeAnalyzer.calculateTotalExpense(transactions, now);
        double sav = totalInc - totalExp;

        double incPct = budget.getTargetIncome() > 0 ? (totalInc / budget.getTargetIncome()) * 100 : 0;
        double expPct = budget.getTargetExpense() > 0 ? (totalExp / budget.getTargetExpense()) * 100 : 0;
        double savPct = budget.getTargetSavings() > 0 ? (sav / budget.getTargetSavings()) * 100 : 0;

        incomeBar.setValue(Math.min((int) incPct, 100));
        incomeBar.setString(String.format("%d%%", (int) incPct));

        expenseBar.setValue(Math.min((int) expPct, 100));
        expenseBar.setString(String.format("%d%%", (int) expPct));

        savingsBar.setValue(Math.min((int) savPct, 100));
        savingsBar.setString(String.format("$%.0f [%d%% goal]", sav, (int) savPct));

        String query = searchField.getText().trim().toLowerCase();
        List<Transaction> filtered = transactions.stream()
                .filter(t -> query.isEmpty() || t.getCategory().toLowerCase().contains(query)
                        || t.getNote().toLowerCase().contains(query) || t.getPerson().toLowerCase().contains(query))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (Transaction t : filtered) {
            String prefix = t.getType() == TransactionType.EXPENSE ? "- $" : "+ $";
            tableModel.addRow(new Object[] { t.getDate(), t.getCategory(), t.getType(),
                    prefix + String.format("%.2f", t.getAmount()), t.getPerson(), t.getNote(), t.getId() });
        }

        Map.Entry<String, Double> top = financeAnalyzer.getTopSpendingCategory(transactions, now);
        double avg = financeAnalyzer.getAverageDailySpend(transactions, now);
        String topCat = top != null ? top.getKey() : "N/A";
        double topPct = top != null && totalExp > 0 ? (top.getValue() / totalExp) * 100 : 0;

        analyticsLabel.setText(
                String.format("LIVE ANALYTICS: TOTAL EXPENSES: $%.2f | TOP SPENDING: %s (%.0f%%) | AVG DAILY: $%.2f",
                        totalExp, topCat, topPct, avg));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ExpenseTrackerApp().setVisible(true);
        });
    }
}