package com.company;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.company.Insert.*;

public class Main {

    private String inputKey;
    private String inputKeyFromEncoded;
    private int iterations = 16;
    private static final int[] LEFT_SHIFT_VALUES = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
    private static final int[] RIGHT_SHIFT_VALUES = new int[]{0, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    public static void main(String[] args) throws IOException {
        Main coder = new Main();
        coder.run();
    }

    private void run() throws IOException {
        List<String> inputStringsList = readFromFile("input_crypto.txt");
        List<String> outputStringList = encode(inputStringsList);
        writeToFile(outputStringList, "output_crypt.txt");

        List<String> inputStringsListEncoded = readFromEncodedFile("input_decrypt.txt");
        List<String> outputDecodedStringsList = decode(inputStringsListEncoded);
        writeToFile(outputDecodedStringsList, "output_decrypt.txt");
    }

    private List<String> decode(List<String> inputStringsListEncoded) throws UnsupportedEncodingException {

        List<String> outputDecodedStringsList = new ArrayList<>();
        for (String str : inputStringsListEncoded) {
            String[] blocks = breakIntoBlocks(str);
            String allStringInChars = "";
            for (String oneBlock : blocks) {
                char[] oneBlockCharArray = oneBlock.toCharArray();
                int[] oneBlockIntArray = new int[64];
                for (int i = 0; i < oneBlockCharArray.length; i++) {
                    String symb = Character.toString(oneBlockCharArray[i]);
                    oneBlockIntArray[i] = Integer.parseInt(symb);
                }
                int[][] encodedBlock = inversedCycle(changedByMatrixP(oneBlockIntArray), inputKeyFromEncoded);
                int[] encodedCharArray = ArrayConverter.to1D(encodedBlock);
                String oneBlockInString = charArrayToString(encodedCharArray);
                String resultBlockInChars = getBlockStringRepresentation(encodedCharArray, oneBlockInString);
                allStringInChars += resultBlockInChars;

            }
            outputDecodedStringsList.add(allStringInChars);

        }
        return outputDecodedStringsList;
    }

    private String charArrayToString(int[] array) {
        String str = "";
        for (int charachter : array) {
            str += charachter;
        }
        return str;
    }

    private List<String> encode(List<String> inputStringsList) throws UnsupportedEncodingException {
        List<String> outputStringList = new ArrayList<>();
        for (String str : inputStringsList) {
            String[] blocks = breakIntoBlocks(str);
            String allStringInChars = "";
            for (String oneBlock : blocks) {
                char[] oneBlockCharArray = oneBlock.toCharArray();
                int[] oneBlockIntArray = new int[64];
                for (int i = 0; i < oneBlockCharArray.length; i++) {
                    String symb = Character.toString(oneBlockCharArray[i]);
                    oneBlockIntArray[i] = Integer.parseInt(symb);
                }
                int[][] encodedBlock = cycle(changedByMatrixP(oneBlockIntArray), inputKey);
                int[] encodedCharArray = ArrayConverter.to1D(encodedBlock);
                String oneBlockInString = charArrayToString(encodedCharArray);
                String resultBlockInChars = getBlockStringRepresentation(encodedCharArray, oneBlockInString);
                allStringInChars += resultBlockInChars;

            }
            outputStringList.add(allStringInChars);

        }
        return outputStringList;
    }

    private String getBlockStringRepresentation(int[] encodedCharArray, String oneBlockInString) {
        String resultBlockInChars = "";
        for (int i1 = 0; i1 < encodedCharArray.length; i1 += 8) {
            String oneSymbolBinary = oneBlockInString.substring(i1, i1 + 8);
            int ascii = Integer.parseInt(oneSymbolBinary, 2);
            char symbol = (char) ascii;
            resultBlockInChars += symbol;
        }
        return resultBlockInChars;
    }

    public static String SYSTEM_NEWLINE_SYMBOL = System.getProperty("line.separator");

    private List<String> readFromFile(String fileName) throws IOException {
        List<String> listOfString = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileReader(fileName, CHARSET))) {
            inputKey = scanner.nextLine();

            var temp = "";
            while (scanner.hasNext()) {
                temp += scanner.nextLine();
                if(scanner.hasNext()) temp += SYSTEM_NEWLINE_SYMBOL;
            }
            listOfString.add(temp);
        }

        return listOfString;
    }

    private List<String> readFromEncodedFile(String fileName) throws IOException {
        List<String> listOfString = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileReader(fileName, CHARSET))) {
            inputKeyFromEncoded = scanner.nextLine();

            var temp = "";
            while (scanner.hasNext()) {
                temp += scanner.nextLine();
                if(scanner.hasNext()) temp += SYSTEM_NEWLINE_SYMBOL;
            }
            listOfString.add(temp);
        }

        return listOfString;
    }

    private void writeToFile(List<String> outStringsList, String fileName) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, CHARSET))) {
            for (String s : outStringsList) {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            }
        }
    }

    private String[] breakIntoBlocks(String input) {
        int sizeOfChar = 8;
        int sizeOfBlockInBits = 64;

        String binaryInput = new String(input.getBytes(CHARSET), CHARSET);
        int mod = (binaryInput.length() * sizeOfChar) % sizeOfBlockInBits;
        int amountOfBlocks = (binaryInput.length() * sizeOfChar) / sizeOfBlockInBits;
        if (mod != 0) {
            amountOfBlocks++;
        }
        String[] Blocks = new String[amountOfBlocks];
        int sizeOfBlockInChars = sizeOfBlockInBits / sizeOfChar;

        for (int i = 0; i < amountOfBlocks; i++) {

            if (i == amountOfBlocks - 1) {
                Blocks[i] = binaryInput.substring(i * sizeOfBlockInChars, binaryInput.length());
            } else {
                Blocks[i] = binaryInput.substring(i * sizeOfBlockInChars, i * sizeOfBlockInChars + sizeOfBlockInChars);
            }
            Blocks[i] = stringToBinary(Blocks[i]);
            while (Blocks[i].length() < 64) {
                Blocks[i] += "0";
            }
        }
        return Blocks;
    }


    public String stringToBinary(String s) {
        String res = "";
        for (byte b : s.getBytes(CHARSET)) {

            res += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');

        }
        return res;
    }

    private int[][] changedByMatrixP(int[] T) {
        int[][] P = getMatrixP();
        return modifyByMatrix(T, P);
    }

    private int[][] getL(int[][] T) {
        int[][] L = new int[4][8];
        for (int i = 0; i < T.length / 2; i++) {
            for (int j = 0; j < T[0].length; j++) {
                L[i][j] = T[i][j];
            }
        }
        return L;
    }

    private int[][] getR(int[][] T) {
        int[][] R = new int[4][8];
        for (int i = 4; i < T.length; i++) {
            for (int j = 0; j < T[0].length; j++) {
                R[i - T.length / 2][j] = T[i][j];
            }
        }
        return R;
    }

    private int[][] generateKeyInTheBegin(String Key) {
        String binaryKeyString = stringToBinary(Key);
        if (binaryKeyString.length() > 64) {
            binaryKeyString = binaryKeyString.substring(0, 64);
        }
        char[] binaryKeyCharArray = binaryKeyString.toCharArray();
        int[] binaryKeyIntArray = new int[64];
        for (int i = 0; i < binaryKeyCharArray.length; i++) {
            String symb = Character.toString(binaryKeyCharArray[i]);
            binaryKeyIntArray[i] = Integer.parseInt(symb);
        }

        return transformKeyMatrixG(binaryKeyIntArray);
    }

    private int[][] mergeTwoArrays(int[][] firstArray, int[][] secondArray) {
        int[][] newArray = new int[firstArray.length + secondArray.length][firstArray[0].length];
        System.arraycopy(firstArray, 0, newArray, 0, firstArray.length);
        System.arraycopy(secondArray, 0, newArray, firstArray.length, secondArray.length);
        return newArray;
    }

    private int[][] transformKeyMatrixG(int[] Key) {
        int[][] G = getMatrixG();
        return modifyByMatrix(Key, G);
    }

    private int[][] getC(int[][] Key) {
        int[][] C = new int[2][14];
        for (int i = 0; i < Key.length / 2; i++) {
            for (int j = 0; j < Key[0].length; j++) {
                C[i][j] = Key[i][j];
            }
        }
        return C;
    }

    private int[][] getD(int[][] Key) {
        int[][] D = new int[2][14];
        for (int i = 2; i < Key.length; i++) {
            for (int j = 0; j < Key[0].length; j++) {
                D[i - 2][j] = Key[i][j];
            }
        }
        return D;
    }

    static int[][] shiftLeft(int[][] C, int iterNumber) {
        int[] arr = ArrayConverter.to1D(C);
        ArrayConverter.shiftLeftArr(arr, LEFT_SHIFT_VALUES[iterNumber - 1]);

        return ArrayConverter.to2D(arr, C[0].length);
    }

    private static int[][] shiftRight(int[][] C, int iterNumber) {
        int[] arr = ArrayConverter.to1D(C);
        ArrayConverter.shiftRightArr(arr, RIGHT_SHIFT_VALUES[iterNumber - 1]);

        return ArrayConverter.to2D(arr, C[0].length);
    }

    private int[][] modifyByH(int[] Key) {
        int[][] H = getMatrixH();
        return modifyByMatrix(Key, H);
    }

    private int[][] cycle(int[][] T, String key) {

        int[][] binaryKey = generateKeyInTheBegin(key);
        int[][] shiftedC = getC(binaryKey);
        int[][] shiftedD = getD(binaryKey);

        for (int i = 1; i <= iterations; i++) {
            shiftedC = shiftLeft(shiftedC, i);
            shiftedD = shiftLeft(shiftedD, i);
            int[][] shiftedKey = mergeTwoArrays(shiftedC, shiftedD);
            int[][] modifiedKey = modifyByH(ArrayConverter.to1D(shiftedKey));
            int[][] L = getL(T);
            int[][] R = getR(T);
            int[][] newL = R;
            R = expansionR(R);
            int[][] F = function(R, modifiedKey);
            int[] F2 = ArrayConverter.to1D(F);
            F = ArrayConverter.to2D(F2, 8);
            int[][] newR = xorForEachElement(L, F);
            T = mergeTwoArrays(newL, newR);

        }

        int[] resultArray = ArrayConverter.to1D(T);
        int[][] invP = invP();
        return modifyByMatrix(resultArray, invP);
    }


    private int[][] inversedCycle(int[][] T, String key) {

        int[][] binaryKey = generateKeyInTheBegin(key);
        int[][] shiftedC = getC(binaryKey);
        int[][] shiftedD = getD(binaryKey);

        for (int i = 1; i <= iterations; i++) {
            shiftedC = shiftRight(shiftedC, i);
            shiftedD = shiftRight(shiftedD, i);
            int[][] shiftedKey = mergeTwoArrays(shiftedC, shiftedD);
            int[][] modifiedKey = modifyByH(ArrayConverter.to1D(shiftedKey));
            int[][] L = getL(T);
            int[][] R = getR(T);
            int[][] newR = L;
            L = expansionR(L);
            int[][] F = function(L, modifiedKey);
            int[] F2 = ArrayConverter.to1D(F);
            F = ArrayConverter.to2D(F2, 8);
            int[][] newL = xorForEachElement(R, F);
            T = mergeTwoArrays(newL, newR);

        }
        int[] resultArray = ArrayConverter.to1D(T);
        int[][] invP = invP();
        return modifyByMatrix(resultArray, invP);
    }

    private int[][] mergeLAndR(int[][] L, int[][] R) {
        int[][] resultArray = new int[L.length][L[0].length + R[0].length];
        for (int i = 0; i < L.length; i++) {
            for (int j = 0; j < L[0].length + R[0].length; j++) {
                if (j < L[0].length) {
                    resultArray[i][j] = L[i][j];
                } else {
                    resultArray[i][j] = R[i][j - L[0].length];
                }
            }
        }
        return resultArray;
    }


    static int[][] xorForEachElement(int[][] firstArray, int[][] secondArray) {
        int[][] resultXor = new int[firstArray.length][firstArray[0].length];

        for (int i = 0; i < firstArray.length; i++) {
            for (int j = 0; j < firstArray[0].length; j++) {
                resultXor[i][j] = firstArray[i][j] ^ secondArray[i][j];
            }
        }
        return resultXor;
    }


    int[][] function(int[][] R, int[][] key) {
        int[][] resultSumKeyAndR = xorForEachElement(key, R);
        return to4BitValue(resultSumKeyAndR);
    }

    private int[][] expansionR(int[][] R) {
        int[][] E = getMatrixE();
        int[] changedR = ArrayConverter.to1D(R);

        return modifyByMatrix(changedR, E);
    }


    int[][] to4BitValue(int[][] result) {
        int[] B = new int[6];
        int[][] S = define();
        int[] toOneArray = new int[48];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                B[j] = result[i][j];
            }
            int stringNumber = (int) (B[5] * Math.pow(2, 0) + B[0] * Math.pow(2, 1));
            int columnNumber = (int) (B[4] * Math.pow(2, 0) + B[3] * Math.pow(2, 1) + B[2] * Math.pow(2, 2) + B[1] * Math.pow(2, 3));
            int modifiedResultIn4Bit = S[i * 4 + stringNumber][columnNumber];
            int[] binaryResultIn4Bit = toBinary(modifiedResultIn4Bit);
            System.arraycopy(binaryResultIn4Bit, 0, toOneArray, i * 4, binaryResultIn4Bit.length);
        }
        int[][] matrixP1 = getMatrixP1();
        return modifyByMatrix(toOneArray, matrixP1);
    }

    private int[][] modifyByMatrix(int[] array, int[][] matrix) {
        int[][] modifiedArray = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                modifiedArray[i][j] = array[matrix[i][j] - 1];
            }
        }
        return modifiedArray;
    }

    private int[] toBinary(int resultIn4Bit) {

        int[] temp = new int[4];
        int count = 0;
        while (resultIn4Bit != 0) {
            temp[4 - 1 - count] = resultIn4Bit % 2;
            resultIn4Bit = resultIn4Bit / 2;
            count++;
        }
        return temp;
    }

}
