package inventoryStocker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections; 
import java.util.List;

// We will build our own Hash Table class, as per the ADS syllabus
class ProductHashTable {

    // Inner class to store product data
    // Implements Comparable to allow sorting by name
    class Product implements Comparable<Product> {
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

        /**
         * Allows Collections.sort() to sort Products by name (default)
         */
        @Override
        public int compareTo(Product other) {
            // Sorts alphabetically by name, ignoring case
            return this.name.compareToIgnoreCase(other.name);
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
        insert("A-100", "Wireless Mouse", 150, true);
        insert("A-150", "Bluetooth Mouse", 50, true);
        insert("M-100", "Monitor 24in", 40, true);
        insert("K-106", "Gaming Keyboard", 80, true); // Causes collision
        insert("L-301", "Acer Laptop 15in", 25, true);
        insert("A-199", "USB-C Mouse", 75, true);
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

        if (!isMock) {
            analysisLogger.setText("--- ANALYSIS: INSERT/UPDATE ---\n");
            analysisLogger.append("Processing '" + name + "' with SKU: " + sku + "\n");
            analysisLogger.append("Initial Hash: " + hashVal + "\n");
        }

        while (table[currentHash] != null) {
            if (table[currentHash].sku.equals(sku)) {
                // --- THIS IS THE FIX ---
                table[currentHash].name = name;         // MODIFIED: Update name
                table[currentHash].quantity = quantity; // MODIFIED: Update quantity
                if (!isMock) analysisLogger.append("SUCCESS: Item updated at index " + currentHash + ".\n");
                
                // Update in allProductsList as well
                for (Product p : allProductsList) {
                    if (p.sku.equals(sku)) {
                        p.name = name;         // MODIFIED: Update name
                        p.quantity = quantity; // MODIFIED: Update quantity
                        break;
                    }
                }
                return;
                // --- END OF FIX ---
            }
            probes++;
            if (!isMock) analysisLogger.append("Collision at index " + currentHash + "! Probing...\n");
            currentHash = (currentHash + 1) % maxSize; // Go to next slot

            if (currentHash == hashVal) {
                if (!isMock) analysisLogger.append("ERROR: Hash table is full.\n");
                return;
            }
        }

        table[currentHash] = item;
        allProductsList.add(item); // Also add to linear list

        long endTime = System.nanoTime();
        if (!isMock) {
            analysisLogger.append("SUCCESS: New item stored at index " + currentHash + ".\n");
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
     * Find item by Name using Linear Search (Slow) - Module 1
     */
    public List<Product> findLinearByName(String name) {
        long startTime = System.nanoTime();
        int comparisons = 0;
        List<Product> results = new ArrayList<>();
        String searchName = name.toLowerCase(); // Not case-sensitive

        analysisLogger.setText("--- ANALYSIS: LINEAR SEARCH (O(n)) ---\n");
        analysisLogger.append("Searching entire list (Size: " + allProductsList.size() + ") for name containing '" + name + "'\n");

        for (Product p : allProductsList) {
            comparisons++;
            if (p.name.toLowerCase().contains(searchName)) {
                results.add(p);
            }
        }

        long endTime = System.nanoTime();
        if (results.isEmpty()) {
            analysisLogger.append("FAILURE: No items found.\n");
        } else {
            analysisLogger.append("SUCCESS: Found " + results.size() + " item(s).\n");
        }
        analysisLogger.append("Comparisons (Array Lookups): " + comparisons + "\n");
        analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
        return results;
    }
    
    /**
     * Deletes an item by SKU and re-hashes the probing cluster.
     */
    public boolean delete(String sku) {
        long startTime = System.nanoTime();
        int hashVal = hashFunc(sku);
        int startVal = hashVal;

        analysisLogger.setText("--- ANALYSIS: DELETE ---\n");
        analysisLogger.append("Searching for SKU: " + sku + " to delete.\n");

        // 1. Find the item to delete
        while (table[hashVal] != null) {
            if (table[hashVal].sku.equals(sku)) {
                // Found it!
                Product itemToDelete = table[hashVal];
                analysisLogger.append("SUCCESS: Found " + itemToDelete.name + " at index " + hashVal + ".\n");
                
                // 2. Remove it from the table, leaving a null "hole"
                table[hashVal] = null; 
                analysisLogger.append("Removed from table. Now rehashing cluster...\n");

                // 3. Remove it from the linear list
                allProductsList.remove(itemToDelete);

                // 4. Now, re-hash the "cluster" of items that followed it
                List<Product> itemsToRehash = new ArrayList<>();
                int probeIndex = (hashVal + 1) % maxSize;

                while (table[probeIndex] != null) {
                    Product item = table[probeIndex];
                    itemsToRehash.add(item);
                    table[probeIndex] = null; // Clear the slot
                    probeIndex = (probeIndex + 1) % maxSize;
                    if (probeIndex == startVal) break; // Safety break
                }

                // 5. Re-insert all items that were in the cluster
                for (Product item : itemsToRehash) {
                    insert(item.sku, item.name, item.quantity, true); 
                }
                
                long endTime = System.nanoTime();
                analysisLogger.append("SUCCESS: Deletion complete. Rehashed " + itemsToRehash.size() + " items.\n");
                analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
                return true;
            }
            hashVal = (hashVal + 1) % maxSize; // Probe to next slot
            
            if (hashVal == startVal) {
                break; // Full circle, item not found
            }
        }

        analysisLogger.append("FAILURE: Item not found. Cannot delete.\n");
        return false;
    }

    /**
     * Uses Java's built-in sort (O(n log n)) on the linear list BY NAME.
     */
    public List<Product> getSortedListByName() {
        analysisLogger.setText("--- ANALYSIS: SORT (O(n log n)) ---\n");
        analysisLogger.append("Sorting all items by name...\n");
        
        // This sorts using the default "compareTo" method in the Product class
        Collections.sort(allProductsList);
        
        analysisLogger.append("Sort complete. Displaying " + allProductsList.size() + " items.\n");
        return allProductsList;
    }
    
    /**
     * Uses Java's built-in sort (O(n log n)) on the linear list BY SKU.
     */
    public List<Product> getSortedListBySku() {
        analysisLogger.setText("--- ANALYSIS: SORT (O(n log n)) ---\n");
        analysisLogger.append("Sorting all items by SKU...\n");

        // This uses a custom "Comparator" (as a lambda) to sort by SKU
        allProductsList.sort((p1, p2) -> p1.sku.compareToIgnoreCase(p2.sku));
        
        analysisLogger.append("Sort complete. Displaying " + allProductsList.size() + " items.\n");
        return allProductsList;
    }
    
    /**
     * Finds all products within a given SKU range (inclusive).
     */
    public List<Product> findSkuRange(String startSku, String endSku) {
        long startTime = System.nanoTime();
        int comparisons = 0;
        List<Product> results = new ArrayList<>();
        
        String start = startSku.toLowerCase();
        String end = endSku.toLowerCase();

        analysisLogger.setText("--- ANALYSIS: SKU RANGE QUERY (O(n)) ---\n");
        analysisLogger.append("Scanning list (Size: " + allProductsList.size() + ") for SKU range: " + startSku + " to " + endSku + "\n");

        for (Product p : allProductsList) {
            comparisons++;
            String currentSku = p.sku.toLowerCase();
            // Check if currentSku is >= start AND <= end
            if (currentSku.compareTo(start) >= 0 && currentSku.compareTo(end) <= 0) {
                results.add(p);
            }
        }
        
        Collections.sort(results); // Sort the small results list by name

        long endTime = System.nanoTime();
        if (results.isEmpty()) {
            analysisLogger.append("FAILURE: No items found in range.\n");
        } else {
            analysisLogger.append("SUCCESS: Found " + results.size() + " item(s).\n");
        }
        analysisLogger.append("Comparisons: " + comparisons + "\n");
        analysisLogger.append("Time: " + (endTime - startTime) / 1000 + " \u00B5s\n");
        return results;
    }
}


// --- Main class with the Swing UI ---
public class InventoryStocker extends JFrame implements ActionListener {

    // --- Data Structure ---
    private ProductHashTable hashTable;

    // --- UI Components ---
    private JTextField skuField, nameField, quantityField;
    private JTextField searchSkuField, searchNameField; 
    private JTextField searchSkuStartField, searchSkuEndField; 
    private JButton addButton, findHashButton, deleteButton, findNameButton, sortByNameButton, sortBySkuButton, findSkuRangeButton; 
    private JTextArea analysisArea;
    private JTextArea resultArea; 

    public InventoryStocker() {
        // --- 1. Setup the main window ---
        setTitle("ADS Project: Inventory System (v7 - Full Update)");
        setSize(850, 850); 
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
        findSkuPanel.setBorder(BorderFactory.createTitledBorder("2. Find/Delete by SKU (Hash Search - O(1))"));
        
        JPanel findNamePanel = new JPanel(new BorderLayout(10, 5));
        findNamePanel.setBorder(BorderFactory.createTitledBorder("3. Find by Name (Linear Search - O(n))"));
        
        JPanel findSkuRangePanel = new JPanel(new BorderLayout(10, 5));
        findSkuRangePanel.setBorder(BorderFactory.createTitledBorder("4. Find by SKU Range (Linear Scan - O(n))"));

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        sortPanel.setBorder(BorderFactory.createTitledBorder("5. View All Items (Sorted)"));
        
        JPanel outputPanel = new JPanel(new GridLayout(2, 1, 10, 10)); 
        JPanel resultPanel = new JPanel(new BorderLayout());
        JPanel analysisPanel = new JPanel(new BorderLayout());

        // --- 3. Create "Add Item" Components ---
        JPanel addFields = new JPanel(new GridLayout(3, 2, 5, 5));
        addFields.add(new JLabel("Product SKU:"));
        skuField = new JTextField(10);
        addFields.add(skuField);
        addFields.add(new JLabel("")); 
        addFields.add(new JLabel("<html><i>(e.g., 'A-123', 'XPS-13')</i></html>"));
        
        addFields.add(new JLabel("Product Name:"));
        nameField = new JTextField(15);
        addFields.add(nameField);
        addFields.add(new JLabel("")); 
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
        skuSearchInner.add(new JLabel("Enter SKU:"));
        searchSkuField = new JTextField(15);
        skuSearchInner.add(searchSkuField);
        
        findHashButton = new JButton("Find (Hash Search)");
        findHashButton.addActionListener(this);
        
        deleteButton = new JButton("Delete by SKU");
        deleteButton.addActionListener(this);
        
        JPanel skuActions = new JPanel(new GridLayout(1, 2, 5, 0));
        skuActions.add(findHashButton);
        skuActions.add(deleteButton);
        
        findSkuPanel.add(skuSearchInner, BorderLayout.CENTER);
        findSkuPanel.add(skuActions, BorderLayout.EAST);
        
        // --- 5. "Search by Name" Components ---
        JPanel nameSearchInner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nameSearchInner.add(new JLabel("Enter Name to Find:"));
        searchNameField = new JTextField(15);
        nameSearchInner.add(searchNameField);
        
        findNameButton = new JButton("Find (Linear Search)");
        findNameButton.addActionListener(this);
        
        findNamePanel.add(nameSearchInner, BorderLayout.CENTER);
        findNamePanel.add(findNameButton, BorderLayout.EAST);

        // --- 6. "Search by SKU Range" Components ---
        JPanel skuRangeInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        skuRangeInner.add(new JLabel("Start SKU:"));
        searchSkuStartField = new JTextField(10);
        skuRangeInner.add(searchSkuStartField);
        skuRangeInner.add(new JLabel("End SKU:"));
        searchSkuEndField = new JTextField(10);
        skuRangeInner.add(searchSkuEndField);
        
        findSkuRangeButton = new JButton("Find Range");
        findSkuRangeButton.addActionListener(this);

        findSkuRangePanel.add(skuRangeInner, BorderLayout.CENTER);
        findSkuRangePanel.add(findSkuRangeButton, BorderLayout.EAST);

        // --- 7. "Sort All" Panel ---
        sortByNameButton = new JButton("Show All (Sorted by Name)");
        sortByNameButton.addActionListener(this);
        sortPanel.add(sortByNameButton);
        
        sortBySkuButton = new JButton("Show All (Sorted by SKU)");
        sortBySkuButton.addActionListener(this);
        sortPanel.add(sortBySkuButton);

        // --- 8. Create Display Components ---
        resultArea = new JTextArea(5, 70); 
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

        // --- 9. Setup the Data Structure ---
        hashTable = new ProductHashTable(20, analysisArea); // Table size of 20

        // --- 10. Assemble the UI ---
        topPanel.add(addPanel);
        topPanel.add(findSkuPanel);
        topPanel.add(findNamePanel); 
        topPanel.add(findSkuRangePanel); 
        topPanel.add(sortPanel); 
        
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
                resultArea.setText("Result: ERROR - 'SKU' field cannot be empty.");
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
        
        // --- ACTION 4: DELETE BY SKU ---
        } else if (e.getSource() == deleteButton) {
            String skuToDelete = searchSkuField.getText();
            if (skuToDelete.isEmpty()) {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: ERROR - 'SKU' field cannot be empty to delete.");
                return;
            }
            
            boolean deleted = hashTable.delete(skuToDelete);
            
            if (deleted) {
                resultArea.setForeground(new Color(0, 102, 0));
                resultArea.setText("Result: Successfully deleted SKU: " + skuToDelete);
            } else {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: Product with SKU '" + skuToDelete + "' not found.");
            }

        // --- ACTION 5: SORT ALL BY NAME ---
        } else if (e.getSource() == sortByNameButton) {
            List<ProductHashTable.Product> sortedList = hashTable.getSortedListByName();
            
            resultArea.setForeground(new Color(0, 102, 0));
            StringBuilder sb = new StringBuilder();
            sb.append("Result: " + sortedList.size() + " TOTAL ITEMS (SORTED BY NAME):\n");
            
            if(sortedList.isEmpty()) {
                sb.append("[No items in stock]");
            }
            
            for (ProductHashTable.Product p : sortedList) {
                sb.append("- " + p.toString() + "\n");
            }
            resultArea.setText(sb.toString());
            
        // --- ACTION 6: FIND BY SKU RANGE ---
        } else if (e.getSource() == findSkuRangeButton) {
            String startSku = searchSkuStartField.getText();
            String endSku = searchSkuEndField.getText();

            if (startSku.isEmpty() || endSku.isEmpty()) {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: ERROR - Both 'Start SKU' and 'End SKU' fields are required.");
                return;
            }
            
            if(startSku.compareToIgnoreCase(endSku) > 0) {
                 resultArea.setForeground(Color.RED);
                 resultArea.setText("Result: ERROR - 'Start SKU' must come before 'End SKU'.");
                 return;
            }
            
            List<ProductHashTable.Product> results = hashTable.findSkuRange(startSku, endSku);
            
             if (!results.isEmpty()) {
                resultArea.setForeground(new Color(0, 102, 0));
                StringBuilder sb = new StringBuilder();
                sb.append("Result: FOUND " + results.size() + " item(s) in SKU range:\n");
                for (ProductHashTable.Product p : results) {
                    sb.append("- " + p.toString() + "\n");
                }
                resultArea.setText(sb.toString());
            } else {
                resultArea.setForeground(Color.RED);
                resultArea.setText("Result: No products found in SKU range: " + startSku + " to " + endSku);
            }
            
        // --- ACTION 7: SORT ALL BY SKU ---
        } else if (e.getSource() == sortBySkuButton) {
            List<ProductHashTable.Product> sortedList = hashTable.getSortedListBySku();
            
            resultArea.setForeground(new Color(0, 102, 0));
            StringBuilder sb = new StringBuilder();
            sb.append("Result: " + sortedList.size() + " TOTAL ITEMS (SORTED BY SKU):\n");
            
            if(sortedList.isEmpty()) {
                sb.append("[No items in stock]");
            }
            
            for (ProductHashTable.Product p : sortedList) {
                sb.append("- " + p.toString() + "\n");
            }
            resultArea.setText(sb.toString());
        }
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InventoryStocker();
            }
        });
    }
}