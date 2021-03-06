import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class includes methods which were implemented in classes of qgramindex
 * and ngram. In main_activity it will be used to accept the user's input and
 * calculate the best three candidates using principles which are introduced
 * in ngram and qgram. Infos about ngram and qgram see the README file.
 */
public class AutoInput {
  public int wordNum = 817824;
  public int sentenceNum = 65784;

  public HashMap<String, Integer> initialWord;
  public ArrayList<HashMap<List<String>, Integer>> ngramList;
  private final int qnum = 3;
  public String padding = "$$";
  public HashMap<String, List<Integer[]>> idx;
  private final List<String> entities;

  private int len;
  private boolean toReplace;
  private boolean capital = true;


  /**
   *
   * @throws IOException avoid io error
   * @throws ClassNotFoundException avoid io error
   */
  @SuppressWarnings("unchecked")
  public AutoInput() throws IOException, ClassNotFoundException {
    FileInputStream fis1 = new FileInputStream("/home/luziang/projekt/modell/ngramlist.txt");
    ObjectInputStream ois1 = new ObjectInputStream(fis1);
    this.ngramList = (ArrayList<HashMap<List<String>, Integer>>) ois1
        .readObject();
    ois1.close();

    FileInputStream fis2 = new FileInputStream("/home/luziang/projekt/modell/initial_word.txt");
    ObjectInputStream ois2 = new ObjectInputStream(fis2);
    this.initialWord = (HashMap<String, Integer>) ois2.readObject();
    ois2.close();

    FileInputStream fis3 = new FileInputStream("/home/luziang/projekt/modell/idx.txt");
    ObjectInputStream ois3 = new ObjectInputStream(fis3);
    this.idx = (HashMap<String, List<Integer[]>>) ois3.readObject();
    ois3.close();

    FileInputStream fis4 = new FileInputStream("/home/luziang/projekt/modell/entities.txt");
    ObjectInputStream ois4 = new ObjectInputStream(fis4);
    this.entities = (List<String>) ois4.readObject();
    ois4.close();
  }

  public int return_len() {
    return this.len;
  }

  public boolean return_to_replace() {
    return this.toReplace;
  }

  /**
   *
   * @param textview string of text which has been inputted
   * @return candidates as list
   */
  public ArrayList<String> get_text(String textview) {
    ArrayList<String> result = new ArrayList<>();
    /*
     * According to the last character of the string in the text field, we can
     * judge which function will be needed
     */
    this.len = 0;
    this.toReplace = false;

    char end1;
    char end2 = 0;
    char end3 = 0;
    if (textview.length() != 0) {
      end1 = textview.charAt(textview.length() - 1);
      if (textview.length() > 1) {
        end2 = textview.charAt((textview.length() - 2));
      }
    } else {
      end1 = ' ';
    }

    if (end1 == 44 || (end1 == 32 & end2 == 44)) {
      this.capital = false;
    } else if (end1 == 33 || end1 == 46 || end2 == 33 || end2 == 46
        || end1 == 63 || end2 == 63) {
      this.capital = true;
    }

    // Convert the long string from text field into a string list without
    // empty string, comma, period, question mark and colon.
    String[] sentenceSlices = textview.split("[,.?:!;]");
    if (sentenceSlices.length == 0) {
      result.add("");
      result.add("");
      result.add("");
      return result;
    }
    String lastPartMix = sentenceSlices[sentenceSlices.length - 1];
    String lastPart = lastPartMix.replaceAll("[^a-zA-Z'\\s]+", " ");
    String[] lastPartList = lastPart.split("\\s+");

    ArrayList<String> wordsListTemp =
      new ArrayList<>(Arrays.asList(lastPartList));
    wordsListTemp.removeAll(Collections.singletonList(""));

    ArrayList<String> wordsList = new ArrayList<>();

    for (String word: wordsListTemp) {
      wordsList.add(word.toLowerCase());
    }
    int lgh = wordsList.size();

    if (lgh > 0) {
      end3 = wordsListTemp.get(lgh - 1).charAt(0);
    }

    if (end1 == 32) {
      /*
       * That means the last character is a empty string  and user just
       * finished input of a word and starts to input the next one or user
       * has not inputted anything
       */
      if (lgh == 0) {
        /*
         * User has not inputted anything or one sentence has ended
         */
        result.add("But");
        result.add("And");
        result.add("I");
      } else {
        /*
         * We need to return the three candidate with the highest probability
         * through calling predict function because user has inputted
         * nothing in this situation
         */
        if (lgh > 2) {
          wordsList = new ArrayList<>(wordsList.subList(lgh - 2, lgh));
        }
        result = get_match_predict(wordsList);
      }
    } else if ((end1 <= 90 && end1 >= 65) || (end1 >= 97 & end1 <= 122)) {
      /*
       * User continues to input and word is not complete since the last
       * character is a-z or A-Z
       */

      ArrayList<String> wordsPre =
        new ArrayList<>(wordsList.subList(0, lgh - 1));

      this.len = wordsList.get(lgh - 1).length();
      this.toReplace = true;
      ArrayList<String> words = this.find_matches(wordsList.get(lgh - 1), 2);

      if (lgh > 3) {
        wordsPre = new ArrayList<>(wordsList.subList(lgh - 3, lgh - 1));
      }
      if (words.size() == 0) {
        result = get_match_predict(wordsPre);
        this.toReplace = false;
      } else {
        result = get_match_select(wordsPre, words, wordsList.get(lgh - 1));
      }
    } else {
      result.add("But");
      result.add("And");
      result.add("I");
    }
    if (!this.capital) {
      result.replaceAll(String::toLowerCase);
    }
    if (result.size() < 3) {
      for (int i = 0; i < 3 - result.size(); i++) {
        result.add(null);
      }
    }
    ArrayList<String> lastresult = new ArrayList<>();
    if ((end1 <= 90 && end1 >= 65) || (end1 >= 97 & end1 <= 122)) {
      if (end3 <= 90 && end3 >= 65) {
        for (String string: result) {
          if (string != null) {
            String cap = string.substring(0, 1).toUpperCase() + string.substring(1);
            lastresult.add(cap);
          } else {
            lastresult.add(null);
          }
        }
        return lastresult;
      }
    }
    return result;
  }


  private ArrayList<String> compute_qgrams(String word) {
    /*
     * Get all qgrams of a word
     * Example:
     * Input: "frei"
     * Output: {"$$f", "$fr", "fre", "rei"}
     */
    ArrayList<String> ret = new ArrayList<>();
    String padded = this.padding + word;
    int lgh = word.length();
    for (int i = 0; i < lgh; i++) {
      ret.add(padded.substring(i, i + this.qnum));
    }
    return ret;
  }


  public ArrayList<String> find_matches(String word, int delta) {
    /* Find words with PED shorter than delta and return
     * all (words id, ped) as list
     */

    /*
     * Set threshold to filter some "unnecessary" words.
     */
    int threshold = word.length()  - (this.qnum * delta);
    List<List<Integer[]>> matches = new ArrayList<>();
    ArrayList<String> qgrams = compute_qgrams(word);

    /*
     * Add word id and frequency if a qgram appears in that word
     */
    for (String qgram: qgrams) {
      if (this.idx.containsKey(qgram)) {
        matches.add(this.idx.get(qgram));
      }
    }

    /* Merge all lists so that every word id corresponds with unique
     * frequency
     */
    List<Integer[]> merged = merge_lists(matches);

    List<Integer[]> ret = new ArrayList<>();
    for (Integer[] pair: merged) {
      if (pair[1] >= threshold) {
        /*
         * Only when comm(x', y') >= |x| - q * delta, we could
         * calculate the PED of x and y.
         */
        int pedist = ped(word, this.entities.get(pair[0]), delta);
        if (pedist <= delta) {
          Integer[] temp = {pair[0], pedist};
          ret.add(temp);
        }
      }
    }

    /*
     * If no match could be found, return a empty list
     */
    if (ret.size() == 0) {
      return new ArrayList<>();
    }


    ArrayList<String> candidates = new ArrayList<>();

    for (Integer[] info: ret) {
      candidates.add(this.entities.get(info[0]));
    }
    return candidates;
  }

  private List<Integer[]> merge_lists(List<List<Integer[]>> bigList) {
    /*
     * Merge all lists so that every word id corresponds with
     * unique frequency
     */

    HashMap<Integer, Integer> info = new HashMap<>();

    List<Integer[]> ret = new ArrayList<>();
    int max = 0;
    for (List<Integer[]> list1: bigList) {
      for (Integer[] list2 : list1) {
        if (list2[0] > max) {
          max = list2[0];
        }
        if (!info.containsKey(list2[0])) {
          info.put(list2[0], list2[1]);
        } else {
          int value = info.get(list2[0]);
          info.replace(list2[0], value + list2[1]);
        }
      }
    }

    int counter = 0;
    while (counter <= max) {
      if (info.containsKey(counter)) {
        Integer[] temp = {counter, info.get(counter)};
        ret.add(temp);
      }
      counter++;
    }
    return ret;
  }


  private int ped(String x, String y, int delta) {
    // Compute the PED between two words.
    int n = x.length() + 1;
    int m = Math.min(n + delta, y.length() + 1);
    int[] matrix = new int[m * n];

    for (int row = 0; row < n; row++) {
      matrix[m * row] = row;
    }

    for (int i = 0; i < m; i++) {
      matrix[i] = i;
    }

    for (int row = 1; row < n; row++) {
      for (int col = 1; col < m; col++) {
        int s = 1;
        if (x.charAt(row - 1) == y.charAt(col - 1)) {
          s = 0;
        }
        int repCosts = matrix[m * (row - 1) + (col - 1)] + s;
        int addCosts = matrix[m * row + (col - 1)] + 1;
        int delCosts = matrix[m * (row - 1) + col] + 1;
        matrix[m * row + col] = Math.min(Math.min(repCosts, addCosts),
          delCosts);
      }
    }

    int deltaMin = delta + 1;
    for (int col = 0; col < m; col++) {
      int val = matrix[m * (n - 1) + col];
      if (val < deltaMin) {
        deltaMin = val;
      }
    }
    return deltaMin;
  }


  public ArrayList<String> get_match_select(ArrayList<String> wordsPre,
                            ArrayList<String> words, String comparator) {
    // System.out.println(wordsPre.size() == 0);
    /*
     * From this, we will decide which gram probability will be used
     */
    int n = wordsPre.size() + 1;
    /*
     * Number of candidates words
     */
    int numCandidate = words.size();
    /*
     * Every candidates word should be connected with a empty word
     * so that calculation for unigram is possible
     */
    ArrayList<ArrayList<String>> unigram = new ArrayList<>();
    for (String word: words) {
      ArrayList<String> temp = new ArrayList<>();
      temp.add(word);
      unigram.add(temp);
    }
    /*
     * Every candidates word will be connected with the prefix words
     */
    ArrayList<ArrayList<String>> sentence = new ArrayList<>();
    for (String word: words) {
      ArrayList<String> temp = new ArrayList<>(wordsPre);
      temp.add(word);
      sentence.add(temp);
    }
    /*
     * If n = 3, the last one word of prefix should also observed
     * as a unigram, when calculating bigram
     */
    ArrayList<String> unigramLastWord = new ArrayList<>();
    if (n == 3) {
      unigramLastWord.add(wordsPre.get(1));
    }

    /* Every candidates word will be connected with last one word
     * of prefix
     */
    ArrayList<ArrayList<String>> bigram = new ArrayList<>();
    if (n == 2 || n == 3) {
      for (String word : words) {
        ArrayList<String> temp = new ArrayList<>();
        temp.add(wordsPre.get(wordsPre.size() - 1));
        temp.add(word);
        bigram.add(temp);
      }
    }
    /*
     * Store (id, probability) as pair in this list
     */
    ArrayList<ArrayList<Double>> wordsCount = new ArrayList<>();

    if (n == 1) {
      /* That means we are processing a word which is at the start of
       * a sentence, so we calculate only the unigram probability and
       * maybe its probability as a start of a scentence.
       * Here we will firstly only calculate its unigram probability
       * A list could store (id, probability of a candidate word)
       * Id will be firstly stored as double number but later will be
       * converted into integer to fetch word in words list
       * words_pre = null
       */

      double lambda1 = 0.95;
      double lambda2 = 0.05;
      double uniProba;
      int wordId = 0;
      for (int i = 0; i < numCandidate; i++) {
        uniProba = 0.0;
        ArrayList<Double> temp = new ArrayList<>();
        temp.add((double)wordId);
        /*
         * Check if there is probability that this word will be at the
         * start of a sentence
         */
        if (this.initialWord.containsKey(sentence.get(i).get(0))) {
          uniProba = lambda1 * (double)
          this.initialWord.get(sentence.get(i).get(0))
              / this.sentenceNum;

          /*
           * Capitalize the first letter of word i
           */
          String wordLowercase = words.get(i);
          String wordCapitalized = wordLowercase.substring(0, 1).toUpperCase()
              + wordLowercase.substring(1);
          words.set(i, wordCapitalized);


        } else if (this.ngramList.get(1).containsKey(sentence.get(i))) {
          uniProba = lambda2 * (double) this.ngramList.get(1)
              .get(sentence.get(i)) / this.wordNum;
        }

        temp.add(uniProba);
        wordsCount.add(temp);
        wordId++;
      }
    } else if (n == 2) {
      /*
       * That means we are at the second word or we only use bigram
       * probability to complete the current word.
       */
      final double lambda1 = 0.1;
      final double lambda2 = 0.9;
      double biProba;
      double uniProba;
      int wordId = 0;
      for (int i = 0; i < numCandidate; i++) {
        biProba = 0.0;
        uniProba = 0.0;
        ArrayList<Double> temp = new ArrayList<>();
        temp.add((double)wordId);
        if (this.ngramList.get(2).containsKey(sentence.get(i))) {
          biProba = lambda2 * (double) this.ngramList
             .get(2).get(sentence.get(i)) / this.ngramList.get(1).get(wordsPre);

        } else if (this.ngramList.get(1).containsKey(unigram.get(i))) {
          uniProba = lambda1 * (double)this.ngramList
              .get(1).get(unigram.get(i)) / this.wordNum;
        }
        temp.add(biProba + uniProba);
        wordsCount.add(temp);
        wordId++;
      }

    } else {
      /*
       * That means we are at least the third word amd we need to calculate
       * probability of uni-, bi- and trigrams to complete the current word.
       */
      final double lambda1 = 0.1;
      final double lambda2 = 0.3;
      final double lambda3 = 0.6;

      double biProba;
      double uniProba;
      double triProba;
      int wordId = 0;
      for (int i = 0; i < numCandidate; i++) {
        biProba = 0.0;
        uniProba = 0.0;
        triProba = 0.0;

        ArrayList<Double> temp = new ArrayList<>();
        temp.add((double)wordId);
        /*
         * In this case, trigram exists for the word i
         */
        if (this.ngramList.get(3).containsKey(sentence.get(i))) {
          triProba = lambda3 * (double) this.ngramList.get(3)
              .get(sentence.get(i)) / this.ngramList.get(2).get(wordsPre);
        /*
         * In this case, we need only to look back into the last
         * previous word.
         */

        } else if (this.ngramList.get(2).containsKey(bigram.get(i))) {
          biProba = lambda2 * (double) this.ngramList.get(2)
              .get(bigram.get(i)) / this.ngramList.get(1).get(unigramLastWord);

        } else if (this.ngramList.get(1).containsKey(unigram.get(i))) {
          uniProba = lambda1 * (double) this.ngramList.get(1)
              .get(unigram.get(i)) / this.wordNum;
        }

        temp.add(biProba + uniProba + triProba);
        wordsCount.add(temp);
        wordId++;
      }
    }
    /*
     * Divide the frequency of observed words by the frequency of prefix words
     * A list could store (id, probability of a candidate word)
     * Id will be firstly stored as double number but later will be
     * converted into integer to fetch word in words list
     */
    double alpha = 0.03;
    int size = wordsCount.size();
    for (int i = 0; i < size; i++) {
      double s = wordsCount.get(i).get(1) - alpha
          * ped(comparator, words.get(wordsCount
          .get(i).get(0).intValue()).toLowerCase(), 2);
      double idx = wordsCount.get(i).get(0);
      ArrayList<Double> temp = new ArrayList<>();
      temp.add(idx);
      temp.add(s);
      wordsCount.set(i, temp);
    }

    wordsCount.sort((ArrayList<Double> x1, ArrayList<Double> x2) ->
        x2.get(1).compareTo(x1.get(1)));


    /*
     * May be shorter as three words
     */
    int count = 0;
    ArrayList<String> ret = new ArrayList<>();
    for (ArrayList<Double> idProba: wordsCount) {
      if (count == 3 || count == numCandidate) {
        break;
      }
      // probability.add((idProba.get(1)));
      ret.add(words.get(idProba.get(0).intValue()));
      count++;
    }

    return ret;
  }


  public ArrayList<String> get_match_predict(ArrayList<String> wordsPre) {
    // System.out.println("i am here");
    /*
     * From this, we will decide which gram probability will be used
     */
    int n = wordsPre.size() + 1;

    ArrayList<String> trigramBase;
    ArrayList<String> bigramBase;

    /*
     * Store (id, probability) as pair in this list
     */
    ArrayList<ArrayList<Double>> wordsCount = new ArrayList<>();
    ArrayList<String> ret = new ArrayList<>();
    ArrayList<String> candidates = new ArrayList<>();

    if (n == 3) {
      trigramBase = new ArrayList<>(wordsPre);
      bigramBase = new ArrayList<>();
      bigramBase.add(wordsPre.get(1));
      double lambda1 = 0.9;
      double lambda2 = 0.1;
      int candidatesId = 0;
      if (this.ngramList.get(2).containsKey(trigramBase)) {
        Set<Map.Entry<List<String>, Integer>> trigramKeyset
            = this.ngramList.get(3).entrySet();
        for (Map.Entry<List<String>, Integer> gram: trigramKeyset) {
          List<String> temp1 = gram.getKey();
          Integer temp2 = gram.getValue();
          if (trigramBase.get(0).equals(temp1.get(0))
              && trigramBase.get(1).equals(temp1.get(1))) {
            candidates.add(temp1.get(2));
            ArrayList<Double> candidatesInfo = new ArrayList<>();
            candidatesInfo.add((double)candidatesId);
            candidatesInfo.add(lambda1 * (double)temp2 / this.ngramList.get(2)
                .get(trigramBase));
            wordsCount.add(candidatesInfo);
            candidatesId++;
          }
        }
      }
      if (this.ngramList.get(1).containsKey(bigramBase)) {
        for (Map.Entry<List<String>, Integer> gram:
            this.ngramList.get(2).entrySet()) {
          List<String> temp1 = gram.getKey();
          Integer temp2 = gram.getValue();
          if (bigramBase.get(0).equals(temp1.get(0))) {
            candidates.add(temp1.get(1));
            ArrayList<Double> candidatesInfo = new ArrayList<>();
            candidatesInfo.add((double)candidatesId);
            candidatesInfo.add(lambda2 * (double)temp2 / this.ngramList.get(1)
                .get(bigramBase));
            wordsCount.add(candidatesInfo);
            candidatesId++;
          }
        }
      }
      if (wordsCount.size() == 0) {
        ret.add("is");
        ret.add("and");
        ret.add("would");
        return ret;
      }

    } else if (n == 2) {
      bigramBase = new ArrayList<>(wordsPre);
      int candidatesId = 0;

      if (this.ngramList.get(1).containsKey(bigramBase)) {
        for (Map.Entry<List<String>, Integer> gram:
          this.ngramList.get(2).entrySet()) {
          List<String> temp1 = gram.getKey();
          Integer temp2 = gram.getValue();
          if (bigramBase.get(0).equals(temp1.get(0))) {
            candidates.add(temp1.get(1));
            ArrayList<Double> candidatesInfo = new ArrayList<>();
            candidatesInfo.add((double)candidatesId);
            candidatesInfo.add((double)temp2 / this.ngramList.get(1)
                .get(bigramBase));
            wordsCount.add(candidatesInfo);
            candidatesId++;
          }
        }
      } else {
        ret.add("is");
        ret.add("and");
        ret.add("would");
        return ret;
      }
    } else {
      /*
       * We are at the start of a sentence since there is no word before.
       *  So we just return the first three words with highest probability
       */
      // ArrayList<Double> prob = new ArrayList<>();
      int count = 0;
      for (Map.Entry<String, Integer> ini: this.initialWord.entrySet()) {
        if (count == 3) {
          break;
        }
        // prob.add((double)ini.getValue() / this.wordNum);
        String wordLowercase = ini.getKey();
        String wordCapitalized = wordLowercase.substring(0, 1)
            .toUpperCase() + wordLowercase.substring(1);
        ret.add(wordCapitalized);
        count++;
      }
      // System.out.println(prob);
      return ret;
    }

    wordsCount.sort((ArrayList<Double> x1, ArrayList<Double> x2)
        -> x2.get(1).compareTo(x1.get(1)));


    /*
     * The length may be shorter than three
     */
    int count = 0;

    for (ArrayList<Double> idFreq: wordsCount) {
      if (count == 3 || count == wordsCount.size()) {
        break;
      }
      ret.add(candidates.get((idFreq.get(0)).intValue()));
      count++;
    }
    return ret;
  }
}
