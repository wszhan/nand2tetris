import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VM2AsmWriter {

    private final String FILE_SUFFIX_DELIMITER = ".";
    private final String ADDR_REG = "@";

    /* Segment base addresses */
    private final String LOCAL_SEGMENT_NAME = "local";
    private final String ARGUMENT_SEGMENT_NAME = "argument";
    private final String POINTER_SEGMENT_NAME = "pointer";

    private final String STACK_POINTER_SYMBOL = "SP";
    private final String LOCAL_SYMBOL = "LCL";
    private final String ARGUMENT_SYMBOL = "ARG";
    private final String THIS_SYMBOL = "THIS";
    private final String THAT_SYMBOL = "THAT";

    /* Constant - virtual segment */
    private final String CONSTANT_SEGMENT_NAME = "constant";

    /* Static for variables */
    private final String STATIC_SEGMENT_NAME = "static";

    /* Boolean commands label */
    private final String SET_TRUE_LABEL = "SET_TRUE_";
    private final String SET_FALSE_LABEL = "SET_FALSE_";
    private final String COMPARISON_END_LABEL = "COMP_END_";

    /* Temp */
    private final String TEMP_SEGMENT_NAME = "temp";
    private final String TEMP_SYMBOL = "5";

    /* Instance variables */
    private FileWriter writer;
    private String fileName; // no suffix
    private int staticVariableNumber;
    private int comparisonCount;
    private Map<String, String> binaryOperators, unaryOperators, comparisons;

    public VM2AsmWriter(String fileName) {
        this(new File(fileName));
    }
    public VM2AsmWriter(File outputFile) {
        setFileName(outputFile.getPath());
        initializeOperatorsTable();
        // createFileWriter(outputFile);
    }
    private void initializeOperatorsTable() {
        binaryOperators = new HashMap<String, String>();
        unaryOperators = new HashMap<String, String>();
        comparisons = new HashMap<String, String>();

        binaryOperators.put("add", "+");
        binaryOperators.put("sub", "-");
        binaryOperators.put("gt", "-");
        binaryOperators.put("lt", "-");
        binaryOperators.put("eq", "-");
        binaryOperators.put("and", "&");
        binaryOperators.put("or", "|");

        unaryOperators.put("neg", "-");
        unaryOperators.put("not", "!");

        comparisons.put("gt", "JGT");
        comparisons.put("lt", "JLT");
        comparisons.put("eq", "JEQ");
    }
    private String getFileNameWithoutSuffix(File file) {
        String fileName = file.getName();
        return getFileNameWithoutSuffix(fileName);
    }
    private String getFileNameWithoutSuffix(String file) {
        int suffixIndex = file.indexOf(FILE_SUFFIX_DELIMITER);
        String fileName = file.substring(0, suffixIndex);
        return fileName;
    }

    /**
     * Create a new file to write into with a new fileWriter.
     * @param fileName
     */
    public void setFileName(String filePath) {
        File newFile = new File(filePath);
        createFileWriter(newFile);
        this.fileName = getFileNameWithoutSuffix(newFile.getName());
        this.staticVariableNumber = 0;
        this.comparisonCount = 0;
    }
    private void createFileWriter(File newFile) {
        try {
            // System.out.printf(
            //     "new file name: %s\nnew file path: %s\n",
            //     newFile.getName(), newFile.getPath());
            this.writer = new FileWriter(newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeToFile(StringBuffer commands) {
        writeToFile(commands.toString());
    }
    private void writeToFile(String commands) {
        try {
            this.writer.write(commands);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close file before leaving.
     */
    public void finishWriting() {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 


    /* Arithmetic */
    public void writeArithmetic(String arithmeticCommand) {
        String res = translateArithmetic(arithmeticCommand);
        writeToFile(res);
    }

    private String translateArithmetic(String arithmeticCommand) {
        StringBuffer res = new StringBuffer();

        if (binaryOperators.containsKey(arithmeticCommand)) {
            binaryArithmetic(res, arithmeticCommand);
        } else if (unaryOperators.containsKey(arithmeticCommand)) {
            unaryArithmetic(res, arithmeticCommand);
        } else {
            throw new IllegalArgumentException(
                "unknown arithmetic command : " + arithmeticCommand
            );
        }

        res.append("\n");

        return res.toString();
    }
    private void binaryArithmetic(StringBuffer buf, String command) {
        String operator = binaryOperators.get(command);
        decrementSP(buf);
        accessSP(buf);

        // retrive 2nd operand from stack
        buf.append("D=M\n");

        decrementSP(buf);
        accessSP(buf);
        // retrive 1st operand and do operation
        buf.append("D=M" + operator + "D\n");

        if (comparisons.containsKey(command)) {
            comparisonAsm(buf, command);
        } else {
            pushAsm(buf);
        }
    }
    private void unaryArithmetic(StringBuffer buf, String command) {
        String operator = unaryOperators.get(command);
        decrementSP(buf);
        accessSP(buf);
        buf.append("D=" + operator + "M\n");
        pushAsm(buf);
    }
    private void comparisonAsm(StringBuffer buf, String command) {
        String setTrueLabel = SET_TRUE_LABEL + this.comparisonCount;
        // String setFalseLabel = SET_FALSE_LABEL + this.comparisonCount;
        String compEndLabel = COMPARISON_END_LABEL + this.comparisonCount;
        this.comparisonCount++;

        buf.append(ADDR_REG + setTrueLabel + "\n");
        buf.append("D;");
        buf.append(comparisons.get(command));
        buf.append("\n");

        // false
        buf.append("@0\nD=A\n");
        pushAsm(buf);
        buf.append("@" + compEndLabel + "\n0; JMP\n");
        
        // true
        buf.append(
            "(" + setTrueLabel + ")" + "\n" + "@1\nD=A\n"
        );
        pushAsm(buf);

        // end
        buf.append(
            "(" + compEndLabel + ")"
        );
    }

    /* Memory access */

    private void accessSP(StringBuffer buf) {
        buf.append(ADDR_REG + STACK_POINTER_SYMBOL);
        buf.append("\nA=M\n");
    }
    private void decrementSP(StringBuffer buf) {
        buf.append(ADDR_REG + STACK_POINTER_SYMBOL);
        buf.append("\n");
        buf.append("M=M-1");
        buf.append("\n");
    }
    private void incrementSP(StringBuffer buf) {
        buf.append(ADDR_REG + STACK_POINTER_SYMBOL);
        buf.append("\n");
        buf.append("M=M+1");
        buf.append("\n");
    }

    public void writePushPop(char commandType, String segment, int index) {
        String res = translatePushPop(commandType, segment, index);
        writeToFile(res);
    }

    private String translatePushPop(char commandType, String segment, int index) {
        StringBuffer res = new StringBuffer();

        if (commandType == VMParser.C_PUSH) {
            res.append(
                translateMemoryAccess(commandType, segment, index)
            );
            pushAsm(res);
            // res.append(
            //     pushAsm()
            // );
        } else if (commandType == VMParser.C_POP) {
            // res.append(
            //     popAsm()
            // );
            popAsm(res);
            res.append(
                translateMemoryAccess(commandType, segment, index)
            );
        }
        
        return res.toString();
    }
    private void pushAsm(StringBuffer res) {
        // StringBuffer res = new StringBuffer();

        accessSP(res);

        // push to *(*SP)
        res.append("M=D\n");

        incrementSP(res);

        // return res;
    }
    private void popAsm(StringBuffer res) {
    // private StringBuffer popAsm() {
        // StringBuffer res = new StringBuffer();

        decrementSP(res);

        accessSP(res);

        // pop from
        res.append("D=M");
        
        // return res;
    }

    /**
     * Validate parameters and translate valid ones.
     * 
     * @param seg
     * @param index
     * @param commandType
     * @return
     */
    private StringBuffer translateMemoryAccess(char commandType, String seg, int index) {
        StringBuffer res = new StringBuffer();
        res.append(ADDR_REG);

        // constant
        if (seg.equals(CONSTANT_SEGMENT_NAME)) {
            res.append(index);
            res.append("\n");
            res.append("D=A\n");
            return res;
        } else if (seg.equals(POINTER_SEGMENT_NAME)) {
        // if (seg.equals(POINTER_SEGMENT_NAME)) {
            if (index == 0) {
                res.append(THIS_SYMBOL);
            } else if (index == 1) {
                res.append(THAT_SYMBOL);
            } else {
                throw new IllegalArgumentException(
                    "incorrect index to access pointer segment - " + index
                    );
            }
        } else if (seg.equals(TEMP_SEGMENT_NAME)) {
            res.append(TEMP_SYMBOL);

            if (index > 7) {
                throw new IllegalArgumentException(
                    "temp segment overflow with index " + index
                    );
            }
        } else if (seg.equals(STATIC_SEGMENT_NAME)) {
            res.append(this.fileName + "." + this.staticVariableNumber);

            if (this.staticVariableNumber > 239) {
                throw new IllegalArgumentException(
                    "static segment overflow with index " + index
                    );
            }
            this.staticVariableNumber++;
        } else {
            if (seg.equals(ARGUMENT_SEGMENT_NAME)) {
                res.append(ARGUMENT_SYMBOL);
            } else if (seg.equals(LOCAL_SEGMENT_NAME)) {
                res.append(LOCAL_SYMBOL);
            } 
        }

        res.append("\n");
        res.append("A=A+" + index + "\n");

        if (commandType == VMParser.C_PUSH) {
            res.append("D=M");
        } else if (commandType == VMParser.C_POP) {
            res.append("M=D");
        }

        res.append("\n");
        
        return res;
    }
    private String translateStackPointerAccess(char commandType) {
        return null;
    }

    public static void main(String[] args) {
        File vmFile = new File(args[0]);
        String vmFileName = vmFile.getName();
        String vmFileDir = vmFile.getParent();
        int suffixIndex = vmFileName.indexOf(".");
        String fileName = vmFileName.substring(0, suffixIndex);

        String asmFileName = vmFileDir + "\\" + fileName + VMTranslator.ASM_FILE_SUFFIX;
        VMParser vmparser = new VMParser(vmFile);
        VM2AsmWriter asmWriter = new VM2AsmWriter(asmFileName);

        asmWriter.finishWriting();
    }
}