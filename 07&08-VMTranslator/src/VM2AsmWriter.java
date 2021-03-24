import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    /* Temp */
    private final String TEMP_SEGMENT_NAME = "temp";
    private final String TEMP_SYMBOL = "5";

    private FileWriter writer;
    private String fileName; // no suffix
    private int staticVariableNumber;

    public VM2AsmWriter(String fileName) {
        this(new File(fileName));
    }
    public VM2AsmWriter(File outputFile) {
        setFileName(outputFile.getPath());
        // createFileWriter(outputFile);
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
        // System.out.printf(
        //     "file name: %s\nfile name without suffix: %s\n",
        //     newFile.getName(),
        //     this.fileName
        //     );
        this.staticVariableNumber = 0;
    }
    private void createFileWriter(File newFile) {
        try {
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

    public void translateArithmetic(String arithmeticCommand) {

    }

    /* Memory access */

    public String translatePushPop(char commandType, String segment, int index) {
        StringBuffer res = new StringBuffer();

        if (commandType == VMParser.C_PUSH) {
            res.append(
                translateMemoryAccess(commandType, segment, index)
            );
            res.append(
                pushAsm()
            );
        } else if (commandType == VMParser.C_POP) {
            res.append(
                popAsm()
            );
            res.append(
                translateMemoryAccess(commandType, segment, index)
            );
        }
        
        return res.toString();
    }
    public StringBuffer pushAsm() {
        StringBuffer res = new StringBuffer();
        res.append(ADDR_REG);
        res.append(STACK_POINTER_SYMBOL);
        res.append("\n");
        res.append("M=D");
        res.append("\n");
        res.append("M=M+1");
        res.append("\n");
        return res;
    }
    public StringBuffer popAsm() {
        StringBuffer res = new StringBuffer();
        res.append(ADDR_REG);
        res.append(STACK_POINTER_SYMBOL);
        res.append("\n");
        res.append("D=M");
        res.append("\n");
        res.append("M=M-1");
        res.append("\n");
        return res;
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
        File outputFile = new File(asmFileName);

        VMParser vmparser = new VMParser(vmFile);

        // System.out.printf(
        //     "\ncurrent line: %s\ncurrent line number: %d\ncurrent command type: %s\nnext command: %s\n",
        //     vmparser.currentCommandLine(),
        //     vmparser.currentLineNumber(),
        //     ((vmparser.currentCommandType() == VMParser.C_PUSH || 
        //         vmparser.currentCommandType() == VMParser.C_POP) ? "Push or pop command" : 
        //         (vmparser.currentCommandType() == VMParser.C_ARITHEMETIC) ? "Arithmetic command" : null
        //     ),
        //     vmparser.nextCommandLine()
        // );

        VM2AsmWriter asmWriter = new VM2AsmWriter(asmFileName);

        String command, segment;
        int index;

        command = "push";
        segment = "argument";
        index = 7;

        System.out.printf(
            "Input command:\t%s\n",
            command + " " + segment + " " + index
        );
        System.out.printf(
            "Output asm code:\n%s",
            asmWriter.translatePushPop(
                command.equals("push") ? VMParser.C_PUSH : VMParser.C_POP,
                segment,
                index
            )
        );

        asmWriter.finishWriting();
    }
}