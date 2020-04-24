package eu.fbk.dh.kd.annotator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovannimoretti on 24/05/16.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class DigiKDResult {

    String keyphrase;
    Integer frequency;
    Double score;
    Double idf;
    Double score_boost;
    Double pattern_boost;
    Integer chain_length;
    ArrayList<String> lemmas = new ArrayList<>();
    ArrayList<String> stems = new ArrayList<>();
    ArrayList<String> synonyms = new ArrayList<>();
    ArrayList<String> tokens = new ArrayList<>();
    List<String> posList = new ArrayList<>();

    /**
     * <p>Constructor for DigiKdResult.</p>
     *
     * @param keyphrase a {@link java.lang.String} object.
     * @param frequency a {@link java.lang.Integer} object.
     * @param score a {@link java.lang.Double} object.
     * @param lemmas a {@link java.util.ArrayList} object.
     * @param tokens a {@link java.util.ArrayList} object.
     * @param posList a {@link java.util.List} object.
     * @param stems a {@link java.util.List} object.
     * @param synonyms a {@link java.util.List} object.
     * @param idf a {@link java.lang.Double} object.
     * @param score_boost a {@link java.lang.Double} object.
     * @param pattern_boost a {@link java.lang.Double} object.
     * @param chain_length a {@link java.lang.Integer} object.
     */
    public DigiKDResult(String keyphrase, Integer frequency, Double score, ArrayList<String> lemmas,
                        ArrayList<String> tokens, List<String> posList, ArrayList<String> stems, ArrayList<String> synonyms, Double idf, Double score_boost, Double pattern_boost, Integer chain_length) {
        this.keyphrase = keyphrase;
        this.frequency = frequency;
        this.score = score;
        this.lemmas = lemmas;
        this.tokens = tokens;
        this.posList = posList;
        this.stems = stems;
        this.synonyms = synonyms;
        this.idf = idf;
        this.score_boost =score_boost;
        this.chain_length = chain_length;
        this.pattern_boost = pattern_boost;

    }

    /**
     * <p>Setter for the field <code>posList</code>.</p>
     *
     * @param posList a {@link java.util.List} object.
     */
    public void setPosList(List<String> posList) {
        this.posList = posList;
    }

    /**
     * <p>Getter for the field <code>posList</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getPosList() {

        return posList;
    }

    /**
     * <p>Getter for the field <code>keyphrase</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKeyphrase() {
        return keyphrase;
    }

    /**
     * <p>Setter for the field <code>keyphrase</code>.</p>
     *
     * @param keyphrase a {@link java.lang.String} object.
     */
    public void setKeyphrase(String keyphrase) {
        this.keyphrase = keyphrase;
    }

    /**
     * <p>Getter for the field <code>frequency</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getFrequency() {
        return frequency;
    }

    /**
     * <p>Setter for the field <code>frequency</code>.</p>
     *
     * @param frequency a {@link java.lang.Integer} object.
     */
    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    /**
     * <p>Getter for the field <code>score</code>.</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getScore() {
        return score;
    }

    /**
     * <p>Setter for the field <code>score</code>.</p>
     *
     * @param score a {@link java.lang.Double} object.
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * <p>Setter for the field <code>lemmas</code>.</p>
     *
     * @param lemmas a {@link java.util.ArrayList} object.
     */
    public void setLemmas(ArrayList<String> lemmas) {
        this.lemmas = lemmas;
    }

    /**
     * <p>Setter for the field <code>tokens</code>.</p>
     *
     * @param tokens a {@link java.util.ArrayList} object.
     */
    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    /**
     * <p>Getter for the field <code>lemmas</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<String> getLemmas() {
        return lemmas;
    }




    /**
     * <p>Getter for the field <code>tokens</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<String> getTokens() {
        return tokens;
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return keyphrase + "\t" + "<" + frequency + "," + score + ">";
    }

    public ArrayList<String> getStems() {
        return stems;
    }

    public ArrayList<String> getSynonyms() {
        return synonyms;
    }

    public Double getIdf() {
        return idf;
    }

    public Double getScore_boost() {
        return score_boost;
    }

    public Double getPattern_boost() {
        return pattern_boost;
    }

    public Integer getChain_length() {
        return chain_length;
    }
}
