public class AsmToBinaryCoder {

    public static String destCoder(String dest) {
        char[] res = {'0', '0', '0'};

        if (dest != null) {
            char memory = 'M';
            char dReg = 'D';
            char aReg = 'A';

            int dLength = dest.length();

            for (int i = 0; i < dLength; i++) {
                char c = dest.charAt(i);
                if (c == memory)
                    res[2] = '1';
                else if (c == dReg)
                    res[1] = '1';
                else if (c == aReg)
                    res[0] = '1';
            }
        }

        return new String(res);
    }


    public static String jumpCoder(String jump) {
        char[] res = {'0', '0', '0'};
        String unconditionalJump = "JMP";
        String notEqual = "JNE";

        if (jump != null) {
            if (jump.equals(unconditionalJump)) return "111";
            if (jump.equals(notEqual)) return "101";

            char greater = 'G';
            char lesser = 'L';
            char equal = 'E';

            int jLength = jump.length();

            for (int i = 1; i < jLength; i++) {
                char c = jump.charAt(i);
                if (c == greater) {
                    res[2] = '1';
                } else if (c == equal) {
                    res[1] = '1';
                } else if (c == lesser) {
                    res[0] = '1';
                }
            }
        }

        return new String(res);
    }

    public static String compCoder(String comp) {
        if (comp == null)
            throw new IllegalArgumentException();

        String aBit = "0";
        int cLength = comp.length();

        // first, set a bit and convert to x y
        char[] preprocessed = {32, 32, 32}; // spaces
        for (int i = 0; i < cLength; i++) {
            char c = comp.charAt(i);

            // set a bit
            if (c == 'M') aBit = "1";

            // convert to x-y arithmetic
            if (c == 'D')
                preprocessed[i] = 'x';
            else if (c == 'M' || c == 'A')
                preprocessed[i] = 'y';
            else if (c != ' ')
                preprocessed[i] = c;
        }

        // second, set c1 to c6
        String processed = new String(preprocessed).trim();
        String res = aBit.concat(primitiveCoder(processed));

        return res;
    }

    public static String primitiveCoder(String processed) {
        String res = null;
        // System.out.println("current computation: " + processed);
        // System.out.println("current computation == \"0\": " + processed.equals("0"));

        if (processed.equals("0")) {
            res = "101010";
        } else if (processed.equals("1")) { 
            res = "111111";
        } else if (processed.equals("-1")) { 
            res = "111010";
        } else if (processed.equals("x")) {  
            res = "001100";
        } else if (processed.equals("y")) {  
            res = "110000";
        } else if (processed.equals("!x")) {  
            res = "001101";
        } else if (processed.equals("!y")) {  
            res = "110001";
        } else if (processed.equals("-x")) {  
            res = "001111";
        } else if (processed.equals("-y")) {  
            res = "110011";
        } else if (processed.equals("x+1") || processed.equals("1+x")) { 
            res = "011111";
        } else if (processed.equals("y+1") || processed.equals("1+y")) { 
            res = "110111";
        } else if (processed.equals("x-1") || processed.equals("-1+x")) { 
            res = "001110";
        } else if (processed.equals("y-1") || processed.equals("-1+y")) { 
            res = "110010";
        } else if (processed.equals("x+y") || processed.equals("y+1")) {
            res = "000010";
        } else if (processed.equals("x-y") || processed.equals("-y+x")) {
            res = "010011";
        } else if (processed.equals("y-x") || processed.equals("-x+y")) {
            res = "000111";
        } else if (processed.equals("x&y") || processed.equals("y&x")) {
            res = "000000";
        } else if (processed.equals("x|y") || processed.equals("y|x")) {
            res = "010101";
        } else {
            throw new InvalidCommandException();
        }

        return res;
    }

    // unit test
    public static void main(String[] args) {
        String d1 = null;
        String dc1 = AsmToBinaryCoder.destCoder(d1);
        assert dc1 == "000";

        String d2 = "AD";
        String dc2 = AsmToBinaryCoder.destCoder(d2);
        assert dc2 == "110";

        String d3 = "M";
        String dc3 = AsmToBinaryCoder.destCoder(d3);
        assert dc3 == "001";

        String j1 = null;
        String jc1 = AsmToBinaryCoder.jumpCoder(j1);
        assert jc1 == "000";

        String j2 = "JGT";
        String jc2 = AsmToBinaryCoder.jumpCoder(j2);
        assert jc2 == "001";

        String j3 = "JGE";
        String jc3 = AsmToBinaryCoder.jumpCoder(j3);
        assert jc3 == "011";

        String c1 = "!M";
        String cc1 = AsmToBinaryCoder.compCoder(c1);
        assert cc1.equals("1110001");

        String c2 = "D-M";
        String cc2 = AsmToBinaryCoder.compCoder(c2);
        assert cc2.equals("010011");
    }
}
