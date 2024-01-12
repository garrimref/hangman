import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

    private enum GameStatus {
        NOT_FINISHED,
        WON,
        LOST
    }
    private static final String RELATIVE_NOUNS_PATH = "src\\data\\nouns.txt";
    private static final String RELATIVE_GALLOWS_IMAGE_FOLDER = "src\\data\\gallows\\";
    private static final int MAX_ERRORS = 6;
    private static final BufferedReader CONSOLE_READER= new BufferedReader(new InputStreamReader(System.in));
    private static RandomAccessFile ACCESS_DICTIONARY_FILE;

    static {
        try {
            ACCESS_DICTIONARY_FILE = new RandomAccessFile(RELATIVE_NOUNS_PATH, "r");
        } catch (FileNotFoundException e) {
            System.err.println("File cannot be found in " + RELATIVE_NOUNS_PATH);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        try {
            startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startGame() throws IOException {
        while (true) {
            System.out.print("\nWant to start a new game? [y/n]: ");
            String answer = CONSOLE_READER.readLine();
            if ("y".equalsIgnoreCase(answer)) {
                startRound();
            }
            else if ("n".equalsIgnoreCase(answer)) {
                System.exit(1);
            }
        }

    }

    public static void startRound() throws IOException {
        String hiddenWord = getRandomWord();
        StringBuilder maskedWord = initMaskedWord(hiddenWord.length());
        List<String> errorLetters = new ArrayList<>();

        GameStatus status;
        do {
            String letter = getPlayerLetter(errorLetters, maskedWord);

            if (hiddenWord.contains(letter))
                openLetterInMask(hiddenWord, maskedWord, letter);
            else
                errorLetters.add(letter);
            
            status = checkGameStatus(hiddenWord, maskedWord, errorLetters);

            printGallows(errorLetters.size());
            printMaskedWord(maskedWord);
            printErrorLetters(errorLetters);
        } while(status == GameStatus.NOT_FINISHED);

        printGameResult(status);
        System.out.println("Hidden word: " + hiddenWord);
    }

    public static String getRandomWord() throws IOException {
        long randomNumber = (long) (Math.random() * ACCESS_DICTIONARY_FILE.length());
        ACCESS_DICTIONARY_FILE.seek(randomNumber);
        ACCESS_DICTIONARY_FILE.readLine();
        return new String(ACCESS_DICTIONARY_FILE.readLine().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private static String getPlayerLetter(List<String> errorLetters, StringBuilder maskedWord) throws IOException {
        Pattern pattern = Pattern.compile("[а-яА-Я]");
        while (true) {
            System.out.print("Enter a letter: ");
            String letter = CONSOLE_READER.readLine();

            if (!pattern.matcher(letter).matches()) {
                System.out.println("Invalid value.");
                continue;
            }
            if (errorLetters.contains(letter) || maskedWord.indexOf(letter) != -1) {
                System.out.println("The letter you entered has already been used before.");
                continue;
            }
            return letter.toLowerCase();
        }
    }

    private static StringBuilder initMaskedWord(int length) {
        StringBuilder sb = new StringBuilder(length);
        sb.append("_".repeat(length));
        return sb;
    }

    private static void openLetterInMask(String hiddenWord, StringBuilder maskedWord, String letter) {
        for (int i = 0; i < hiddenWord.length(); i++) {
            if (String.valueOf(hiddenWord.charAt(i)).equals(letter)) {
                maskedWord.replace(i, i+1, letter);
            }
        }
    }

    public static GameStatus checkGameStatus(String hiddenWord, StringBuilder maskedWord, List<String> errorLetters) {
        if(hiddenWord.contentEquals(maskedWord)) {
            return GameStatus.WON;
        }
        if (errorLetters.size() >= MAX_ERRORS) {
            return GameStatus.LOST;
        }
        return GameStatus.NOT_FINISHED;
    }

    public static void printGallows (int errorCount) throws IOException {
        Path path = Paths.get(RELATIVE_GALLOWS_IMAGE_FOLDER + errorCount + ".txt");
        String content = Files.readString(path);
        System.out.println(content);
    }

    private static void printMaskedWord(StringBuilder maskedWord) {
        System.out.println("Word: " + maskedWord);
    }

    private static void printErrorLetters(List<String> playerLetters) {
        System.out.print("Errors: " );
        for (String s : playerLetters) {
            System.out.print(s + " ");
        }
        System.out.println();
    }
    private static void printGameResult(GameStatus status) {
        switch (status){
            case WON: System.out.println("\nCongratulations! You won!");
            case LOST: System.out.println("\nYou lose! You will definitely be lucky in the next game!");
        }
    }
}