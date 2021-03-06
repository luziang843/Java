import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Ngram {
    /*
     * Ngram will be used to calculate the probability of word when looking back to
     * the previous n-1 words. For the calculation for keyboard, we need bigram or
     * trigrams.
     * For the first word, we just calculate the unigram probability of it as the
     * start of a scentence. For the second, we will only calculate the bigram
     * probability. From the third one, it is possible to calculate trigram porabability
     * To avoid absence of amy trigrams bigrams, we will use P(Wn|Wn-2,n-1) =
     * lambda1 * Pe(Wn) + lambda2 * (Pe(Wn|Wn-1) + lambda3 * Pe(Wn|Wn-2,n-1) for
     * probability of trigram, lambda 1-3 is respectively 0.1, 0.3, 0.6
     * And if bigrams are absent, we just calculate the probability of Wn itself.
     * At worst case, if Wn doesn't exist in corpus at all, we just give the choices
     * accroding to the PED
     */

        public int n_num;
        private int word_num = 0;
        private int sentence_num = 0;

        private String[] file_names;
        public HashMap<String, Integer> initial_word = new HashMap<>();
        public ArrayList<HashMap<List<String>, Integer>> n_gram_list;
        private String file_position;

        // Initialize a Ngram class and get the value of n
        public Ngram(int n) {
            this.n_num = n;
        }

        // Get the position of corpus documents and all names of file there
        public void get_files(String file_position) {
            File f  = new File(file_position);
            this.file_names = f.list();
            this.file_position = file_position;
        }


        // Read files and calculate grams according to n and their frquency
        public void build_from_file() throws IOException {
            long start = System.nanoTime();

            this.n_gram_list = new ArrayList<>();
            this.n_gram_list.add(null);
            for (int n = 0; n < this.n_num; n++) {
                HashMap<List<String>, Integer> grams = new
                        HashMap<>();
                this.n_gram_list.add(grams);
            }

            for (String file_name: this.file_names) {
                FileInputStream inputStream =
                        new FileInputStream(this.file_position + '/' + file_name);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));
                String str;
                ArrayList<String> raw_sentence_list = new ArrayList<>();

                while ((str = bufferedReader.readLine()) != null){
                    if (!str.equals("") && !str.equals(" ")) {
                        raw_sentence_list.add(str);
                    }
                }
                inputStream.close();
                bufferedReader.close();


                for (String raw_sentence: raw_sentence_list) {
                    String[] mixed_sentences = raw_sentence.split("[^a-zA-Z'\\s]+");
                    for (String singe_sentence: mixed_sentences) {

                        // Replace all other characters with space.
                        String temp1 = singe_sentence.replaceAll("[^a-zA-Z']+", " ");

                        // Split a sentence into words by space
                        String[] temp2 = temp1.split("\s+");
                        ArrayList<String> words_list = new ArrayList<>(Arrays.asList(temp2));

                        // Remove all empty strings in words list
                        words_list.removeAll(Collections.singletonList(""));
                        if (words_list.size() == 0) {
                            continue;
                        }
                        // Sum number of words into the total amount
                        this.word_num += words_list.size();

                        ArrayList<String> words_list_lowercase = new ArrayList<>();
                        for (String word_mix : words_list) {
                            if (Character.isUpperCase(word_mix.charAt(0))) {
                                if (this.initial_word.containsKey(word_mix.toLowerCase())) {
                                    this.initial_word.put(word_mix.toLowerCase(),
                                            this.initial_word.get(word_mix.toLowerCase()) + 1);
                                } else {
                                    this.initial_word.put(word_mix.toLowerCase(), 1);
                                }
                                this.sentence_num += 1;
                            }

                            String word_low = word_mix.toLowerCase();
                            words_list_lowercase.add(word_low);
                        }

                        // Ngrams list processing

                        if (words_list_lowercase.size() >= 3) {
                            for (int n = 1; n <= this.n_num; n++) {
                                // Build ngrams from single words from every file

                                int lgh = words_list_lowercase.size();
                                for (int i = 0; i < (lgh - (n - 1)); i++) {
                                    List<String> gram = new ArrayList<>();
                                    for (int j = 0; j < n ; j++) {
                                        gram.add(words_list_lowercase.get(i+j));
                                    }
                                    if (this.n_gram_list.get(n).containsKey(gram)) {
                                        int freq = this.n_gram_list.get(n).get(gram);
                                        freq++;
                                        this.n_gram_list.get(n).replace(gram, freq);
                                    } else {
                                      this.n_gram_list.get(n).put(gram, 1);
                                    }
                                }
                            }
                        }
                        else if (words_list_lowercase.size() == 2) {
                            for (int n = 1; n <= 2; n++) {
                                // Build ngrams from single words from every file

                                int lgh = words_list_lowercase.size();
                                for (int i = 0; i < (lgh - (n - 1)); i++) {
                                    List<String> gram = new ArrayList<>();
                                    for (int j = 0; j < n ; j++) {
                                        gram.add(words_list_lowercase.get(i+j));
                                    }
                                    if (this.n_gram_list.get(n).containsKey(gram)) {
                                        int freq = this.n_gram_list.get(n).get(gram);
                                        freq++;
                                        this.n_gram_list.get(n).replace(gram, freq);
                                    }
                                    else {
                                        this.n_gram_list.get(n).put(gram, 1);
                                    }
                                }
                            }
                        }
                        else {
                            List<String> gram = new ArrayList<>();
                            gram.add(words_list_lowercase.get(0));
                            if (this.n_gram_list.get(1).containsKey(gram)) {
                                int freq = this.n_gram_list.get(1).get(gram);
                                freq++;
                                this.n_gram_list.get(1).replace(gram, freq);
                            }
                            else {
                                this.n_gram_list.get(1).put(gram, 1);
                            }
                        }
                    }
                }
            }


            // Sort initial_word on the value(frequency)
            this.initial_word = this.initial_word.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));


            HashMap<List<String>, Integer> unigram = this.n_gram_list.get(1);
            ArrayList<List<String>> noise = new ArrayList<>();
            Iterator munigram = unigram.entrySet().iterator();
            while (munigram.hasNext()) {
                Map.Entry mapelement = (Map.Entry)munigram.next();
                if ((int) mapelement.getValue() <= 2) {
                    noise.add((List<String>)mapelement.getKey());
                }
            }
            for (List<String> temp: noise) {
                unigram.remove(temp);
            }
            this.n_gram_list.set(1, unigram);

            HashMap<List<String>, Integer> bigram = this.n_gram_list.get(2);
            ArrayList<List<String>> noisy = new ArrayList<>();
            Iterator mbigram = bigram.entrySet().iterator();
            while (mbigram.hasNext()) {
                Map.Entry mapelement = (Map.Entry)mbigram.next();
                if ((int) mapelement.getValue() <= 2) {
                    noisy.add((List<String>)mapelement.getKey());
                }
            }
            for (List<String> temp: noisy) {
                bigram.remove(temp);
            }
            this.n_gram_list.set(2, bigram);


            HashMap<List<String>, Integer> trigram = this.n_gram_list.get(3);
            ArrayList<List<String>> noisi = new ArrayList<>();
            Iterator mtrigram = trigram.entrySet().iterator();
            while (mtrigram.hasNext()) {
                Map.Entry mapelement = (Map.Entry)mtrigram.next();
                if ((int) mapelement.getValue() <= 2) {
                    noisi.add((List<String>)mapelement.getKey());
                }
            }
            for (List<String> temp: noisi) {
                trigram.remove(temp);
            }
            this.n_gram_list.set(3, trigram);



            long finish = System.nanoTime();
            long timeElapsed = (finish - start) / 1000000;
            System.out.println("time used to build grams:  " + timeElapsed + " milliseconds");
            get_gram_length();

        }

        public void get_gram_length() {
            System.out.println(this.n_gram_list.get(3).size() +
                    this.n_gram_list.get(1).size() + this.n_gram_list.get(2).size());
        }

        public ArrayList<String> get_match_complete(ArrayList<String> words_pre,
                                                    ArrayList<String> words) {

            long start = System.nanoTime();

            // From this, we will decide which gram probability will be used
            int n = words_pre.size() + 1;

            // Number of candidates words
            int num_candidate = words.size();

            // Every candidates word should be connected with a empty word
            // so that calculation for unigram is possible
            ArrayList<ArrayList<String>> unigram = new ArrayList<>();
            for (String word: words) {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(word);
                unigram.add(temp);
            }

            // Every candidates word will be connected with the prefix words
            ArrayList<ArrayList<String>> sentence = new ArrayList<>();
            for (String word: words) {
                ArrayList<String> temp = new ArrayList<>(words_pre);
                temp.add(word);
                sentence.add(temp);
            }

            // If n = 3, the last one word of prefix should also observed
            // as a unigram, when calculating bigram
            ArrayList<String> unigram_last_word = new ArrayList<>();
            if (n == 3) {
                unigram_last_word.add(words_pre.get(1));
            }

            // Every candidates word will be connected with last one word
            // of prefix
            ArrayList<ArrayList<String>> bigram = new ArrayList<>();
            if (n == 2 || n == 3) {
                for (String word : words) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(words_pre.get(words_pre.size()-1));
                    temp.add(word);
                    bigram.add(temp);
                }
            }

            // Store (id, probability) as pair in this list
            ArrayList<ArrayList<Double>> words_count = new ArrayList<>();

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
                System.out.println(1);
                double lambda1 = 0.7;
                double lambda2 = 0.3;
                double uni_proba;
                int word_id = 0;
                for (int i = 0; i < num_candidate; i++) {
                    uni_proba = 0.0;
                    ArrayList<Double> temp = new ArrayList<>();
                    temp.add((double)word_id);
                    // Check if there is probability that this word will be at the
                    // start of a sentence
                    if (this.initial_word.containsKey(sentence.get(i).get(0))){

                        uni_proba = lambda1 * (double)
                                this.initial_word.get(sentence.get(i).get(0))
                                / this.sentence_num;

                        // Capitalize the first letter of word i
                        String word_lowercase = words.get(i);
                        String word_capitalized =
                                word_lowercase.substring(0, 1).toUpperCase()
                                        + word_lowercase.substring(1);
                        words.set(i, word_capitalized);
                    }

                    else if (this.n_gram_list.get(1).containsKey(sentence.get(i))){
                        uni_proba = lambda2 * (double)this.n_gram_list.get(1).
                                get(sentence.get(i)) / this.word_num;
                    }


                    temp.add(uni_proba);
                    words_count.add(temp);
                    word_id++;
                }
            }
            else if (n == 2) {
                /*
                 * That means we are at the second word or we only use bigram
                 * probability to complete the current word.
                 */
                final double lambda1 = 0.1;
                final double lambda2 = 0.9;
                double bi_proba;
                double uni_proba;
                int word_id = 0;

                for (int i = 0; i < num_candidate; i++ ) {
                    bi_proba = 0.0;
                    uni_proba = 0.0;
                    ArrayList<Double> temp = new ArrayList<>();
                    temp.add((double)word_id);
                    if (this.n_gram_list.get(2).containsKey(sentence.get(i))){
                        bi_proba = lambda2 * (double) this.n_gram_list.
                                get(2).get(sentence.get(i)) /
                                this.n_gram_list.get(1).get(words_pre);
                    }

                    else if (this.n_gram_list.get(1).containsKey(unigram.get(i))) {
                        uni_proba = lambda1 * (double)this.n_gram_list
                                .get(1).get(unigram.get(i)) / this.word_num;

                    }

                    temp.add(bi_proba + uni_proba);
                    words_count.add(temp);
                    word_id ++;
                }
            }
            else {
                /*
                 * That means we are at least the third word amd we need to calculate
                 * probability of uni-, bi- and trigrams to complete the current word.
                 */
                final double lambda1 = 0.1;
                final double lambda2 = 0.3;
                final double lambda3 = 0.6;

                double bi_proba;
                double uni_proba;
                double tri_proba;
                int word_id = 0;

                for (int i = 0; i < num_candidate; i++ ) {
                    bi_proba = 0.0;
                    uni_proba = 0.0;
                    tri_proba = 0.0;

                    ArrayList<Double> temp = new ArrayList<>();
                    temp.add((double)word_id);

                    // In this case, trigram exists for the word i
                    if (this.n_gram_list.get(3).containsKey(sentence.get(i))){
                        tri_proba = lambda3 * (double) this.n_gram_list.get(3).
                                get(sentence.get(i)) /
                                this.n_gram_list.get(2).get(words_pre);
                    }

                    // In this case, we need only to look back into the last
                    // previous word.
                    else if (this.n_gram_list.get(2).containsKey(bigram.get(i))) {
                        bi_proba = lambda2 * (double) this.n_gram_list.get(2).
                                get(bigram.get(i)) / this.n_gram_list.get(1).
                                get(unigram_last_word);
                    }

                    else if (this.n_gram_list.get(1).containsKey(unigram.get(i))) {
                        uni_proba = lambda1 * (double) this.n_gram_list.get(1).
                                get(unigram.get(i)) / this.word_num;
                    }

                    temp.add(bi_proba + uni_proba + tri_proba);
                    words_count.add(temp);
                    word_id ++;
                }
            }
            /*
             * Divide the frequency of observed words by the frequency of prefix words
             * A list could store (id, probability of a candidate word)
             * Id will be firstly stored as double number but later will be
             * converted into integer to fetch word in words list
             */
            words_count.sort((ArrayList<Double> x1, ArrayList<Double> x2) ->
                    x2.get(1).compareTo(x1.get(1)));

            ArrayList<Double> probability = new ArrayList<>();
            // May be shorter as three words
            int count = 0;
            ArrayList<String> ret = new ArrayList<>();
            for (ArrayList<Double> id_proba: words_count) {
                if (count == 10 || count == num_candidate) {
                    break;
                }
                probability.add((id_proba.get(1)));
                ret.add(words.get(id_proba.get(0).intValue()));
                count ++;
            }
            System.out.println(probability);

            long finish = System.nanoTime();
            long timeElapsed = (finish - start) / 1000000;
            System.out.println("time used to calculate candidates:  " + timeElapsed + " milliseconds");

            return ret;

        }


        public ArrayList<String> get_match_predict(ArrayList<String> words_pre) {
            long start = System.nanoTime();

            // From this, we will decide which gram probability will be used
            int n = words_pre.size() + 1;

            /* "I, want" before we have two words
             *
             * "want"    before we have one word
             *           or two words before doesn't exist in corpus
             * ""        we are at the start of a sentence.
             *           1. No word before
             *           2. ?.;! is detected before
             */
            ArrayList<String> trigram_base;
            ArrayList<String> bigram_base;

            // Store (id, probability) as pair in this list
            ArrayList<ArrayList<Integer>> words_count = new ArrayList<>();
            ArrayList<String> ret = new ArrayList<>();
            ArrayList<String> candidates = new ArrayList<>();

            if (n == 3) {

                trigram_base = new ArrayList<>(words_pre);
                bigram_base = new ArrayList<>();
                bigram_base.add(words_pre.get(1));
                int candidates_id = 0;
                if (this.n_gram_list.get(2).containsKey(trigram_base)) {
                    for (Map.Entry gram: this.n_gram_list.get(3).entrySet()) {
                        ArrayList<String> temp1 = (ArrayList<String>) gram.getKey();
                        Integer temp2 = (Integer) gram.getValue();
                        if (trigram_base.get(0).equals(temp1.get(0)) &&
                                trigram_base.get(1).equals(temp1.get(1))) {
                            candidates.add(temp1.get(2));
                            ArrayList<Integer> candidates_info= new ArrayList<>();
                            candidates_info.add(candidates_id);
                            candidates_info.add(temp2);
                            words_count.add(candidates_info);
                            candidates_id ++;
                        }
                    }
                }
                else if (this.n_gram_list.get(1).containsKey(bigram_base)){

                    for (Map.Entry gram: this.n_gram_list.get(2).entrySet()) {
                        ArrayList<String> temp1 = (ArrayList<String>) gram.getKey();
                        Integer temp2 = (Integer) gram.getValue();
                        if (bigram_base.get(0).equals(temp1.get(0))) {
                            candidates.add(temp1.get(1));
                            ArrayList<Integer> candidates_info = new ArrayList<>();
                            candidates_info.add(candidates_id);
                            candidates_info.add(temp2);
                            words_count.add(candidates_info);
                            candidates_id++;
                        }
                    }
                }
                else {

                    ret.add("is");ret.add("and");ret.add("would"); // hardcoded !!!
                    return ret;
                }
                if (words_count.size() == 0) {
                    ret.add("is");ret.add("and");ret.add("would"); // hardcoded !!!
                    return ret;
                }
            }
            else if (n == 2) {
                bigram_base = new ArrayList<>(words_pre);
                int candidates_id = 0;

                if (this.n_gram_list.get(1).containsKey(bigram_base)){
                    for (Map.Entry gram: this.n_gram_list.get(2).entrySet()) {
                        ArrayList<String> temp1 = (ArrayList<String>) gram.getKey();
                        Integer temp2 = (Integer) gram.getValue();
                        if (bigram_base.get(0).equals(temp1.get(0))) {
                            candidates.add(temp1.get(1));
                            ArrayList<Integer> candidates_info =
                                    new ArrayList<>();
                            candidates_info.add(candidates_id);
                            candidates_info.add(temp2);
                            words_count.add(candidates_info);
                            candidates_id++;
                        }
                    }
                }
                else {
                    ret.add("is");ret.add("and");ret.add("would"); // hardcoded !!!
                    return ret;
                }
            }
            else {
                // We are at the start of a sentence since there is no word before.
                // So we just return the first three words with highest probability
                int count = 0;
                for (Map.Entry ini: this.initial_word.entrySet()) {
                    if (count == 3) {break;}
                    String word_lowercase = (String)ini.getKey();
                    String word_capitalized = word_lowercase.substring(0, 1).
                            toUpperCase() + word_lowercase.substring(1);
                    ret.add(word_capitalized);
                    count ++;
                }
                return ret;
            }
            words_count.sort((ArrayList<Integer> x1, ArrayList<Integer> x2)
                    -> x2.get(1).compareTo(x1.get(1)));
            // The length may be shorter than three
            int count = 0;

            for (ArrayList<Integer> id_freq: words_count) {
                if (count == 3 || count == words_count.size()) {
                    break;
                }
                ret.add(candidates.get(id_freq.get(0)));
                count ++;
            }

            long finish = System.nanoTime();
            long timeElapsed = (finish - start) / 1000000;
            System.out.println("time used to calculate candidates:  " + timeElapsed + " milliseconds");

            return ret;

        }
    }
