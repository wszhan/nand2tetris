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
    private String newline = System.getProperty("line.separator");

    private String MULTIPLE_LINE_COMMENT_START = "/*";
    private String MULTIPLE_LINE_COMMENT_END = "*/";
    private String SINGLE_LINE_COMMENT_START = "//";

    private int MIN_INT = 0;
    private int MAX_INT = 32767;

    private BufferedReader reader;
    // private FileReader reader;
    // private Scanner sc;
    private FileWriter writer;

    private Map<String, Keyword> keywordMap = null;
    private Set<Character> symbolSet = null;
    private Set<Character> specialSymbols = null;

    // intermediate processing
    private LinkedList<String> tokenBuffer;

    private String currentTokenValue;
    private String nextTokenValue;
    private Token currentTokenType;
    private Token nextTokenType;

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
            // this.reader = new FileReader(inputJackFile);
            // this.sc = new Scanner(inputJackFile);
            this.writer = new FileWriter(outputXMLFile);
            this.writer.write("<tokens>\n");

            // System.out.printf("more tokens? %b\n", hasMoreTokens());

            // debug
            while (hasMoreTokens()) {
                advance();
                // System.out.printf(
                //     "token - %s\t\ttype - %s\n", currentTokenValue, tokenType() 
                    // (currentTokenType == Token.SYMBOL ? "symbol" : 
                    //     (currentTokenType == Token.KEYWORD ? "keyword" : null))
                // );
            }

            this.writer.write("</tokens>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String s) {
        try {
            writer.write(s);
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

        // if (specialSymbols == null) {
        //     specialSymbols.add('&');
        //     specialSymbols.add('<');
        //     specialSymbols.add('>');
        // }
    }

    /** Read and process Token */
    public String readTokenFromStream() {
        if (tokenBuffer.size() == 0) {
            readTokensIntoBuffer();
        }

        String res = null;
        if (tokenBuffer.size() > 0) {
            res  = tokenBuffer.get(0);
            tokenBuffer.remove(0);
        }

        // if (res == null) throw new IllegalArgumentException("null token discovered");

        return res;
        // return  ? tokenBuffer.get(0) : null;
    }

    private void readTokensIntoBuffer() {
        // StringBuilder curr = new StringBuilder();

        try {
            String line;
            boolean multipleLineComment = false;

            while (tokenBuffer.size() == 0 && (line = reader.readLine()) != null) {
                line = line.trim();
                // Skip multiple-line comments 
                if (multipleLineComment) {
                    // skil all lines in the multiple line comment
                    if (line.endsWith(MULTIPLE_LINE_COMMENT_END)) {
                        multipleLineComment = !multipleLineComment;
                        continue;
                    }
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

                // process the current line and move all processed tokens
                // to the buffer
                processCurrentLine(line);
                
                // for (String token : tokenBuffer) {
                //     System.out.printf("token in buffer - %s\n", token);
                // }
            }

            // currentTokenValue = tokenBuffer.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the line and add processed tokens to the buffer.
     */
    private void processCurrentLine(String currLine) {
        // first pass processing: split by space
        // edge case: string
        // String[] candidates = currLine.split(" ");
        // List<String> candidates = new ArrayList<>();
        StringBuilder currCandidate = new StringBuilder();
        boolean stringFlag = false;

        for (char c : currLine.toCharArray()) {
            // ignore empty character where c == 0 (character with value 0 is null)
            if (c == 0) continue;

            if (stringFlag && c != '\"') {   
                currCandidate.append(c);
            } else if (c == '\"') { // highest priority
                currCandidate.append(c);
                if (stringFlag) {
                    appendToBuffer(currCandidate.toString());
                    // tokenBuffer.add(currCandidate.toString());
                    currCandidate = new StringBuilder(); // reset
                }
                stringFlag = !stringFlag;
            } else if (symbolSet.contains(c)) {
                if (!currCandidate.isEmpty()) {
                    // take care of the possible token already in buffer
                    appendToBuffer(currCandidate.toString());
                    // tokenBuffer.add(currCandidate.toString());
                    currCandidate = new StringBuilder();
                }
                // if (c == '>') {
                // // if (candidate.equals(">")) {
                //     currCandidate.append("&gt;");
                // } else if (c == '<') {
                // // } else if (candidate.equals("<")) {
                //     currCandidate.append("&lt;");
                // } else if (c == '&') {
                // // } else if (candidate.equals("&")) {
                //     currCandidate.append("&amp;");
                // } else if (c == '\"') {
                // // } else if (candidate.equals("\"")) {
                //     currCandidate.append("&quot;");
                // } else {
                //     currCandidate.append(c);
                // }
                appendToBuffer("" + c);
                // appendToBuffer(currCandidate.toString());
                // tokenBuffer.add(currCandidate.toString());
                currCandidate = new StringBuilder(); // reset
            } else if (isWhitespace(c)) {
                if (!currCandidate.isEmpty()) {
                    appendToBuffer(currCandidate.toString());
                    // tokenBuffer.add(currCandidate.toString());
                    currCandidate = new StringBuilder(); // reset
                }
            } else {
                currCandidate.append(c);
            }

            // ignore other symbols not recognizable
        }

        // for (String candidate : candidates) {
        //     System.out.printf("candidate - %s\n", candidate);
        // }
    }

    private void appendToBuffer(String token) {
        if (token != null) {
            tokenBuffer.add(token);
        }
    }

    private List<String> processTokenCandidate(String candidate) {
        List<String> res = new ArrayList<>();

        // return if keyword or symbol
        if (keywordMap.containsKey(candidate) ||
            symbolSet.contains(candidate)) {
            res.add(candidate);
        // } else if (symbolSet.contains(candidate)) {
        //     if (candidate.equals(">")) {
        //         res.add("&lt;");
        //     } else if (candidate.equals("<")) {
        //         res.add("&gt;");
        //     } else if (candidate.equals("&")) {
        //         res.add("&amp;");
        //     } else if (candidate.equals("\"")) {
        //         res.add("&quot;");
        //     } else {
        //         res.add(candidate);
        //     }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < candidate.length(); i++) {

            }
        }

        return res;
    }
    /** 
     * New line is to mark the end of an comment.
     */
    private boolean isNewline(char c) {
        return c == '\n';
    }
    /** 
     * White space excluding newline.
     */
    private boolean isWhitespace(char c) {
        return (c == '\t' || c == '\s' || c == '\n' || c == ' ');
    }

    /** Write to XML */
    private void writeToXML() {
        // Token type opening tag

        // Token value

        // Token type closing tag
        
    }

    /** API */

    /** 
     * True if next is not null.
     * False is next is null.
     */
    public boolean hasMoreTokens() {
        if (tokenBuffer.size() == 0) readTokensIntoBuffer();
        return nextTokenValue != null || tokenBuffer.size() > 0;
        // return true;
    }

    /**
     * Assign the value of next to current.
     * Update the value of next.
     */
    public void advance() {
        // System.out.printf("curr token value - %s\n", currentTokenValue);
        // System.out.printf("next token value - %s\n", nextTokenValue);
        // if (!hasMoreTokens()) throw new ExhaustedTokenException("no more tokens");

        // init
        if (nextTokenValue == null) {
            nextTokenValue = readTokenFromStream();
        }

        currentTokenValue = nextTokenValue;
        nextTokenValue = readTokenFromStream();

        // write to XML after updating the currentTokenValue
        String tokenTypeString = tokenTypeXMLTag();
        // System.out.printf(
        //     "token, tokenType, tokenTypeString - %s\t%s\t%s\n", 
        //     tokenValue(), tokenType(), tokenTypeString);
        writeToFile("\t<" + tokenTypeString + ">");
        writeToFile(tokenValue());
        writeToFile("</" + tokenTypeString + ">\n");

        // if (currentTokenValue == null) {
        //     if (nextTokenValue != null) {
        //     } else {
        //         currentTokenValue = readTokenFromStream();
        //     }
        // } else {
        // }
    }
    
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

        return null;
    }

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

    public Token tokenType() {
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
        if (currentTokenType == Token.IDENTIFIER) return currentTokenValue;

        return null;
    }

    public int integerValue() {
        if (currentTokenType == Token.INT_CONST) return Integer.parseInt(currentTokenValue);

        return -1;
    }

    public String stringValue() {
        if (currentTokenType == Token.STRING_CONST) return currentTokenValue.substring(1, currentTokenValue.length()-1);

        return null;
    }

    public void endTokenization() {
        try {
            this.writer.close();
            this.reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}