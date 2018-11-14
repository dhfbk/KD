package eu.fbk.dh.kd.lib;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class represents the configuration object used by the KD Core
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class KD_configuration {


    /**
     * Default location for the languages folder
     */
    protected final static String default_language_pack_location = System.getProperty("user.home") + File.separator + ".kd"+ File.separator + "languages" + File.separator;


    /**
     * Max keyword length allowed during the extraction process.
     * By default is set to 4
     */
    public Integer max_keyword_length = 4;

    /**
     * Number of keyconcepts to be retrieved by the KD Core
     * By default is set to -1 that implies the extraction of all keyconcepts present in the ranked list.
     */
    public Integer numberOfConcepts = -1;
    /**
     * The frequency threshold for multiword recognition at document level: a multiword can (but is not necessarily) recognized in a text, if it occurs in a reference corpus at least the number of times specified by this option.
     * In other words is the threshold under which the keyconcept are discarded.
     * By default is set to 2.
     */
    public Integer local_frequency_threshold = 2;
    /**
     * This option tunes the re-ranking mechanism that tends to raise the position of more specific (longer) key-concepts. The option has 5 possible values: no, weak, medium, strong, max (No By default)
     */
    public Prefer_Specific_Concept prefer_specific_concept = Prefer_Specific_Concept.NO;
    /**
     * If you use this option, KD will re-rank key-concepts by raising concepts that appear early in the text. (default true)
     */
    public boolean rerank_by_position = true;
    /**
     * Configuration variables used by KD
     */
    public Group group_by = Group.NONE;
    /**
     * If you use this option, the tool discards the proper noun identified by the part of speech. The part of speech of the proper noun can be specified in the properNounPosList file.
     */
    public boolean skip_proper_noun = false;
    /**
     * If you use this option, the tool discards the key-concepts with inside proper noun identified by the part of speech. The part of speech of the proper noun can be specified in the properNounPosList file.
     */
    public boolean skip_keyword_with_proper_noun = false;

    /**
     * If you use this option, the tool discards the key-concepts with inside an item contained in the keyconcept-no file.
     */
    public boolean skip_keyword_with_not_allowed_words = false;


    /**
     * Parameter use by the KD core to choose the column configuration of the file
     * Default is TOKEN_POS_LEMMA
     */
    public ColumExtraction column_configuration = ColumExtraction.TOKEN_POS_LEMMA;

    /**
     * If you use this option, the tool extracts only the multi-word (multi-token) concept.
     */
    public boolean only_multiword = false;
    /**
     * KD does not tries to recognize any synonymous phrases specified in the synonyms list.
     */
    public boolean no_syn = false;
    /**
     * Disables the re-ranking of key-concepts based on their first occurrence in the text.
     */
    public boolean no_rerank = false;


    /**
     * Disables or enables the frequency re-computation on shorter/longer key-phrases.
     */
    public boolean skipFrequencyAbsorption = false;


    /**
     * Disables or enables the verbose mode.(Disable by default)
     */
    public boolean verbose = false;

    /**
     * Do not use any kind of IDF information
     * (even if the domain  corpus, or docset  corpus options are specified]
     */
    public boolean no_idf = false;
    /**
     * Do not use any boost for any abstract concept suffix
     *
     */
    public boolean no_abstract = false;

    /**
     * Force to include in the results the multi-words with an abstract concept inside
     *
     */
    public boolean force_abstract = false;


    /**
     * specify the token column position (in case of CUSTOM ColumExtraction option)
     *
     */
    public Integer token_position = 0;
    /**
     * specify the lemma column position (in case of CUSTOM ColumExtraction option)
     *
     */
    public Integer lemma_position = 0;
    /**
     * specify the pos column position (in case of CUSTOM ColumExtraction option)
     *
     */
    public Integer pos_position = 0;


    /**
     * Specify the custom language folder path.
     * If it is not specified the current working directory is taken by default.
     *
     */
    public String languagePackPath = this.default_language_pack_location;

    /**
     * use weight of pattern (default false)
     *
     */
    public boolean use_pattern_weight = false;

    /**
     * capitalize the pos contained in the configuration file.
     *
     */
    public boolean capitalize_pos = false;

    /**
     * boost acronyms in scientific articles (defalut false)
     *
     */
    public boolean boost_acronyms = false;



    /**
     * Parameter use by the KD core to choose the pos tagger tagset.
     * Default is TEXTPRO.
     *
     */
    public Tagset tagset = Tagset.TEXTPRO;

    /**
     * Enum Type that setups the columns configuration in the input file.
     * It is possible to choose among:
     * <ul>
     * <li>TOKEN_LEMMA_POS (Token in column 0, Lemma in column 1 and pos in column 2)</li>
     * <li>TOKEN_POS_LEMMA (Token in column 0, Lemma in column 2 and pos in column 1)</li>
     * <li>CUSTOM if this option has been selected the KD Core takes in account the token_position,lemma_position,pos_position configuration variables.</li>
     * </ul>
     *
     * @author Giovanni Moretti - DH Group FBK.
     */
    public enum ColumExtraction {
        /**
         * TOKEN_LEMMA_POS (Token in column 0, Lemma in column 1 and pos in column 2)
         */
        TOKEN_LEMMA_POS,
        /**
         * TOKEN_POS_LEMMA (Token in column 0, Lemma in column 2 and pos in column 1)
         */
        TOKEN_POS_LEMMA,
        /**
         * CUSTOM if this option has been selected the KD Core takes in account the token_position,lemma_position,pos_position configuration variables
         */
        CUSTOM
    }


    public Path getPath(){
        return Paths.get(this.languagePackPath);
    }


    /**
     * Enum Type that setups the tagset used in the preprocessed file.
     * It is possible to choose among:
     * <ul>
     * <li>TREETAGGER</li>
     * <li>STANFORD</li>
     * <li>TEXTPRO</li>
     * <li>CUSTOM</li>
     * </ul>
     * Default is TEXTPRO.
     * The CUSTOM option can be used to create another setup for an additional tagset.
     *
     * @author Giovanni Moretti - DH Group FBK.
     */
    public enum Tagset {
        TEXTPRO, STANFORD,TREETAGGER, CUSTOM;

        private String custom_tagset = "CUSTOM";

        /**
         * Specify the tagset name to be chosen in the languages folder.
         * Please note that this value is taking in account only if the Tagset object is declared as "CUSTOM"
         *
         * @param name The language name as string.
         */
        public void set_Custom_Tagset(String name) {
            this.custom_tagset = name;
        }

        //public String get_Custom_Language(){
        //    return this.custom_lang;
        //}

        @Override
        public String toString() {
            if (this != CUSTOM){
                return this.name();
            }else{
                return this.custom_tagset;
            }

        }
    }

    /**
     * Enum Type that setups the lemmatization approach of the tool (Experimental).
     * It is possible to choose among:
     * <ul>
     * <li>ALL - Collapses all the key-phrases into the corresponding lemmas </li>
     * <li>BY_LIST - Collapses all the key-phrases into the corresponding lemmas in alist available as configuration files</li>
     * <li>BY_STEM - Collapses all the key-phrases into the corresponding stems (Coming soon) </li>
     * <li>NONE - No Group.</li>
     * </ul>
     * Default is NONE
     * @author Giovanni Moretti - DH Group FBK.
     */
    public enum Group {
        /**
         * Collapses all the key-phrases into the corresponding lemmas
         */
        ALL_LEMMA,
        /**
         * Collapses all the key-phrases into the corresponding lemmas in a list available as configuration files
         */
        BY_LIST,
        /**
         * Collapses all the key-phrases into the corresponding stems (coming soon)
         */
        BY_STEM,
        /**
         * No Grouping
         */
        NONE
    }


    /**
     * Enum Type that gives more relevance to specific(longer) key-phrases.
     * It is possible to choose among:
     * <ul>
     * <li>NO - no boost for the longer key-phrases</li>
     * <li>WEAK - slightly boost for the longer key-phrases based only on the length</li>
     * <li>MEDIUM - medium boost with partial absorption of the score into the longer key-phrases</li>
     * <li>STRONG - strong boost with complete absorption of the score into the longer key-phrases </li>
     * <li>MAX - max boost includes all the other boosts operation iterated two times </li>
     * </ul>
     * Default is NONE
     * @author Giovanni Moretti - DH Group FBK.
     */
    public enum Prefer_Specific_Concept {
        /**
         * No boost for the longer key-phrases
         */
        NO,
        /**
         * Slightly boost for the longer key-phrases based only on the length
         */
        WEAK,
        /**
         * Medium boost with partial absorption of the score into the longer key-phrases
         */
        MEDIUM,
        /**
         * Strong boost with complete absorption of the score into the longer key-phrases
         */
        STRONG,
        /**
         * Max boost includes all the other boosts operation iterated two times
         */
        MAX
    }

    /**
     * <p>getDefault_laguage_pack_localtion.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getDefault_laguage_pack_localtion (){
        return  default_language_pack_location;
    }



}
