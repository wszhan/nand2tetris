import java.io.File;

public class JackAnalyzer {
    /** Constants */
    public static final String JACK_FILE_SUFFIX = ".jack";
    public static final String XML_FILE_SUFFIX = ".xml";
    public static final String VM_FILE_SUFFIX = ".vm";
    public static final String EXTENSION_DELIMITER = ".";

    /** Instance Variables */
    private String pathDelimiter; // OS dependent

    public JackAnalyzer(String inputFileName) {
        this(new File(inputFileName));
    }

    public JackAnalyzer(File inputFile) {
        if (inputFile == null) throw new IllegalArgumentException("null jack file");

        getOSInfo(); // cross-OS support 

        // directory and single file processing
        boolean isDir = inputFile.isDirectory();

        if (isDir) {
            compileDirectory(inputFile);
        } else {
            compileSingleJackFile(inputFile);
        }
    }

    /** Initialization */

    public void compileDirectory(File dir) {
        for (File file : dir.listFiles()) {
            System.out.println(file.getName());
            if (validateJackFileName(file)) {
                compileSingleJackFile(file);
            }
        }
    }

    public void compileSingleJackFile(File jackFile) {
        String fileNameWithoutExtension = absoluteFileNameWithoutExtension(jackFile);
        String outputTokenizedFileName = fileNameWithoutExtension + "T" + JackAnalyzer.XML_FILE_SUFFIX;
        String outXML = fileNameWithoutExtension + JackAnalyzer.XML_FILE_SUFFIX;
        String outVM = fileNameWithoutExtension + JackAnalyzer.VM_FILE_SUFFIX;
        JackTokenizer tokenizer = new JackTokenizer(jackFile, new File(outputTokenizedFileName));
        CompilationEngine engine = new CompilationEngine(
            tokenizer, 
            new File(outXML),
            new File(outVM)
            );
    }


    /** Helpers */
    private String absoluteFileNameWithoutExtension(File inputJackFile) {
        String fileDir = inputJackFile.getParent();

        // construct .xml file name
        String inputJackFileName = inputJackFile.getName();
        int suffixIndex = inputJackFileName.indexOf(JackAnalyzer.EXTENSION_DELIMITER);
        String inputFileNameWithoutSuffix = inputJackFileName.substring(0, suffixIndex);

        return fileDir + pathDelimiter + inputFileNameWithoutSuffix;
    }

    private boolean validateJackFileName(File inputFile) {
        String inputFileName = inputFile.getName();

        if (inputFileName.endsWith(JACK_FILE_SUFFIX)) {
            return true;
        }

        return false;
    }

    private void getOSInfo() {
        String osName = System.getProperty("os.name");

        if (osName.contains("indows")) { // windows
            this.pathDelimiter = "\\";
        } else { // linux
            this.pathDelimiter = "/";
        }
    }

    /**
     * Project 10 Main method.
     */
    public static void main(String[] args) {
        if (args != null && args.length == 1) {
            String path = args[0];
            File file = new File(path);

            JackAnalyzer analyzer = new JackAnalyzer(file);
        }
    }
}
