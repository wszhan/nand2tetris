import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Create a writer instance for each jack file (for each class).
 */
public class VMWriter {

    private FileWriter vmWriter;
    private String className = null;
    
    public VMWriter(File outputVMFile) {
        if (outputVMFile == null) {
            throw new IllegalArgumentException("null output VM file");
        }
        
        initWriter(outputVMFile);
        initClassName(outputVMFile);
    }

    /** APIs */

    public void writePush(VirtualSegment seg, int index) {
        String targetSegment = seg.toString().toLowerCase();
        writeToVMFile("push" + " " + targetSegment + " " + index);
    }
    public void writePop(VirtualSegment seg, int index) {
        String targetSegment = seg.toString().toLowerCase();
        writeToVMFile("pop" + " " + targetSegment + " " + index);
    }
    public void writeArithmetic(String command) {
        writeToVMFile(command);
    }
    public void writeLabel(String label) {
        writeToVMFile("(" + label + ")");
    }
    public void writeGoto(String label) {
        writeToVMFile("goto " + label);
    }
    public void writeIf(String label) {
        writeToVMFile("if-goto " + label);
    }
    public void writeCall(String functionName, int nArgs) {
        writeToVMFile("call " + className + "." + functionName + " " + nArgs);
        // writeToVMFile("call " + functionName + " " + nArgs);
    }
    public void writeFunction(String functionName, int nLocals) {
        System.out.printf("className, funcName, #ofVars - %s, %s, %d\n", className, functionName, nLocals);
        writeToVMFile("function " + className + "." + functionName + " " + nLocals);
    }
    public void writeReturn() {
        writeToVMFile("return");
    }
    public void close() {
        try {
            vmWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Initialization */

    private void initWriter(File outputVMFile) {
        try {
            vmWriter = new FileWriter(outputVMFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initClassName(File outputVMFile) {
        String fileName = outputVMFile.getName();
        int idx = fileName.indexOf(".");
        String className = fileName.substring(0, idx);
        this.className = className;
    }

    /** Helpers */
    private void writeToVMFile(String vmCommand) {
        try {
            vmWriter.write(vmCommand + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
