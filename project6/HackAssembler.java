
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HackAssembler {

    private static HashMap<String, Integer> symbolTable;
    private static HashMap<String, String> commandTable;
    private static HashMap<String, String> destTable;
    private static HashMap<String, String> jumpTable;

    private static String withoutWhitespaces(String line) {
        int idx = 0;
        int n = line.length();
        while (idx < n && line.charAt(idx) == ' ') {
            idx++;
        }
        if (idx == n || line.charAt(idx) == '/') {
            return "";
        }
        String instruction = "";
        while (idx < n && line.charAt(idx) != '/') {
            if (line.charAt(idx) == ' ') {
                idx++;
            } else {
                instruction += line.charAt(idx);
                idx = idx + 1;
            }
        }
        return instruction;
    }

    private static ArrayList<String> readFile(String fileName) {
        System.out.println("Processing " + fileName);
        ArrayList<String> file = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String str;
            while ((str = br.readLine()) != null) {
                String formatted = withoutWhitespaces(str);
                if (formatted.isEmpty()) {
                    continue;
                }
                file.add(formatted);
            }
        } catch (IOException exception) {
            System.err.print("Error reading file");
        }
        return file;
    }

    private static boolean isLabel(String str) {
        return str.startsWith("(");
    }

    private static void firstPass(ArrayList<String> contents) {
        int counter = 0;
        for (String line : contents) {
            if (isLabel(line)) {
                symbolTable.put(line.substring(1, line.length() - 1), counter);
            } else {
                counter = counter + 1;
            }
        }
    }

    private static String reverse(String str) {
        String reversedString = "";
        for (int j = str.length() - 1; j >= 0; --j) {
            reversedString += str.charAt(j);
        }
        return reversedString;
    }

    private static String toBinary(Integer num) {
        String result = "";
        while (num > 0) {
            result += (num % 2 == 1 ? '1' : '0');
            num /= 2;
        }
        String binaryResult = "";
        for (int i = 0; i < 15; ++i) {
            binaryResult += '0';
        }
        binaryResult += reverse(result);
        return binaryResult;
    }

    private static String getLast(String str, int last) {
        int len = str.length();
        return str.substring(len - last);
    }

    private static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    private static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }

    private static ArrayList<String> secondPass(ArrayList<String> contents) {
        ArrayList<String> output = new ArrayList<>();
        int nextEmptySpace = 16;
        for (String line : contents) {
            if (isLabel(line)) {
                continue;
            }
            if (isAInstruction(line)) {
                String value = line.substring(1);
                Integer number;
                if (isInteger(value)) {
                    number = Integer.valueOf(line.substring(1));
                } else if (symbolTable.containsKey(value)) {
                    number = symbolTable.get(value);
                } else {
                    number = nextEmptySpace;
                    symbolTable.put(value, number);
                    nextEmptySpace += 1;
                }
                String binaryValue = getLast(toBinary(number), 15);
                String aInstruction = '0' + binaryValue;
                output.add(aInstruction);
            } else {
                // is a c instruction
                ArrayList<String> components = parseCIns(line);
                String dest = components.get(0);
                String comp = components.get(1);
                String jmp = components.get(2);
                String cInstruction = "111" + commandTable.get(comp) + destTable.get(dest) + jumpTable.get(jmp);
                output.add(cInstruction);
            }
        }
        return output;
    }

    private static void initializeTables() {
        symbolTable = new HashMap<>();
        for (int i = 0; i < 16; ++i) {
            String registerName = "R" + i;
            symbolTable.put(registerName, i);
        }
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);

        commandTable = new HashMap<>();
        commandTable.put("0", "0101010");
        commandTable.put("1", "0111111");
        commandTable.put("-1", "0111010");
        commandTable.put("D", "0001100");
        commandTable.put("A", "0110000");
        commandTable.put("!D", "0001101");
        commandTable.put("!A", "0110001");
        commandTable.put("-D", "0001111");
        commandTable.put("-A", "0110011");
        commandTable.put("D+1", "0011111");
        commandTable.put("1+D", "0011111");
        commandTable.put("A+1", "0110111");
        commandTable.put("1+A", "0110111");
        commandTable.put("D-1", "0001110");
        commandTable.put("A-1", "0110010");
        commandTable.put("D+A", "0000010");
        commandTable.put("A+D", "0000010");
        commandTable.put("D-A", "0010011");
        commandTable.put("A-D", "0000111");
        commandTable.put("D&A", "0000000");
        commandTable.put("A&D", "0000000");
        commandTable.put("D|A", "0010101");
        commandTable.put("A|D", "0010101");
        commandTable.put("M", "1110000");
        commandTable.put("!M", "1110001");
        commandTable.put("-M", "1110011");
        commandTable.put("M+1", "1110111");
        commandTable.put("M-1", "1110010");
        commandTable.put("D+M", "1000010");
        commandTable.put("M+D", "1000010");
        commandTable.put("D-M", "1010011");
        commandTable.put("M-D", "1000111");
        commandTable.put("D&M", "1000000");
        commandTable.put("M&D", "1000000");
        commandTable.put("D|M", "1010101");
        commandTable.put("M|D", "1010101");

        destTable = new HashMap<>();

        destTable.put("", "000");
        destTable.put("M", "001");
        destTable.put("D", "010");
        destTable.put("MD", "011");
        destTable.put("DM", "011");
        destTable.put("A", "100");
        destTable.put("AM", "101");
        destTable.put("MA", "101");
        destTable.put("AD", "110");
        destTable.put("DA", "110");
        destTable.put("ADM", "111");
        destTable.put("AMD", "111");
        destTable.put("MAD", "111");
        destTable.put("MDA", "111");
        destTable.put("DAM", "111");
        destTable.put("DMA", "111");

        jumpTable = new HashMap<>();

        jumpTable.put("", "000");
        jumpTable.put("JGT", "001");
        jumpTable.put("JEQ", "010");
        jumpTable.put("JGE", "011");
        jumpTable.put("JLT", "100");
        jumpTable.put("JNE", "101");
        jumpTable.put("JLE", "110");
        jumpTable.put("JMP", "111");
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("Please provide a filename as an argument.");
            return;
        }
        initializeTables();
        String fileName = args[0];
        ArrayList<String> contents = readFile(fileName);
        firstPass(contents);
        ArrayList<String> output = secondPass(contents);
        String fileNameWithoutExt = fileName.substring(0, fileName.indexOf('.'));
        writeToFile(output, fileNameWithoutExt + ".hack");
    }

    private static void print(ArrayList<String> result) {
        for (String line : result) {
            System.out.println(line);
        }
    }

    private static boolean isAInstruction(String line) {
        return line.startsWith("@");
    }

    private static ArrayList<String> parseCIns(String line) {
        int idx = 0;
        int n = line.length();
        String dest = "";
        String command = "";
        String jump = "";
        ArrayList<String> output = new ArrayList<>();
        if (line.contains("=")) {
            while (idx < n && line.charAt(idx) != '=') {
                dest += line.charAt(idx);
                idx++;
            }
            idx++;
            while (idx < n && line.charAt(idx) != ';') {
                command += line.charAt(idx);
                idx++;
            }
            idx++;
            while (idx < n) {
                jump += line.charAt(idx);
                idx++;
            }
        } else {
            while (idx < n && line.charAt(idx) != ';') {
                command += line.charAt(idx);
                idx++;
            }
            idx++;
            while (idx < n) {
                jump += line.charAt(idx);
                idx++;
            }
        }
        output.add(dest);
        output.add(command);
        output.add(jump);
        return output;
    }

    private static void writeToFile(ArrayList<String> output, String string) {
        try {
            try ( // Create an object of BufferedWriter
                    BufferedWriter f_writer = new BufferedWriter(new FileWriter(string))) {
                for (String line : output) {
                    // Write text(content) to file
                    f_writer.write(line);
                    f_writer.write("\n");

                }
            }
        } catch (IOException io) {
            System.err.println(io.getMessage());
        }
    }
}
