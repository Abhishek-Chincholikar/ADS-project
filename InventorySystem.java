package inventorySystem;
/*
 * -----------------------------------------------------------------------------
 * --- PROJECT 2: Inventory Stock Checker (v3 - Search by Name) ---
 * -----------------------------------------------------------------------------
 * This version adds two key features based on your feedback:
 * 1. A "Search by Name" button that uses LINEAR SEARCH (Module 1).
 * 2. A more user-friendly UI with helper text.
 *
 * This now demonstrates TWO different data structures/algorithms:
 * - HASH TABLE (Module 2) for fast O(1) SKU lookup.
 * - LINEAR SEARCH (Module 1) for slow O(n) name lookup.
 *
 * This perfectly matches the project requirements.
 * You might get multiple errors when you paste the code. Just go to (public class InventorySystem extends JFrame implements ActionListener) 
 * and Hover over ActionListener You will get a option add 'requires java.desktop' to module-info.java click on that and all errors resolved for me ( Eclipse IDE used Latest version)
 * 
 * 
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// We will build our own Hash Table class, as per the ADS syllabus
class ProductHashTable {

    // Inner class to store product data
    class Product {
        String sku;
        String name;
        int quantity;
        long hashVal; 

        public Product(String sku, String name, int quantity, long hashVal) {
            this.sku = sku;
            this.name = name;
            this.quantity = quantity;
            this.hashVal = hashVal;
        }

        @Override
        public String toString() {
            return "SKU: " + sku + ", Name: " + name + ", Stock: " + quantity;
        }
    }

    private Product[] table;
    private List<Product> allProductsList; // For Linear Search
    private int maxSize;
    private JTextArea analysisLogger; 

    public ProductHashTable(int size, JTextArea logger) {
        maxSize = size;
        table = new Product[maxSize];
        allProductsList = new ArrayList<>();
        this.analysisLogger = logger;
        
        // Add initial mock data
        insert("A-200", "Wireless Mouse", 150, true); 
        insert("K-106", "Gaming Keyboard", 80, true); // Causes collision with A-200
        insert("L-301", "Acer Laptop 15in", 25, true);
        insert("L-302", "Dell Laptop 13in", 30, true);
    }
    
    /**
     * Hashing Function (Module 2: Modulo Division)
     */
    private int hashFunc(String sku) {
        int hashVal = 0;
        for (int i = 0; i < sku.length(); i++) {
            hashVal += sku.charAt(i);
        }
        return hashVal % maxSize;
    }

    /**
     * Insert an item with Linear Probing (Module 2)
     */
    public void insert(String sku, String name, int quantity, boolean isMock) {
        long startTime = System.nanoTime();
        int hashVal = hashFunc(sku);
        int currentHash = hashVal;
        int probes = 0;
        
        Product item = new Product(sku, name, quantity, currentHash);
        
        if(!isMock) {
             analysisLogger.setText("--- ANALYSIS: INSERT ---\n");
             analysisLogger.append("Inserting '" + name + "' with SKU: " + sku + "\n");
             analysisLogger.append("Initial Hash: " + hashVal + "\n");
        }

        while (table[currentHash] != null) {
            if(table[currentHash].sku.equals(sku)) {
                 table[currentHash].quantity = quantity; // Update existing
                 if(!isMock) analysisLogger.append("SUCCESS: Item updated at index " + currentHash + ".\n");
                 // Update in allProductsList as well
                 for(Product p : allProductsList) {
                    if(p.sku.equals(sku)) {
                        p.quantity = quantity;
                        break;
                    }
                 }
                 return;
            }
            probes++;
            if(!isMock) analysisLogger.append("Collision at index " + currentHash + "! Probing...\n");
            currentHash = (currentHash + 1) % maxSize; // Go to next slot
            
            if(currentHash == hashVal) {
                 if(!isMock) analysisLogger.append("ERROR: Hash table is full.\n");
                 return;
            }
        }
        
        table[currentHash] = item;
        allProductsList.add(item); // Also add to linear list
        
        long endTime = System.nanoTime();
        if(!isMock) {
            analysisLogger.append("SUCCESS: Item stored at index " + currentHash + ".\n");
            analysisLogger.append("Probes: " + probes + "\n");
            analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
        }
    }
    
    public void insert(String sku, String name, int quantity) {
        insert(sku, name, quantity, false);
    }

    /**
     * Find item using Hashing with Linear Probing (Fast)
     */
    public ProductHashTable.Product findHash(String sku) {
        long startTime = System.nanoTime();
        int hashVal = hashFunc(sku);
        int startVal = hashVal;
        int probes = 0;

        analysisLogger.setText("--- ANALYSIS: HASH SEARCH (O(1) Average) ---\n");
        analysisLogger.append("Searching for SKU: " + sku + "\n");
        analysisLogger.append("Initial Hash: " + hashVal + "\n");

        while (table[hashVal] != null) {
            probes++;
            if (table[hashVal].sku.equals(sku)) {
                long endTime = System.nanoTime();
                analysisLogger.append("SUCCESS: Item found at index " + hashVal + ".\n");
                analysisLogger.append("Probes (Hash Lookups): " + probes + "\n");
                analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
                return table[hashVal];
            }
            hashVal = (hashVal + 1) % maxSize; // Go to next slot
            
            if (hashVal == startVal) {
                break; // Full circle, item not found
            }
        }
        
        long endTime = System.nanoTime();
        analysisLogger.append("FAILURE: Item not found.\n");
        analysisLogger.append("Probes (Hash Lookups): " + probes + "\n");
        analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
        return null; // Can't find item
    }
    
    /**
     * NEW: Find item by Name using Linear Search (Slow) - Module 1
     * This is what you asked for.
     */
    public List<Product> findLinearByName(String name) {
        long startTime = System.nanoTime();
        int comparisons = 0;
        List<Product> results = new ArrayList<>();
        String searchName = name.toLowerCase(); // Not case-sensitive

        analysisLogger.setText("--- ANALYSIS: LINEAR SEARCH (O(n)) ---\n");
        analysisLogger.append("Searching entire list (Size: " + allProductsList.size() + ") for name containing '" + name + "'\n");

        for(Product p : allProductsList) {
            comparisons++;
            if(p.name.toLowerCase().contains(searchName)) {
                results.add(p);
            }
        }
        
        long endTime = System.nanoTime();
        if(results.isEmpty()) {
            analysisLogger.append("FAILURE: No items found.\n");
        } else {
            analysisLogger.append("SUCCESS: Found " + results.size() + " item(s).\n");
        }
        analysisLogger.append("Comparisons (Array Lookups): " + comparisons + "\n");
        analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
        return results;
    }
}


// --- Main class with the Swing UI ---
public class InventorySystem extends JFrame implements ActionListener {

    // --- Data Structure ---
    private ProductHashTable hashTable;

    // --- UI Components ---
    private JTextField skuField, nameField, quantityField;
    private JTextField searchSkuField, searchNameField; // NEW field for name search
    private JButton addButton, findHashButton, findLinearButton, findNameButton; // NEW button
    private JTextArea analysisArea;
    private JTextArea resultArea; // CHANGED from JLabel to JTextArea for multiple results

    public InventorySystem() {
        // --- 1. Setup the main window ---
        setTitle("ADS Project: Inventory System (v3 - Hash vs. Linear)");
        setSize(850, 700); // Made window taller
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 2. Create Panels ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        JPanel addPanel = new JPanel(new BorderLayout(10, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("1. Add or Update Item"));
        
        JPanel findSkuPanel = new JPanel(new BorderLayout(10, 5));
        findSkuPanel.setBorder(BorderFactory.createTitledBorder("2. Find by SKU (Hash Search - O(1))"));
        
        // NEW Panel for searching by name
        JPanel findNamePanel = new JPanel(new BorderLayout(10, 5));
        findNamePanel.setBorder(BorderFactory.createTitledBorder("3. Find by Name (Linear Search - O(n))"));
        
        JPanel outputPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // Split output
        JPanel resultPanel = new JPanel(new BorderLayout());
        JPanel analysisPanel = new JPanel(new BorderLayout());

        // --- 3. Create "Add Item" Components (with helper text) ---
        JPanel addFields = new JPanel(new GridLayout(3, 2, 5, 5));
        addFields.add(new JLabel("Product SKU:"));
        skuField = new JTextField(10);
        addFields.add(skuField);
        addFields.add(new JLabel("")); // Spacer
        addFields.add(new JLabel("<html><i>(Unique ID, e.g., 'A-123', 'XPS-13')</i></html>"));
        
        addFields.add(new JLabel("Product Name:"));
        nameField = new JTextField(15);
        addFields.add(nameField);
        addFields.add(new JLabel("")); // Spacer
        addFields.add(new JLabel("<html><i>(e.g., 'Acer Laptop', 'Wireless Mouse')</i></html>"));
        
        addFields.add(new JLabel("Quantity:"));
        quantityField = new JTextField(5);
        addFields.add(quantityField);
        
        addButton = new JButton("Add/Update Item");
        addButton.addActionListener(this);
        
        addPanel.add(addFields, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.EAST);

        // --- 4. Create "Search by SKU" Components ---
        JPanel skuSearchInner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skuSearchInner.add(new JLabel("Enter SKU to Find:"));
        searchSkuField = new JTextField(15);
        skuSearchInner.add(searchSkuField);
        
        findHashButton = new JButton("Find (Hash Search)");
        findHashButton.addActionListener(this);
        
        findSkuPanel.add(skuSearchInner, BorderLayout.CENTER);
        findSkuPanel.add(findHashButton, BorderLayout.EAST);
        
        // --- 5. NEW "Search by Name" Components ---
        JPanel nameSearchInner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nameSearchInner.add(new JLabel("Enter Name to Find:"));
        searchNameField = new JTextField(15);
        nameSearchInner.add(searchNameField);
        
        findNameButton = new JButton("Find (Linear Search)");
        findNameButton.addActionListener(this);
        
        findNamePanel.add(nameSearchInner, BorderLayout.CENTER);
        findNamePanel.add(findNameButton, BorderLayout.EAST);

        // --- 6. Create Display Components ---
        resultArea = new JTextArea(5, 70); // Changed to JTextArea
        resultArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setForeground(new Color(0, 102, 0));
        resultPanel.add(new JLabel("Result:"), BorderLayout.NORTH);
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        resultArea.setText("[Waiting for action... Mock data inserted.]");

        analysisArea = new JTextArea(15, 70);
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        analysisArea.setForeground(Color.BLUE);
        analysisPanel.add(new JLabel("Analysis / Probing Log:"), BorderLayout.NORTH);
        analysisPanel.add(new JScrollPane(analysisArea), BorderLayout.CENTER);

        // --- 7. Setup the Data Structure ---
        hashTable = new ProductHashTable(20, analysisArea); // Table size of 20

        // --- 8. Assemble the UI ---
        topPanel.add(addPanel);
        topPanel.add(findSkuPanel);
        topPanel.add(findNamePanel); // Add the new panel
        
        outputPanel.add(resultPanel);
        outputPanel.add(analysisPanel);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(outputPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        setVisible(true);
    }

    /**
     * Handle button clicks
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // --- ACTION 1: ADD ITEM ---
        if (e.getSource() == addButton) {
            String sku = skuField.getText();
            String name = nameField.getText();
            
            if (sku.isEmpty() || name.isEmpty()) {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: ERROR - SKU and Name cannot be empty.");
                return;
            }
            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
            } catch (NumberFormatException ex) {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: ERROR - Quantity must be a number.");
                return;
            }
            
            hashTable.insert(sku, name, quantity);
            resultArea.setForeground(new Color(0, 102, 0));
            resultArea.setText("Result: Item Added/Updated: " + sku);
            
            skuField.setText("");
            nameField.setText("");
            quantityField.setText("");
            
        // --- ACTION 2: FIND BY SKU (HASH) ---
        } else if (e.getSource() == findHashButton) {
            String skuToFind = searchSkuField.getText();
            if (skuToFind.isEmpty()) {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: ERROR - 'SKU to Find' field cannot be empty.");
                return;
            }
            
            ProductHashTable.Product found = hashTable.findHash(skuToFind);
            
            if (found != null) {
                resultArea.setForeground(new Color(0, 102, 0));
                resultArea.setText("Result: FOUND! \n" + found.toString());
            } else {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: Product with SKU '" + skuToFind + "' not found.");
            }

        // --- ACTION 3: FIND BY NAME (LINEAR) ---
        } else if (e.getSource() == findNameButton) {
            String nameToFind = searchNameField.getText();
            if (nameToFind.isEmpty()) {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: ERROR - 'Name to Find' field cannot be empty.");
                return;
            }
            
            List<ProductHashTable.Product> results = hashTable.findLinearByName(nameToFind);
            
            if (!results.isEmpty()) {
                resultArea.setForeground(new Color(0, 102, 0));
                StringBuilder sb = new StringBuilder();
                sb.append("Result: FOUND " + results.size() + " item(s) containing '" + nameToFind + "':\n");
                for (ProductHashTable.Product p : results) {
                    sb.append("- " + p.toString() + "\n");
                }
                resultArea.setText(sb.toString());
            } else {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: No products found with name containing '" + nameToFind + "'.");
            }
        }
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InventorySystem();
            }
        });
    }

}

