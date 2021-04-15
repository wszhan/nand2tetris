import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class JackTokenizer {
    
    /**
     * Class constants.
     */
    private final String MULTIPLE_LINE_COMMENT_START = "/*";
    private final String MULTIPLE_LINE_COMMENT_END = "*/";
    private final String SINGLE_LINE_COMMENT_START = "//";
    private final int MIN_INT = 0;
    private final int MAX_INT = 32767;

    /**
     * I/O.
     */
    private BufferedReader reader;
    private FileWriter writer;

    /**
     * Token processing
     */
    private Map<String, Keyword> keywordMap = null;
    private Set<Character> symbolSet = null;
    private LinkedList<String> tokenBuffer;
    /* use private*/ public String currentTokenValue;
    // private String currentTokenValue;
    private String nextTokenValue;
    private Token currentTokenType;

    public JackTokenizer() {
        initializeKeywordMap();
        initializeSymbolSet();
    }

    public JackTokenizer(File inputFile, File outputXMLFile) {
        if (!validateInputFile(inputFile)) {
            throw new IllegalArgumentException("null input file");
        }
        if (!validateOutputFile(outputXMLFile)) {
            throw new IllegalArgumentException("null output file");
        }

        initializeKeywordMap();
        initializeSymbolSet();

        tokenBuffer = new LinkedList<>();

        tokenizeNewFile(inputFile, outputXMLFile);
    }

    /** Validation */

    private boolean validateInputFile(File inputFile) {
        if (inputFile != null) {
            if (inputFile.getName().contains(JackAnalyzer.JACK_FILE_SUFFIX)) {
                return true;
            }
        }

        return false;
    }
    private boolean validateOutputFile(File outputFile) {
        if (outputFile != null) {
            if (outputFile.getName().contains(JackAnalyzer.XML_FILE_SUFFIX)) {
                return true;
            }
        }

        return false;
    }


    /** Initialization */

    private void tokenizeNewFile(File inputJackFile, File outputXMLFile) {
        try {
            this.reader = new BufferedReader(new FileReader(inputJackFile));
            this.writer = new FileWriter(outputXMLFile);
            this.writer.write("<tokens>\n");

            // while (hasMoreTokens()) {
            //     advance();
            // }

            // this.writer.write("</tokens>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initializeKeywordMap() {
        if (this.keywordMap == null) {
            this.keywordMap = new HashMap<>();

            keywordMap.put("class", Keyword.CLASS);
            keywordMap.put("method", Keyword.METHOD); 
            keywordMap.put("function", Keyword.FUNCTION);
            keywordMap.put("constructor", Keyword.CONSTRUCTOR); 
            keywordMap.put("int", Keyword.INT);
            keywordMap.put("boolean", Keyword.BOOLEAN); 
            keywordMap.put("char", Keyword.CHAR); 
            keywordMap.put("void", Keyword.VOID);
            keywordMap.put("var", Keyword.VAR); 
            keywordMap.put("static", Keyword.STATIC); 
            keywordMap.put("field", Keyword.FIELD); 
            keywordMap.put("let", Keyword.LET);
            keywordMap.put("do", Keyword.DO); 
            keywordMap.put("if", Keyword.IF); 
            keywordMap.put("else", Keyword.ELSE); 
            keywordMap.put("while", Keyword.WHILE);
            keywordMap.put("return", Keyword.RETURN); 
            keywordMap.put("true", Keyword.TRUE); 
            keywordMap.put("false", Keyword.FALSE);
            keywordMap.put("null", Keyword.NULL); 
            keywordMap.put("this", Keyword.THIS);
        }
    }

    private void initializeSymbolSet() {
        if (this.symbolSet == null) {
            this.symbolSet = new HashSet<>();

            symbolSet.add('{');
            symbolSet.add('}');
            symbolSet.add('(');
            symbolSet.add(')');
            symbolSet.add('[');
            symbolSet.add(']');
            symbolSet.add('.');
            symbolSet.add(',');
            symbolSet.add(';');
            symbolSet.add('+');
            symbolSet.add('-');
            symbolSet.add('*');
            symbolSet.add('/');
            symbolSet.add('&');
            symbolSet.add('|');
            symbolSet.add('<');
            symbolSet.add('>');
            symbolSet.add('=');
            symbolSet.add('~');
            symbolSet.add('\"'); // should we include this one?
        }
    }

    /** Read and process Token */

    /**
     * Write strings to the output file.
     * 
     * Method takes care of catching exceptions.
     */
    private void writeToFile(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 


    /**
     * Retrieve one token from buffer.
     * 
     * If the buffer is empty, fill the buffer by reading from input stream.
     */
    private String readTokenFromStream() {
        if (tokenBuffer.size() == 0) {
            readTokensIntoBuffer();
        }

        String res = null;
        if (tokenBuffer.size() > 0) {
            res  = tokenBuffer.get(0);
            tokenBuffer.remove(0);
        }

        return res;
    }

    /**
     * Read one line (excluding single- and multiple- line comment, whitespaces),
     * removed comments appended, process line by identifying different tokens,
     * append tokens to the tokenBuffer instance variable.
     * 
     * Can be called continually to read all tokens into the buffer, but it is best
     * to read only when no tokens are available in the buffer.
     * 
     * If a null is read, it is guaranteed EOF is reached.
     */
    private void readTokensIntoBuffer() {
        try {
            String line;
            boolean multipleLineComment = false;

            if (reader == null) return;

            while (
                tokenBuffer.size() == 0 && (line = reader.readLine()) != null) {

                line = line.trim();

                if (line.length() == 0) continue;

                // Skip API comment
                if (line.startsWith(MULTIPLE_LINE_COMMENT_START)
                    &&
                    line.endsWith(MULTIPLE_LINE_COMMENT_END)) {
                    continue;
                }

                // Skip multiple-line comments 
                if (multipleLineComment) {
                    // skil all lines in the multiple line comment
                    if (line.endsWith(MULTIPLE_LINE_COMMENT_END)) {
                        // end multiple line comment
                        multipleLineComment = !multipleLineComment;
                    }
                    continue;
                }

                if (line.startsWith(MULTIPLE_LINE_COMMENT_START)) {
                    multipleLineComment = !multipleLineComment;
                    continue;
                }

                // comment lines and empty lines
                if (line.length() == 0 || 
                    line.startsWith(SINGLE_LINE_COMMENT_START)) {
                    continue;
                }

                // comment appended at line end?
                int commentStartIndex = line.indexOf(SINGLE_LINE_COMMENT_START);
                if (commentStartIndex != -1) {
                    line = line.substring(0, commentStartIndex).trim();
                }

                // process the current line and move all processed tokens
                // to the buffer
                processCurrentLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the line and add processed tokens to the buffer.
     */
    private void processCurrentLine(String currLine) {
        StringBuilder currCandidate = new StringBuilder();
        boolean stringFlag = false;

        for (char c : currLine.toCharArray()) {
            // ignore empty character where c == 0 (character with value 0 is null)
            if (c == 0) continue;

            if (stringFlag && c != '\"') {  // currently within a string
                currCandidate.append(c);
            } else if (c == '\"') { // start or end a string
                currCandidate.append(c);
                if (stringFlag) {
                    appendToBuffer(currCandidate.toString());
                    currCandidate = new StringBuilder(); // reset
                }
                stringFlag = !stringFlag;
            } else if (symbolSet.contains(c)) { // encounter a symbol
                // if (currCandidate.capacity() == 0) {
                // if (!currCandidate.isEmpty()) {
                if (!currCandidate.toString().isEmpty()) {
                    // take care of the possible token already in buffer
                    appendToBuffer(currCandidate.toString());
                    currCandidate = new StringBuilder();
                }
                appendToBuffer("" + c);
                currCandidate = new StringBuilder(); // reset
            } else if (isWhitespace(c)) { // skip whitespaces
                // if (currCandidate.capacity() == 0) {
                // if (!currCandidate.isEmpty()) {
                if (!currCandidate.toString().isEmpty()) {
                    appendToBuffer(currCandidate.toString());
                    currCandidate = new StringBuilder(); // reset
                }
            } else { // mostly identifiers
                currCandidate.append(c);
            }

            // ignore other symbols not recognizable
        }
    }


    /**
     * Add a new token to the token buffer.
     * Serves to check null inputs.
     */
    private void appendToBuffer(String token) {
        if (token != null) {
            tokenBuffer.add(token);
        }
    }

    /** 
     * White space excluding newline.
     */
    private boolean isWhitespace(char c) {
        return c == ' ';
        // String charString = c + "";
        // if (
        //     charString.equals("\t") ||
        //     charString.equals("\s") ||
        //     charString.equals("\n") ||
        //     charString.equals(" ")) {
        //     return true;
        // }

        // return false;

        // the following line does not compiler in Coursera's grader for
        // "illegal escape character", need further investigation
        // return (c == '\t' || c == '\s' || c == '\n' || c == ' ');
    }

    /** APIs */

    /** 
     * True if next is not null.
     * False is next is null.
     */
    public boolean hasMoreTokens() {
        if (reader == null) return false;

        // read more from buffer, if reader is not null
        if (tokenBuffer.size() == 0) readTokensIntoBuffer();
        if (currentTokenValue == null && nextTokenValue == null) advance();
        boolean res = nextTokenValue != null || tokenBuffer.size() > 0;
        // System.out.printf(
        //     "buffer size - %d\ncurr - %s\nnext - %s\nres - %b\n", 
        //     tokenBuffer.size(), currentTokenValue, nextTokenValue, res);

        if (!res) { // no more tokens from this input jack file
            writeToFile("</tokens>");

            endTokenization();
            // try {
            // System.out.printf("reader is ready? - %b\n", this.reader.ready());
            // System.out.printf("writer is ready? - %b\n", this.writer.ready());
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }

            // endTokenization();
        }

        return res;
    }

    /**
     * Assign the value of next to current.
     * Update the value of next.
     */
    public void advance() {
        // init
        if (nextTokenValue == null) {
            nextTokenValue = readTokenFromStream();
        }

        currentTokenValue = nextTokenValue;
        nextTokenValue = readTokenFromStream();

        // side effects: write to XML after updating the currentTokenValue
        if (currentTokenValue != null) {
            String tokenTypeString = tokenTypeXMLTag();
            writeToFile("<" + tokenTypeString + ">");
            writeToFile(tokenValue());
            writeToFile("</" + tokenTypeString + ">\n");
        }
    }
    

    public Token tokenType() {
        // if (currentTokenValue == null) {
        // System.out.printf("curr token - %s\n", currentTokenValue);
        // }
        if (keywordMap.containsKey(currentTokenValue)) {
            currentTokenType = Token.KEYWORD;
        } else if (currentTokenValue.length() == 1 && 
                    symbolSet.contains(currentTokenValue.charAt(0))) {
            currentTokenType = Token.SYMBOL;
        } else {
            // regex matching
            if (currentTokenValue.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                currentTokenType = Token.IDENTIFIER;
            } else if (currentTokenValue.matches("[0-9]+")) {
                int intVal = Integer.parseInt(currentTokenValue);
                if (intVal < MIN_INT || intVal > MAX_INT) {
                    throw new RuntimeException("integer constant value out of bound");
                }
                currentTokenType = Token.INT_CONST;
            } else if (currentTokenValue.startsWith("\"") &&
                       currentTokenValue.endsWith("\"")) {
                currentTokenType = Token.STRING_CONST;
            }
        }
        // if (currentTokenType == null) {
        //     System.out.printf(
        //         "null tag found: token type - %s\ttoken value - %s\ntoken len: %d\n",
        //         currentTokenType, currentTokenValue, currentTokenValue.length());

        //     currentTokenValue = currentTokenValue.trim();

        //     for (int i = 0; i < currentTokenValue.length(); i++) {
        //         System.out.printf("index, char - %d, %c\n", i, currentTokenValue.charAt(i));
        //     }

        //     System.out.printf("keyword? - %b\n", keywordMap.containsKey(currentTokenValue.trim()));
        // }

        return currentTokenType;
    }
    
    public String keyword() {
        if (currentTokenType == Token.KEYWORD) {
            Keyword k = keywordMap.get(currentTokenValue);
            return k.toString().toLowerCase();
        }

        return null;
    }

    public char symbol() {
        if (currentTokenType == Token.SYMBOL) {
            return currentTokenValue.charAt(0);
        }

        return 0;
    }

    public String identifier() {
        if (currentTokenType == Token.IDENTIFIER) {
            return currentTokenValue;
        }

        return null;
    }

    public int integerValue() {
        if (currentTokenType == Token.INT_CONST) {
            return Integer.parseInt(currentTokenValue);
        }

        return -1;
    }

    public String stringValue() {
        if (currentTokenType == Token.STRING_CONST) { 
            return currentTokenValue.substring(1, currentTokenValue.length()-1);
        }

        return null;
    }

    /**
     * Close input and output files.
     */
    public void endTokenization() {
        try {
            if (writer != null) {
                this.writer.close();
                writer = null;
            }
            if (reader != null) {
                this.reader.close();
                reader = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** API helpers */

    /**
     * Tag name corresponding to the current token type.
     */
    private String tokenTypeXMLTag() {
        Token token = tokenType();

        if (token == Token.KEYWORD) {
            return "keyword";
        } else if (token == Token.SYMBOL) {
            return "symbol";
        } else if (token == Token.INT_CONST) {
            return "integerConstant";
        } else if (token == Token.STRING_CONST) {
            return "stringConstant";
        } else if (token == Token.IDENTIFIER) {
            return "identifier";
        }
        // System.out.printf(
        //     "null tag found: token type - %s\ttoken value - %s\n",
        //     token.toString(), currentTokenValue);
        return null;
    }


    /**
     * The value to be displayed in the XML file.
     */
    private String tokenValue() {
        Token token = tokenType();
        StringBuilder res = new StringBuilder();

        if (token == Token.KEYWORD) {
            res.append(keyword());
        } else if (token == Token.SYMBOL) {
            
            if (currentTokenValue.equals("<")) {
                res.append("&lt;");
            } else if (currentTokenValue.equals(">")) {
                res.append("&gt;");
            } else if (currentTokenValue.equals("&")) {
                res.append("&amp;");
            } else if (currentTokenValue.equals("\"")) {
                res.append("&quot;");
            } else {
                res.append(symbol());
            }
        } else if (token == Token.INT_CONST) {
            res.append(integerValue());
        } else if (token == Token.STRING_CONST) {
            res.append(stringValue());
        } else if (token == Token.IDENTIFIER) {
            res.append(identifier());
        }

        return res.toString();
    }
}