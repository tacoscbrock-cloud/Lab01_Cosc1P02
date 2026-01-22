
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeTutor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TutorFrame().setVisible(true));
    }

    static class TutorFrame extends JFrame {
        private final JTextPane explainPane = new JTextPane();
        private final JTextPane codePane = new JTextPane();

        private final JButton backBtn = new JButton("Back");
        private final JButton nextBtn = new JButton("Next");

        private final JTextField importField = new JTextField("brock.*", 14);
        private final JTextField classNameField = new JTextField("Exercise1", 14);

        private final JTextField fieldTypeField = new JTextField("int", 10);
        private final JTextField fieldNameField = new JTextField("distance", 10);
        private final JTextField fieldValueField = new JTextField("10", 10);

        private final JTextField methodNameField = new JTextField("drawSymbol", 14);

        private final JTextField loopNField = new JTextField("3", 6);
        private final JTextField loopStartField = new JTextField("int j = 0", 14);
        private final JTextField loopConditionField = new JTextField("j < n", 14);
        private final JTextField loopStepField = new JTextField("j++", 14);

        private final JTextField rowsField = new JTextField("4", 6);
        private final JTextField outerStartField = new JTextField("int i = 0", 14);
        private final JTextField outerConditionField = new JTextField("i < rows", 14);
        private final JTextField outerStepField = new JTextField("i++", 14);

        private final JTextField innerStartField = new JTextField("int j = 0", 14);
        private final JTextField innerConditionField = new JTextField("j < n", 14);
        private final JTextField innerStepField = new JTextField("j++", 14);

        private int pageIndex = 0;
        private final TutorPage[] pages;

        private Timer typeTimer;
        private String fullText = "";
        private int typed = 0;

        private final Color BG_MINT = new Color(234, 250, 245);
        private final Color PANEL_MINT = new Color(228, 248, 242);
        private final Color PASTEL_PURPLE = new Color(210, 198, 235);
        private final Color PASTEL_PINK = new Color(245, 210, 225);
        private final Color BORDER_PURPLE = new Color(185, 168, 220);
        private final Color TEXT_DARK_GREY = new Color(55, 55, 65);

        TutorFrame() {
            super("Java Code Tutor (Animated) - Pastel Purple/Pink/Mint");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1200, 820);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BG_MINT);

            pages = new TutorPage[] {
                    new PageComments(),
                    new PageClass(),
                    new PageFieldsVsLocals(),
                    new PageSemicolons(),
                    new PageMethods(),
                    new PageConstructors(),
                    new PageForLoopsAndNested(),
                    new PageImports()
            };

            setLayout(new BorderLayout(10, 10));

            JPanel topRow = new JPanel(new BorderLayout(10, 10));
            topRow.setBorder(new EmptyBorder(10, 10, 0, 10));
            topRow.setBackground(BG_MINT);

            JPanel inputs = buildInputsPanel();
            inputs.setPreferredSize(new Dimension(520, 10));
            topRow.add(inputs, BorderLayout.WEST);
            topRow.add(buildCodePanel(), BorderLayout.CENTER);

            JPanel bottomRow = buildExplainPanel();

            JSplitPane verticalSplit = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    topRow,
                    bottomRow
            );
            verticalSplit.setResizeWeight(0.62);
            verticalSplit.setDividerSize(10);
            verticalSplit.setBorder(new EmptyBorder(0, 10, 10, 10));

            add(verticalSplit, BorderLayout.CENTER);
            add(buildNavPanel(), BorderLayout.SOUTH);

            renderPage(0);
        }

        private JPanel buildInputsPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(PANEL_MINT);
            p.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER_PURPLE, 2),
                    "Student Inputs"
            ));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 6, 2, 6);
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;

            int r = 0;

            addRow(p, c, r++, 0, "Import:", importField);
            addRow(p, c, r++, 0, "Class name:", classNameField);
            addRow(p, c, r++, 0, "Field type:", fieldTypeField);
            addRow(p, c, r++, 0, "Field name:", fieldNameField);
            addRow(p, c, r++, 0, "Field value:", fieldValueField);
            addRow(p, c, r++, 0, "Method name:", methodNameField);

            int r2 = 0;

            addRow(p, c, r2++, 2, "n:", loopNField);
            addRow(p, c, r2++, 2, "Loop START:", loopStartField);
            addRow(p, c, r2++, 2, "Loop COND:", loopConditionField);
            addRow(p, c, r2++, 2, "Loop STEP:", loopStepField);

            addRow(p, c, r2++, 2, "rows:", rowsField);
            addRow(p, c, r2++, 2, "Outer START:", outerStartField);
            addRow(p, c, r2++, 2, "Outer COND:", outerConditionField);
            addRow(p, c, r2++, 2, "Outer STEP:", outerStepField);

            addRow(p, c, r2++, 2, "Inner START:", innerStartField);
            addRow(p, c, r2++, 2, "Inner COND:", innerConditionField);
            addRow(p, c, r2++, 2, "Inner STEP:", innerStepField);

            JButton refresh = new JButton("Refresh");
            styleButton(refresh, PASTEL_PURPLE);
            refresh.addActionListener(e -> renderPage(pageIndex));

            c.gridx = 0;
            c.gridy = Math.max(r, r2) + 1;
            c.gridwidth = 4;
            c.weightx = 1.0;
            p.add(refresh, c);

            c.gridwidth = 1;

            return p;
        }

        private void addRow(JPanel p, GridBagConstraints c, int row, int colStart, String labelText, JTextField field) {
            JLabel lbl = label(labelText);

            c.gridy = row;

            c.gridx = colStart;
            c.weightx = 0.0;
            p.add(lbl, c);

            c.gridx = colStart + 1;
            c.weightx = 1.0;
            p.add(styledField(field), c);
        }

        private JLabel label(String s) {
            JLabel l = new JLabel(s);
            l.setForeground(TEXT_DARK_GREY);
            return l;
        }

        private JTextField styledField(JTextField tf) {
            tf.setBackground(Color.WHITE);
            tf.setForeground(TEXT_DARK_GREY);
            tf.setCaretColor(TEXT_DARK_GREY);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_PURPLE, 1),
                    new EmptyBorder(4, 6, 4, 6)
            ));
            return tf;
        }

        private void styleButton(JButton b, Color bg) {
            b.setBackground(bg);
            b.setForeground(TEXT_DARK_GREY);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_PURPLE, 1),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }

        private JPanel buildExplainPanel() {
            explainPane.setEditable(false);
            explainPane.setFont(new Font("SansSerif", Font.PLAIN, 16));
            explainPane.setMargin(new Insets(10, 10, 10, 10));
            explainPane.setBackground(PASTEL_PINK);
            explainPane.setForeground(TEXT_DARK_GREY);
            explainPane.setCaretColor(TEXT_DARK_GREY);
            explainPane.setBorder(BorderFactory.createLineBorder(BORDER_PURPLE, 2));

            JScrollPane sp = new JScrollPane(explainPane);
            sp.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER_PURPLE, 2),
                    "Explanation (Animated)"
            ));
            sp.getViewport().setBackground(PASTEL_PINK);

            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(BG_MINT);
            p.add(sp, BorderLayout.CENTER);
            return p;
        }

        private JPanel buildCodePanel() {
            codePane.setEditable(false);
            codePane.setFont(new Font("Monospaced", Font.PLAIN, 16));
            codePane.setMargin(new Insets(10, 10, 10, 10));
            codePane.setBackground(PASTEL_PURPLE);
            codePane.setForeground(TEXT_DARK_GREY);
            codePane.setCaretColor(TEXT_DARK_GREY);
            codePane.setBorder(BorderFactory.createLineBorder(BORDER_PURPLE, 2));

            JScrollPane sp = new JScrollPane(codePane);
            sp.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER_PURPLE, 2),
                    "Generated Code Preview"
            ));
            sp.getViewport().setBackground(PASTEL_PURPLE);

            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(BG_MINT);
            p.add(sp, BorderLayout.CENTER);
            return p;
        }

        private JPanel buildNavPanel() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            p.setBackground(BG_MINT);

            styleButton(backBtn, PASTEL_PINK);
            styleButton(nextBtn, PASTEL_PURPLE);

            backBtn.addActionListener(e -> renderPage(pageIndex - 1));
            nextBtn.addActionListener(e -> renderPage(pageIndex + 1));

            p.add(backBtn);
            p.add(nextBtn);
            return p;
        }

        private void renderPage(int idx) {
            if (idx < 0 || idx >= pages.length) return;
            pageIndex = idx;

            backBtn.setEnabled(pageIndex > 0);
            nextBtn.setEnabled(pageIndex < pages.length - 1);

            if (typeTimer != null && typeTimer.isRunning()) typeTimer.stop();

            TutorPage page = pages[pageIndex];
            String explain = page.explain(this);
            String code = page.code(this);

            animateExplain(explain);
            setCodeWithHighlight(code);
        }

        private void animateExplain(String text) {
            fullText = text;
            typed = 0;
            explainPane.setText("");

            typeTimer = new Timer(12, (ActionEvent e) -> {
                typed++;
                if (typed >= fullText.length()) {
                    typed = fullText.length();
                    typeTimer.stop();
                }
                explainPane.setText(fullText.substring(0, typed));
                explainPane.setCaretPosition(explainPane.getDocument().getLength());
            });
            typeTimer.start();
        }

        private void setCodeWithHighlight(String code) {
            codePane.setText(code);

            StyledDocument doc = codePane.getStyledDocument();
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

            Style base = doc.addStyle("base", defaultStyle);
            StyleConstants.setForeground(base, TEXT_DARK_GREY);

            Style keyword = doc.addStyle("keyword", base);
            StyleConstants.setForeground(keyword, new Color(90, 80, 125));
            StyleConstants.setBold(keyword, true);

            Style commentStyle = doc.addStyle("comment", base);
            StyleConstants.setForeground(commentStyle, new Color(85, 110, 95));
            StyleConstants.setItalic(commentStyle, true);

            Style braces = doc.addStyle("braces", base);
            StyleConstants.setForeground(braces, new Color(125, 85, 115));
            StyleConstants.setBold(braces, true);

            Style semi = doc.addStyle("semi", base);
            StyleConstants.setForeground(semi, new Color(120, 95, 70));
            StyleConstants.setBold(semi, true);

            doc.setCharacterAttributes(0, doc.getLength(), base, true);

            String text = codePane.getText();

            int i = 0;
            while (i < text.length()) {
                int idx = text.indexOf("//", i);
                if (idx < 0) break;
                int end = text.indexOf("\n", idx);
                if (end < 0) end = text.length();
                doc.setCharacterAttributes(idx, end - idx, commentStyle, false);
                i = end;
            }

            for (int pos = 0; pos < text.length(); pos++) {
                char ch = text.charAt(pos);
                if (ch == '{' || ch == '}') doc.setCharacterAttributes(pos, 1, braces, false);
                else if (ch == ';') doc.setCharacterAttributes(pos, 1, semi, false);
            }

            highlightWord(doc, text, "public", keyword);
            highlightWord(doc, text, "class", keyword);
            highlightWord(doc, text, "private", keyword);
            highlightWord(doc, text, "void", keyword);
            highlightWord(doc, text, "int", keyword);
            highlightWord(doc, text, "for", keyword);
            highlightWord(doc, text, "new", keyword);
            highlightWord(doc, text, "return", keyword);
            highlightWord(doc, text, "import", keyword);
        }

        private void highlightWord(StyledDocument doc, String text, String word, Style style) {
            int from = 0;
            while (true) {
                int idx = text.indexOf(word, from);
                if (idx < 0) break;

                boolean leftOk = (idx == 0) || !Character.isLetterOrDigit(text.charAt(idx - 1));
                boolean rightOk = (idx + word.length() >= text.length()) ||
                        !Character.isLetterOrDigit(text.charAt(idx + word.length()));

                if (leftOk && rightOk) doc.setCharacterAttributes(idx, word.length(), style, false);
                from = idx + word.length();
            }
        }

        String importLib() {
            String s = importField.getText().trim();
            if (s.isEmpty()) return "brock.*";
            return s.replaceAll("[^A-Za-z0-9_\\.*]", "");
        }

        String className() {
            String s = classNameField.getText().trim();
            if (s.isEmpty()) return "Exercise1";
            return s.replaceAll("[^A-Za-z0-9_]", "");
        }

        String fieldType() {
            String s = fieldTypeField.getText().trim();
            if (s.isEmpty()) return "int";
            return s.replaceAll("[^A-Za-z0-9_\\[\\]]", "");
        }

        String fieldName() {
            String s = fieldNameField.getText().trim();
            if (s.isEmpty()) return "distance";
            return s.replaceAll("[^A-Za-z0-9_]", "");
        }

        String fieldValue() {
            String s = fieldValueField.getText().trim();
            if (s.isEmpty()) return "10";
            return s;
        }

        String methodName() {
            String s = methodNameField.getText().trim();
            if (s.isEmpty()) return "drawSymbol";
            return s.replaceAll("[^A-Za-z0-9_]", "");
        }

        int loopN() {
            try { return Math.max(0, Integer.parseInt(loopNField.getText().trim())); }
            catch (Exception e) { return 3; }
        }

        int rows() {
            try { return Math.max(0, Integer.parseInt(rowsField.getText().trim())); }
            catch (Exception e) { return 4; }
        }

        String loopStart() { return safeLoopPart(loopStartField.getText(), "int j = 0"); }
        String loopCond()  { return safeLoopPart(loopConditionField.getText(), "j < n"); }
        String loopStep()  { return safeLoopPart(loopStepField.getText(), "j++"); }

        String outerStart() { return safeLoopPart(outerStartField.getText(), "int i = 0"); }
        String outerCond()  { return safeLoopPart(outerConditionField.getText(), "i < rows"); }
        String outerStep()  { return safeLoopPart(outerStepField.getText(), "i++"); }

        String innerStart() { return safeLoopPart(innerStartField.getText(), "int j = 0"); }
        String innerCond()  { return safeLoopPart(innerConditionField.getText(), "j < n"); }
        String innerStep()  { return safeLoopPart(innerStepField.getText(), "j++"); }

        private String safeLoopPart(String raw, String fallback) {
            String s = (raw == null ? "" : raw.trim());
            if (s.isEmpty()) return fallback;
            s = s.replaceAll("[^A-Za-z0-9_\\s\\+\\-\\*/%<>=!&|;()]+", "");
            s = s.replace(";", "").trim();
            return s.isEmpty() ? fallback : s;
        }

        String singleForHeader() {
            return "for (" + loopStart() + "; " + loopCond() + "; " + loopStep() + ")";
        }

        String nestedForHeaderOuter() {
            return "for (" + outerStart() + "; " + outerCond() + "; " + outerStep() + ")";
        }

        String nestedForHeaderInner() {
            return "for (" + innerStart() + "; " + innerCond() + "; " + innerStep() + ")";
        }

        Integer inferIterations(String start, String cond, String step, int n, int rows) {
            String var = inferVarFromStart(start);
            if (var == null) return null;

            Integer startVal = inferStartValue(start, var);
            if (startVal == null) return null;

            Integer limit = inferLimitFromCondition(cond, var, n, rows);
            if (limit == null) return null;

            Integer delta = inferStepDelta(step, var);
            if (delta == null || delta <= 0) return null;

            String compactCond = cond.replaceAll("\\s+", "");
            if (!compactCond.contains(var + "<")) return null;

            if (startVal >= limit) return 0;

            int count = 0;
            for (int v = startVal; v < limit; v += delta) count++;
            return count;
        }

        private String inferVarFromStart(String start) {
            Matcher m = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b\\s*=").matcher(start);
            if (m.find()) return m.group(1);
            return null;
        }

        private Integer inferStartValue(String start, String var) {
            Matcher m = Pattern.compile("\\b" + Pattern.quote(var) + "\\b\\s*=\\s*(-?\\d+)").matcher(start.replaceAll("\\s+", ""));
            if (m.find()) {
                try { return Integer.parseInt(m.group(1)); }
                catch (Exception ignored) { return null; }
            }
            return null;
        }

        private Integer inferLimitFromCondition(String cond, String var, int n, int rows) {
            String c = cond.replaceAll("\\s+", "");
            Matcher m = Pattern.compile("\\b" + Pattern.quote(var) + "\\b<([A-Za-z_][A-Za-z0-9_]*|-?\\d+)").matcher(c);
            if (!m.find()) return null;

            String rhs = m.group(1);
            if (rhs.matches("-?\\d+")) {
                try { return Integer.parseInt(rhs); }
                catch (Exception ignored) { return null; }
            }
            if (rhs.equals("n")) return n;
            if (rhs.equals("rows")) return rows;
            return null;
        }

        private Integer inferStepDelta(String step, String var) {
            String s = step.replaceAll("\\s+", "");

            if (s.equals(var + "++") || s.equals("++" + var)) return 1;

            Matcher m1 = Pattern.compile("\\b" + Pattern.quote(var) + "\\b=\\b" + Pattern.quote(var) + "\\b\\+(\\d+)").matcher(s);
            if (m1.find()) {
                try { return Integer.parseInt(m1.group(1)); }
                catch (Exception ignored) { return null; }
            }

            Matcher m2 = Pattern.compile("\\b" + Pattern.quote(var) + "\\b\\+=(\\d+)").matcher(s);
            if (m2.find()) {
                try { return Integer.parseInt(m2.group(1)); }
                catch (Exception ignored) { return null; }
            }

            return null;
        }
    }

    interface TutorPage {
        String explain(TutorFrame f);
        String code(TutorFrame f);
    }

    static class PageComments implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 1: Comments (//)\n\n"
                    + "A comment starts with // and is NOT executed.\n"
                    + "Java ignores comments.\n\n"
                    + "Beginner pattern:\n"
                    + "1) Put the comment BEFORE the line/block it explains.\n"
                    + "2) A strong comment explains WHY.\n\n"
                    + "Examples:\n"
                    + "  // Create the drawing tool\n"
                    + "  Turtle yertle = new Turtle();\n"
                    + "  // Repeat the symbol n times\n"
                    + "  for (...) { ... }\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "public class " + f.className() + " {\n"
                    + "    public void demo() {\n"
                    + "        // A comment starts with // and Java ignores it\n"
                    + "        // Put the comment BEFORE the code it explains\n"
                    + "        int " + f.fieldName() + " = " + f.fieldValue() + ";\n"
                    + "        System.out.println(\"Started demo\");\n"
                    + "    }\n"
                    + "}\n";
        }
    }

    static class PageClass implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 2: Class pattern\n\n"
                    + "Class pattern:\n"
                    + "  public class Name {\n"
                    + "      fields\n"
                    + "      constructors\n"
                    + "      methods\n"
                    + "  }\n\n"
                    + "Highlights:\n"
                    + "1) public class\n"
                    + "2) Name\n"
                    + "3) { } is the body\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "public class " + f.className() + " {\n"
                    + "    // Fields (class variables) go here\n"
                    + "    // Constructors go here\n"
                    + "    // Methods go here\n"
                    + "}\n";
        }
    }

    static class PageFieldsVsLocals implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 3: Fields vs local variables\n\n"
                    + "Field: inside the class, outside methods. Property of the object.\n"
                    + "Local: inside a method. Temporary.\n\n"
                    + "Field pattern:\n"
                    + "  private Type name = value;\n\n"
                    + "Local pattern:\n"
                    + "  public void f() { Type name = value; }\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "public class " + f.className() + " {\n"
                    + "    // Field: belongs to the object\n"
                    + "    private " + f.fieldType() + " " + f.fieldName() + " = " + f.fieldValue() + ";\n\n"
                    + "    public void demoScope() {\n"
                    + "        // Local variable: belongs only to this method\n"
                    + "        int temp = 1;\n"
                    + "        System.out.println(\"temp = \" + temp);\n"
                    + "        System.out.println(\"field " + f.fieldName() + " = \" + " + f.fieldName() + ");\n"
                    + "    }\n"
                    + "}\n";
        }
    }

    static class PageSemicolons implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 4: Semicolons\n\n"
                    + "Most statements end with ';' (declarations, assignments, method calls).\n"
                    + "Lines that open blocks with '{' usually do not end with ';' (class/method/for headers).\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "public class " + f.className() + " {\n"
                    + "    private int x = 3;\n"
                    + "    public void demo() {\n"
                    + "        x = x + 1;\n"
                    + "        System.out.println(x);\n"
                    + "    }\n"
                    + "}\n";
        }
    }

    static class PageMethods implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 5: Methods\n\n"
                    + "Method pattern:\n"
                    + "  public void name() {\n"
                    + "      instruction;\n"
                    + "  }\n\n"
                    + "Instructions go inside the method body.\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "public class " + f.className() + " {\n"
                    + "    private " + f.fieldType() + " " + f.fieldName() + " = " + f.fieldValue() + ";\n\n"
                    + "    public void " + f.methodName() + "() {\n"
                    + "        // Instructions (statements) go inside the method body\n"
                    + "        System.out.println(\"Method started\");\n"
                    + "        System.out.println(\"" + f.fieldName() + " = \" + " + f.fieldName() + ");\n"
                    + "        " + f.fieldName() + " = " + f.fieldName() + " + 1;\n"
                    + "        System.out.println(\"After update: " + f.fieldName() + " = \" + " + f.fieldName() + ");\n"
                    + "    }\n"
                    + "}\n";
        }
    }

    static class PageConstructors implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 6: Constructors\n\n"
                    + "Constructor rules:\n"
                    + "- Same name as the class\n"
                    + "- No return type\n"
                    + "- Runs when the object is created\n\n"
                    + "Constructor pattern:\n"
                    + "  public ClassName() { setup; }\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "public class " + f.className() + " {\n"
                    + "    // Fields (class variables)\n"
                    + "    private " + f.fieldType() + " " + f.fieldName() + " = " + f.fieldValue() + ";\n\n"
                    + "    public " + f.className() + "() {\n"
                    + "        // Constructor runs when you create the object\n"
                    + "        System.out.println(\"Constructor running\");\n"
                    + "        System.out.println(\"" + f.fieldName() + " = \" + " + f.fieldName() + ");\n"
                    + "    }\n\n"
                    + "    public void " + f.methodName() + "() {\n"
                    + "        System.out.println(\"Method running\");\n"
                    + "    }\n"
                    + "}\n";
        }
    }

    static class PageForLoopsAndNested implements TutorPage {
        public String explain(TutorFrame f) {
            int n = f.loopN();
            int rows = f.rows();

            Integer singleIters = f.inferIterations(f.loopStart(), f.loopCond(), f.loopStep(), n, rows);
            Integer outerIters  = f.inferIterations(f.outerStart(), f.outerCond(), f.outerStep(), n, rows);
            Integer innerIters  = f.inferIterations(f.innerStart(), f.innerCond(), f.innerStep(), n, rows);

            String singleRuns = (singleIters == null)
                    ? "Single loop runs: cannot safely calculate from the current pattern."
                    : "Single loop runs: " + singleIters + " time(s).";

            String nestedRuns;
            if (outerIters != null && innerIters != null) {
                nestedRuns = "Nested loops: outer runs " + outerIters + " time(s), inner runs "
                        + innerIters + " time(s) per outer. Total inner-body executions = " + (outerIters * innerIters) + ".";
            } else {
                nestedRuns = "Nested loops: cannot safely calculate unless both loops match common patterns (i=0; i<rows; i++ and j=0; j<n; j++).";
            }

            return ""
                    + "Lesson 7: for-loops + nested for-loops\n\n"
                    + "Single loop pattern:\n"
                    + "  for (START; CONDITION; STEP) { BODY }\n\n"
                    + "Your SINGLE loop header:\n"
                    + "  " + f.singleForHeader() + "\n"
                    + singleRuns + "\n\n"
                    + "Valid STEP examples:\n"
                    + "  j++\n"
                    + "  j = j + 1\n"
                    + "  j += 1\n\n"
                    + "Nested loop pattern:\n"
                    + "  for (OUTER_START; OUTER_COND; OUTER_STEP) {\n"
                    + "      for (INNER_START; INNER_COND; INNER_STEP) {\n"
                    + "          INNER BODY\n"
                    + "      }\n"
                    + "  }\n\n"
                    + "Your NESTED loop headers:\n"
                    + "  " + f.nestedForHeaderOuter() + "\n"
                    + "  " + f.nestedForHeaderInner() + "\n"
                    + nestedRuns + "\n";
        }

        public String code(TutorFrame f) {
            int n = f.loopN();
            int rows = f.rows();

            String singleHeader = f.singleForHeader();
            String outerHeader = f.nestedForHeaderOuter();
            String innerHeader = f.nestedForHeaderInner();

            return ""
                    + "public class " + f.className() + " {\n"
                    + "    public void loopDemo() {\n"
                    + "        int n = " + n + ";\n"
                    + "        int rows = " + rows + ";\n\n"
                    + "        // for (START; CONDITION; STEP)\n"
                    + "        // START runs once before the loop begins\n"
                    + "        // CONDITION is checked before every iteration\n"
                    + "        // STEP runs after each iteration\n"
                    + "        " + singleHeader + " {\n"
                    + "            System.out.println(\"Single loop body\");\n"
                    + "        }\n\n"
                    + "        // STEP examples:\n"
                    + "        // j++ means j = j + 1\n"
                    + "        // j = j + 1 means increase j by 1\n"
                    + "        // j += 1 also means increase j by 1\n\n"
                    + "        // Nested loops: outer controls rows, inner repeats inside each row\n"
                    + "        " + outerHeader + " {\n"
                    + "            " + innerHeader + " {\n"
                    + "                System.out.println(\"Nested inner body\");\n"
                    + "            }\n"
                    + "        }\n"
                    + "    }\n"
                    + "}\n";
        }
    }

    static class PageImports implements TutorPage {
        public String explain(TutorFrame f) {
            return ""
                    + "Lesson 8: import statements\n\n"
                    + "Import patterns:\n"
                    + "  import package.ClassName;\n"
                    + "  import package.*;\n\n"
                    + "In your Turtle lab you use:\n"
                    + "  import brock.*;\n"
                    + "Imports go at the top of the file.\n";
        }

        public String code(TutorFrame f) {
            return ""
                    + "import " + f.importLib() + ";\n\n"
                    + "public class " + f.className() + " {\n"
                    + "    // Class variables (fields)\n"
                    + "    private Turtle yertle;\n"
                    + "    private TurtleDisplayer display;\n\n"
                    + "    public " + f.className() + "() {\n"
                    + "        // Create the turtle (brush) and the display (canvas)\n"
                    + "        yertle = new Turtle();\n"
                    + "        display = new TurtleDisplayer();\n\n"
                    + "        // Link them together\n"
                    + "        display.placeTurtle(yertle);\n\n"
                    + "        // Wait for the user before starting\n"
                    + "        display.waitForUser();\n\n"
                    + "        // Draw something simple\n"
                    + "        yertle.penDown();\n"
                    + "        yertle.forward(50);\n"
                    + "    }\n"
                    + "}\n";
        }
    }
}