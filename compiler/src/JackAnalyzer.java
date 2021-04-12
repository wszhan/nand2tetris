import java.io.File;
import java.nio.file.Files;

public class JackAnalyzer {
    /** Constants */
    public static final String JACK_FILE_SUFFIX = ".jack";
    public static final String XML_FILE_SUFFIX = ".xml";
    public static final String EXTENSION_DELIMITER = ".";

    /** Instance Variables */
    private JackTokenizer tokenizer;
    private CompilationEngine engine;
    private String pathDelimiter; // OS dependent

    public JackAnalyzer(String inputFileName) {
        this(new File(inputFileName));
    }

    public JackAnalyzer(File inputFile) {
        if (inputFile == null) throw new IllegalArgumentException("null input file");

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
    public void initializeTokenizer() {
        this.tokenizer = new JackTokenizer();
    }

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
        String outputFileName = fileNameWithoutExtension + JackAnalyzer.XML_FILE_SUFFIX;
        this.tokenizer = new JackTokenizer(jackFile, new File(outputTokenizedFileName));

        int n = 0;
        // while (n < 500) {
            tokenizer.readTokenFromStream();
        //     n++;
        // }
        tokenizer.endTokenization();
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

        if (osName.contains("indows")) {
            this.pathDelimiter = "\\";
        } else { // linux
            this.pathDelimiter = "/";
        }
    }

    public static void main(String[] args) {
        if (args != null && args.length == 1) {
            String path = args[0];
        // String path = "../10/Square/SquareGame.jack";
            File file = new File(path);

            JackAnalyzer analyzer = new JackAnalyzer(file);
        }
        // String path1 = "C:\\Users\\Yuen\\Documents\\nand2Tetris\\nand2tetris-software-suite\\projects\\10\\testFolder\\test1.jack";
        // String path2 = "C:\\Users\\Yuen\\Documents\\nand2Tetris\\nand2tetris-software-suite\\projects\\10\\testFolder";

        // File file1 = new File(path1);
        // File file2 = new File(path2);

        // JackAnalyzer analyzer = new JackAnalyzer(file2);
    }
}
