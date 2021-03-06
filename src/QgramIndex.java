import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * When looking for words whose PED between them and a given
 * word should be smaller delta, one can use this model to exclude some
 * impossible words in advance so that there will be fewer words could be
 * compared with that give word and efficient calculation could be achieved
 */
public class QgramIndex {

  private final int qnum;
  /*
   * Q - 1 times "$" will be padded to the left of word for PED.
   */
  public String padding = "$";

  /*
   * Use Hashmap as data structure to store q-grams and in which word
   * it has appeared and its frequency. word id and frequency will be
   * as a integer array stored.
   */
  public HashMap<String, List<Integer[]>> idx = new HashMap<>();

  /*
   * An Arraylist to store all words as string.
   */
  public List<String> entities;

  /*
   * Show the id of word which is being processed.
   */
  private int id = 0;

  /**
   * Constructor of class Qgram
   * @param q
   *       define the length of a single qgram
   */
  public QgramIndex(int q) {
    this.qnum = q;
    this.padding = this.padding.repeat(this.qnum - 1);
  }


  /**
   * Read file and process word
   * @param filename
   *        file path of a dictionary
   * @throws IOException
   *        avoid IO-errors
   */
  public void build_from_file(String filename) throws IOException {
    FileInputStream inputStream = new FileInputStream(filename);
    BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(inputStream));
    String str;
    entities = new ArrayList<>();
    while ((str = bufferedReader.readLine()) != null) {
      // Read word in every line
      this.entities.add(str);

      // Get all qgrams of a word using method compute_qgrams
      ArrayList<String> word = compute_qgrams(str);
      for (String qgram : word) {
        // Handle with single qgram
        if (!this.idx.containsKey(qgram)) {
          /* If the qgram has not been seen before,
             then we will put (qgram, None) into Hashmap idx. */
          this.idx.put(qgram, null);
        }

        if ((this.idx.get(qgram) != null) && (this.idx.get(qgram)
            .get(this.idx.get(qgram).size() - 1)[0] == this.id)) {
          /* This case implies that a qgram appears more than once
             in a word. */
          List<Integer[]> value = this.idx.get(qgram);
          Integer[] freqId = value.get(value.size() - 1);
          freqId[1]++;
          value.set(value.size() - 1, freqId);
          this.idx.replace(qgram, value);
        } else {
          /* In the 90th line a new qgram has been discovered and
          stored with its value. But value was as null initialized.
          And now we will take word id and its frequency as value
          for corresponding qgram */

          Integer[] temp = {this.id, 1};
          List<Integer[]> value = new ArrayList<>();
          List<Integer[]> temp1 = this.idx.get(qgram);
          if (temp1 == null) {
            value.add(temp);
            this.idx.replace(qgram, value);
          } else {
            temp1.add(temp);
            this.idx.replace(qgram, temp1);
          }
        }
      }
      this.id++;
    }
    inputStream.close();
    bufferedReader.close();
  }

  /**
   *
   * @param word
   *       a word which should be padded and separated into several substrings.
   * @return
   *       return qgrams of a word
   */
  private ArrayList<String> compute_qgrams(String word) {
    /* Get all qgrams of a word
    Example:
    Input: "frei"
    Output: {"$$f", "$fr", "fre", "rei"} */
    ArrayList<String> ret = new ArrayList<>();
    String padded = this.padding + word;
    int lgh = word.length();
    for (int i = 0; i < lgh; i++) {
      ret.add(padded.substring(i, i + this.qnum));
    }
    return ret;
  }

  /**
   *
   * @param word
   *        the word for which we need to find matches
   * @param delta
   *        a predefined value which the PED between the given word and
   *        other words at most should be.
   * @return
   *        return a arraylist which includes all words on which PED between
   *        them and the given word smaller or equal to delta
   */
  public ArrayList<String> find_matches(String word, int delta) {
    /* Find words with PED shorter than delta and return
     * all (words id, ped) as list
     */

    // Set threshold to filter some "unnecessary" words.
    int threshold = word.length()  - (this.qnum * delta);
    List<List<Integer[]>> matches = new ArrayList<>();
    ArrayList<String> qgrams = compute_qgrams(word);

    // Add word id and frequency if a qgram appears in that word
    for (String qgram: qgrams) {
      if (this.idx.containsKey(qgram)) {
        matches.add(this.idx.get(qgram));
      }
    }

    /* Merge all lists so that every word id corresponds with unique
       frequency */
    List<Integer[]> merged = merge_lists(matches);

    List<Integer[]> ret = new ArrayList<>();
    for (Integer[] pair: merged) {
      if (pair[1] >= threshold) {
        /* Only when comm(x', y') >= |x| - q * delta, we could
         calculate the PED of x and y. */
        // System.out.println(this.entities.get(pair[0]));
        int pedist = ped(word, this.entities.get(pair[0]), delta);
        int edist = ed(word, this.entities.get(pair[0]));
        if (pedist <= delta && edist <= delta + 2) {
          Integer[] temp = {pair[0], edist};
          ret.add(temp);
        }
      }
    }

    // If no match could be found, return a empty list
    if (ret.size() == 0) {
      return new ArrayList<>();
    }

    ArrayList<String> candidates = new ArrayList<>();
    for (Integer[] info: ret) {
      candidates.add(this.entities.get(info[0]));
    }
    return candidates;
  }


  /**
   *
   * @param bigList
   *      a list shows that with which word's gram a single qgram
   *      of a given word corresponds and its frequency
   * @return
   *      after merging same index of the word, we will get a tuple
   *      (idx, frequency) to show how a word corresponds with that
   *      given word
   */
  private List<Integer[]> merge_lists(List<List<Integer[]>> bigList) {
    /* Merge all lists so that every word id corresponds with
    unique frequency */

    HashMap<Integer, Integer> info = new HashMap<>();

    List<Integer[]> ret = new ArrayList<>();
    int max = 0;
    for (List<Integer[]> list1: bigList) {
      for (Integer[] list2 : list1) {
        if (list2[0] > max) {
          max = list2[0];
        }
        if (!info.containsKey(list2[0])) {
          info.put(list2[0], 1);
        } else {
          int value = info.get(list2[0]);
          info.replace(list2[0], value + 1);
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

  /**
   * Calculate PED between two words
   * @param x
   *        a given word
   * @param y
   *        a candidate word
   * @param delta
   *        a pre-defined value which regulates how big PED between x and y
   *        should be
   * @return
   *        give the PED between x and y
   */
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


  /**
   * Calculate ED between two words
   * @param x
   *        a given word
   * @param y
   *        a candidate word
   * @return
   *        give the ED between x and y
   */
  private int ed(String x, String y) {
    // Compute the ED between two words.
    if (x.equals(y)) {
      return 0;
    }
    int n = x.length();
    int m = y.length();
    int[][] table = new int[n + 1][m + 1];
    for (int i = 0; i <= n; i++) {
      table[i][0] = i;
    }
    for (int j = 0; j <= m; j++) {
      table[0][j] = j;
    }
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        int ed1 = table[i][j - 1] + 1;
        int ed2 = table[i - 1][j] + 1;
        int ed3 = table[i - 1][j - 1];
        if (x.charAt(i - 1) != y.charAt(j - 1)) {
          ed3 += 1;
        }
        table[i][j] = Math.min(Math.min(ed1, ed2), ed3);
      }
    }
    return table[n][m];
  }
}
