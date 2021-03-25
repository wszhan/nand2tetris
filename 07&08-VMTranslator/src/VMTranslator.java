import java.io.File;

public class VMTranslator {

    public static final String VM_FILE_SUFFIX = ".vm";
    public static final String ASM_FILE_SUFFIX = ".asm";
    
    public static void main(String[] args) {
        if (args.length == 1) {
            String pathName = args[0];
            String asmFileName = null;

            // single file translation
            if (pathName.endsWith(VM_FILE_SUFFIX)) {
                // get file name
                File vmFile = new File(pathName);
                String vmFileName = vmFile.getName();
                String vmFileDir = vmFile.getParent();
                vmFileDir = vmFileDir != null ? vmFileDir : ".";
                int suffixIndex = vmFileName.indexOf(".");
                String fileName = vmFileName.substring(0, suffixIndex);

                // create file in the same path as .asm file
                // cross OS support
                String osName = System.getProperty("os.name");
                if (osName.contains("indows")) {
                    asmFileName = vmFileDir + "\\" + fileName + ASM_FILE_SUFFIX; 
                } else { // linux
                    asmFileName = vmFileDir + "/" + fileName + ASM_FILE_SUFFIX; 
                }

                VMParser vmparser = new VMParser(vmFile);
                VM2AsmWriter asmWriter = new VM2AsmWriter(asmFileName);

                while (vmparser.currentCommandLine() != null) {
                    if (vmparser.currentCommandType() == VMParser.C_ARITHEMETIC) {
                        String curr = vmparser.currentCommandLine();
                        asmWriter.writeArithmetic(curr);
                    } else if (
                        vmparser.currentCommandType() == VMParser.C_PUSH || 
                        vmparser.currentCommandType() == VMParser.C_POP) {

                        char commandType = vmparser.currentCommandType();
                        String arg1 = vmparser.arg1();
                        int arg2 = vmparser.arg2();
                        asmWriter.writePushPop(commandType, arg1, arg2);
                    }

                    vmparser.advance();
                }

                asmWriter.finishWriting();
            } else { // single directory translation

            }
        }
    }
}
