import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Variable> st;
    private Map<VariableKind, Integer> indexCount;

    /**
     * The ST is within either the scope of a class or that of a subroutine.
     */
    public SymbolTable(boolean isClass) {
        this.st = new HashMap<>();
        initIndexCountST(isClass);
    }

    private class Variable {
        VariableKind kind;
        String dataType; // char, boolean, int, void
        int index; // index assigned in the kind category

        private Variable(VariableKind inputKind, String type, int inputIndex) {
            this.kind = inputKind;
            this.dataType = type;
            this.index = inputIndex;
        }
    }

    /** APIs */

    /**
     * Define a variable in the current symbol table.
     */
    public void define(String varName, String dataType, VariableKind kind) {
        if (kind == VariableKind.NONE) return;

        int varIndex = indexCount.get(kind);
        Variable newVar = new Variable(kind, dataType, varIndex);
        st.put(varName, newVar);

        // increment the corresponding value in the ST
        indexCount.put(kind, varIndex + 1);
    }


    /**
     * Kind defines the scope.
     * Return NONE type if the kind is unknown.
     */
    public VariableKind kindOf(String var) {
        if (var == null) return null;

        Variable v = st.get(var);

        if (v == null) return VariableKind.NONE;

        return v.kind;
    }


    /**
     * Type defines the dataType.
     * Return null if the type is unknown.
     */
    public String typeOf(String varName) {
        if (varName == null) return null;

        Variable var = st.get(varName);

        return var == null ? null : var.dataType;
    }


    /**
     * Returns the index of the variable in its corresponding
     * virtual memory segment.
     */
    public int indexOf(String varName) {
        if (varName == null) return -1;

        Variable var = st.get(varName);

        if (var == null) return -1;

        Integer res = var.index;

        return res == null ? -1 : res;
    }

    /**
     * Did not use.
     */
    // public int varCount(VariableKind varKind) {
    //     if (varKind == VariableKind.NONE) return 0;

    //     return indexCount.get(varKind);
    // }

    /** Initialization */

    private void initIndexCountST(boolean isClass) {
        indexCount = new HashMap<>();

        if (isClass) {
            indexCount.put(VariableKind.FIELD, 0);
            indexCount.put(VariableKind.STATIC, 0);
        } else {
            indexCount.put(VariableKind.ARG, 0);
            indexCount.put(VariableKind.VAR, 0);
        }
    }
    
    public static void main(String[] args) {
        SymbolTable testClassST = new SymbolTable(true);
        SymbolTable testSubroutineST = new SymbolTable(false);
    }
}