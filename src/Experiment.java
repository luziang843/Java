import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This experiment consists of two parts.
 * The first part is to count how much work (how many keys-pressed) could be
 * reduced by the autocomplete and auto-predict of keyboard
 * The second part is to count how many wrong words (here we assume every word
 * has a spelling mistake at one position, which consists of at least three
 * letters) could have been corrected.
 */
public class Experiment {
  /**
   *
   * @param args choose to evaluate keyboard with corpus in every word
   *             errors appears or not
   * @throws IOException avoid IO error
   * @throws ClassNotFoundException avoid IO error
   */
  public static void main(String [] args) throws IOException,
    ClassNotFoundException {
    boolean error = false;
    /*
    if (args[0].equals("WithErrors")) {
      error = true;
    }

     */

    /* Statistics from a test corpus
     * Number of how many times one should press the key to finish input of
     * test corpus
     */
    long buttonPressedNoError = 0;
    long buttonPressedWithError = 0;
    /* Number of words which are predicted correctly without giving any
     * prefix of those words
     */
    long predictNum = 0;
    long completeNum = 0;
    /*
     * Number of all characters of the test corpus (including spaces)
     */
    long totalNum = 0;
    long totalWordNum = 0;
    /* Number of spaces which should be padded between two words or between
     * symbols and words automatically. In the experiment, it will be
     * counted in advance.
     */
    int spaceNum = 0;

    /*
     * In some cases, it is difficult to decide for a machine to judge if
     * spaces after words or symbols should be padded because of uncertainty of
     * sentences. Therefore, in this experiment, spaces would not be padded
     * automatically like in the real keyboard and those spaces will be counted
     * into the total number of reduced steps, which should be padded
     * automatically between two words and between symbols and words
     */

    // String filePath = "/home/luziang/IdeaProjects/n-gram/src/test.txt";
    String filePath = "/home/luziang/projekt/dataset/howto.txt";
    String regex1 = "[a-zA-Z] [a-zA-Z]";
    Path filename = Path.of(filePath);
    String actual1 = Files.readString(filename);
    Pattern pattern1 = Pattern.compile(regex1);
    Matcher matcher1 = pattern1.matcher(actual1);
    while (matcher1.find()) {
      spaceNum++;
    }
    String regex2 = "[a-zA-Z][!.,?] [a-zA-Z]";
    String actual2 = Files.readString(filename);
    Pattern pattern2 = Pattern.compile(regex2);
    Matcher matcher2 = pattern2.matcher(actual2);
    while (matcher2.find()) {
      spaceNum++;
    }
    System.out.println(spaceNum);
    // To ensure, from which position of a text
    // the chosen candidate should replace
    int wordShadowLength;

    // Initialize class AutoInput as ai
    AutoInput ai = new AutoInput();

    // Read 5% of corpus and store every sentence from a line in a long
    // list
    FileInputStream inputStream = new FileInputStream(filePath);
    BufferedReader bufferedReader = new BufferedReader(
      new InputStreamReader(inputStream));
    String str;
    ArrayList<String> sentences = new ArrayList<>();
    while ((str = bufferedReader.readLine()) != null) {
      totalNum += str.length();
      if (!str.equals("")) {
        sentences.add(str);
      }

    }
    if (!error) {
      /* Simulate the process in which a normal person press keys to finish
       * input of test corpus. Part 1 (no spelling mistakes)
       */

      for (String sentence: sentences) {
        String inputFinished = "";
        int sentenceEnd = sentence.length();
        int inputPointer = 0;
        ArrayList<String> candidates;
        candidates = ai.get_text(inputFinished);
        wordShadowLength = ai.return_len();
        boolean toReplace = ai.return_to_replace();

        while (inputPointer < sentenceEnd) {
          StringBuilder edittext = new StringBuilder(inputFinished);
          boolean edited = false;
          final int le = edittext.length();
          buttonPressedNoError++;
          for (String candidate: candidates) {
            edittext = new StringBuilder(inputFinished);
            if (candidate == null) {
              continue;
            }
            edittext.replace(le - wordShadowLength, le, candidate);
            if (sentence.contains(edittext) && edittext.length() > le) {
              if (!toReplace) {
                predictNum++;
              }
              inputFinished = edittext.toString();
              inputPointer = edittext.length();
              edited = true;
              break;
            }
          }
          if (!edited) {
            edittext = new StringBuilder(inputFinished);
            edittext.append(sentence.charAt(inputPointer));
            inputFinished = edittext.toString();
            inputPointer++;
          }
          candidates = ai.get_text(inputFinished);
          wordShadowLength = ai.return_len();
        }
      }

      System.out.println(totalNum);
      System.out.println();
      System.out.println(buttonPressedNoError
        + " keyboard keys have been pressed.");
      System.out.println(predictNum
        + " words have benn correctly predicted.");
      System.out.println(totalNum - buttonPressedNoError + spaceNum
        + " keys pressed could be reduced.");
      System.out.println((double) (totalNum - buttonPressedNoError + spaceNum)
        / totalNum * 100 + "% of work has been reduced.");
    } else {
      /*
       * Part 2 every word will have a spelling mistake at the first one
       * position, which consists of at least three letters
       */
      for (String sentence: sentences) {

        String sentenceNew = sentence.strip();
        String[] rightWords = sentenceNew.split("\\s");

        ArrayList<String> wrongWords = new ArrayList<>();
        for (String word: rightWords) {
          assert false;
          if (word.equals("")) {
            wrongWords.add("");
          } else  {
            if (word.charAt(0) == 'a') {
              wrongWords.add("b" + word.substring(1));
            } else {
              wrongWords.add("a" + word.substring(1));
            }
          }
        }

        String inputFinished = "";
        ArrayList<String> candidates;
        candidates = ai.get_text(inputFinished);
        wordShadowLength = ai.return_len();

        assert false;
        int len = wrongWords.size();
        totalWordNum += len;
        for (int i = 0; i < len; i++) {
          if (wrongWords.get(i).equals("")) {
            inputFinished += "";
            continue;
          }
          boolean edited = false;
          int wordLen = wrongWords.get(i).length();
          for (int j = 0; j < wordLen; j++) {
            buttonPressedWithError++;
            for (String candidate: candidates) {
              StringBuilder edittext1 = new StringBuilder(inputFinished);
              int le = edittext1.length();
              if (candidate == null) {
                continue;
              }
              edittext1.replace(le - wordShadowLength, le, candidate);
              if (candidate.equals(rightWords[i])) {
                inputFinished = edittext1.toString();
                completeNum += 1;
                if (i != len - 1) {
                  inputFinished += " ";
                  buttonPressedWithError++;
                }
                edited = true;
                break;
              }
            }
            if (edited) {
              break;
            }
            if (!edited) {
              if (j == wordLen - 1) {
                buttonPressedWithError += 2;
                StringBuilder edittext2 = new StringBuilder(inputFinished);
                int le = edittext2.length();
                edittext2.replace(le - wordShadowLength, le, rightWords[i]);
                if (i != len - 1) {
                  edittext2.append(" ");
                  buttonPressedWithError++;
                }
                inputFinished = edittext2.toString();
              } else {
                inputFinished = inputFinished + wrongWords.get(i).charAt(j);
              }
            }
            candidates = ai.get_text(inputFinished);
            wordShadowLength = ai.return_len();
          }
        }
      }

      System.out.println(totalWordNum + " words need to be inputted.");
      System.out.println(buttonPressedWithError
        + " keyboard keys have been pressed.");
      System.out.println(completeNum
        + " words have benn correctly completed.");
      System.out.println(totalNum - buttonPressedWithError + spaceNum
        + " keys pressed could be reduced.");
      System.out.println((double) (totalNum - buttonPressedWithError + spaceNum)
        / totalNum * 100 + "% of work has been reduced.");
    }
  }
}

