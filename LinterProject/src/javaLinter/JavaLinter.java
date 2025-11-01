package javaLinter;


/*
 * -----------------------------------------------------------------------------
 * --- ECLIPSE ROADMAP: How to Build This Project ---
 * -----------------------------------------------------------------------------
 * This is a single, complete file.
 * 1.  Open Eclipse.
 * 2.  File -> New -> Java Project (Name it LinterProject)
 * 3.  Right-click 'src' -> New -> Class (Name it JavaLinter)
 * 4.  Copy and paste ALL of this code into the JavaLinter.java file.
 * 5.  Save and Run (Click the green play button).
 * -----------------------------------------------------------------------------
 * --- PROJECT CODE (V13 - FINAL - ROBUST PARSING) ---
 * -----------------------------------------------------------------------------
 * This version uses a new, more robust parser to correctly handle
 * strings, comments, and character literals, fixing all previous bugs.
 */

// Import necessary libraries
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class JavaLinter extends JFrame implements ActionListener {

    // --- UI Components ---
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton checkButton;
    private JLabel titleLabel;
    private JLabel inputLabel;
    private JLabel outputLabel;

    // --- Highlighter Components ---
    private Highlighter highlighter;
    private Highlighter.HighlightPainter errorPainter;

    // --- Helper classes for advanced error reporting ---

    /**
     * A helper class to store the precise location of an error.
     */
    class ErrorSpan {
        int line;
        int startCol;
        int endCol;

        public ErrorSpan(int line, int startCol, int endCol) {
            this.line = line;
            this.startCol = startCol;
            this.endCol = endCol;
        }
    }

    /**
     * An advanced inner class to hold a full linter report.
     * It now stores a list of specific error locations (spans).
     */
    class LinterResult {
        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        List<ErrorSpan> errorSpans = new ArrayList<>();
        boolean isError = false;

        public void addError(String message, int line, int startCol, int endCol) {
            this.isError = true;
            this.errorMessages.add(message);
            // Avoid adding duplicate error highlights for the same spot
            boolean exists = false;
            for(ErrorSpan span : errorSpans) {
                if(span.line == line && span.startCol == startCol) {
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                this.errorSpans.add(new ErrorSpan(line, startCol, endCol));
            }
        }

        public void addSuccess(String message) {
            this.successMessages.add(message);
        }
    }

    /**
     * A helper class to store a character and its position.
     */
    class BracketInfo {
        char character;
        int line;
        int col;
        BracketInfo(char c, int l, int col) {
            this.character = c;
            this.line = l;
            this.col = col;
        }
    }

    /**
     * Constructor: Sets up the entire User Interface.
     */
    public JavaLinter() {
        // --- 1. Setup the main window (the JFrame) ---
        super("ADS Project: Linter (v13 - Final Fixed)");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 2. Create Panels for organization ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        
        // --- 3. Create UI Components ---
        titleLabel = new JLabel("ADS Project: Java Linter (Robust Parser)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        inputLabel = new JLabel("Paste your Java code here:");
        inputArea = new JTextArea();
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setPreferredSize(new Dimension(400, 300)); 

        outputLabel = new JLabel("Linter Analysis Output:");
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setPreferredSize(new Dimension(400, 150)); 

        checkButton = new JButton("Run Linter");
        checkButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkButton.addActionListener(this);

        // --- 4. Setup the Highlighter ---
        highlighter = inputArea.getHighlighter();
        errorPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 180, 180)); // Light Pink

        // --- 5. Assemble the UI ---
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(outputPanel, BorderLayout.SOUTH);
        mainPanel.add(checkButton, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * This method is called when the "Run Linter" button is clicked.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == checkButton) {
            String codeToTest = inputArea.getText();
            
            // --- 1. Clear all old highlights ---
            highlighter.removeAllHighlights();

            long startTime = System.nanoTime();

            // --- 2. Run All Checks and Collect Results ---
            LinterResult bracketResult = checkBrackets(codeToTest);
            LinterResult semicolonResult = checkSemicolons(codeToTest);
            
            long endTime = System.nanoTime();
            long durationMicro = (endTime - startTime) / 1000;

            // --- 3. Compile the Final Report ---
            StringBuilder finalReport = new StringBuilder();
            boolean hasErrors = bracketResult.isError || semicolonResult.isError;

            if (hasErrors) {
                outputArea.setForeground(Color.RED);
                finalReport.append("--- LINTER FAILED: Found errors ---\n\n");
                
                // Add all bracket errors
                for (String err : bracketResult.errorMessages) {
                    finalReport.append(err).append("\n");
                }
                
                // Add all semicolon errors
                for (String err : semicolonResult.errorMessages) {
                    finalReport.append(err).append("\n");
                }
                
                // Highlight all error spans
                for (ErrorSpan span : bracketResult.errorSpans) {
                    highlightErrorSpan(span);
                }
                for (ErrorSpan span : semicolonResult.errorSpans) {
                    highlightErrorSpan(span);
                }

            } else {
                // --- 4. Show Success Report ---
                outputArea.setForeground(new Color(0, 102, 0)); // Dark green
                finalReport.append("--- LINTER SUCCESS ---\n\n");
                for (String msg : bracketResult.successMessages) {
                    finalReport.append(msg).append("\n");
                }
                for (String msg : semicolonResult.successMessages) {
                    finalReport.append(msg).append("\n");
                }
            }
            
            finalReport.append("\nTotal time to check: " + durationMicro + " \u00B5s");
            outputArea.setText(finalReport.toString());
        }
    }

    /**
     * Helper method to paint the background of a specific character span.
     */
    private void highlightErrorSpan(ErrorSpan span) {
        if (span.line < 0) return;
        try {
            int startOffset = inputArea.getLineStartOffset(span.line);
            int start = startOffset + span.startCol;
            int end = startOffset + span.endCol;
            
            // Ensure end doesn't go past the document end
            end = Math.min(end, inputArea.getDocument().getLength());
            start = Math.max(0, start);

            // Handle case where start and end are the same (e.g., end of line)
            // and make the highlight at least 1 char wide
            if (start == end) {
                if (start > startOffset) start--; // Highlight char before
                else end++; // Highlight first char
            }
            
            highlighter.addHighlight(start, end, errorPainter);
            inputArea.setCaretPosition(start); // Move cursor to first error
        } catch (BadLocationException ble) {
            // Ignore (can happen with invalid lines)
        }
    }


    /**
     * CHECK 1: The CORE LOGIC for the bracket checker.
     * This version correctly handles strings, chars, and comments.
     * @return A LinterResult object with the full report.
     */
    private LinterResult checkBrackets(String code) {
        LinterResult result = new LinterResult();
        Stack<BracketInfo> stack = new Stack<>();
        
        String[] lines = code.split("\n");
        boolean inString = false;
        boolean inChar = false;
        boolean inBlockComment = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean inLineComment = false;

            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                char prevC = (j > 0) ? line.charAt(j - 1) : '\0';

                // --- NEW ROBUST PARSER LOGIC ---
                if (inLineComment) {
                    continue; // Stay in line comment mode until the end of the line
                }
                if (inBlockComment) {
                    if (c == '/' && prevC == '*') {
                        inBlockComment = false;
                    }
                    continue;
                }
                if (inString) {
                    if (c == '"' && prevC != '\\') {
                        inString = false;
                    }
                    continue;
                }
                if (inChar) {
                    if (c == '\'' && prevC != '\\') {
                        inChar = false;
                    }
                    continue;
                }

                // --- Check for new state transitions ---
                if (c == '"') {
                    inString = true;
                    continue;
                }
                if (c == '\'') {
                    inChar = true;
                    continue;
                }
                if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
                    inLineComment = true;
                    continue;
                }
                if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*') {
                    inBlockComment = true;
                    continue;
                }
                // --- END OF PARSER LOGIC ---
                
                // --- Bracket Logic (Only runs if not in a string/comment) ---
                switch (c) {
                    case '(': 
                    case '{': 
                    case '[':
                        stack.push(new BracketInfo(c, i, j)); 
                        break;
                    case ')':
                        if (stack.isEmpty() || stack.peek().character != '(') {
                            result.addError("Bracket Error: Mismatched ')'", i, j, j + 1);
                        } else {
                            stack.pop();
                        }
                        break;
                    case '}':
                        if (stack.isEmpty() || stack.peek().character != '{') {
                            result.addError("Bracket Error: Mismatched '}'", i, j, j + 1);
                        } else {
                            stack.pop();
                        }
                        break;
                    case ']':
                        if (stack.isEmpty() || stack.peek().character != '[') {
                            result.addError("Bracket Error: Mismatched ']'", i, j, j + 1);
                        } else {
                            stack.pop();
                        }
                        break;
                }
            }
        }

        // Check for any unclosed brackets at the end
        while (!stack.isEmpty()) {
            BracketInfo missing = stack.pop();
            char expected = (missing.character == '(') ? ')' : (missing.character == '{' ? '}' : ']');
            
            // Highlight the *original opening bracket* that was never closed
            result.addError(
                "Bracket Error: Missing '" + expected + "' for opening bracket on line " + (missing.line + 1),
                missing.line, missing.col, missing.col + 1
            );
        }

        if (!result.isError) {
            result.addSuccess("Bracket Check: SUCCESS! All brackets are balanced.");
        }
        return result;
    }

    /**
     * CHECK 2: The CORE LOGIC for the semicolon linter.
     * This version correctly handles strings, chars, and comments.
     * @return A LinterResult object with the full report.
     */
    private LinterResult checkSemicolons(String code) {
        LinterResult result = new LinterResult();
        String[] lines = code.split("\n");
        int linesEndingInSemicolon = 0;
        
        boolean inBlockComment = false;

        for (int i = 0; i < lines.length; i++) {
            String originalLine = lines[i];
            String line = originalLine;
            int highlightCol = -1; // We'll find the highlight spot
            
            // --- NEW ROBUST PARSER LOGIC ---
            // This loop finds the *real* end of the statement, skipping comments/strings
            boolean inString = false;
            boolean inChar = false;
            String cleanStatement = "";
            
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                char prevC = (j > 0) ? line.charAt(j - 1) : '\0';
                
                if (inBlockComment) {
                    if (c == '/' && prevC == '*') {
                        inBlockComment = false;
                    }
                    continue;
                }
                if (inString) {
                    if (c == '"' && prevC != '\\') inString = false;
                    continue;
                }
                if (inChar) {
                    if (c == '\'' && prevC != '\\') inChar = false;
                    continue;
                }
                if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
                    break; // End of statement for this line
                }
                if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*') {
                    inBlockComment = true;
                    continue;
                }
                
                if (c == '"') inString = true;
                else if (c == '\'') inChar = true;
                else {
                    cleanStatement += c;
                    highlightCol = j; // Update the end of the "real" code
                }
            }
            // --- END OF PARSER LOGIC ---
            
            String trimmedStatement = cleanStatement.trim();
            
            // --- Rule Checks ---
            if (trimmedStatement.isEmpty()) {
                continue;
            }
            
            // This is a line that SHOULD NOT have a semicolon
            if (trimmedStatement.endsWith("{") || trimmedStatement.endsWith("}") ||
                trimmedStatement.startsWith("if") || trimmedStatement.startsWith("for") || trimmedStatement.startsWith("while") ||
                trimmedStatement.startsWith("public") || trimmedStatement.startsWith("private") || trimmedStatement.startsWith("protected") ||
                trimmedStatement.startsWith("class") || trimmedStatement.startsWith("interface") || trimmedStatement.startsWith("try") ||
                (trimmedStatement.startsWith("else") && !trimmedStatement.startsWith("else if")) || 
                trimmedStatement.startsWith("switch") || trimmedStatement.endsWith(":") /* for case/default */) {
                continue;
            }
            
            // This is a line that SHOULD have a semicolon
            if (!trimmedStatement.endsWith(";")) {
                result.addError("Semicolon Error: Missing ';' at end of line.", i, highlightCol + 1, highlightCol + 2);
            } else {
                linesEndingInSemicolon++;
            }
        }
        
        if (!result.isError) {
            result.addSuccess("Semicolon Check: SUCCESS! (" + linesEndingInSemicolon + " statements checked)");
        }
        return result;
    }

    /**
     * Main method to create and run the application.
     * THIS VERSION IS FIXED (v14)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() { // <-- Added the missing 'void'
                new JavaLinter();
            }
        });
    }
}