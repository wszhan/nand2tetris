import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.File;

public class CompilationEngine {

    /** I/O */
    private JackTokenizer tokenizer;
    private FileWriter xmlWriter;

    /** Instance variables */
    private String currentToken, nextToken;
    private Token currTokenType;

    private Set<String> statementKeyword;
    private Set<String> dataTypes;
    private Set<String> unaryOperators, binaryOperators, keywordConstants;

    /** Compiler XXX methods */
    public CompilationEngine(String inputJackFilePath, String outputXMLFilePath) {
        this(new File(inputJackFilePath), new File(outputXMLFilePath));
    }

    public CompilationEngine(File inputJackFile, File outputXMLFile) {
    }
    public CompilationEngine(JackTokenizer jackTokenizer, File outputXMLFile) {
        if (jackTokenizer == null || outputXMLFile == null) {
            throw new IllegalArgumentException("null input to constructor.");
        }

        this.tokenizer = jackTokenizer;

        initInstanceVariables();

        try {
            this.xmlWriter = new FileWriter(outputXMLFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        startCompiling();
    }

    /** Init */
    private void initInstanceVariables() {
        initStatementTypes();
        initDataTypes();
        initOperators();
        initKeywordConstants();
    }
    private void initKeywordConstants() {
        keywordConstants = new HashSet<>();
        keywordConstants.add("this"); 
        keywordConstants.add("null"); 
        keywordConstants.add("true"); 
        keywordConstants.add("false"); 
    }
    private void initOperators() {
        unaryOperators = new HashSet<>();
        unaryOperators.add("-");
        unaryOperators.add("~");

        binaryOperators = new HashSet<>();
        binaryOperators.add("+");
        binaryOperators.add("-");
        binaryOperators.add("*");
        binaryOperators.add("/");
        binaryOperators.add("&");
        binaryOperators.add("|");
        binaryOperators.add("<");
        binaryOperators.add(">");
        binaryOperators.add("=");
    }
    private void initStatementTypes() {
        statementKeyword = new HashSet<>();
        
        statementKeyword.add("if");
        statementKeyword.add("while");
        statementKeyword.add("do");
        statementKeyword.add("return");
        statementKeyword.add("let");
    }
    private void initDataTypes() {
        dataTypes = new HashSet<>();
        
        dataTypes.add("int");
        dataTypes.add("boolean");
        dataTypes.add("char");
        dataTypes.add("void");
    }

    /** Go */
    private void startCompiling() {
        while (tokenizer.hasMoreTokens()) {
            // what to compile
            currTokenType = tokenizer.tokenType();
            if (currTokenType == Token.KEYWORD) {
                currentToken = tokenizer.keyword();
                if (currentToken.equals("class")) {
                    compileClass();
                }
            }

            // finish process or peek
            tokenizer.advance();
        }

        endCompilation();
    }
    /**
     * Takes care of exceptions.
     */
    private void writeToOutputFile(String s) {
        // System.out.printf("writing - %s\n", s);
        try {
            xmlWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close input and output files.
     */
    private void endCompilation() {
        try {
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tokenizer.endTokenization();
    }

    private void nextToken() {
        System.out.printf("token - %s\n", currentToken);
        if (nextToken != null) {
            currentToken = nextToken;
            currTokenType = tokenizer.tokenType();
            nextToken = null;
            return;
        }

        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
            currTokenType = tokenizer.tokenType();
            currentToken = getTokenValueByType(currTokenType);
            return;
        }

        currentToken = null;
        currTokenType = null;
        nextToken = null;

        // if (currTokenType == Token.KEYWORD) {
        //     currentToken = tokenizer.keyword();
        // } else if (currTokenType == Token.SYMBOL) {
        //     currentToken = tokenizer.symbol();
        // } else if (currTokenType == Token.IDENTIFIER) {
        //     currentToken = tokenizer.identifier();
        // } else if (currTokenType == Token.INT_CONST) {
        //     currentToken = tokenizer.integerValue();
        // } else if (currTokenType == Token.STRING_CONST) {
        //     currentToken = tokenizer.stringValue();
        // }
    }

    private void peek() {
        if (nextToken != null) {
            throw new RuntimeException("don't peek more than 1 token ahead");
        } 

        tokenizer.advance();
        nextToken = getTokenValueByType(tokenizer.tokenType());
    }

    private String getTokenValueByType() {
        Token type = tokenizer.tokenType();
        return getTokenValueByType(type);
    }

    private String getTokenValueByType(Token type) {
        String res = null;

        if (type == Token.KEYWORD) {
            res = tokenizer.keyword();
        } else if (type == Token.SYMBOL) {
            res = tokenizer.symbol() + "";
        } else if (type == Token.IDENTIFIER) {
            res = tokenizer.identifier();
        } else if (type == Token.INT_CONST) {
            res = tokenizer.integerValue() + "";
        } else if (type == Token.STRING_CONST) {
            res = tokenizer.stringValue();
        }

        return res;
    }
    private void writeCurrentToken() {
        String tag = currTokenType.toString().toLowerCase();
        String outputToken = currentToken;

        if (tag.contains("string")) {
            tag = "stringConstant";
        } else if (tag.contains("int")) {
            tag = "integerConstant";
        }

        if (outputToken.equals("<")) {
            outputToken = "&lt;";
        } else if (outputToken.equals(">")) {
            outputToken = "&gt;";
        } else if (outputToken.equals("&")) {
            outputToken = "&amp;";
        } else if (outputToken.equals("\"")) {
            outputToken = "&quot;";
        }

        writeTagAndValue(tag, outputToken);
    }
    private void writeTagAndValue(String tagName, String val) {
        writeToOutputFile(
            "<" + tagName + ">" + // opening tag
            val + 
            "</" + tagName + ">\n"); // closing tag
    }


    /** Statements */

    /**
     * Five types of statement.
     */
    public void compileStatements() {
        writeToOutputFile("<statements>\n"); // opening tag

        while (statementKeyword.contains(currentToken)) {

            // System.out.printf("stuck with %s\n", currentToken);

            // this is definitely not a statement, for a statement must start with
            // let, if, while, do, or return 
            if (currTokenType != Token.KEYWORD) {
                throw new RuntimeException("not a statement");
            }
            
            if (currentToken.equals("let")) {
                compileLet();
            } else if (currentToken.equals("do")) {
                compileDo();
            } else if (currentToken.equals("while")) {
                compileWhile();
            } else if (currentToken.equals("if")) {
                compileIf();
            } else if (currentToken.equals("return")) {
                compileReturn();
            }

            // current token should be semi-colon
            // advance to the next one
            // writeCurrentToken();
            // nextToken();
        }
        // System.out.printf("finish statements, curr - %s\n", currentToken);

        writeToOutputFile("</statements>\n"); // closing tag
    }

    public void compileLet() {
        writeToOutputFile("<letStatement>\n"); // opening tag

        // let keyword
        writeCurrentToken();
        nextToken();

        // identifier
        if (currTokenType != Token.IDENTIFIER) {
            throw new RuntimeException("expect identifier and found " + currTokenType);
        }
        writeCurrentToken();
        nextToken();
        // System.out.printf("before array access check - %s\n", currentToken);

        // array access?
        if (currentToken.equals("[")) {
            // System.out.printf("[? - %s\n", currentToken);
            // write [
            writeCurrentToken();
            nextToken();

            // expression
            compileExpression();

            // ]
            if (!currentToken.equals("]")) {
                throw new RuntimeException("expect ] and found " + currentToken);
            }
            // System.out.printf("]? - %s\n", currentToken);
            writeCurrentToken();
            nextToken();
        }

        // assignment symbol "="
        if (!currentToken.equals("=")) {
            throw new RuntimeException("expect = and found " + currentToken);
        }
        writeCurrentToken();
        nextToken();

        // expression
        compileExpression();
        // System.out.printf("Token at the end of let statement - %s\n", currentToken);
        // nextToken();
        // writeCurrentToken();

        // semi-colon; end of statement
        if (!currentToken.equals(";")) {
            throw new RuntimeException("expect ; and found " + currentToken);
        }
        writeCurrentToken();
        nextToken();
        // System.out.printf("Token at the end of let statement - %s\n", currentToken);

        writeToOutputFile("</letStatement>\n"); // closing tag
    }
    public void compileIf() {
        writeToOutputFile("<ifStatement>\n"); // opening tag

        boolean ifClause = true;

        while (
            currentToken.equals("if") && ifClause || 
            currentToken.equals("else") && !ifClause) {

            // System.out.printf("inside if statement - %s\n", currentToken);

            // if (currentToken.equals("if")) expectElseKeyword = true;

            // if/else keyword
            // if (!currentToken.equals("if")) {
            //     throw new RuntimeException("expect if and found " + currentToken);
            // }
            writeCurrentToken();
            nextToken();

            if (ifClause) {
                // left paranthesis
                if (!currentToken.equals("(")) {
                    throw new RuntimeException("expect left parenthesis");
                }
                writeCurrentToken();
                nextToken();

                // expression
                compileExpression(); // no need to next
                // nextToken();

                // right paranthesis
                if (!currentToken.equals(")")) {
                    throw new RuntimeException("expect right parenthesis - " + currentToken);
                }
                writeCurrentToken();
                nextToken();
            }

            // left curly bracket 
            if (!currentToken.equals("{")) {
                throw new RuntimeException("expect left curly bracket - " + currentToken);
            }
            writeCurrentToken();
            nextToken();

            // statements
            compileStatements();

            // right curly bracket 
            if (!currentToken.equals("}")) {
                throw new RuntimeException("expect right curly bracket, and found " + currentToken);
            }
            writeCurrentToken();
            nextToken();

            // next should be else clause if any
            ifClause = !ifClause;
        }

        // System.out.printf("exit if with token - %s\n", currentToken);

        writeToOutputFile("</ifStatement>\n"); // closing tag
    }
    public void compileWhile() {
        writeToOutputFile("<whileStatement>\n"); // opening tag

        if (currentToken.equals("while")) {

            // while keyword
            writeCurrentToken();
            nextToken();

            // left paranthesis
            if (!currentToken.equals("(")) {
                throw new RuntimeException("expect left parenthesis, found " + currentToken);
            }
            writeCurrentToken();
            nextToken();

            // expression
            compileExpression();
            // nextToken();

            // right paranthesis
            if (!currentToken.equals(")")) {
                throw new RuntimeException("expect right parenthesis, found " + currentToken);
            }
            writeCurrentToken();
            nextToken();

            // left curly bracket 
            if (!currentToken.equals("{")) {
                throw new RuntimeException("expect left curly bracket");
            }
            writeCurrentToken();
            nextToken();

            // statements
            compileStatements();

            // right curly bracket 
            if (!currentToken.equals("}")) {
                throw new RuntimeException("expect right curly bracket");
            }
            writeCurrentToken();
            nextToken();
        }

        writeToOutputFile("</whileStatement>\n"); // closing tag
    }
    public void compileDo() {
        System.out.printf("Do statement\n");
        writeToOutputFile("<doStatement>\n"); // opening tag

        // do keyword
        writeCurrentToken();
        System.out.printf("write do keyword - %s\n", currentToken);
        nextToken();

        // Class or method before left parenthesis
        while (!currentToken.equals("(")) {

            System.out.printf("stuck with ? - %s\n", currentToken);

            // identifier: class name, method name, or class object name
            writeCurrentToken();
            // System.out.printf("write identifier - %s\n", currentToken);
            nextToken();

            // write dot operator and proceed to the identifier (method/function)
            if (currentToken.equals(".")) {
                // System.out.printf("write dot operator - %s\n", currentToken);
                writeCurrentToken();
                nextToken();
            }

            // move on
            // nextToken();
        }

        // System.out.printf("loop broken, write left paren - %s\n", currentToken);

        // left paranthesis
        writeCurrentToken();
        nextToken();

        // expression
        System.out.printf("BEFORE exp list - %s\n", currentToken);
        compileExpressionList();
        System.out.printf("AFTER exp list - %s\n", currentToken);

        // System.out.printf("write right paren - %s\n", currentToken);

        // right paranthesis
        writeCurrentToken();
        nextToken();

        // semi-colon
        writeCurrentToken();
        nextToken();

        writeToOutputFile("</doStatement>\n"); // closing tag
    }

    public void compileReturn() {
        writeToOutputFile("<returnStatement>\n"); // opening tag

        // return keyword
        writeCurrentToken();
        nextToken();

        // expression?
        if (!currentToken.equals(";")) {
            compileExpression();
            // nextToken();
        }

        // semi-colon
        writeCurrentToken();
        nextToken();

        writeToOutputFile("</returnStatement>\n"); // closing tag
    }

    /** Expressions */

    /**
     * Expression might come in different forms:
     * - () enclosed in parantheses
     * - to the right side of assignment symbol
     * - array[expression]
     */
    public void compileExpression() {
        writeToOutputFile("<expression>\n"); // opening tag
        System.out.printf("==== expression begins ====\n");

        if (
            // currentToken.equals("(") || 
            currentToken.equals("[") || 
            currentToken.equals("=")) {
            
            throw new RuntimeException("invalid first token in expression - " + currentToken);

        //     System.out.printf("nested term\n");
        //     compileTerm();
        //     // // write (
        //     // writeCurrentToken();
        //     // nextToken();

        //     // compileExpression();

        //     // // write )
        //     // writeCurrentToken();
        //     // nextToken();
        }

        // if (currentToken.equals("null")) {
        //     System.out.printf("null token type - %s\n", currTokenType);
        //     System.out.printf("keyword const? %b\n", keywordConstants.contains(currentToken));
        // }
        int n = 0;
        boolean unary = unaryOperators.contains(currentToken);

        while (
            !currentToken.equals(",") && // end this expression and move on to the next
            !currentToken.equals(";") &&
            !currentToken.equals(")") &&
            !currentToken.equals("]")) {
            n++;
            if (n > 20) break;
            System.out.printf("exp get stuck with - %s\n", currentToken);
            // System.out.printf("tokenType - %s\n", currTokenType);
            if (currentToken.equals("(")) {
                compileTerm();
            } else if (
                currTokenType == Token.IDENTIFIER || 
                currTokenType == Token.STRING_CONST||
                currTokenType == Token.INT_CONST ||
                currTokenType == Token.INT_CONST ||
                keywordConstants.contains(currentToken)
                ) {
                compileTerm();
            } else if (
                unaryOperators.contains(currentToken) ||
                binaryOperators.contains(currentToken)
            ) {
            // } else if (unaryOperators.contains(currentToken)) {
            //     compileTerm();
            // } else if (binaryOperators.contains(currentToken)) {
                if (!unary) {
                    // write operator
                    writeCurrentToken();
                    System.out.printf("operator - %s\n", currentToken);
                    nextToken();
                }

                System.out.printf("left operend 1st token - %s\n", currentToken);
                // operator always followed by expression
                compileTerm();
                // if (currTokenType == Token.IDENTIFIER) compileTerm();
                // else compileExpression();
            }
        }

        System.out.printf("loop broken, current token - %s\n", currentToken);
        System.out.printf("==== expression ends ====\n");
        writeToOutputFile("</expression>\n"); // closing tag
    }
    public void compileTerm() {
        writeToOutputFile("<term>\n"); // opening tag

        // nested
        if (currentToken.equals("(")) {
            // left paranthesis
            writeCurrentToken();
            nextToken();

            compileExpression();

            // right paranthesis
            writeCurrentToken();
            nextToken();

            writeToOutputFile("</term>\n"); // closing tag

            return;
        }

        if (unaryOperators.contains(currentToken)) {
            // write operator
            writeCurrentToken();
            nextToken();

            compileTerm();

            writeToOutputFile("</term>\n"); // closing tag

            return;
        }
        
        // identifier
        writeCurrentToken();
        nextToken();

        // array access
        if (currentToken.equals("[")) {
            // write [
            writeCurrentToken();
            nextToken();

            // expression
            compileExpression();

            if (!currentToken.equals("]")) {
                throw new RuntimeException("expect ] and found " + currentToken);
            } else {
                writeCurrentToken();
                nextToken();
            }
        }
        
        // subroutine call
        if (currentToken.equals(".")) {
            // write dot operator
            writeCurrentToken();
            nextToken();

            // subroutine identifier
            writeCurrentToken();
            nextToken();

            if (!currentToken.equals("(")) {
                throw new RuntimeException("expect ( and found " + currentToken);
            } else {
                // (
                writeCurrentToken();
                nextToken();

                compileExpressionList();

                // )
                writeCurrentToken();
                nextToken();
            }
        }

        writeToOutputFile("</term>\n"); // closing tag
    }
    public void compileExpressionList() {
        writeToOutputFile("<expressionList>\n"); // opening tag

        // while (currentToken.equals(",")) {
        //     System.out.printf("curr in exp list comma loop - %s\n", currentToken);
        //     compileExpression();
        // }

        while (!currentToken.equals(")") || currentToken.equals(",")) {
            System.out.printf("stuck exp list - %s\n", currentToken);
            // at least one expression
            if (currentToken.equals(",")) {
                // write comma and move on to the next expression
                writeCurrentToken();
                nextToken();
            }

            compileExpression();
        }
        System.out.printf("exit exp list - %s\n", currentToken);

        writeToOutputFile("</expressionList>\n"); // closing tag
    }


    /** Subroutines */

    public void compileSubroutine() {
        writeToOutputFile("<subroutineDec>\n"); // opening tag

        compileSubroutineDeclaration();

        compileSubroutineBody();

        writeToOutputFile("</subroutineDec>\n"); // closing tag
    }

    public void compileSubroutineDeclaration() {

        // keyword: function/method
        writeCurrentToken();

        // keyword: return type
        // could be keyword data type or custom type
        nextToken();
        writeCurrentToken();

        // identifier: function or method name
        nextToken();
        writeCurrentToken();

        // left paranthesis
        nextToken();
        writeCurrentToken();
        
        // parameter list
        nextToken();
        compileParameterList();

        // right paranthesis
        // nextToken();
        writeCurrentToken();
        nextToken();

    }
    public void compileParameterList() {
        writeToOutputFile("<parameterList>\n"); // opening tag

        // right paranthesis if empty list, in which case just skip to the end
        // otherwise, go through the list
        while (!currentToken.equals(")")) {
            // keyword: type
            writeCurrentToken();
            nextToken();

            // identifier
            writeCurrentToken();
            nextToken();

            // list found, write the comma and move on,
            // wait for the writing of next type and variable
            if (currentToken.equals(",")) { 
                writeCurrentToken();
                nextToken();
            }
        }

        writeToOutputFile("</parameterList>\n"); // closing tag
    }
    public void compileSubroutineBody() {
        writeToOutputFile("<subroutineBody>\n"); // opening tag

        // left curly bracket
        if (!currentToken.equals("{")) {
            throw new RuntimeException("expect left curly bracket");
        }
        writeCurrentToken();
        nextToken();

        // local variables declaraction
        while (currentToken.equals("var")) {
            compileSubroutineVariableDeclaration();
        }

        // statements
        compileStatements();

        // right curly bracket
        writeCurrentToken();
        nextToken();

        writeToOutputFile("</subroutineBody>\n"); // closing tag

    }

    /**
     * Compile one line of declaration only.
     */
    public void compileSubroutineVariableDeclaration() {
        writeToOutputFile("<varDec>\n"); // opening tag

        // while (currentToken.equals("var")) {
            // System.out.printf("new declaration line\n");
            // var keyword
            writeCurrentToken();
            nextToken();

            // keyword: type
            // System.out.printf("var data type - %s\n", currentToken);
            writeCurrentToken();
            nextToken();

            // during a variable declaration
            while (!currentToken.equals(";")) {

                // identifier
                // System.out.printf("var name - %s\n", currentToken);
                writeCurrentToken();
                nextToken();

                if (currentToken.equals(",")) { // list found
                    // System.out.printf("same line next variable\n");
                    writeCurrentToken();
                    nextToken();
                } else if (currentToken.equals(";")) { // end current declaration
                    // System.out.printf("current declaration line ended\n");
                    // write semi-colon and move on
                    writeCurrentToken(); 
                    nextToken();
                    break;
                }
            }
        // }

        // System.out.printf("all local declaration ended \n");
        writeToOutputFile("</varDec>\n"); // closing tag
    }

    /** Classes */
    public void compileClass() {
        writeToOutputFile("<class>\n"); // class opening tag

        // keyword: class
        writeCurrentToken();

        // identifier: class name 
        nextToken();
        if (currTokenType != Token.IDENTIFIER) {
            throw new RuntimeException("class construct error: identifier expected");
        }
        writeCurrentToken();

        // symbol: {
        nextToken();
        if (currTokenType != Token.SYMBOL || !currentToken.equals("{")) {
            // System.out.printf("current token - %s and len %d\n", currentToken, currentToken.length());
            throw new RuntimeException("class construct error: { expected");
        }
        writeCurrentToken();

        // class variable declarations or subroutine declaration
        nextToken();
        // int n = 0;
        while (!currentToken.equals("}")) {
            // System.out.printf("current token - %s\n", currentToken);
            // System.out.printf("current token type - %s\n", currTokenType);
            if (currTokenType == Token.KEYWORD) {
                if (
                    currentToken.equals("field") ||
                    currentToken.equals("static")) {
                    compileClassVariableDeclaration();
                } else if (
                    currentToken.equals("method") || 
                    currentToken.equals("constructor") || 
                    currentToken.equals("function")) {
                    // System.out.printf("a method declared\n");
                    compileSubroutine();
                    // compileSubroutineDeclaration();
                    // compileSubroutineBody();
                }
            }

            // n++;
            // if (n > 10) {
            //     // writeToOutputFile("\nbreak here\n");
            //     break;
            // }
        }

        // if (currTokenType == Token.KEYWORD && currentToken.equals("field")) {
        //     compileClassVariableDeclaration();
        // } else if (
        //     (currentToken.equals("method")) {

        // }

        // statements
        // nextToken();
        // compileStatements();

        // symbol: }
        writeCurrentToken();
        nextToken();
        // if (currTokenType != Token.SYMBOL || !currentToken.equals("}")) {
        //     // System.out.printf("current token - %s and len %d\n", currentToken, currentToken.length());
        //     throw new RuntimeException("class construct error: } expected");
        // }
        // writeCurrentToken();
        
        writeToOutputFile("</class>"); // class closing tag
    }
    public void compileClassVariableDeclaration() {
        while (
            currentToken.equals("field") ||
            currentToken.equals("static")) {
            writeToOutputFile("<classVarDec>\n"); // opening tag

            // var keyword
            writeCurrentToken();
            nextToken();

            // keyword: type
            writeCurrentToken();
            nextToken();

            // during a variable declaration
            while (!currentToken.equals(";")) {

                // identifier
                writeCurrentToken();
                nextToken();

                if (currentToken.equals(",")) { // list found
                    writeCurrentToken();
                    nextToken();
                } else if (currentToken.equals(";")) { // end current declaration
                    // write semi-colon and move on
                    writeCurrentToken(); 
                    nextToken();
                    break;
                }
            }

            writeToOutputFile("</classVarDec>\n"); // closing tag
        }
    }

    /**
     * Called after the field/const/var keyword.
     */
    private void compileVariablesDeclaraction() {
        writeToOutputFile("<classVarDec>\n"); // opening tag

        // System.out.printf("all local declaration ended \n");
        writeToOutputFile("</classVarDec>\n"); // closing tag
        
    }
}
