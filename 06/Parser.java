import java.io.File;
import java.util.Scanner;

public class Parser {

    public static final char A_COMMAND = 1; // 0000 0001
    public static final char C_COMMAND = 2; // 0000 0010
    public static final char L_COMMAND = 4; // 0000 0100
    public static final char INVALID_COMMAND = 0; // 0000 0000
    public static final char A_PREFIX = '@';
    public static final char L_PREFIX = '(';
    public static final char L_SUFFIX = ')';
    public static final char ASSIGNMENT_DELIMITER = '=';
    public static final char JUMP_DELIMITER = ';';
    public static final String COMMENT_PREFIX = "//";

    private Scanner sc;
    private String currentLine = null;
    private char commandType = 0;
    private int currentLineNumber = -1;

    public Parser(String fileName) {
        this(new File(fileName));
    }

    public Parser(File file) {
        try {
            Scanner s = new Scanner(file);
            this.sc = s;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Parser(Scanner s) {
        if (s == null)
            throw new IllegalArgumentException();

        this.sc = s;
    }

    public String nextCommand() {
        while (sc.hasNextLine()) {
            // currentLine = sc.nextLine().trim();
            currentLine= sc.nextLine().trim();
            // System.out.printf("Raw command: %s\n", rawCommand);
            if (!currentLine.isBlank() && !currentLine.startsWith(COMMENT_PREFIX)) {
                currentLine = trimAsmCommand(currentLine);
                this.commandType = currentCommandType();
                if (this.currentLineNumber == -1) this.currentLineNumber = 0;
                else if (this.commandType != Parser.L_COMMAND) this.currentLineNumber++;
                return currentLine;
            }
        }

        return null;
    }

    private String trimAsmCommand(String instruction) {
        String res = instruction;

        if (instruction != null) {
            // remove inline comment
            int commentIndex = instruction.indexOf(COMMENT_PREFIX);
            if (commentIndex != -1) {
                res = instruction.substring(0, commentIndex);
                // System.out.println(res);
            }

            // remove white space
            res.trim();
        }

        return res;
    }

    public String currentCommandLine() {
        String res = new String(currentLine);
        return res;
    }

    public int currentCommandLineNumber() {
        return this.currentLineNumber;
    }

    public char currentCommandType() {
        char firstChar = currentLine.charAt(0);
        if (firstChar == A_PREFIX) {
            return A_COMMAND;
        } else if (firstChar == L_PREFIX) {
            char lastChar = currentLine.charAt(currentLine.length() - 1);
            // System.out.printf("first char, last char - %c, %c\n", firstChar, lastChar);
            // System.out.printf("L Command? %b\n", firstChar == L_PREFIX && lastChar == L_SUFFIX);
            if (lastChar == L_SUFFIX) {
                return L_COMMAND;
            } else {
                throw new InvalidCommandException("unrecognizable command type");
            }
        } else {
            return C_COMMAND;
        }
    }

    public String symbol() {
        if (this.commandType == L_COMMAND) {
            int start = currentLine.indexOf(L_PREFIX);
            int end = currentLine.indexOf(L_SUFFIX);
            if (end - start > 1) {
                return currentLine.substring(start+1, end);
            } else {
                throw new InvalidCommandException("invalid label declaration");
            }
        } else if (this.commandType == A_COMMAND) {
            int atSignIndex = currentLine.indexOf(A_PREFIX);
            return currentLine.substring(atSignIndex+1);
        }

        return null;
    }

    public String dest() {
        if (this.commandType == C_COMMAND) {
            int assignmentDelimiterIndex = currentLine.indexOf(ASSIGNMENT_DELIMITER);
            if (assignmentDelimiterIndex != -1) {
                return currentLine.substring(0, assignmentDelimiterIndex);
            }
        }

        return null;
    }
    public String comp() {
        if (this.commandType == C_COMMAND) {
            int assignmentDelimiterIndex = currentLine.indexOf(ASSIGNMENT_DELIMITER);
            if (assignmentDelimiterIndex != -1) {
                return currentLine.substring(assignmentDelimiterIndex+1, currentLine.length());
            }

            int semicolonDelimiter = currentLine.indexOf(JUMP_DELIMITER);
            if (semicolonDelimiter != -1) {
                String res = currentLine.substring(0, semicolonDelimiter);
                return res.trim();
            }
        }

        return null;
    }
    public String jump() {
        if (this.commandType == C_COMMAND) {
            int semicolonDelimiter = currentLine.indexOf(JUMP_DELIMITER);
            if (semicolonDelimiter != -1) {
                String res = currentLine.substring(semicolonDelimiter+1, currentLine.length());
                return res.trim();
            }
        }

        return null;
    }
}