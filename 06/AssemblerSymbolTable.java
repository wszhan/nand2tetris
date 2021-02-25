import java.io.File;
import java.util.Scanner;
import java.util.TreeMap;

public class AssemblerSymbolTable {

    private Parser ps;
    private TreeMap<String, Integer> st;

    public AssemblerSymbolTable(String fileName) {
        this(new File(fileName));
    }

    public AssemblerSymbolTable(File fileName) {
        this.ps = new Parser(fileName);
        this.st = new TreeMap<>();
        initializeTable();
        initializeLabels();
    }

    public int resolveSymbol(String symbol) {
        Integer val = st.get(symbol);
        if (val != null) return val;
        else return -1;
    }

    public void addEntry(String key, int val) {
        if (st.containsKey(key)) throw new IllegalArgumentException();

        st.put(key, val);
    }

    private void initializeTable() {
        // init registers
        for (int i = 0; i < 16; i++) {
            String key = "R" + i;
            st.put(key, i);
        }

        // init screen and keyboard
        st.put("SCREEN", 16384);
        st.put("KBD", 24576);

        // init others
        st.put("SP", 0);
        st.put("LCL", 1);
        st.put("ARG", 2);
        st.put("THIS", 3);
        st.put("THAT", 4);
    }

    private void initializeLabels() {
        while (ps.nextCommand() != null) {
            if (ps.currentCommandType() == Parser.L_COMMAND) {
                String key = ps.symbol();
                int val = ps.currentCommandLineNumber() + 1;
                addEntry(key, val);
            }
        }
    }
}