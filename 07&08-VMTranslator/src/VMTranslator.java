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
                int suffixIndex = vmFileName.indexOf(".");
                String fileName = vmFileName.substring(0, suffixIndex);

                // create file in the same path as .asm file
                asmFileName = vmFileDir + "\\" + fileName + ASM_FILE_SUFFIX; 

            } else { // single directory translation

            }

            System.out.printf("Output file name: %s\n", asmFileName);
        }
    }
}
