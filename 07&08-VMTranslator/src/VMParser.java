import java.io.File;
import java.net.URL;
import java.util.Scanner;

public class VMParser {

    // for cleaning up whitespaces/comments
    private final String COMMENT_PREFIX = "//";

    // command types
    public final static char C_ARITHEMETIC = 1;
    public final static char C_PUSH = 2;
    public final static char C_POP = 3;
    public final static char C_LABEL = 4;
    public final static char C_GOTO = 5;
    public final static char C_IF = 6;
    public final static char C_FUNCTION = 7;
    public final static char C_RETURN = 8;
    public final static char C_CALL = 9;

    // instance variables
    private Scanner sc;
    private String currLine, nextLine; // valid lines (e.g. non-blank)
    private String currentCommand, currentArg1, currentArg2;
    private char currentCommandType = 0;
    private int currentCommandLineNumber = -1;

    /**
     * Constructors.
     * @param fileName
     */
    public VMParser(String fileName) {
        this(new File(fileName));
    }


    public VMParser(File file) {
        try {
            Scanner s = new Scanner(file);
            this.sc = s;
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VMParser(Scanner s) {
        if (s == null) throw new IllegalArgumentException("null scanner input");

        this.sc = s;
        initialize();
    }


    // initialize instance variables
    private void initialize() {
        this.currLine = readNextCommandFromStream();
        this.nextLine = readNextCommandFromStream();
        this.currentCommandLineNumber = 0;
    }


    private String readNextCommandFromStream() {
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (validCommandLine(line)) {
                return line;
            }
        }
        
        // close the file while no more lines
        sc.close();

        return null;
    }


    /**
     * If there is more valid commands to be processed.
     * @return
     */
    public boolean hasNextCommand() {
        return this.nextLine != null;
    }


    /**
     * Move current line forward.
     * In fact assign the next non-null string to the current one,
     * and set the curent line to be null if no more line to go.
     */
    public void advance() {
        if (this.nextLine != null) {
            this.currLine = nextCommandLine();
            this.currentCommandLineNumber++;

            // invalidate the current line, waiting to be processed
            this.currentCommand = null;
            this.currentArg1 = null;
            this.currentArg2 = null;
            this.currentCommandType = 0; 

            this.nextLine = readNextCommandFromStream();
        } else {
            this.currLine = null;
        }
    }


    /**
     * Preferred getter for this methods cleans up inline comments.
     * @return
     */
    public String currentCommandLine() {
        if (this.currLine != null) {
            return trimVMCommandLine(this.currLine);
        }
        
        return null;
    }
    public String nextCommandLine() {
        if (this.nextLine != null) {
            return trimVMCommandLine(this.nextLine);
        }

        return null;
    }
    public int currentLineNumber() {
        return this.currentCommandLineNumber;
    }
    public char currentCommandType() {
        if (this.currLine != null && this.currentCommandType == 0) {
            String[] splited = currentCommandLine().split("\\s+");

            if (splited.length > 0) {
                this.currentCommand = splited[0];

                if (splited.length > 1) {
                    this.currentArg1 = splited[1];

                    if (splited.length == 3) {
                        this.currentArg2 = splited[2];
                    }
                }

            }

            if (currentCommand.equals("push")) {
                this.currentCommandType = C_PUSH;
            } else if (currentCommand.equals("pop"))  {
                this.currentCommandType = C_POP;
            } else {
                this.currentCommandType = C_ARITHEMETIC;
            }
        }

        return this.currentCommandType;
    }
    public String arg1() {
        return new String(this.currentArg1);
    }
    public int arg2() {
        return Integer.parseInt(this.currentArg2);
    }


    /**
     * Check if the current line is a valid VM command
     * rather than a comment/white space.
     * 
     * @param commandLine
     * @return
     */
    private boolean validCommandLine(String commandLine) {
        if (commandLine.isBlank() || commandLine.startsWith(COMMENT_PREFIX)) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * Remove appended comments.
     */
    private String trimVMCommandLine(String command) {
        if (command != null) {
            int commentIndex = command.indexOf(COMMENT_PREFIX);

            if (commentIndex != -1) {
                command = command.substring(0, commentIndex);
            }
        }

        return command.trim();
    }
}