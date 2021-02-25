import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Assembler {

    public static final String HACK_FILE_SUFFIX = ".hack";

    public static String encodeAddrCommand(int decimalAddr) {
        StringBuffer res = new StringBuffer();

        res.append("0");

        String addrBinary = Integer.toBinaryString(decimalAddr);
        int padding = 15 - addrBinary.length();
        for (int i = 0; i < padding; i++) {
            res.append("0");
        }

        res.append(addrBinary);
        res.append("\n");

        return res.toString();

    }
    
    public static String encodeAddrCommand(String decimalAddr) {
        return encodeAddrCommand(Integer.valueOf(decimalAddr));
    }

    public static String encodeCompCommand(String comp, String dest, String jump) {
        StringBuffer res = new StringBuffer();

        res.append("111"); // c-instruction bit and 2 padding bits
        res.append(AsmToBinaryCoder.compCoder(comp));
        res.append(AsmToBinaryCoder.destCoder(dest));
        res.append(AsmToBinaryCoder.jumpCoder(jump));

        res.append("\n");

        return res.toString();
    }
    public static void main(String[] args) {

        if (args.length == 1) {
            File asmFile = new File(args[0]);

            String asmFileDir = asmFile.getParent();

            // get file name
            String asmFileName = asmFile.getName();
            int suffixIndex = asmFileName.indexOf(".");
            String fileName = asmFileName.substring(0, suffixIndex);
            // create file in the same path as .asm file
            String hackFileName = asmFileDir + "\\" + fileName + HACK_FILE_SUFFIX; 

            try {
                // read from file
                Scanner sc = new Scanner(asmFile);
                AssemblerSymbolTable ast = new AssemblerSymbolTable(asmFile);
                Parser ps = new Parser(sc);

                // create file
                FileWriter binaryWriter = new FileWriter(hackFileName);
                String binaryInstruction;

                // process and output
                int variableAddr = 16;
                while (ps.nextCommand() != null) {
                    char commandType = ps.currentCommandType();
                    binaryInstruction = null;

                    // parse .asm instructions
                    if (commandType == Parser.A_COMMAND) {
                        String sym = ps.symbol();

                        int val = -1;

                        try {
                            val = Integer.parseInt(sym);
                        } catch (Exception e) {
                            val = ast.resolveSymbol(sym);
                            if (val == -1) {
                                val = variableAddr;
                                ast.addEntry(sym, val);
                                variableAddr++;
                            }
                        }

                        binaryInstruction = Assembler.encodeAddrCommand(val);
                    } else if (commandType == Parser.C_COMMAND) {
                        binaryInstruction = Assembler.encodeCompCommand(
                            ps.comp(), ps.dest(), ps.jump()
                        );
                    } else if (commandType == Parser.L_COMMAND) {
                        // do nothing
                    } else {
                        // close file before throw runtime exception
                        binaryWriter.close();
                        throw new InvalidCommandException();
                    }

                    // write to file
                    if (binaryInstruction != null) {
                        binaryWriter.write(binaryInstruction);
                    }
                }

                // close file
                binaryWriter.close();
                sc.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCommandException e) {
                e.printStackTrace();
            }
        }
    }
}