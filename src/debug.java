import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class debug {
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    ArrayList<String> words = new ArrayList<>();
    String filePath = "/home/luziang/projekt/dataset/word_common_used.txt";
    FileInputStream inputStream = new FileInputStream(filePath);
    BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(inputStream));
    String str;
    while ((str = bufferedReader.readLine()) != null) {
      if (!str.equals("")) {
        words.add(str.strip());
      }
    }
    FileInputStream fis1 = new FileInputStream("/home/luziang/projekt/modell/ngramlist.txt");
    ObjectInputStream ois1 = new ObjectInputStream(fis1);
    ArrayList<HashMap<List<String>, Integer>> ngramList = (ArrayList<HashMap<List<String>, Integer>>) ois1
      .readObject();
    ois1.close();
    HashMap<List<String>, Integer> unigram = ngramList.get(1);
    BufferedWriter writer = new BufferedWriter(new FileWriter("/home/luziang/projekt/dataset/word_common.txt"));
    int lineNum = 0;
    ArrayList<String> wordsNew = new ArrayList<>();
    for (String word: words) {
      List<String> key = new ArrayList<>();
      key.add(word);
      if (unigram.containsKey(key)) {
        writer.write(word + "\n");
        wordsNew.add(word);
        lineNum++;
      }
    }
    unigram = unigram.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (oldValue, newValue) -> oldValue, HashMap::new));
    int restLineNum = 10000 - lineNum;
    int newWordNum = 0;
      for (Map.Entry<List<String>, Integer> gram :
        unigram.entrySet()) {
          if (newWordNum == restLineNum) {
              break;
          }
          List<String> temp1 = gram.getKey();
          if (!wordsNew.contains(temp1.get(0)) && temp1.get(0).length() > 2) {
            writer.write(temp1.get(0) + "\n");
            newWordNum++;
          }
      }
    writer.close();
  }
}
