package syntaxChecker;

//Import necessary libraries for UI (Swing) and the Stack (util)
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

/**
* A simple "real-life" syntax checker project for ADS.
* This class creates a basic UI and implements the stack-based logic
* for `Practical 4: Balancing of Parenthesis`.
* * It's a single file that can be compiled and run directly.
*/
public class SyntaxChecker extends JFrame implements ActionListener {

 // --- UI Components ---
 private JTextArea inputArea;      // Where the user pastes their code
 private JTextArea outputArea;     // Where the analysis is shown
 private JButton checkButton;      // The button to start the check
 private JLabel titleLabel;
 private JLabel inputLabel;
 private JLabel outputLabel;

 /**
  * Constructor: Sets up the entire User Interface.
  */
 public SyntaxChecker() {
     // --- 1. Setup the main window (the JFrame) ---
     super("Basic Syntax & Bracket Checker"); // Title
     setSize(700, 600);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setLocationRelativeTo(null); // Center the window
     setLayout(new BorderLayout(10, 10)); // Use BorderLayout

     // --- 2. Create Panels for organization ---
     JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
     mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

     JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
     JPanel outputPanel = new JPanel(new BorderLayout(5, 5));

     // --- 3. Create UI Components ---
     titleLabel = new JLabel("ADS Project: Basic Syntax Checker");
     titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
     titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

     inputLabel = new JLabel("Paste your code here:");
     inputArea = new JTextArea();
     inputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
     JScrollPane inputScroll = new JScrollPane(inputArea); // Make it scrollable

     outputLabel = new JLabel("Analysis Output:");
     outputArea = new JTextArea();
     outputArea.setFont(new Font("Monospaced", Font.BOLD, 14));
     outputArea.setEditable(false); // Make output read-only
     outputArea.setForeground(new Color(0, 102, 0)); // Dark green text
     JScrollPane outputScroll = new JScrollPane(outputArea); // Make it scrollable

     checkButton = new JButton("Check Syntax");
     checkButton.setFont(new Font("Arial", Font.BOLD, 16));
     checkButton.addActionListener(this); // Hook up the button click

     // --- 4. Assemble the UI ---
     inputPanel.add(inputLabel, BorderLayout.NORTH);
     inputPanel.add(inputScroll, BorderLayout.CENTER);

     outputPanel.add(outputLabel, BorderLayout.NORTH);
     outputPanel.add(outputScroll, BorderLayout.CENTER);

     // Add components to the main panel
     mainPanel.add(titleLabel, BorderLayout.NORTH);
     mainPanel.add(inputPanel, BorderLayout.CENTER);
     mainPanel.add(outputPanel, BorderLayout.SOUTH);
     mainPanel.add(checkButton, BorderLayout.EAST); // Put button on the side

     // Add the main panel to the window
     add(mainPanel);

     // Make the window visible
     setVisible(true);
 }

 /**
  * This method is called when the "Check Syntax" button is clicked.
  */
 @Override
 public void actionPerformed(ActionEvent e) {
     if (e.getSource() == checkButton) {
         String codeToTest = inputArea.getText();

         // Start the timer (for analysis)
         long startTime = System.nanoTime();

         // Run the checker logic
         // *** THIS IS THE CORRECTED LINE ***
         String result = checkSyntax(codeToTest);

         // Stop the timer and calculate duration
         long endTime = System.nanoTime();
         long durationMicro = (endTime - startTime) / 1000; // Time in microseconds

         // Display the final analysis
         outputArea.setText(result + "\nTime to check: " + durationMicro + " \u00B5s"); // \u00B5s is microsecond symbol
     }
 }

 /**
  * The CORE LOGIC for the project.
  * Uses a Stack to perform `Practical 4: Balancing of Parenthesis`.
  * * @param code The input code as a String.
  * @return A formatted String with the analysis results.
  */
 private String checkSyntax(String code) {
     // Core Data Structure: Stack
     Stack<Character> stack = new Stack<>();

     // Analysis Counters
     int openParen = 0, closeParen = 0;
     int openCurly = 0, closeCurly = 0;
     int openSquare = 0, closeSquare = 0;

     // Convert string to character array to loop through
     char[] chars = code.toCharArray();

     for (int i = 0; i < chars.length; i++) {
         char c = chars[i];

         switch (c) {
             // --- PUSH opening brackets ---
             case '(':
                 stack.push(c);
                 openParen++;
                 break;
             case '{':
                 stack.push(c);
                 openCurly++;
                 break;
             case '[':
                 stack.push(c);
                 openSquare++;
                 break;

             // --- POP closing brackets ---
             case ')':
                 closeParen++;
                 if (stack.isEmpty() || stack.pop() != '(') {
                     outputArea.setForeground(Color.RED);
                     return "--- ANALYSIS FAILED ---\nResult: Mismatched ')' at character " + i;
                 }
                 break;
             case '}':
                 closeCurly++;
                 if (stack.isEmpty() || stack.pop() != '{') {
                     outputArea.setForeground(Color.RED);
                     return "--- ANALYSIS FAILED ---\nResult: Mismatched '}' at character " + i;
                 }
                 break;
             case ']':
                 closeSquare++;
                 if (stack.isEmpty() || stack.pop() != '[') {
                     outputArea.setForeground(Color.RED);
                     return "--- ANALYSIS FAILED ---\nResult: Mismatched ']' at character " + i;
                 }
                 break;
         }
     }

     // --- Final Check ---
     // If the stack is empty, all brackets were matched!
     if (stack.isEmpty()) {
         outputArea.setForeground(new Color(0, 102, 0)); // Dark green
         // Build the success report
         StringBuilder report = new StringBuilder();
         report.append("--- ANALYSIS COMPLETE ---\n");
         report.append("Result: SUCCESS! All brackets are balanced.\n\n");
         report.append("--- Statistics ---\n");
         report.append("Found " + openParen + " opening '(' and " + closeParen + " closing ')'\n");
         report.append("Found " + openCurly + " opening '{' and " + closeCurly + " closing '}'\n");
         report.append("Found " + openSquare + " opening '[' and " + closeSquare + " closing ']'\n");

         return report.toString();
     } else {
         // If the stack is NOT empty, a closing bracket is missing.
         outputArea.setForeground(Color.RED);
         char missing = stack.pop(); // Get the last bracket left on the stack
         char expected = ' ';
         if(missing == '(') expected = ')';
         if(missing == '{') expected = '}';
         if(missing == '[') expected = ']';

         return "--- ANALYSIS FAILED ---\nResult: Code has unbalanced brackets.\nError: Missing a '" + expected + "'";
     }
 }

 /**
  * Main method to create and run the application.
  */
 public static void main(String[] args) {
     // This ensures the UI is created on the correct thread
     SwingUtilities.invokeLater(new Runnable() {
         public void run() {
             new SyntaxChecker();
         }
     });
 }
}


