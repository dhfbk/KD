package eu.fbk.dh.kd.lib;

import eu.fbk.dh.kd.lib.KD_core.Language;
import eu.fbk.dh.stemmer.snowball.SnowballStemmer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * KD Extractor Class of the tool.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */

class KD_extractor implements Callable<Map<String, KD_keyconcept>> {

    private Logger logger = LoggerFactory.getLogger(KD_extractor.class);

    private List<Resultset_Record> records;
    private Language lang;
    private Integer max_keyword_lenght = 0;
    private Double max_idf_value = 0.0;
    KD_configuration.Group lemmatization;
    private boolean skip_proper_noun = false;
    private boolean skip_keyword_with_propernoun = false;
    private boolean no_idf = false;
    private boolean no_abstract = false;
    private boolean verbose = true;
    private boolean skip_keyword_with_not_allowed_words = false;
    private boolean use_pattern_weight = false;
    private KD_configuration.Tagset tagset;
    private String languagePath = "languages" + File.separator;
    private boolean capitalize_pos = false;
    DB languageDB = null;
    private SnowballStemmer stemmer = null;

    //

    /**
     * <p>Constructor for KD_extractor.</p>
     *
     * @param name          a {@link java.lang.String} object.
     * @param partition     a {@link java.util.List} object.
     * @param l             a {@link eu.fbk.dh.kd.lib.KD_core.Language} object.
     * @param max_idf_value a {@link java.lang.Double} object.
     * @param conf          a {@link eu.fbk.dh.kd.lib.KD_configuration} object.
     * @param langDB        a {@link org.mapdb.DB} object.
     */
    public KD_extractor(String name, List<Resultset_Record> partition, Language l, Double max_idf_value, KD_configuration conf, DB langDB) {
        this.records = partition;
        this.lang = l;
        this.max_keyword_lenght = conf.max_keyword_length;
        this.max_idf_value = max_idf_value;
        this.lemmatization = conf.group_by;
        this.skip_proper_noun = conf.skip_proper_noun;
        this.skip_keyword_with_propernoun = conf.skip_keyword_with_proper_noun;
        this.use_pattern_weight = conf.use_pattern_weight;
        this.no_idf = conf.no_idf;
        this.no_abstract = conf.no_abstract;
        this.languagePath = conf.languagePackPath;
        this.verbose = conf.verbose;
        this.tagset = conf.tagset;
        this.capitalize_pos = conf.capitalize_pos;
        this.skip_keyword_with_not_allowed_words = conf.skip_keyword_with_not_allowed_words;
        languageDB = langDB;


        Class stemClass = null;

        try {
            stemClass = Class.forName("eu.fbk.dh.stemmer.snowball.ext." + lang.toString().toLowerCase() + "Stemmer");
            stemmer = (SnowballStemmer) stemClass.newInstance();
        } catch (Exception e) {
            logger.info("ATTENTION!! Invalid stemmer for language " + lang.toString());
        }


    }


    /**
     * <p>call.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, KD_keyconcept> call() {

        //DB languageDB = DBMaker.newFileDB(new File(this.languagePath + "/" + this.lang + "/" + this.lang + ".map")).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();

        HTreeMap<String, Double> hashLemmaBlackKey = languageDB.hashMap("lemma_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        HTreeMap<String, Double> hashFileLemmata = languageDB.hashMap("lemmata").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        HTreeMap<String, Double> hashPosBlackKey = languageDB.hashMap("pos_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        HTreeMap<String, Double> hashProperNounPosBlackKey = languageDB.hashMap("properNounPos_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        HTreeMap<String, Double> hashFileWhitelist = languageDB.hashMap("whitelist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();


        Pattern p = Pattern.compile("[\\(\\)\\{\\}\\[\\],;*/&!%^|\\+<>←\"λπψιγφ∗βﬄ→δ.:ςˆ~˜θ∈[0-9]]");


        ArrayList<String> capitalized_pos = new ArrayList<String>();
        if (this.capitalize_pos) {
            File f = new File(this.languagePath + File.separator + this.lang + File.separator + "configuration_files" + File.separator + "capitalization_pos.txt");
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                String line = "";
                while ((line = in.readLine()) != null) {
                    capitalized_pos.add(line.trim());
                }
            } catch (Exception e) {
                logger.error("Error reading capitalization_pos.txt file");
            }
        }

        /******************** FIll THE CHIANS IN REVERSE MODE ********************/

        Map<String[], String> possible_pos_chain = new HashMap<String[], String>();
        Map<String[], String> possible_pos_chain_lemma = new HashMap<String[], String>();
        Map<String[], String> possible_pos_chain_token = new HashMap<String[], String>();
        Map<String[], String> possible_pos_chain_stem = new HashMap<String[], String>();
        Map<String[], Double> possible_pos_chain_boost = new HashMap<String[], Double>();

        File f = new File(this.languagePath + File.separator + this.lang + File.separator + "configuration_files" + File.separator + "tagset" + File.separator + this.tagset.name() + File.separator + "patterns.txt");
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line = "";
            while ((line = in.readLine()) != null) {


                if (!line.trim().startsWith("#") && line.trim().length() > 0) {

                    String[] chian_itms = line.replace("\"", "").replace(" ", "").trim().toUpperCase().split(",");
                    ArrayUtils.reverse(chian_itms);
                    Double weight = 0.0;
                    if (this.use_pattern_weight) {
                        weight = Double.parseDouble(chian_itms[0]);
                    }
                    chian_itms = Arrays.copyOfRange(chian_itms, 1, chian_itms.length);


                    if (chian_itms.length <= max_keyword_lenght) {
                        possible_pos_chain.put(chian_itms, "");
                        possible_pos_chain_lemma.put(chian_itms, "");
                        possible_pos_chain_token.put(chian_itms, "");
                        possible_pos_chain_stem.put(chian_itms, "");
                        possible_pos_chain_boost.put(chian_itms, weight);
                    }
                }

            }
            in.close();
        } catch (IOException e) {
            if (this.verbose) {
                logger.error("No Pattern File!!");
            }
            System.exit(1);
        }


        Set<String> allThePosEntries = new HashSet<String>();

        for (String[] pos_items : possible_pos_chain.keySet()) {
            for (String s : pos_items) {
                allThePosEntries.add(s);
            }
        }

        Map<String[], Integer> possible_pos_chain_insertion = new HashMap<String[], Integer>();
        Map<String[], Boolean> possible_pos_chain_touch = new HashMap<String[], Boolean>();

        for (String[] chain : possible_pos_chain.keySet()) {
            possible_pos_chain_insertion.put(chain, chain.length);
        }


        for (String[] chain : possible_pos_chain.keySet()) {
            possible_pos_chain_touch.put(chain, false);
        }

        String pos = "";
        String pos_head3 = "";
        String pos_head2 = "";
        String pos_head = "";

        ConcurrentHashMap<String, KD_keyconcept> expressions_collected = new ConcurrentHashMap<String, KD_keyconcept>();
        //int current_sentence_id = records.get(0).sentence_id;
        int recordset_current_position = -1;
        for (Resultset_Record record : records) {


            recordset_current_position++;
            pos = record.pos;
            if (pos.compareTo(",") == 0) {
                //System.out.print("s");
            }

            if (record.token.length() <= 1) {
                for (String[] chain : possible_pos_chain.keySet()) {
                    possible_pos_chain.put(chain, "");
                    possible_pos_chain_lemma.put(chain, "");
                    possible_pos_chain_token.put(chain, "");
                    possible_pos_chain_stem.put(chain, "");
                    possible_pos_chain_insertion.put(chain, chain.length);
                    possible_pos_chain_touch.put(chain, false);
                }
                continue;
            }


            if (hashPosBlackKey.containsKey(pos)) {
                for (String[] chain : possible_pos_chain.keySet()) {
                    possible_pos_chain.put(chain, "");
                    possible_pos_chain_lemma.put(chain, "");
                    possible_pos_chain_token.put(chain, "");
                    possible_pos_chain_stem.put(chain, "");
                    possible_pos_chain_insertion.put(chain, chain.length);
                    possible_pos_chain_touch.put(chain, false);
                }
                continue;
            }


            Matcher m = p.matcher(record.token);
            // boolean b = m.matches();
            boolean b = m.find();
            if (b) {
                //System.out.println(record.token);
                for (String[] chain : possible_pos_chain.keySet()) {
                    possible_pos_chain.put(chain, "");
                    possible_pos_chain_lemma.put(chain, "");
                    possible_pos_chain_token.put(chain, "");
                    possible_pos_chain_stem.put(chain, "");
                    possible_pos_chain_insertion.put(chain, chain.length);
                    possible_pos_chain_touch.put(chain, false);
                }
                continue;
            }


            String thelemma = "";
            String thestem = "";
            String theExpressionKey = "";
            String theExpressionKeyToken = "";
            String theExpressionKeyLemma = "";
            String theExpressionKeyStem = "";
            for (String[] chain : possible_pos_chain.keySet()) {
                //if (chain[possible_pos_chain_insertion.get(chain) - 1].compareTo(pos_head) == 0 || chain[possible_pos_chain_insertion.get(chain) - 1].compareTo(pos_head2) == 0 || chain[possible_pos_chain_insertion.get(chain) - 1].compareTo(pos_head3) == 0) {
                if (pos.startsWith(chain[possible_pos_chain_insertion.get(chain) - 1])) {

                    possible_pos_chain_touch.put(chain, true);
                    thelemma = (record.lemma.compareTo("__NULL__") == 0 || record.lemma.contains("<") || record.lemma.contains(">")) ? record.token : record.lemma;

                    if (this.capitalize_pos && capitalized_pos.contains(record.pos.toUpperCase())) {
                        record.token = WordUtils.capitalize(record.token);
                        record.lemma = WordUtils.capitalize(record.lemma);
                    } else {
                        record.token = record.token.toLowerCase();
                        record.lemma = record.lemma.toLowerCase();
                    }

                    thelemma = (record.lemma.compareTo("__NULL__") == 0 || record.lemma.contains("<") || record.lemma.contains(">")) ? record.token : record.lemma;


                    try {
                        if (stemmer != null) {
                            stemmer.setCurrent(record.token);
                            stemmer.stem();
                            thestem = stemmer.getCurrent();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    thestem = thestem.length() > 0 ? thestem : record.token;

                    switch (this.lemmatization) {
                        case ALL_LEMMA:
                            possible_pos_chain.put(chain, possible_pos_chain.get(chain) + "÷•÷" + thelemma);
                            break;
                        case BY_LIST:
                            if (hashFileLemmata.containsKey(record.lemma)) {
                                possible_pos_chain.put(chain, possible_pos_chain.get(chain) + "÷•÷" + thelemma);
                            }
                            break;
                        case BY_STEM:
                            if (stemmer == null) {
                                logger.error("invalid stemmer");
                                System.exit(1);
                            }
                            possible_pos_chain.put(chain, possible_pos_chain.get(chain) + "÷•÷" + thestem);
                            break;
                        default:
                            possible_pos_chain.put(chain, possible_pos_chain.get(chain) + "÷•÷" + record.token);
                            break;
                    }

                    possible_pos_chain_token.put(chain, possible_pos_chain_token.get(chain) + "÷•÷" + record.token);
                    possible_pos_chain_lemma.put(chain, possible_pos_chain_lemma.get(chain) + "÷•÷" + thelemma);
                    possible_pos_chain_stem.put(chain, possible_pos_chain_stem.get(chain) + "÷•÷" + thestem);
                    possible_pos_chain_insertion.put(chain, possible_pos_chain_insertion.get(chain) - 1);


                    if (possible_pos_chain_insertion.get(chain) == 0) {
                        // only unigram rules
                        if (((hashLemmaBlackKey.containsKey(record.lemma) || (skip_proper_noun && hashProperNounPosBlackKey.containsKey(pos.toLowerCase())) || record.token.endsWith(".")) && chain.length == 1) && !hashFileWhitelist.containsKey(record.token.toLowerCase().trim())) {

                        } else {

                            // multiword rules
                            if ((skip_keyword_with_propernoun && hashProperNounPosBlackKey.containsKey(pos.toLowerCase())) && !hashFileWhitelist.containsKey(record.token.toLowerCase().trim())) {

                            } else {

                                theExpressionKey = possible_pos_chain.get(chain);
                                theExpressionKeyLemma = possible_pos_chain_lemma.get(chain);
                                theExpressionKeyStem = possible_pos_chain_stem.get(chain);
                                theExpressionKeyToken = possible_pos_chain_token.get(chain);


                                switch (this.lemmatization) {
                                    case ALL_LEMMA:
                                        theExpressionKey = theExpressionKeyLemma;
                                        break;
                                    case BY_LIST:
                                        if (hashFileLemmata.containsKey(record.lemma)) {
                                            theExpressionKey = record.lemma;
                                        }
                                        break;
                                    default:
                                        break;
                                }

                                if (expressions_collected.containsKey(theExpressionKey)) {
                                    KD_keyconcept expVal = expressions_collected.get(theExpressionKey);
                                    expVal.incrementFrequency();
                                    expVal.addCurrentPosition(record.token_number);
                                    expVal.addCurrentSentence(record.sentence_id);

                                    expVal.add_variation(theExpressionKeyToken);
                                    expressions_collected.put(theExpressionKey, expVal);
                                } else {
                                    KD_keyconcept expVal = new KD_keyconcept();
                                    expVal.frequency = 1;
                                    expVal.addCurrentPosition(record.token_number);
                                    expVal.setPosChain(chain);
                                    expVal.cleanup(theExpressionKey);
                                    expVal.cleanupLemma(theExpressionKeyLemma);
                                    expVal.cleanupStem(theExpressionKeyStem);
                                    expVal.cleanupToken(theExpressionKeyToken);

                                    expVal.add_variation(theExpressionKeyToken);
                                    //expVal.setStem(stemmatizeArray(expVal.getTokenArray()));
                                    expVal.addCurrentSentence(record.sentence_id);
                                    if (expVal.chainlenght == 1 && record.token.length() <= 5) {
                                        if (KD_utils.countUppercase(record.token) > 1) {
                                            expVal.isAcronym = true;
                                        }
                                    }
                                    expVal.patternScoreBoost += possible_pos_chain_boost.get(chain);
                                    expressions_collected.put(theExpressionKey, expVal);
                                }
                            }
                        }
                        possible_pos_chain.put(chain, "");
                        possible_pos_chain_lemma.put(chain, "");
                        possible_pos_chain_stem.put(chain, "");
                        possible_pos_chain_token.put(chain, "");
                        possible_pos_chain_insertion.put(chain, chain.length);
                        possible_pos_chain_touch.put(chain, false);

                    }
                } else {
                    possible_pos_chain_touch.put(chain, false);
                }

            }

            for (String[] chain_to_be_clear : possible_pos_chain_touch.keySet()) {
                if (!possible_pos_chain_touch.get(chain_to_be_clear)) {
                    possible_pos_chain.put(chain_to_be_clear, "");
                    possible_pos_chain_lemma.put(chain_to_be_clear, "");
                    possible_pos_chain_stem.put(chain_to_be_clear, "");
                    possible_pos_chain_token.put(chain_to_be_clear, "");
                    possible_pos_chain_insertion.put(chain_to_be_clear, chain_to_be_clear.length);
                }
            }

            if ((recordset_current_position + 1) < records.size()) {
                Resultset_Record next = records.get(recordset_current_position + 1);


                if (record.sentence_id.intValue() != next.sentence_id.intValue()) {
                    for (String[] chain : possible_pos_chain.keySet()) {
                        possible_pos_chain.put(chain, "");
                        possible_pos_chain_lemma.put(chain, "");
                        possible_pos_chain_stem.put(chain, "");
                        possible_pos_chain_token.put(chain, "");
                        possible_pos_chain_insertion.put(chain, chain.length);
                        possible_pos_chain_touch.put(chain, false);
                    }
                }
            }


        }

        //this.records.clear();
        this.records = new LinkedList<Resultset_Record>();

        HTreeMap<String, Double> hashFileBlacklist = languageDB.hashMap("blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();


        ArrayList<String> banned = new ArrayList<>();


        for (Object tobeBanned : hashFileBlacklist.keySet()) {
            // System.out.println((String)tobeBanned);


            expressions_collected.remove((String) tobeBanned);
            if (skip_keyword_with_not_allowed_words) {
                for (String expKey : expressions_collected.keySet()) {
                    if (expressions_collected.get(expKey).getTokenArray().contains(((String) tobeBanned).replace("÷•÷", "").toLowerCase())) {
                        banned.add(expKey);
                    }
                    if (expressions_collected.get(expKey).getLemmaArray().contains(((String) tobeBanned).replace("÷•÷", "").toLowerCase())) {
                        banned.add(expKey);
                    }
                   // System.out.println(((String) tobeBanned));
                   // System.out.println(expressions_collected.get(expKey).getString().toLowerCase());

                    // uncomment to remove keyword string containing the blacklisted keyword
                    /*if (expressions_collected.get(expKey).getString().toLowerCase().contains(((String) tobeBanned).replace("÷•÷", "") )){
                       banned.add(expKey);
                   }*/
                }
            }
        }



        /*
        if (skip_keyword_with_not_allowed_words){
            ArrayList<String> banned = new ArrayList<>();
            for (String tobeBanned : expressions_collected.keySet()) {
                if (expressions_collected.get(tobeBanned).getTokenArray().contains(tobeBanned.toLowerCase()) && expressions_collected.get(tobeBanned).getTokenArray().size() > 1 ){
                    banned.add(tobeBanned);
                }
            }
            for (String b: banned){
                expressions_collected.remove(b);
            }
        }
        */


        for (String b : banned) {
            expressions_collected.remove(b);
        }
        //DB inveDocFreQLanguageDB = DBMaker.newFileDB(new File(this.languagePath+this.lang+"/invdocfreq.map")).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        HTreeMap<String, Double> hashFileKey = languageDB.hashMap("invdocfreq").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();

        for (Map.Entry<String, KD_keyconcept> entry : expressions_collected.entrySet()) {

            //shift the keyoword position in the text to the head of the keyword

            if (entry.getValue().elements.size() > 1) {
                for (int pos_idx = 0; pos_idx < entry.getValue().position_in_text.size(); pos_idx++) {
                    entry.getValue().position_in_text.set(pos_idx, entry.getValue().position_in_text.get(pos_idx) - entry.getValue().elements.size() + 1);
                }
            }


            //idf computation based on language
            if (!no_idf) {
                Double d = 0.0;
                for (String elem : entry.getValue().elementsLemma) {
                    if (hashFileKey.get(elem) != null) {
                        if (hashFileKey.get(elem) > d) {
                            d = hashFileKey.get(elem);
                        }
                    }
                }

                if (hashFileKey.get(entry.getValue().getString()) != null) {
                    if (hashFileKey.get(entry.getValue().getString()) > d) {
                        d = hashFileKey.get(entry.getValue().getString());
                    }
                }

                if (d != 0.0) {
                    entry.getValue().idf = 1 + (d / Math.pow(max_idf_value, 3));
                } else {
                    if (max_idf_value > 1) {
                        entry.getValue().idf = 1 + (Math.log(max_idf_value * 2) / Math.log(max_idf_value * 2));
                    } else {
                        entry.getValue().idf = 1.0;
                    }
                }
            } else {
                entry.getValue().idf = 1.0;
            }

            //score boosting based on suffix
            if (!no_abstract) {
                double scoreBoost = 0.0;
                String en_abs_suffix = ".*(tion|ism|ity|ment|ness|age|ance|ence|ship|ability|acy)$";
                String it_abs_suffix = ".*(ismo|esimo|eria|ezza|izia|ità|età|itudine|anza|enza)$";

                String abs_suffix = en_abs_suffix;

                if (this.lang == Language.ITALIAN) {
                    abs_suffix = it_abs_suffix;
                }

                for (String element : entry.getValue().elements) {
                    if (element.matches(abs_suffix)) {
                        scoreBoost += 1.1;// + Math.log(entry.getValue().elements.size());
                        entry.getValue().isAbstract = true;
                    }
                }
                entry.getValue().scoreBoost += scoreBoost;
            } else {
                entry.getValue().scoreBoost = 1.0;
            }


        }

        //inveDocFreQLanguageDB.close();

        //hashLemmaBlackKey.close();
        //hashFileLemmata.close();
        //hashPosBlackKey.close();
        //hashProperNounPosBlackKey.close();


        if (this.verbose) {
            logger.info("x");
        }
        return expressions_collected;

    }

}
