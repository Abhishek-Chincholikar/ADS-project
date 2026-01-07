package inventoryStockerAdvanced;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.DefaultCategoryDataset;


/*
 ============================================================================
 ADS PROJECT: INVENTORY SYSTEM (ADVANCED)
 SINGLE FILE – SPLIT DELIVERY

 PART 1 / 2  (UI + CORE STRUCTURE)
 DO NOT RUN UNTIL PART 2 IS PASTED
 ============================================================================
*/

public class InventoryStocker extends JFrame implements ActionListener {

    /* ============================================================
       PRODUCT MODEL
       ============================================================ */
    static class Product {
        String sku;
        String name;
        int quantity;

        Product(String sku, String name, int quantity) {
            this.sku = sku;
            this.name = name;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "SKU: " + sku + " | Name: " + name + " | Qty: " + quantity;
        }
    }

    /* ============================================================
       DATA STRUCTURES (ADS)
       ============================================================ */
    private final List<Product> linearList = new ArrayList<>();
    private final Map<String, Product> hashMap = new HashMap<>();
    private final TreeMap<String, Product> treeMap = new TreeMap<>();

    /* ============================================================
       UI COMPONENTS
       ============================================================ */
    private JTextField skuField, nameField, qtyField;
    private JTextField searchSkuField, searchNameField;
    private JTextField rangeStartField, rangeEndField;

    private JButton addButton;
    private JButton updateButton;

    private JButton findHashButton;
    private JButton deleteButton;
    private JButton findLinearButton;
    private JButton findRangeButton;

    private JButton sortByNameButton;
    private JButton sortBySkuButton;

    // PART 2 buttons (declared now, implemented later)
    private JButton benchmarkSearchButton;
    private JButton benchmarkSortButton;
    private JButton showSearchGraphButton;
    private JButton showSortGraphButton;

    private JTextArea resultArea;
    private JTextArea analysisArea;

    /* ============================================================
       VALIDATION
       ============================================================ */
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z]-\\d{3}$");

    /* ============================================================
       BENCHMARK STORAGE (USED IN PART 2)
       ============================================================ */
    private long linearSearchTime;
    private long hashSearchTime;
    private long binarySearchTime;
    private long treeSearchTime;

    private long bubbleSortTime;
    private long insertionSortTime;
    private long selectionSortTime;
    private long mergeSortTime;
    private long quickSortTime;
    private long timSortTime;

    /* ============================================================
       CONSTRUCTOR
       ============================================================ */
    public InventoryStocker() {
        setTitle("ADS Project: Inventory System (Advanced)");
        setSize(900, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildMainPanel());
        preloadMockData();

        setVisible(true);
    }

    /* ============================================================
       UI BUILDERS (UNCHANGED STRUCTURE)
       ============================================================ */
    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        top.add(buildAddPanel());
        top.add(buildHashPanel());
        top.add(buildLinearPanel());
        top.add(buildRangePanel());
        top.add(buildSortPanel());
        top.add(buildBenchmarkPanel()); // buttons only, logic later

        main.add(top, BorderLayout.NORTH);
        main.add(buildOutputPanel(), BorderLayout.CENTER);

        return main;
    }

    private JPanel buildAddPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("1. Add or Update Item"));

        JPanel grid = new JPanel(new GridLayout(3, 2, 5, 5));

        skuField = new JTextField();
        nameField = new JTextField();
        qtyField = new JTextField();

        grid.add(new JLabel("Product SKU:"));
        grid.add(skuField);
        grid.add(new JLabel("Product Name:"));
        grid.add(nameField);
        grid.add(new JLabel("Quantity:"));
        grid.add(qtyField);

        addButton = new JButton("Add Item");
        updateButton = new JButton("Update Item");

        addButton.setEnabled(false);
        updateButton.setEnabled(false);

        addButton.addActionListener(this);
        updateButton.addActionListener(this);

        skuField.getDocument().addDocumentListener(
            new SimpleDocumentListener(this::validateSkuLive)
        );

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);

        panel.add(grid, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildHashPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(
            BorderFactory.createTitledBorder(
                "2. Find/Delete by SKU (Hash Search - O(1))"
            )
        );

        searchSkuField = new JTextField(15);
        findHashButton = new JButton("Find (Hash Search)");
        deleteButton = new JButton("Delete by SKU");

        findHashButton.addActionListener(this);
        deleteButton.addActionListener(this);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Enter SKU:"));
        left.add(searchSkuField);

        JPanel right = new JPanel(new GridLayout(1, 2, 5, 0));
        right.add(findHashButton);
        right.add(deleteButton);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildLinearPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(
            BorderFactory.createTitledBorder(
                "3. Find by Name (Linear Search - O(n))"
            )
        );

        searchNameField = new JTextField(15);
        findLinearButton = new JButton("Find (Linear Search)");
        findLinearButton.addActionListener(this);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Enter Name to Find:"));
        left.add(searchNameField);

        panel.add(left, BorderLayout.CENTER);
        panel.add(findLinearButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildRangePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(
            BorderFactory.createTitledBorder(
                "4. Find by SKU Range (Linear Scan - O(n))"
            )
        );

        rangeStartField = new JTextField(10);
        rangeEndField = new JTextField(10);
        findRangeButton = new JButton("Find Range");
        findRangeButton.addActionListener(this);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Start SKU:"));
        left.add(rangeStartField);
        left.add(new JLabel("End SKU:"));
        left.add(rangeEndField);

        panel.add(left, BorderLayout.CENTER);
        panel.add(findRangeButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildSortPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBorder(
            BorderFactory.createTitledBorder("5. View All Items (Sorted)")
        );

        sortByNameButton = new JButton("Show All (Sorted by Name)");
        sortBySkuButton = new JButton("Show All (Sorted by SKU)");

        sortByNameButton.addActionListener(this);
        sortBySkuButton.addActionListener(this);

        panel.add(sortByNameButton);
        panel.add(sortBySkuButton);

        return panel;
    }

    private JPanel buildBenchmarkPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBorder(
            BorderFactory.createTitledBorder("6. Algorithm Benchmarking")
        );

        benchmarkSearchButton = new JButton("Benchmark Searching");
        benchmarkSortButton = new JButton("Benchmark Sorting");
        showSearchGraphButton = new JButton("Show Search Graph");
        showSortGraphButton = new JButton("Show Sort Graph");

        benchmarkSearchButton.addActionListener(this);
        benchmarkSortButton.addActionListener(this);
        showSearchGraphButton.addActionListener(this);
        showSortGraphButton.addActionListener(this);

        panel.add(benchmarkSearchButton);
        panel.add(benchmarkSortButton);
        panel.add(showSearchGraphButton);
        panel.add(showSortGraphButton);

        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        resultArea = new JTextArea(6, 70);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        resultArea.setForeground(new Color(0, 102, 0));
        resultArea.setText("[Waiting for action... Mock data inserted.]");

        analysisArea = new JTextArea(14, 70);
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        analysisArea.setForeground(Color.BLUE);

        panel.add(new JScrollPane(resultArea));
        panel.add(new JScrollPane(analysisArea));

        return panel;
    }

    /* ============================================================
       VALIDATION (LIVE)
       ============================================================ */
    private void validateSkuLive() {
        String sku = skuField.getText().trim();
        boolean valid = SKU_PATTERN.matcher(sku).matches();
        boolean exists = hashMap.containsKey(sku);

        if (valid) {
            skuField.setBackground(new Color(200, 255, 200));
            addButton.setEnabled(!exists);
            updateButton.setEnabled(exists);
        } else {
            skuField.setBackground(new Color(255, 200, 200));
            addButton.setEnabled(false);
            updateButton.setEnabled(false);
        }
    }

    /* ============================================================
       MOCK DATA
       ============================================================ */
    private void preloadMockData() {
        addMock("A-101", "Wireless Mouse", 50);
        addMock("A-102", "Bluetooth Mouse", 40);
        addMock("M-201", "Monitor 24in", 20);
        addMock("K-301", "Mechanical Keyboard", 30);
        addMock("L-401", "Laptop 15in", 10);
    }

    private void addMock(String sku, String name, int qty) {
        Product p = new Product(sku, name, qty);
        hashMap.put(sku, p);
        treeMap.put(sku, p);
        linearList.add(p);
    }

    /* ============================================================
       ADD / UPDATE CORE
       ============================================================ */
    private void addProduct() {
        if (hashMap.containsKey(skuField.getText().trim())) {
            showError("SKU already exists. Use Update.");
            return;
        }
        saveProduct();
    }

    private void updateProduct() {
        if (!hashMap.containsKey(skuField.getText().trim())) {
            showError("SKU does not exist. Use Add.");
            return;
        }
        saveProduct();
    }

    private void saveProduct() {
        String sku = skuField.getText().trim();
        String name = nameField.getText().trim();
        int qty;

        if (name.isEmpty()) {
            showError("Product Name cannot be empty.");
            return;
        }

        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Quantity must be numeric.");
            return;
        }

        Product p = new Product(sku, name, qty);

        hashMap.put(sku, p);
        treeMap.put(sku, p);
        linearList.removeIf(x -> x.sku.equals(sku));
        linearList.add(p);

        resultArea.setForeground(new Color(0, 102, 0));
        resultArea.setText("Item saved:\n" + p);

        analysisArea.setText(
            "Stored in:\n" +
            "- HashMap → O(1)\n" +
            "- TreeMap → O(log n)\n" +
            "- Linear List → O(n)"
        );

        skuField.setText("");
        nameField.setText("");
        qtyField.setText("");
        skuField.setBackground(Color.WHITE);
        addButton.setEnabled(false);
        updateButton.setEnabled(false);
    }

    private void showError(String msg) {
        resultArea.setForeground(Color.RED);
        resultArea.setText("ERROR: " + msg);
    }

    /* ============================================================
       HELPER
       ============================================================ */
    private static class SimpleDocumentListener
            implements javax.swing.event.DocumentListener {

        private final Runnable r;

        SimpleDocumentListener(Runnable r) {
            this.r = r;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }

    /* ============================================================
       PART 2 CONTINUES BELOW
       DO NOT CLOSE CLASS
       ============================================================ */
    
    /* ============================================================
    ACTION HANDLER – COMPLETE
    ============================================================ */
 @Override
 public void actionPerformed(ActionEvent e) {

     Object src = e.getSource();

     if (src == addButton) {
         addProduct();
     }
     else if (src == updateButton) {
         updateProduct();
     }
     else if (src == findHashButton) {
         performHashSearch();
     }
     else if (src == deleteButton) {
         performDelete();
     }
     else if (src == findLinearButton) {
         performLinearSearch();
     }
     else if (src == findRangeButton) {
         performSkuRangeSearch();
     }
     else if (src == sortByNameButton) {
         showAllSortedByName();
     }
     else if (src == sortBySkuButton) {
         showAllSortedBySku();
     }
     else if (src == benchmarkSearchButton) {
         benchmarkSearching();
     }
     else if (src == benchmarkSortButton) {
         benchmarkSorting();
     }
     else if (src == showSearchGraphButton) {
         showSearchGraph();
     }
     else if (src == showSortGraphButton) {
         showSortGraph();
     }
 }

 /* ============================================================
    SEARCH OPERATIONS
    ============================================================ */

 private void performHashSearch() {
     String sku = searchSkuField.getText().trim();
     if (sku.isEmpty()) {
         showError("Enter SKU to search.");
         return;
     }

     long start = System.nanoTime();
     Product p = hashMap.get(sku);
     long end = System.nanoTime();

     if (p != null) {
         resultArea.setForeground(new Color(0, 102, 0));
         resultArea.setText("FOUND:\n" + p);
     } else {
         showError("Product not found.");
     }

     analysisArea.setText(
         "HASH SEARCH\n" +
         "Time: " + (end - start) + " ns\n" +
         "Complexity: O(1) average"
     );
 }

 private void performLinearSearch() {
     String name = searchNameField.getText().trim().toLowerCase();
     if (name.isEmpty()) {
         showError("Enter name to search.");
         return;
     }

     long start = System.nanoTime();
     List<Product> results = new ArrayList<>();
     for (Product p : linearList) {
         if (p.name.toLowerCase().contains(name)) {
             results.add(p);
         }
     }
     long end = System.nanoTime();

     if (results.isEmpty()) {
         showError("No matching products found.");
         return;
     }

     StringBuilder sb = new StringBuilder("FOUND:\n");
     for (Product p : results) sb.append(p).append("\n");

     resultArea.setForeground(new Color(0, 102, 0));
     resultArea.setText(sb.toString());

     analysisArea.setText(
         "LINEAR SEARCH\n" +
         "Items scanned: " + linearList.size() + "\n" +
         "Time: " + (end - start) + " ns\n" +
         "Complexity: O(n)"
     );
 }

 private void performSkuRangeSearch() {
     String startSku = rangeStartField.getText().trim();
     String endSku = rangeEndField.getText().trim();

     if (startSku.isEmpty() || endSku.isEmpty()) {
         showError("Enter both SKU range values.");
         return;
     }

     long start = System.nanoTime();
     List<Product> results = new ArrayList<>();

     for (Product p : linearList) {
         if (p.sku.compareToIgnoreCase(startSku) >= 0 &&
             p.sku.compareToIgnoreCase(endSku) <= 0) {
             results.add(p);
         }
     }
     long end = System.nanoTime();

     if (results.isEmpty()) {
         showError("No products in given range.");
         return;
     }

     results.sort(Comparator.comparing(a -> a.sku));

     StringBuilder sb = new StringBuilder("FOUND IN RANGE:\n");
     for (Product p : results) sb.append(p).append("\n");

     resultArea.setForeground(new Color(0, 102, 0));
     resultArea.setText(sb.toString());

     analysisArea.setText(
         "SKU RANGE SEARCH\n" +
         "Scanned: " + linearList.size() + "\n" +
         "Time: " + (end - start) + " ns\n" +
         "Complexity: O(n)"
     );
 }

 private void performDelete() {
     String sku = searchSkuField.getText().trim();
     if (sku.isEmpty()) {
         showError("Enter SKU to delete.");
         return;
     }

     Product removed = hashMap.remove(sku);
     treeMap.remove(sku);
     linearList.removeIf(p -> p.sku.equals(sku));

     if (removed != null) {
         resultArea.setForeground(new Color(0, 102, 0));
         resultArea.setText("DELETED:\n" + removed);
     } else {
         showError("Product not found.");
     }
 }

 /* ============================================================
    SORT DISPLAY
    ============================================================ */

 private void showAllSortedByName() {
     linearList.sort(Comparator.comparing(p -> p.name.toLowerCase()));

     StringBuilder sb = new StringBuilder("ALL ITEMS (SORTED BY NAME):\n");
     for (Product p : linearList) sb.append(p).append("\n");

     resultArea.setForeground(new Color(0, 102, 0));
     resultArea.setText(sb.toString());

     analysisArea.setText(
         "SORT BY NAME\n" +
         "Algorithm: TimSort\n" +
         "Complexity: O(n log n)"
     );
 }

 private void showAllSortedBySku() {
     linearList.sort(Comparator.comparing(p -> p.sku));

     StringBuilder sb = new StringBuilder("ALL ITEMS (SORTED BY SKU):\n");
     for (Product p : linearList) sb.append(p).append("\n");

     resultArea.setForeground(new Color(0, 102, 0));
     resultArea.setText(sb.toString());

     analysisArea.setText(
         "SORT BY SKU\n" +
         "Algorithm: TimSort\n" +
         "Complexity: O(n log n)"
     );
 }

 /* ============================================================
    BENCHMARKING – SEARCH
    ============================================================ */

 private void benchmarkSearching() {

     if (linearList.isEmpty()) {
         showError("No data to benchmark.");
         return;
     }

     Product target = linearList.get(linearList.size() / 2);

     long s, e;

     s = System.nanoTime();
     for (Product p : linearList) if (p.sku.equals(target.sku)) break;
     e = System.nanoTime();
     linearSearchTime = e - s;

     s = System.nanoTime();
     hashMap.get(target.sku);
     e = System.nanoTime();
     hashSearchTime = e - s;

     List<Product> sorted = new ArrayList<>(linearList);
     sorted.sort(Comparator.comparing(p -> p.sku));

     s = System.nanoTime();
     Collections.binarySearch(sorted, target, Comparator.comparing(p -> p.sku));
     e = System.nanoTime();
     binarySearchTime = e - s;

     s = System.nanoTime();
     treeMap.get(target.sku);
     e = System.nanoTime();
     treeSearchTime = e - s;

     exportCSV(
         "search_results.csv",
         new String[]{"Linear", "HashMap", "Binary", "TreeMap"},
         new long[]{linearSearchTime, hashSearchTime, binarySearchTime, treeSearchTime}
     );

     analysisArea.setText(
         "SEARCH BENCHMARK COMPLETE\n" +
         "Linear: " + linearSearchTime + " ns\n" +
         "HashMap: " + hashSearchTime + " ns\n" +
         "Binary: " + binarySearchTime + " ns\n" +
         "TreeMap: " + treeSearchTime + " ns"
     );
 }

 /* ============================================================
    BENCHMARKING – SORT
    ============================================================ */

 private void benchmarkSorting() {

     if (linearList.isEmpty()) {
         showError("No data to benchmark.");
         return;
     }

     bubbleSortTime = timeSort(this::bubbleSort);
     insertionSortTime = timeSort(this::insertionSort);
     selectionSortTime = timeSort(this::selectionSort);
     mergeSortTime = timeSort(this::mergeSortWrapper);
     quickSortTime = timeSort(this::quickSortWrapper);
     timSortTime = timeSort(list -> list.sort(Comparator.comparing(p -> p.sku)));

     exportCSV(
         "sort_results.csv",
         new String[]{"Bubble", "Insertion", "Selection", "Merge", "Quick", "TimSort"},
         new long[]{bubbleSortTime, insertionSortTime, selectionSortTime,
                    mergeSortTime, quickSortTime, timSortTime}
     );

     analysisArea.setText(
         "SORT BENCHMARK COMPLETE\n" +
         "Bubble: " + bubbleSortTime + " ns\n" +
         "Insertion: " + insertionSortTime + " ns\n" +
         "Selection: " + selectionSortTime + " ns\n" +
         "Merge: " + mergeSortTime + " ns\n" +
         "Quick: " + quickSortTime + " ns\n" +
         "TimSort: " + timSortTime + " ns"
     );
 }

 private long timeSort(java.util.function.Consumer<List<Product>> sorter) {
     List<Product> copy = new ArrayList<>(linearList);
     long s = System.nanoTime();
     sorter.accept(copy);
     return System.nanoTime() - s;
 }

 /* ============================================================
    SORT ALGORITHMS
    ============================================================ */

 private void bubbleSort(List<Product> list) {
     for (int i = 0; i < list.size(); i++)
         for (int j = 0; j < list.size() - i - 1; j++)
             if (list.get(j).sku.compareTo(list.get(j + 1).sku) > 0)
                 Collections.swap(list, j, j + 1);
 }

 private void insertionSort(List<Product> list) {
     for (int i = 1; i < list.size(); i++) {
         Product key = list.get(i);
         int j = i - 1;
         while (j >= 0 && list.get(j).sku.compareTo(key.sku) > 0) {
             list.set(j + 1, list.get(j));
             j--;
         }
         list.set(j + 1, key);
     }
 }

 private void selectionSort(List<Product> list) {
     for (int i = 0; i < list.size(); i++) {
         int min = i;
         for (int j = i + 1; j < list.size(); j++)
             if (list.get(j).sku.compareTo(list.get(min).sku) < 0)
                 min = j;
         Collections.swap(list, i, min);
     }
 }

 private void mergeSortWrapper(List<Product> list) {
     mergeSort(list, 0, list.size() - 1);
 }

 private void mergeSort(List<Product> list, int l, int r) {
     if (l < r) {
         int m = (l + r) / 2;
         mergeSort(list, l, m);
         mergeSort(list, m + 1, r);
         merge(list, l, m, r);
     }
 }

 private void merge(List<Product> list, int l, int m, int r) {
     List<Product> temp = new ArrayList<>(list.subList(l, r + 1));
     int i = 0, j = m - l + 1, k = l;

     while (i <= m - l && j <= r - l)
         list.set(k++, temp.get(i).sku.compareTo(temp.get(j).sku) <= 0 ? temp.get(i++) : temp.get(j++));

     while (i <= m - l) list.set(k++, temp.get(i++));
     while (j <= r - l) list.set(k++, temp.get(j++));
 }

 private void quickSortWrapper(List<Product> list) {
     quickSort(list, 0, list.size() - 1);
 }

 private void quickSort(List<Product> list, int low, int high) {
     if (low < high) {
         int p = partition(list, low, high);
         quickSort(list, low, p - 1);
         quickSort(list, p + 1, high);
     }
 }

 private int partition(List<Product> list, int low, int high) {
     Product pivot = list.get(high);
     int i = low - 1;
     for (int j = low; j < high; j++)
         if (list.get(j).sku.compareTo(pivot.sku) <= 0)
             Collections.swap(list, ++i, j);
     Collections.swap(list, i + 1, high);
     return i + 1;
 }

 /* ============================================================
    CSV EXPORT
    ============================================================ */

 private void exportCSV(String file, String[] headers, long[] values) {
     try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
         pw.println("Algorithm,Time(ns)");
         for (int i = 0; i < headers.length; i++)
             pw.println(headers[i] + "," + values[i]);
     } catch (Exception ignored) {}
 }

 /* ============================================================
    GRAPH (JFreeChart wired separately)
    ============================================================ */

 private void showSearchGraph() {

	    if (linearSearchTime == 0) {
	        showError("Run search benchmark first.");
	        return;
	    }

	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    dataset.addValue(linearSearchTime, "Time (ns)", "Linear");
	    dataset.addValue(hashSearchTime, "Time (ns)", "HashMap");
	    dataset.addValue(binarySearchTime, "Time (ns)", "Binary");
	    dataset.addValue(treeSearchTime, "Time (ns)", "TreeMap");

	    JFreeChart chart = ChartFactory.createBarChart(
	        "Search Algorithm Performance",
	        "Algorithm",
	        "Time (ns)",
	        dataset
	    );

	    CategoryPlot plot = chart.getCategoryPlot();
	    NumberAxis axis = (NumberAxis) plot.getRangeAxis();
	    axis.setAutoRange(true);

	    ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new Dimension(700, 400));

	    JDialog dialog = new JDialog(this, "Search Performance Graph", true);
	    dialog.setContentPane(chartPanel);
	    dialog.pack();
	    dialog.setLocationRelativeTo(this);
	    dialog.setVisible(true);
	}
 private void showSortGraph() {

	    if (bubbleSortTime == 0) {
	        showError("Run sort benchmark first.");
	        return;
	    }

	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    dataset.addValue(bubbleSortTime, "Time (ns)", "Bubble");
	    dataset.addValue(insertionSortTime, "Time (ns)", "Insertion");
	    dataset.addValue(selectionSortTime, "Time (ns)", "Selection");
	    dataset.addValue(mergeSortTime, "Time (ns)", "Merge");
	    dataset.addValue(quickSortTime, "Time (ns)", "Quick");
	    dataset.addValue(timSortTime, "Time (ns)", "TimSort");

	    JFreeChart chart = ChartFactory.createBarChart(
	        "Sorting Algorithm Performance",
	        "Algorithm",
	        "Time (ns)",
	        dataset
	    );

	    CategoryPlot plot = chart.getCategoryPlot();
	    NumberAxis axis = (NumberAxis) plot.getRangeAxis();
	    axis.setAutoRange(true);

	    ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new Dimension(800, 450));

	    JDialog dialog = new JDialog(this, "Sorting Performance Graph", true);
	    dialog.setContentPane(chartPanel);
	    dialog.pack();
	    dialog.setLocationRelativeTo(this);
	    dialog.setVisible(true);
	}

 /* ============================================================
 MAIN METHOD (ENTRY POINT)
 ============================================================ */
public static void main(String[] args) {
  SwingUtilities.invokeLater(() -> {
      new InventoryStocker();
  });
}


} // END OF CLASS

