package eu.fbk.dh.kd.lib;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Object representing a single key-phrase with all it's attributes
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class KD_keyconcept {
    String value;
    /**
     * int value representing the number of occurrences of this key-phrase in the document
     */
    public int frequency = 0;
    /**
     * double values representing the score gives by the tool to the current key-phrase
     */
    public double score = 0;
    /**
     * double values representing the normalized score from 0 to 1 gives by the tool to the current key-phrase
     */
    public double normalized_score = 0;

    int chainlenght = 0;
    Double scoreBoost = 1.0;
    Double patternScoreBoost = 0.0;

    Double idf = 1.0;

    //Set<String> collected_variation = new HashSet<String>();
    Map<String, Integer> collected_variation = new HashMap<>();


    ArrayList<Integer> position_in_text = new ArrayList<Integer>();
    ArrayList<Integer> sentences_in_text = new ArrayList<Integer>();
    ArrayList<String> elements = new ArrayList<String>();
    ArrayList<String> elementsLemma = new ArrayList<String>();
    ArrayList<String> elementsToken = new ArrayList<String>();
    ArrayList<String> elementsStem = new ArrayList<String>();
    List<String> elementsPos = new ArrayList<String>();
    boolean frequencyrecounted = false;
    ArrayList<String> synonyms = new ArrayList<String>();
    boolean isAcronym = false;
    boolean isAbstract = false;

    //public boolean already_included = false;

    void appendToValue(String s) {
        this.value += " " + s;
    }


    /**
     * increments the frequency of this key-phrase by one
     */
    void incrementFrequency() {
        this.frequency++;
    }


    void setPosChain(String[] chain) {
        this.elementsPos = Arrays.asList(chain);
    }


    void cleanup(String s) {
        String[] tokens = s.split("÷•÷");
        for (String tkn : tokens) {
            if (tkn.trim().length() > 0) {
                elements.add(tkn.trim());
            }
        }
        this.chainlenght = elements.size();
    }

    void cleanupLemma(String s) {
        String[] lemmas = s.split("÷•÷");
        for (String lemma : lemmas) {
            if (lemma.trim().length() > 0) {
                elementsLemma.add(lemma.trim());
            }
        }
        //this.chainlenght = elements.size();
    }

    void cleanupStem(String s) {
        String[] stems = s.split("÷•÷");
        for (String stem : stems) {
            if (stem.trim().length() > 0) {
                elementsStem.add(stem.trim());
            }
        }
        //this.chainlenght = elements.size();
    }

    void cleanupToken(String s) {
        String[] tokenss = s.split("÷•÷");
        for (String tok : tokenss) {
            if (tok.trim().length() > 0) {
                elementsToken.add(tok.trim());
            }
        }
        //this.chainlenght = elements.size();
    }

    void add_variation(String s) {
        if (!collected_variation.containsKey(s.toLowerCase())) {
            collected_variation.put(s.toLowerCase(), 1);
        } else {
            collected_variation.put(s.toLowerCase(), collected_variation.get(s.toLowerCase()) + 1);
        }

    }


    /**
     * merges this object with the other specified by the parameter and returns a new KD_keyconcept with merged values
     *
     * @param o the other KD_keyconcept to be merged
     * @return KD_keyconcept
     */
    KD_keyconcept mergeValue(KD_keyconcept o) {
        this.frequency += o.frequency;
        this.position_in_text.addAll(o.position_in_text);
        this.sentences_in_text.addAll(o.sentences_in_text);

        this.collected_variation = Stream.of(this.collected_variation, o.collected_variation)
                .map(Map::entrySet)          // converts each map into an entry set
                .flatMap(Collection::stream) // converts each set into an entry stream, then
                // "concatenates" it in place of the original set
                .collect(
                        Collectors.toMap(        // collects into a map
                                Map.Entry::getKey,   // where each entry is based
                                Map.Entry::getValue, // on the entries in the stream
                                Integer::sum         // such that if a value already exist for
                                // a given key, the max of the old
                                // and new value is taken
                        )
                )
        ;

        //this.collected_variation.addAll(o.collected_variation);
        return this;
    }


    void calculateNormFreqByToken(Integer tot_tokens) {
        this.score = (frequency / (double) tot_tokens) * 1000;
    }

    void calculateNormFreqBySentence(Integer tot_sentences) {
        this.score = (frequency / (double) tot_sentences) * 1000;
    }

    /**
     * Adds new position in the text of this key-phrase
     *
     * @param i - the postion in the text to be added
     */
    void addCurrentPosition(Integer i) {
        this.position_in_text.add(i);
    }


    void addCurrentSentence(Integer i) {
        this.sentences_in_text.add(i);
    }

    /**
     * Get the string of current key-phrase
     *
     * @return String containing the current key-phrase
     */
    public String getString() {
        return Joiner.on(" ").join(elements);
    }


    /**
     * Get string containing the comma separated list of synonyms
     *
     * @return String containing the comma separated list of synonyms
     */
    public String getSysnonyms() {
        return Joiner.on(", ").join(synonyms);
    }

    /**
     * Get ArrayList containing the list of synonyms
     *
     * @return ArrayList containing the list of synonyms
     */
    public ArrayList<String> getSysnonymsArray() {
        return synonyms;
    }

    /**
     * Get a list containing the position of all key-phrase heads (first token of the key-phrase)
     *
     * @return ArrayList containing the heads of the key-phrase
     */
    public ArrayList<Integer> getHeadOfRetrievedKey() {
        return this.position_in_text;
    }

    /**
     * Get the stems of current key-phrase
     *
     * @return String containing the stems of the key-phrase
     */
    public String getStemString() {
        return Joiner.on(" ").join(elementsStem);
    }

    /**
     * Get the token of current key-phrase
     *
     * @return String containing the tokens of the key-phrase
     */
    public String getTokenString() {
        return Joiner.on(" ").join(elementsToken);
    }

    /**
     * Get the lemma of current key-phrase
     *
     * @return String containing the lemmas of the key-phrase
     */
    public String getLemmaString() {
        return Joiner.on(" ").join(elementsLemma);
    }


    /**
     * Get the lemma array of current key-phrase
     *
     * @return Array containing the lemmas of the key-phrase
     */
    public ArrayList<String> getLemmaArray() {
        return this.elementsLemma;
    }

    /**
     * Get the stems array of current key-phrase
     *
     * @return Array containing the stems of the key-phrase
     */
    public ArrayList<String> getStemArray() {
        return this.elementsStem;
    }

    /**
     * Get the token array of current key-phrase
     *
     * @return Array containing the tokens of the key-phrase
     */
    public ArrayList<String> getTokenArray() {
        return this.elementsToken;
    }

    /**
     * Get the pos list of current key-phrase
     *
     * @return List containing the pos of the key-phrase
     */
    public List<String> getPosList() {
        return Lists.reverse(this.elementsPos);
    }


    /**
     * Get the idf value of  current key-phrase
     *
     * @return Double containing the idf of the key-phrase
     */
    public Double getIdf() {
        return idf;
    }


    /**
     * Get the scoreBoost value of current key-phrase
     *
     * @return Double containing the boost of the key-phrase
     */
    public Double getScoreBoost() {
        return this.scoreBoost;
    }


    /**
     * Get the pattern boost value of current key-phrase
     *
     * @return Double containing the pattern boost of the key-phrase
     */
    public Double getPatternBoost() {
        return this.patternScoreBoost;
    }


    /**
     * Get the length of current key-phrase
     *
     * @return Integer containing the length of the key-phrase
     */
    public Integer getTokenChainLength() {
        return this.chainlenght;
    }


    public static String getArrayString(ArrayList<String> array) {
        return Joiner.on(" ").join(array);
    }


    public ArrayList<String> getMostUsedVariation() {

        ArrayList<String> variation = new ArrayList<>();

        String out = "";

        Optional<Map.Entry<String, Integer>> first = this.collected_variation.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(1)
                .findFirst();
        if (first.isPresent()) {
            out = first.get().getKey();

            String[] tokenss = out.split("÷•÷");
            for (String tok : tokenss) {
                if (tok.trim().length() > 0) {
                    variation.add(tok.trim());
                }
            }
        }
        return variation;
    }


    /**
     * Get all the variations colleted in the key-phrase extraction
     *
     * @return String
     */
    public ArrayList<ArrayList<String>> getAllTheVariationsArray() {
        ArrayList<ArrayList<String>> variations = new ArrayList<ArrayList<String>>();


        for (String var : this.collected_variation.keySet()) {
            ArrayList<String> variation = new ArrayList<String>();

            String[] tokenss = var.split("÷•÷");
            for (String tok : tokenss) {
                if (tok.trim().length() > 0) {
                    variation.add(tok.trim());
                }
            }
            variations.add(variation);
        }
        return variations;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        Collections.sort(position_in_text);
        return getTokenString() + "\t" + getLemmaString() + "\t" + getStemString() + "\t" + "(" + Joiner.on(", ").join(synonyms) + ")\tfrequency: " + frequency + " ,score: " + score + " idf: " + idf + " boost " + scoreBoost + " pattern_boost " + patternScoreBoost + " chain_l " + chainlenght;


        //+" located in: " + position_in_text;

        //return Joiner.on(" ").join(elements) + "["+Joiner.on(" ").join(elementsLemma)+"] (" + Joiner.on(", ").join(synonyms) + ")\tfrequency: " + frequency + " ,score: " + score + " idf: " + idf + " boost " + scoreBoost + " chain_l " + chainlenght;//+" located in: " + position_in_text;
    }

}
