import java.io.File;

public class VMTranslator {

    public static final String VM_FILE_SUFFIX = ".vm";
    public static final String ASM_FILE_SUFFIX = ".asm";
    public static final String EXTENSION_DELIMITER = ".";

    private VM2AsmWriter asmWriter; 
    private String pathDelimiter; // OS dependent

    public VMTranslator(String inputFileName) {
        this(new File(inputFileName));
    }
    public VMTranslator(File inputFile) {
        getOSInfo();

        // String inputFileName = inputFile.getName();
        String outputFileDir = "."; // initialized to current working directory

        if (validVMFile(inputFile)) {
        // if (inputFileName.endsWith(".vm")) {
            // single file
            String vmFileDir = inputFile.getParent();

            // replace output dir if a prent dir exists
            if (vmFileDir != null) outputFileDir = vmFileDir;

            String vmFileName = inputFile.getName();
            int suffixIndex = vmFileName.indexOf(".");
            String fileName = vmFileName.substring(0, suffixIndex);

            createASMWriter(fileName, outputFileDir);
            parseAndWriteVMFile(inputFile);
        } else {
            // directory
            String dirPath = inputFile.getPath();
            if (dirPath != null) outputFileDir = dirPath;

            String asmFileName = createASMFileName(inputFile);
            createASMWriter(asmFileName, dirPath);

            for (File file : inputFile.listFiles()) {
                if (validVMFile(file)) {
                    parseAndWriteVMFile(file);
                }
            }
        }

        endTranslating();
    }

    private boolean validVMFile(File inputFile) {
        String inputFileName = inputFile.getName();

        if (inputFileName.endsWith(".vm")) {
            return true;
        }

        return false;
    }

    private void parseAndWriteVMFile(File inputFile) {
        VMParser vmparser = new VMParser(inputFile);

        while (vmparser.currentCommandLine() != null) {
            char cmdType = vmparser.currentCommandType();

            // System.out.printf("command line: %s\n", vmparser.currentCommandLine());

            if (cmdType == VMParser.C_ARITHEMETIC) {
            // if (vmparser.currentCommandType() == VMParser.C_ARITHEMETIC) {
                String curr = vmparser.currentCommandLine();
                asmWriter.writeArithmetic(curr);
            } else if (
                cmdType == VMParser.C_PUSH || 
                cmdType == VMParser.C_POP) {

                char commandType = vmparser.currentCommandType();
                String arg1 = vmparser.arg1();
                int arg2 = vmparser.arg2();
                asmWriter.writePushPop(commandType, arg1, arg2);
            } else if (cmdType == VMParser.C_GOTO) {
                String cmd = vmparser.currentComamnd();
                String label = vmparser.arg1();
                asmWriter.writeGoto(cmd, label);
            } else if (cmdType == VMParser.C_IF) {
                String cmd = vmparser.currentComamnd();
                String label = vmparser.arg1();
                asmWriter.writeIf(cmd, label);
            } else if (cmdType == VMParser.C_LABEL) {
                String label = vmparser.arg1();
                asmWriter.writeLabel(label);
            } else if (cmdType == VMParser.C_FUNCTION) {
                String funcName = vmparser.arg1();
                int numberOfLocalVariables = vmparser.arg2();
                asmWriter.writeFunctionDeclaration(funcName, numberOfLocalVariables);
            } else if (cmdType == VMParser.C_RETURN) {
                asmWriter.writeFunctionReturn();
            } else if (cmdType == VMParser.C_CALL) {

            }

            vmparser.advance();
        }
    }


    private void getOSInfo() {
        // create output file dir
        String osName = System.getProperty("os.name");

        if (osName.contains("indows")) {
            this.pathDelimiter = "\\";
        } else { // linux
            this.pathDelimiter = "/";
        }
    }
    /**
     * Name should be either the dir name if multiple VM files
     * are presented or the name of a single VM file.
     * 
     * Writer created only once.
     * 
     * @param outputFileName
     */
    private void createASMWriter(String outputFileName, String outputFileDir) {
        String asmFileName;

        asmFileName = outputFileDir + this.pathDelimiter + outputFileName + ASM_FILE_SUFFIX; 

        this.asmWriter = new VM2AsmWriter(asmFileName);
    }
    private String createASMFileName(File dir) {
        String absPath = dir.getAbsolutePath();
        if (absPath.endsWith(EXTENSION_DELIMITER)) {
            int end = absPath.lastIndexOf(this.pathDelimiter);
            int start = absPath.substring(0, end).lastIndexOf(this.pathDelimiter);
            return absPath.substring(start+1, end);
        } else {
            int start = absPath.lastIndexOf(this.pathDelimiter);
            return absPath.substring(start+1);
        }
    }


    private void endTranslating() {
        if (this.asmWriter != null) {
            this.asmWriter.finishWriting();
        }
    }


    public static void main(String[] args) {
        if (args.length == 1) {
            VMTranslator translator = new VMTranslator(args[0]);
        }
    }
}
