import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.io.File;

public class CompilationEngine {

    /** I/O */
    private JackTokenizer tokenizer;
    private FileWriter xmlWriter;
    private VMWriter vmWriter;
    private SymbolTable classSymbolTable, currSubroutineST;

    /** Instance variables */
    private String currentToken, nextToken;
    private Token currTokenType;
    private String className;

    private Set<String> statementKeyword;
    private Set<String> dataTypes;
    private Set<String> unaryOperators, binaryOperators, keywordConstants;

    /** Compiler XXX methods */
    public CompilationEngine(
        String inputJackFilePath, 
        String outputXMLFilePath, 
        String outputVmFilePath) {
        this(
            new File(inputJackFilePath), new File(outputXMLFilePath), 
            new File(outputVmFilePath));
    }

    public CompilationEngine(File inputJackFile, File outputXMLFile, File outputVMFile) {
    }
    public CompilationEngine(JackTokenizer jackTokenizer, File outputXMLFile, File outputVMFile) {
        if (jackTokenizer == null || outputXMLFile == null) {
            throw new IllegalArgumentException("null input to constructor.");
        }

        this.tokenizer = jackTokenizer;

        initInstanceVariables();
        initClassName(outputVMFile);
        this.vmWriter = new VMWriter(outputVMFile);

        try {
            this.xmlWriter = new FileWriter(outputXMLFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // String fileName = outputVMFile.getName();
        // int idx = fileName.indexOf(".");
        // String className = fileName.substring(0, idx);
        // this.className = className;

        startCompiling();
    }

    /** Init */
    private void initInstanceVariables() {
        initStatementTypes();
        initDataTypes();
        initOperators();
        initKeywordConstants();
        classSymbolTable = new SymbolTable(true); // class symbol table
    }
    private void initClassName(File outputVMFile) {
        String fileName = outputVMFile.getName();
        int idx = fileName.indexOf(".");
        String className = fileName.substring(0, idx);
        this.className = className;
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
        vmWriter.close();
    }

    /**
     * Takes care of exceptions.
     */
    private void writeToOutputFile(String s) {
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
        // System.out.printf("token - %s\n", currentToken);
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
    }

    /**
     * Due to the JackTokenizer API, different token type returns values
     * of different types.
     */
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

            // semi-colon or right curly bracket (closing code block)
            // should be taken care in respective methods rather than here
        }

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
        String varToBeDefined = currentToken;
        nextToken();

        // array access?
        if (currentToken.equals("[")) {
            // write [
            writeCurrentToken();
            nextToken();

            // expression
            compileExpression();

            // ]
            if (!currentToken.equals("]")) {
                throw new RuntimeException("expect ] and found " + currentToken);
            }
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

        // semi-colon; end of statement
        if (!currentToken.equals(";")) {
            throw new RuntimeException("expect ; and found " + currentToken);
        }
        writeCurrentToken();
        nextToken();

        // assignment
        VariableKind kind = getVariableKind(varToBeDefined);
        int idx = getVariableIndex(varToBeDefined);
        VirtualSegment seg = kindToVirtualSegment(kind);
        vmWriter.writePop(seg, idx);

        writeToOutputFile("</letStatement>\n"); // closing tag
    }
    
    /**
     * if or if-else. no else if is allowed.
     */
    public void compileIf() {
        writeToOutputFile("<ifStatement>\n"); // opening tag

        boolean ifClause = true;

        while (
            currentToken.equals("if") && ifClause || 
            currentToken.equals("else") && !ifClause) {

            // if/else keyword
            writeCurrentToken();
            nextToken();

            // if is followed by an expression evaluated to a boolean value,
            // while else is followed by a code block
            if (ifClause) {
                // left paranthesis
                if (!currentToken.equals("(")) {
                    throw new RuntimeException("expect left parenthesis");
                }
                writeCurrentToken();
                nextToken();

                // expression
                compileExpression(); // no need to next

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


    /**
     * Three cases:
     * Do method(expression);
     * Do ClassName.method(expression);
     * Do classObjectIdentifier.method(expression);
     * 
     * Question:
     * Is ObjectArray[index].method/function allowed?
     */
    public void compileDo() {
        String firstPart = null, secondPart = null, func = null;
        boolean hasDotOperator = false;
        boolean isMethodCall = true;
        writeToOutputFile("<doStatement>\n"); // opening tag

        // do keyword
        writeCurrentToken();
        nextToken();

        // Class or method before left parenthesis
        while (!currentToken.equals("(")) {

            // identifier: class name, method name, or class object name
            writeCurrentToken();
            if (firstPart == null && secondPart == null) firstPart = currentToken;
            else if (firstPart != null && secondPart == null) secondPart = currentToken;

            nextToken();

            // write dot operator and proceed to the identifier (method/function)
            if (currentToken.equals(".")) {
                hasDotOperator = true;
                writeCurrentToken();
                nextToken();

                char c = firstPart.charAt(0);

                if (c >= 'A' && c <= 'Z') isMethodCall = false; // is class function call
                hasDotOperator = true;
            }
        }

        // System.out.printf("1st, 2nd - %s, %s\n", firstPart, secondPart);
        // left paranthesis
        writeCurrentToken();
        nextToken();

        // push obj if any before push other arguments
        if (firstPart != null && secondPart != null) {
            if (isMethodCall) {
                // case 1: obj.method()
                // push the obj to the stack as the 1st argument, push others as well
                // call Class.method numberOfArguments+1

                // search the obj in the subroutine and class STs
                VariableKind kind = getVariableKind(firstPart);
                int idx = getVariableIndex(firstPart);
                String objClass = getVariableType(firstPart);;
                // VariableKind kind = currSubroutineST.kindOf(firstPart);
                // int idx = currSubroutineST.indexOf(firstPart);
                // String objClass = currSubroutineST.typeOf(firstPart);;
                // if (kind == VariableKind.NONE || kind == null) { // cannot find in local scope
                //     kind = classSymbolTable.kindOf(firstPart); // search in class scope
                //     idx = classSymbolTable.indexOf(firstPart);
                //     objClass = currSubroutineST.typeOf(firstPart);;
                // }
                VirtualSegment seg = kindToVirtualSegment(kind);
                vmWriter.writePush(seg, idx); // push obj to stack as args[0]

                // func name is ObjClass.method
                func = objClass + "." + secondPart;
            } else {
                // case 2: Class.function()
                // no need to push obj, just call the func name
                func = firstPart + "." + secondPart;
            }

        } else if (firstPart != null) {
            // case 3: method()
            // calling method within the class, no dot operator presented, thus 
            // 2nd part is null
            func = className + "." + firstPart;
        }

        // expression
        int numberOfArguments = compileExpressionList();

        // right paranthesis
        writeCurrentToken();
        nextToken();

        // semi-colon
        writeCurrentToken();
        nextToken();

        // System.out.printf("func, nArgs - %s, %d\n", func, numberOfArguments);
        vmWriter.writeCall(func, numberOfArguments);
        vmWriter.writePop(VirtualSegment.TEMP, 0); // discard stack top element of which the value is 0

        writeToOutputFile("</doStatement>\n"); // closing tag
    }

    public void compileReturn() {
        boolean returnVoid = true;
        writeToOutputFile("<returnStatement>\n"); // opening tag

        // return keyword
        writeCurrentToken();
        nextToken();

        // expression?
        if (!currentToken.equals(";")) {
            returnVoid = false;
            compileExpression();
        }

        // semi-colon
        writeCurrentToken();
        nextToken();

        if (returnVoid) {
            vmWriter.writePush(VirtualSegment.CONSTANT, 0); // 0 at stack top
            // vmWriter.writePop(VirtualSegment.TEMP, 0);
        }

        vmWriter.writeReturn();
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

        // expression should not start with [ or =
        if (currentToken.equals("[") || 
            currentToken.equals("=")) {
            throw new RuntimeException("invalid first token in expression - " + currentToken);
        }

        boolean unary = unaryOperators.contains(currentToken);

        while (
            !currentToken.equals(",") && // end this expression and move on to the next
            !currentToken.equals(";") &&
            !currentToken.equals(")") &&
            !currentToken.equals("]")
            ) {
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
                String op = currentToken;
                // unary operator is part of the term itself
                if (!unary) {
                    // write operator
                    writeCurrentToken();
                    nextToken();
                }

                // operator always followed by expression
                compileTerm();
                compileOperator(op, !unary);
            }
        }

        writeToOutputFile("</expression>\n"); // closing tag
    }
    private void compileOperator(String op, boolean binary) {
        if (op.equals("*")) {
            vmWriter.writeCall("Math.multiply", 2);
        } else if (op.equals("/")) {
            vmWriter.writeCall("Math.divide", 2);
        } else if (op.equals("+")) {
            vmWriter.writeArithmetic("add");
        } else if (op.equals("=")) {
            vmWriter.writeArithmetic("eq");
        } else if (op.equals("<")) {
            vmWriter.writeArithmetic("lt");
        } else if (op.equals(">")) {
            vmWriter.writeArithmetic("gt");
        } else if (op.equals("~")) {
            vmWriter.writeArithmetic("not");
        } else if (op.equals("&")) {
            vmWriter.writeArithmetic("and");
        } else if (op.equals("|")) {
            vmWriter.writeArithmetic("or");
        } else if (op.equals("-")) {
            if (binary) {
                vmWriter.writeArithmetic("sub");
            } else {
                vmWriter.writeArithmetic("neg");
            }
        }
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
            String op = currentToken;
            nextToken();

            compileTerm();
            compileOperator(op, false); // unary operator

            writeToOutputFile("</term>\n"); // closing tag

            return;
        }
        
        // identifier
        writeCurrentToken();
        String currTerm = currentToken;
        boolean currTermProcessed = false;
        // currTerm possibility: integerConstant, stringConstant, keywordConstant, 
        // varName(object, primitive type, etc.), arrayName (taken care of in the next if)
        if (currTokenType == Token.INT_CONST) {
            vmWriter.writePush(VirtualSegment.CONSTANT, Integer.parseInt(currTerm));
            currTermProcessed = true;
        } else if (currTokenType == Token.STRING_CONST) {
            currTermProcessed = true;
            compileString(currTerm);
        } else if (currTokenType == Token.KEYWORD) {
            if (currTerm.equals("null")) {
                vmWriter.writePush(VirtualSegment.CONSTANT, 0);
            } else if (currTerm.equals("false")) {
                vmWriter.writePush(VirtualSegment.CONSTANT, 0);
            } else if (currTerm.equals("false")) {
                vmWriter.writePush(VirtualSegment.CONSTANT, 1);
                vmWriter.writeArithmetic("neg");
            }
            currTermProcessed = true;
        } else {

        }
        
        nextToken();

        if (currentToken.equals("[")) { // array access
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
        } else if (currentToken.equals(".")) { // subroutine call
            char c = currTerm.charAt(0);
            boolean functionCall = c >= 'A' && c <= 'Z'; // last token is class name

            // write dot operator
            writeCurrentToken();
            nextToken();

            // subroutine identifier
            writeCurrentToken();
            String subroutineName = currentToken;
            if (functionCall) {
                subroutineName = currTerm + "." + subroutineName;
            } else { // method call
                // push the obj, find the type, then call the method
                VariableKind kind = getVariableKind(currTerm);
                int idx = getVariableIndex(currTerm);
                String objClass = getVariableType(currTerm);;
                VirtualSegment seg = kindToVirtualSegment(kind);
                vmWriter.writePush(seg, idx); // push obj to stack as args[0]
                subroutineName = objClass + "." + subroutineName;
            }
            int numberOfArgs = 0;
            nextToken();

            if (!currentToken.equals("(")) {
                throw new RuntimeException("expect ( and found " + currentToken);
            } else {
                // (
                writeCurrentToken();
                nextToken();

                numberOfArgs = compileExpressionList();

                // )
                writeCurrentToken();
                nextToken();
            }

            // call the subroutine when we know the name and nArgs
            vmWriter.writeCall(subroutineName, numberOfArgs);
        } else { // just identifier
            if (!currTermProcessed) {
                VariableKind kind = getVariableKind(currTerm);
                int idx = getVariableIndex(currTerm);
                VirtualSegment seg = kindToVirtualSegment(kind);
                if (seg == null) {
                    System.out.printf(
                        "null seg found\ncurr Term, kind, index, segName - %s, %s, %d, %s\n",
                        currTerm, kind.toString(), idx, seg);
                }
                vmWriter.writePush(seg, idx); 
            }
        }

        writeToOutputFile("</term>\n"); // closing tag
    }

    /**
     * Expressions separated by comma(s)
     * 
     * Return the number of arguments.
     */
    public int compileExpressionList() {
        int numberOfExpressions = 0;

        writeToOutputFile("<expressionList>\n"); // opening tag

        while (!currentToken.equals(")") || currentToken.equals(",")) {
            // at least one expression
            if (currentToken.equals(",")) {
                // write comma and move on to the next expression
                writeCurrentToken();
                nextToken();
            }

            compileExpression();
            numberOfExpressions++;
        }

        writeToOutputFile("</expressionList>\n"); // closing tag
        return numberOfExpressions;
    }


    /** Subroutines */

    public void compileSubroutine() {
        String functionName = null;
        currSubroutineST = new SymbolTable(false); // boolean: not a class

        writeToOutputFile("<subroutineDec>\n"); // opening tag

        functionName = compileSubroutineDeclaration();

        compileSubroutineBody(functionName); // write function once nLocals is known

        // currSubroutineST.printST();

        // System.out.printf("funcName, #ofVars - %s, %d\n", functionName, numberOfLocalVariables);
        // vmWriter.writeFunction(functionName, numberOfLocalVariables);

        writeToOutputFile("</subroutineDec>\n"); // closing tag
    }


    public String compileSubroutineDeclaration() {
        String functionName;
        // keyword: function/method
        writeCurrentToken();
        if (currentToken.equals("method")) {
            // args[0] is the class instance itself, referenced by "this" keyword
            currSubroutineST.define("this", className, VariableKind.ARG);
        // } else if (currentToken.equals("function")) {
            // args[0] is not "this" keyword in the case of function declaration
        }

        // keyword: return type
        // could be keyword data type or custom type
        nextToken();
        writeCurrentToken();

        // identifier: function or method name
        nextToken();
        functionName = currentToken;
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

        return functionName;
    }
    public void compileParameterList() {
        writeToOutputFile("<parameterList>\n"); // opening tag

        // right paranthesis if empty list, in which case just skip to the end
        // otherwise, go through the list
        while (!currentToken.equals(")")) {
            String parameterType, parameterName;
            // keyword: type
            writeCurrentToken();
            parameterType = currentToken;
            nextToken();

            // identifier
            writeCurrentToken();
            parameterName = currentToken;
            nextToken();

            // define a variable/symbol in subroutine ST
            currSubroutineST.define(parameterName, parameterType, VariableKind.ARG);

            // list found, write the comma and move on,
            // wait for the writing of next type and variable
            if (currentToken.equals(",")) { 
                writeCurrentToken();
                nextToken();
            }
        }

        writeToOutputFile("</parameterList>\n"); // closing tag
    }
    public int compileSubroutineBody(String functionName) {
        int nLocals = 0;
        writeToOutputFile("<subroutineBody>\n"); // opening tag

        // left curly bracket
        if (!currentToken.equals("{")) {
            throw new RuntimeException("expect left curly bracket");
        }
        writeCurrentToken();
        nextToken();

        // local variables declaraction
        while (currentToken.equals("var")) {
            nLocals += compileSubroutineVariableDeclaration();
        }
        vmWriter.writeFunction(className + "." + functionName, nLocals);

        // statements
        compileStatements();

        // right curly bracket
        writeCurrentToken();
        nextToken();

        writeToOutputFile("</subroutineBody>\n"); // closing tag
        return nLocals;
    }

    /**
     * Compile one line of declaration only.
     */
    public int compileSubroutineVariableDeclaration() {
        int currLineNumberOfVars = 0;
        writeToOutputFile("<varDec>\n"); // opening tag

        // var keyword
        writeCurrentToken();
        nextToken();

        // keyword: type
        writeCurrentToken();
        String varType = currentToken;
        nextToken();

        // during a variable declaration
        while (!currentToken.equals(";")) {
            // identifier
            writeCurrentToken();
            String varName = currentToken;
            nextToken();

            currSubroutineST.define(varName, varType, VariableKind.VAR); // locals
            currLineNumberOfVars++;

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

        writeToOutputFile("</varDec>\n"); // closing tag
        return currLineNumberOfVars;
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
            throw new RuntimeException("class construct error: { expected");
        }
        writeCurrentToken();

        // class variable declarations or subroutine declaration
        nextToken();
        // int n = 0;
        while (!currentToken.equals("}")) {
            if (currTokenType == Token.KEYWORD) {
                if (
                    currentToken.equals("field") ||
                    currentToken.equals("static")) {
                    compileClassVariableDeclaration();
                } else if (
                    currentToken.equals("method") || 
                    currentToken.equals("constructor") || 
                    currentToken.equals("function")) {
                    compileSubroutine();
                }
            }
        }

        // symbol: }
        writeCurrentToken();
        nextToken();
        
        writeToOutputFile("</class>"); // class closing tag
    }

    /**
     * Each var line is an independent declaration.
     */
    public void compileClassVariableDeclaration() {
        while (
            currentToken.equals("field") ||
            currentToken.equals("static")) {
            writeToOutputFile("<classVarDec>\n"); // opening tag

            VariableKind kind = VariableKind.NONE;
            String type, varName;

            // determine kind
            if (currentToken.equals("field")) kind = VariableKind.FIELD;
            else if (currentToken.equals("static")) kind = VariableKind.STATIC;

            // field/static keyword
            writeCurrentToken();
            nextToken();

            // keyword: type
            writeCurrentToken();
            type = currentToken;
            nextToken();

            // during a variable declaration
            while (!currentToken.equals(";")) {

                // identifier
                writeCurrentToken();
                varName = currentToken;
                nextToken();
                classSymbolTable.define(varName, type, kind);

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

    private static VirtualSegment kindToVirtualSegment(VariableKind kind) {
        if (kind == VariableKind.STATIC) {
            return VirtualSegment.STATIC;
        } else if (kind == VariableKind.FIELD) {
            return VirtualSegment.THIS;
        } else if (kind == VariableKind.VAR) {
            return VirtualSegment.LOCAL;
        } else if (kind == VariableKind.ARG) {
            return VirtualSegment.ARGUMENT;
        }

        return null;
    }

    // look into the STs, first local scope ST then class scope ST if not found
    private VariableKind getVariableKind(String variableName) {
        VariableKind kind = currSubroutineST.kindOf(variableName);

        if (kind == VariableKind.NONE || kind == null) { // cannot find in local scope
            kind = classSymbolTable.kindOf(variableName); // search in class scope
        }
        
        return kind;
    }
    private int getVariableIndex(String variableName) {
        int idx = currSubroutineST.indexOf(variableName);

        if (idx == -1) {
            idx = classSymbolTable.indexOf(variableName);
        }

        return idx;
    }
    private String getVariableType(String variableName) {
        String objClass = currSubroutineST.typeOf(variableName);;

        if (objClass == null) {
            objClass = currSubroutineST.typeOf(variableName);;
        }

        return objClass;
    }

    private void compileString(String content) {
        int len = content.length();

        // instantiate String object
        vmWriter.writePush(VirtualSegment.CONSTANT, len);
        vmWriter.writeCall("String.new", 1);

        // append characters one by one
        for (int i = 0; i < len; i++) {
            char c = content.charAt(i);
            vmWriter.writePush(VirtualSegment.CONSTANT, c);
            vmWriter.writeCall("String.appendChar", 2);
        }
    }
                // VariableKind kind = currSubroutineST.kindOf(firstPart);
                // int idx = currSubroutineST.indexOf(firstPart);
                // String objClass = currSubroutineST.typeOf(firstPart);;
                // if (kind == VariableKind.NONE || kind == null) { // cannot find in local scope
                //     kind = classSymbolTable.kindOf(firstPart); // search in class scope
                //     idx = classSymbolTable.indexOf(firstPart);
                //     objClass = currSubroutineST.typeOf(firstPart);;
                // }

}
