import java.io.File;
import java.net.URL;
import java.util.Scanner;

public class VMParser {

    public final static char C_ARITHEMETIC = 1;
    public final static char C_PUSH = 2;
    public final static char C_POP = 3;
    public final static char C_LABEL = 4;
    public final static char C_GOTO = 5;
    public final static char C_IF = 6;
    public final static char C_FUNCTION = 7;
    public final static char C_RETURN = 8;
    public final static char C_CALL = 9;

    private final String COMMENT_PREFIX = "//";

    private Scanner sc;
    private String currentCommandLine, nextCommandLine;
    private String currentCommand, currentArg1, currentArg2;
    private char currentCommandType = 0;
    private int currentCommandLineNumber = -1;

    public VMParser(String fileName) {
        this(new File(fileName));
    }

    public VMParser(File file) {
        try {
            Scanner s = new Scanner(file);
            // while (s.hasNextLine()) {
            //     System.out.println(s.nextLine());
            // }
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
        this.currentCommandLine = readNextCommandFromStream();
        this.nextCommandLine = readNextCommandFromStream();
        this.currentCommandLineNumber = 0;
    }
    private void initialize1() {
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();

            if (validCommandLine(line)) {
                this.currentCommandLine = trimVMCommandLine(line);
                this.currentCommandLineNumber++;

                // invalidate the current line, waiting to be processed
                this.currentCommand = null;
                this.currentArg1 = null;
                this.currentArg2 = null;
                this.currentCommandType = 0; 

                while (sc.hasNextLine()) {
                    String next = sc.nextLine().trim();

                    if (validCommandLine(next)) {
                        this.nextCommandLine = trimVMCommandLine(next);
                        return;
                    }
                }

                // explicitly set nextCommandLine to be null
                // input stream is exhausted
                this.nextCommandLine = null;
                return;
            }
        }
    }

    private String readNextCommandFromStream() {
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (validCommandLine(line)) {
                return line;
            }
        }
        
        sc.close();
        return null;
    }

    public boolean hasNextCommand() {
        return this.nextCommandLine != null;
    }

    /**
     * Move current line forward.
     */
    public void advance() {
        if (this.nextCommandLine != null) {
            currentCommandLine = this.nextCommandLine;
            this.currentCommandLineNumber++;

            // invalidate the current line, waiting to be processed
            this.currentCommand = null;
            this.currentArg1 = null;
            this.currentArg2 = null;
            this.currentCommandType = 0; 

            this.nextCommandLine = readNextCommandFromStream();
        } else {
            this.currentCommandLine = null;
        }
    }


    public String currentCommandLine() {
        if (currentCommandLine != null) {
            return new String(currentCommandLine);
        }
        
        return null;
    }
    public int currentLineNumber() {
        return this.currentCommandLineNumber;
    }
    public char currentCommandType() {
        if (this.currentCommandLine != null && this.currentCommandType == 0) {
            String[] splited = this.currentCommandLine.split("\\s+");

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

        return command;
    }

    // TO BE REMOVED
    public String nextCommandLine() {
        return this.nextCommandLine;
    }

    public static void main(String[] args) {
        File file = new File(args[0]);

        VMParser vmparser = new VMParser(file);
        vmparser.advance();
        vmparser.advance();
        System.out.printf(
            "\ncurrent line: %s\ncurrent line number: %d\ncurrent command type: %s\nnext command: %s\n",
            vmparser.currentCommandLine(),
            vmparser.currentLineNumber(),
            ((vmparser.currentCommandType() == VMParser.C_PUSH || 
                vmparser.currentCommandType() == VMParser.C_POP) ? "Push or pop command" : 
                (vmparser.currentCommandType() == VMParser.C_ARITHEMETIC) ? "Arithmetic command" : null
            ),
            vmparser.nextCommandLine()
        );
    }
}