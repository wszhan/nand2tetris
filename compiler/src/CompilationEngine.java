import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

public class CompilationEngine {

    /** Subroutine type */
    private char UNDEFIEND_SUBROUTINE = 0;
    private char CONSTRUCTOR_TYPE = 1;
    private char METHOD_TYPE = 2;
    private char FUNCTION_TYPE = 3;

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
    private int subroutineWhileCount = -1, subroutineIfCount = -1;

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

        startCompiling();
    }


    /** Initialization */

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


    /**
     * Safely read the next token.
     */
    private void nextToken() {
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
        boolean arrayManipulation = false;
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
            arrayManipulation = true;
            // array access to the left of =
            // 1. push base value onto Stack // for comparison, put it after step 2
            // pushVariableOntoStack(varToBeDefined);

            // write [
            writeCurrentToken();
            nextToken();

            // 2. push expression value onto stack
            // expression
            compileExpression();
            pushVariableOntoStack(varToBeDefined);

            // ]
            if (!currentToken.equals("]")) {
                throw new RuntimeException("expect ] and found " + currentToken);
            }
            writeCurrentToken();
            nextToken();

            // 3. add up but delay the assignment to THAT
            vmWriter.writeArithmetic("add");
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

        if (arrayManipulation) {
            vmWriter.writePop(VirtualSegment.TEMP, 0); // store temporarily at temp[0]
            vmWriter.writePop(VirtualSegment.POINTER, 1); // that = (arr + i)
            vmWriter.writePush(VirtualSegment.TEMP, 0); // evaluated epxression result
            vmWriter.writePop(VirtualSegment.THAT, 0); // *that = *(arr + i) <- exp value
        } else {
            popValueToVariable(varToBeDefined);
        }

        writeToOutputFile("</letStatement>\n"); // closing tag
    }
    
    /**
     * if or if-else. no else if is allowed.
     */
    public void compileIf() {
        writeToOutputFile("<ifStatement>\n"); // opening tag

        boolean ifClause = true, hasElse = false;
        int localIfCount = ++subroutineIfCount;

        while (
            currentToken.equals("if") && ifClause || 
            currentToken.equals("else") && !ifClause) {

            // if/else keyword
            writeCurrentToken();
            if (currentToken.equals("else")) {
                vmWriter.writeGoto("IF_END" + localIfCount);
                hasElse = true;
            }

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

                vmWriter.writeIf("IF_TRUE" + localIfCount);
                vmWriter.writeGoto("IF_FALSE" + localIfCount);

                // right paranthesis
                if (!currentToken.equals(")")) {
                    throw new RuntimeException("expect right parenthesis - " + currentToken);
                }
                writeCurrentToken();
                nextToken();
            }

            if (ifClause) {
                vmWriter.writeLabel("IF_TRUE" + localIfCount);
            } else {
                vmWriter.writeLabel("IF_FALSE" + localIfCount);
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
            // it doesn't make sense to let ELSE followed by another IF in Jack
            if (currentToken.equals("if")) {
                break;
            }

            // next should be else clause if any
            ifClause = !ifClause;
        }

        if (hasElse) {
            vmWriter.writeLabel("IF_END" + localIfCount);
        } else {
            vmWriter.writeLabel("IF_FALSE" + localIfCount);
        }

        writeToOutputFile("</ifStatement>\n"); // closing tag
    }

    public void compileWhile() {
        writeToOutputFile("<whileStatement>\n"); // opening tag

        int localWhileCount = ++subroutineWhileCount;

        if (currentToken.equals("while")) {
            vmWriter.writeLabel("WHILE_EXP" + localWhileCount);

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

            // write if-goto
            vmWriter.writeArithmetic("not");
            // condition failed (or meet the negated condition), break the loop (go to the end)
            vmWriter.writeIf("WHILE_END" + localWhileCount);

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
            // go to the beginning of the while loop
            vmWriter.writeGoto("WHILE_EXP" + localWhileCount); 
        }
        vmWriter.writeLabel("WHILE_END" + localWhileCount);

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

        // isMethodCall == true && hasDotOperator == false
        // calling Class.method in the Class scope; need to use THIS as the implicit first arguments
        // must be pushed BEFORE pushing explicitly listed parameters
        if (isMethodCall && !hasDotOperator) vmWriter.writePush(VirtualSegment.POINTER, 0);

        // expression
        int numberOfArguments = compileExpressionList();

        // if a method is called on an object, the object itself is, though implicitly,
        // the very first argument and should be pushed onto the stack before calling
        if (isMethodCall) numberOfArguments++;

        // right paranthesis
        writeCurrentToken();
        nextToken();

        // semi-colon
        writeCurrentToken();
        nextToken();

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
                // writeArithmetic happens after arguments are pushed
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
            nextToken();

            compileTerm();

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
            } else if (currTerm.equals("true")) {
                vmWriter.writePush(VirtualSegment.CONSTANT, 0);
                vmWriter.writeArithmetic("not");
            } else if (currTerm.equals("this")) {
                vmWriter.writePush(VirtualSegment.POINTER, 0);
            }
            currTermProcessed = true;
        }
        
        nextToken();

        if (currentToken.equals("[")) { // array access
            // write [
            writeCurrentToken();
            nextToken();

            // expression
            // 1. expression value is to be pushed
            compileExpression();
            // 2. push base value
            pushVariableOntoStack(currTerm);
            // 3. add up
            vmWriter.writeArithmetic("add");
            // 4. address result stored as pointer[1]
            vmWriter.writePop(VirtualSegment.POINTER, 1);
            // 5. push the value pointed by pointer[1] onto stack
            vmWriter.writePush(VirtualSegment.THAT, 0);

            if (!currentToken.equals("]")) {
                throw new RuntimeException("expect ] and found " + currentToken);
            } else {
                writeCurrentToken();
                nextToken();
            }
        } else if (currentToken.equals(".")) { // subroutine call
            char c = currTerm.charAt(0);
            boolean functionCall = c >= 'A' && c <= 'Z'; // last token is class name
            int numberOfArgs = 0;

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
                numberOfArgs++;
            }
            nextToken();

            if (!currentToken.equals("(")) {
                throw new RuntimeException("expect ( and found " + currentToken);
            } else {
                // (
                writeCurrentToken();
                nextToken();

                numberOfArgs += compileExpressionList();

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
                vmWriter.writePush(seg, idx); 
            }
        }

        writeToOutputFile("</term>\n"); // closing tag
    }


    /**
     * Expressions separated by comma(s)
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

    public void compileSubroutine(int nClassVars) {
        String functionName = null;
        char subroutineType = UNDEFIEND_SUBROUTINE;

        subroutineWhileCount = -1; // reset
        subroutineIfCount = -1; // reset

        currSubroutineST = new SymbolTable(false); // boolean: not a class

        writeToOutputFile("<subroutineDec>\n"); // opening tag

        if (currentToken.equals("constructor")) subroutineType = CONSTRUCTOR_TYPE;
        else if (currentToken.equals("method")) subroutineType = METHOD_TYPE;
        else if (currentToken.equals("function")) subroutineType = FUNCTION_TYPE;

        functionName = compileSubroutineDeclaration();

        compileSubroutineBody(functionName, subroutineType, nClassVars); // write function once nLocals is known

        writeToOutputFile("</subroutineDec>\n"); // closing tag
    }


    public String compileSubroutineDeclaration() {
        String functionName;

        // keyword: function/method
        writeCurrentToken();
        if (currentToken.equals("method")) {
            // THIS is args[0] in the scope of all methods (as compared to functions)
            currSubroutineST.define("this", className, VariableKind.ARG);
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


    public int compileSubroutineBody(String functionName, char subroutineType, int nClassVars) {
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

        // if a method is being defined, THIS should be anchored to arguments[0]
        // argument[0] must have a name of "this" 
        if (subroutineType == METHOD_TYPE) {
            pushVariableOntoStack("this");
            vmWriter.writePop(VirtualSegment.POINTER, 0); // anchoring
        }

        if (nClassVars > -1 && subroutineType == CONSTRUCTOR_TYPE) {
            vmWriter.writePush(VirtualSegment.CONSTANT, nClassVars);
            vmWriter.writeCall("Memory.alloc", 1);
            // anchor to THIS
            vmWriter.writePop(VirtualSegment.POINTER, 0);
        }

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
        int nClassVars = -1; // -1 as a flag indicating no class variables
        while (!currentToken.equals("}")) {
            if (currTokenType == Token.KEYWORD) {
                if (
                    currentToken.equals("field") ||
                    currentToken.equals("static")) {
                    nClassVars = compileClassVariableDeclaration();
                } else if (
                    currentToken.equals("method") || 
                    currentToken.equals("constructor") || 
                    currentToken.equals("function")) {
                    compileSubroutine(nClassVars);
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
    public int compileClassVariableDeclaration() {
        int nClassVars = 0;

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
                if (kind == VariableKind.FIELD) nClassVars++;
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

        return nClassVars;
    }


    /**
     * Get the corresponding virtual segment of a certain kind.
     */
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


    /**
     * Look into the STs, first local scope ST then class scope ST if not found.
     */
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
            objClass = classSymbolTable.typeOf(variableName);;
        }

        return objClass;
    }


    /**
     * Syntax sugar.
     * Initialize space for the string and append characters one-by-one.
     */
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


    /**
     * 
     * Push the value of a known variable onto the stack.
     */
    private void pushVariableOntoStack(String variableName) {
        VariableKind kind = getVariableKind(variableName);
        int idx = getVariableIndex(variableName);
        VirtualSegment seg = kindToVirtualSegment(kind);
        vmWriter.writePush(seg, idx); 
    }


    /**
     * Pop the stack top value to a known variable.
     */
    private void popValueToVariable(String variableName) {
        VariableKind kind = getVariableKind(variableName);
        int idx = getVariableIndex(variableName);
        VirtualSegment seg = kindToVirtualSegment(kind);
        vmWriter.writePop(seg, idx);
    }
}
