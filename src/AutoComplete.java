import java.io.*;
import java.util.*;
import java.lang.*;
import java.io.FileWriter;
import java.io.IOException;


public class AutoComplete {
    public static void main(String[] args) throws NullPointerException, IOException {
        /*
        // Q would be set as 3 here.
        */
        QgramIndex qg = new QgramIndex(3);
        qg.build_from_file("/home/luziang/projekt/dataset/word_common.txt");
        System.out.println(qg.entities.subList(0,10));

        /*
        Ngram ng = new Ngram(3);
        ng.get_files("/home/luziang/projekt/dataset/corpus");
        ng.build_from_file();
        */
        /*
        ArrayList<String> candidates_words;
        candidates_words = qg.find_matches("want", 2);

        ArrayList<String> words_prefix = new ArrayList<>();
        words_prefix.add("I"); // words_prefix.add("");
        ArrayList<String> ret1 = ng.get_match_complete(words_prefix, candidates_words);
        //ArrayList<String> ret1 = ng.get_match_predict(words_prefix);
        System.out.println(ret1);
        */

        /*
        FileOutputStream fos = new FileOutputStream("/home/luziang/projekt/modell/initial_word.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(ng.initial_word);
        oos.close();

        FileOutputStream fos1 = new FileOutputStream("/home/luziang/projekt/modell/ngramlist.txt");
        ObjectOutputStream oos1 = new ObjectOutputStream(fos1);
        oos1.writeObject(ng.n_gram_list);
        oos1.close();
        */

        FileOutputStream fos2 = new FileOutputStream("/home/luziang/projekt/modell/idx.txt");
        ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
        oos2.writeObject(qg.idx);
        oos2.close();

        FileOutputStream fos3 = new FileOutputStream("/home/luziang/projekt/modell/entities.txt");
        ObjectOutputStream oos3 = new ObjectOutputStream(fos3);
        oos3.writeObject(qg.entities);
        oos3.close();

        /*
        *
        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/luziang/nltk_data/corpora/twitter_samples/tweets.20150430-223406.txt"));
        BufferedReader br = null;
        JSONParser parser = new JSONParser();

        try {

          String sCurrentLine;

          br = new BufferedReader(new FileReader("/home/luziang/nltk_data/corpora/twitter_samples/tweets.20150430-223406.json"));

          while ((sCurrentLine = br.readLine()) != null) {

            Object obj;
            try {
              obj = parser.parse(sCurrentLine);
              JSONObject jsonObject = (JSONObject) obj;

              String rel = (String) jsonObject.get("text");
              writer.write(rel + "\n");
              // System.out.println(rel);

                } catch (ParseException e) {

                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        writer.close();
        */

    }
}

