
package eu.fbk.dh.kd.lib;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import eu.fbk.dh.kd.lib.KD_concept_sorter.Sort;
import eu.fbk.dh.kd.lib.KD_concept_sorter.SortDirection;
import eu.fbk.dh.kd.lib.KD_configuration.ColumExtraction;
import eu.fbk.dh.kd.lib.KD_rerank_methods.Method;
import eu.fbk.dh.kd.models.KD_Model;
import org.apache.commons.math.util.MathUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.lucene.util.IOUtils.close;


/**
 * Core Class of the tool.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class KD_core {


    private static final Logger LOGGER = LoggerFactory.getLogger(KD_core.class);

    private KD_Model model = new KD_Model(FileSystems.getDefault().getPath(KD_configuration.getDefault_laguage_pack_localtion()));


    // version method

    /**
     * Get the current version of the tool
     *
     * @return String with the current version of the tool
     */
    public static String getVersion() {
        return KD_core.class.getPackage().getImplementationTitle() + " - " + KD_core.class.getPackage().getImplementationVendor() + " (" + KD_core.class.getPackage().getImplementationVersion() + ") developed by Giovanni Moretti";
    }


    private Logger logger = LoggerFactory.getLogger(KD_core.class);

    Integer threadsNumber = 2;

    /**
     * Enum Type that setups the language of the input files.
     * It is possible to choose among:
     * <ul>
     * <li>ITALIAN</li>
     * <li>ENGLISH</li>
     * <li>CUSTOM</li>
     * </ul>
     * Default is ENGLISH.
     * The CUSTOM option can be used to create another setup for an additional language.
     */
    public enum Language {


        /**
         * Set the italian language
         */
        ITALIAN,
        /**
         * Set the english language
         */
        ENGLISH,
        /**
         * The CUSTOM option can be used to create another setup for an additional language
         */
        CUSTOM;

        private String custom_lang = "CUSTOM";

        /**
         * Specify the language name to be chosen in the languages folder.
         * Please note that this value is taking in account only if the Language object is declared as "CUSTOM"
         *
         * @param name The language name as string.
         */
        public void set_Custom_Language(String name) {
            this.custom_lang = name;
        }

        //public String get_Custom_Language(){
        //    return this.custom_lang;
        //}

        @Override
        public String toString() {
            if (this != CUSTOM) {
                return this.name();
            } else {
                return this.custom_lang;
            }

        }
    }

    /**
     * Enum Type that setups the number of threads used by the tool.
     * It is possible to choose among:
     * <ul>
     * <li>ONE</li>
     * <li>TWO</li>
     * <li>FOUR</li>
     * <li>SIX</li>
     * <li>EIGHT</li>
     * <li>TEN</li>
     * <li>TWELVE</li>
     * </ul>
     * Default is TWO.
     * If the document is short the tool scales to ONE.
     */
    public enum Threads {
        ONE, TWO, FOUR, SIX, EIGHT, TEN, TWELVE
    }


    //private String input_type, inFile, outputDir, input, language, domain, prefer_specific_concepts, param, max_ngram_length, domain_lists, domain_corpus, docset_corpus, ngr_dir, doc_ngr, doc_colloc, indir, outdir, encoding;
    //private boolean debug, verbose, trace, help, version, noidf, no_language_idf, no_domain_idf, expand, nocolloc, no_corpus_colloc, no_domain_colloc, no_filter_colloc, nosyn, nostop, notoken, no_rerank_by_position;
    //private Integer n, recycle, corpus_frequency_threshold, local_frequency_threshold;


    private DB languageDB = null;
    private HTreeMap<String, Double> hashLemmaBlackKey = null;
    private HTreeMap<String, Double> hashFileLemmata = null;
    private HTreeMap<String, Double> hashPosBlackKey = null;
    private HTreeMap<String, Double> hashProperNounPosBlackKey = null;
    private HTreeMap<String, Double> hashFileKey = null;
    private HTreeMap<ArrayList<String>, Integer> hashFileStoplistDB = null;
    private HTreeMap<ArrayList<String>, Integer> hashFileSynonyms = null;

    /**
     * Creates an instance of the KD tool with t number of threads
     *
     * @param t Thread Number
     */
    public KD_core(Threads t) {
        switch (t) {
            case ONE:
                threadsNumber = 1;
                break;
            case TWO:
                threadsNumber = 2;
                break;
            case FOUR:
                threadsNumber = 4;
                break;
            case SIX:
                threadsNumber = 6;
                break;
            case EIGHT:
                threadsNumber = 8;
                break;
            case TEN:
                threadsNumber = 10;
                break;
            case TWELVE:
                threadsNumber = 12;
                break;
            default:
                threadsNumber = 2;
                break;
        }

    }


    /**
     * Creates an instance of the KD tool with t number of threads , with lang Language and configuration KD_configuration
     *
     * @param t             Thread Number
     * @param lang          KD Language object
     * @param configuration KD_configuration object
     */
    public KD_core(Threads t, Language lang, KD_configuration configuration) {
        switch (t) {
            case ONE:
                threadsNumber = 1;
                break;
            case TWO:
                threadsNumber = 2;
                break;
            case FOUR:
                threadsNumber = 4;
                break;
            case SIX:
                threadsNumber = 6;
                break;
            case EIGHT:
                threadsNumber = 8;
                break;
            case TEN:
                threadsNumber = 10;
                break;
            case TWELVE:
                threadsNumber = 12;
                break;
            default:
                threadsNumber = 2;
                break;
        }
        String pathPrefix = configuration.languagePackPath + File.separator + lang + File.separator;

        this.languageDB = DBMaker.fileDB(new File(pathPrefix + lang + ".map")).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().readOnly().make();
        this.hashLemmaBlackKey = languageDB.hashMap("lemma_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        this.hashFileLemmata = languageDB.hashMap("lemmata").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        this.hashPosBlackKey = languageDB.hashMap("pos_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        this.hashProperNounPosBlackKey = languageDB.hashMap("properNounPos_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        this.hashFileKey = languageDB.hashMap("invdocfreq").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
        this.hashFileStoplistDB = languageDB.hashMap("stoplist").keySerializer(Serializer.JAVA).valueSerializer(Serializer.INTEGER).open();
        this.hashFileSynonyms = languageDB.hashMap("synonyms").keySerializer(Serializer.JAVA).valueSerializer(Serializer.INTEGER).open();
    }




	/*
     * public int computeFrequency(Map<String, KD_keyconcept>
	 * expressions_collected, AtomicInteger totalOfTokens,
	 * ConcurrentHashMap<String, Map<String, KD_keyconcept>>
	 * all_related_expressions) { ConcurrentHashMap<String, KD_keyconcept>
	 * relatedExpressionCollected = new ConcurrentHashMap<String,
	 * KD_keyconcept>(); Iterator<Entry<String, KD_keyconcept>> it =
	 * expressions_collected.entrySet().iterator(); Integer frequencyToSubtract
	 * = new Integer(0); while (it.hasNext()) { Entry<String, KD_keyconcept>
	 * entry = it.next(); if (!entry.getValue().frequencyrecounted) {
	 * relatedExpressionCollected
	 * .putAll(all_related_expressions.get(entry.getKey())); }
	 *
	 * if (relatedExpressionCollected.size() > 0) {
	 *
	 * Integer value = computeFrequency(relatedExpressionCollected,
	 * totalOfTokens, all_related_expressions); entry.getValue().frequency -=
	 * value; totalOfTokens.decrementAndGet(); frequencyToSubtract +=
	 * entry.getValue().frequency; entry.getValue().frequencyrecounted = true;
	 * relatedExpressionCollected = new ConcurrentHashMap<String,
	 * KD_keyconcept>(); } else {
	 *
	 * frequencyToSubtract += entry.getValue().frequency;
	 *
	 * entry.getValue().frequencyrecounted = true; } } return
	 * frequencyToSubtract;
	 *
	 * }
	 */

    /**
     * Creates a new folder structure in the languages folder in order to allow the user to set up a custom (not provided) language easily.
     *
     * @param name          The new language name.
     * @param configuration KD_configuration object.
     */
    public void createNewEmptyLanguage(String name, KD_configuration configuration) {

        if (this.model.getCurrent_language_path().compareTo(configuration.languagePackPath) != 0) {
            this.model = new KD_Model(Paths.get(configuration.languagePackPath));
        }


        //Check if the language folder already exists
        String languageMainFolder = configuration.languagePackPath + File.separator + name + File.separator;
        File laguageFolder = new File(languageMainFolder);
        //Check if the langguage folder already exists
        if (laguageFolder.exists()) {

            logger.error("A folder with this name already exists in the \"languages\" folder! Please change name or delete the previously created directory");
            return;
        } else {
            laguageFolder.mkdirs();
            laguageFolder = new File(languageMainFolder);

            String example_of_patterns_file = "# The # char is the comment.See the other languages for a complete example \n" +
                    "#Example of a Bigram: \"Tag_adj\",\"Tag_noun\",weight(1 is neutral)\n" +
                    "#Unigrams\n" +
                    "\"Tag_For_adjective\", 1\n" +
                    "\"Tag_For_noun\", 1\n" +
                    "\n" +
                    "#Bigrams\n" +
                    "\"Tag_for_adjective\",\"Tag_for_noun\",1";

            try {
                String tagset = configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "tagset" + File.separator + "TEXTPRO";
                File tset = new File(tagset);
                tset.mkdirs();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tagset + File.separator + "patterns.txt"), "UTF-8"));
                out.write(example_of_patterns_file);
                out.close();
                tagset = configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "tagset" + File.separator + "TREETAGGER";
                tset = new File(tagset);
                tset.mkdirs();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tagset + File.separator + "patterns.txt"), "UTF-8"));
                out.write(example_of_patterns_file);
                out.close();
                tagset = configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "tagset" + File.separator + "STANFORD";
                tset = new File(tagset);
                tset.mkdirs();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tagset + File.separator + "patterns.txt"), "UTF-8"));
                out.write(example_of_patterns_file);
                out.close();
                tagset = configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "tagset" + File.separator + "CUSTOM";
                tset = new File(tagset);
                tset.mkdirs();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tagset + File.separator + "patterns.txt"), "UTF-8"));
                out.write(example_of_patterns_file);
                out.close();


                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "synonyms.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "stoplist.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "properNounPosList.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "pos-no.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "lemmalist.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "lemma-no.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "keyconcept-no.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "keyconcept-yes.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "idf_lang.txt"), "UTF-8"));
                out.write("");
                out.close();
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configuration.languagePackPath + File.separator + name + File.separator + "configuration_files" + File.separator + "capitalization_pos.txt"), "UTF-8"));
                out.write("");
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            Properties prop = new Properties();
            OutputStream output = null;
            try {
                output = new FileOutputStream(languageMainFolder + File.separator + "config.properties");

                // set the properties value
                prop.setProperty("keyconcept_yes_md5", "");
                prop.setProperty("keyconcept_no_md5", "");
                prop.setProperty("lemma_list_md5", "");
                prop.setProperty("lemma_blacklist_md5", "");
                prop.setProperty("stop_list_md5", "");
                prop.setProperty("synonyms_md5", "");
                prop.setProperty("pos_blacklist_md5", "");
                prop.setProperty("invFreq_md5", "");
                prop.setProperty("properNounPosList_md5", "");

                // save properties to project root folder
                prop.store(output, null);

                output.close();
            } catch (Exception e) {
                logger.error("Error during the  \"config.properties\" file creation");
            }

            Language lang = Language.CUSTOM;
            lang.set_Custom_Language(name.toUpperCase());
            KD_loader.run_the_updater(lang, configuration.languagePackPath);


        }


    }


    /**
     * Extracts the key-phrases from data.
     *
     * @param lang          Language of the input file.
     * @param configuration KD_configuration object.
     * @param filePath      String with path of the input file.
     * @param fileContent   StringBuffer containing the input data. If it is not null or empty the method uses this variable as data container.
     * @return LinkedList of KD_keyconcept objects.
     */
    public LinkedList<KD_keyconcept> extractExpressions(Language lang, KD_configuration configuration, String filePath, StringBuffer fileContent) {

        if (this.model.getCurrent_language_path().compareTo(Paths.get(configuration.languagePackPath).toString()) != 0) {
            //System.out.println("differnet");
            this.model = new KD_Model(Paths.get(configuration.languagePackPath));
        }

        //configuration variables
        LinkedList<KD_keyconcept> outputList = new LinkedList<KD_keyconcept>();

        String pathPrefix = configuration.languagePackPath + File.separator + lang + File.separator;

        if (filePath == null) {
            filePath = "";
        }

        if (fileContent == null) {
            fileContent = new StringBuffer();
        }

        if (filePath.length() == 0 && fileContent.length() == 0) {
            LOGGER.warn("No valid data submitted to KD");
            return outputList;
        }


        boolean use_lucene = configuration.use_lucene;

        Integer numberOfConcepts = configuration.numberOfConcepts;
        Integer local_frequency_threshold = configuration.local_frequency_threshold;
        KD_configuration.Prefer_Specific_Concept prefer_speficic_concept = configuration.prefer_specific_concept;
        boolean rerank_by_position = configuration.rerank_by_position;

        ColumExtraction column_configuration = configuration.column_configuration;

        boolean only_multiword = configuration.only_multiword;

        boolean no_syn = configuration.no_syn;
        boolean no_rerank = configuration.no_rerank;


        Integer averaging_cycles = 1;
        Integer absorbtion_cycles = 1;
        boolean rerank_by_token_lenght = false;
        boolean rerank_shorter_first_by_average = false;
        boolean rerank_shorter_first_by_boosting = false;
        boolean rerank_longer_first_by_absorbtion = false;

        ExecutorService executor = null;
        ForkJoinPool p = new ForkJoinPool(threadsNumber);

        if (languageDB == null) {
            languageDB = DBMaker.fileDB(new File(pathPrefix + lang + ".map")).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().readOnly().make();

            //languageDB = DBMaker.newFileDB(new File(pathPrefix + lang + ".map")).mmapFileEnable().transactionDisable().closeOnJvmShutdown().readOnly().make();
        }


        /////// ------------- override parameter -------------------////

        /*
        configuration.skip_proper_noun = true;
        configuration.skipFrequncyAbsorption = false;
        configuration.rerank_by_position = false;
        configuration.numberOfConcepts = 50;
        configuration.local_frequency_threshold=1;
        configuration.skip_keyword_with_proper_noun = true;
        prefer_speficic_concept = Prefer_Specific_Concept.STRONG;
        */


        switch (prefer_speficic_concept) {
            case WEAK:
                rerank_by_token_lenght = true;
                break;
            case MEDIUM:
                rerank_by_token_lenght = true;
                rerank_shorter_first_by_average = true;
                averaging_cycles = 1;
                break;
            case STRONG:
                rerank_by_token_lenght = true;
                rerank_shorter_first_by_average = true;
                rerank_shorter_first_by_boosting = true;
                rerank_longer_first_by_absorbtion = true;
                averaging_cycles = 1;
                absorbtion_cycles = 1;
                break;
            case MAX:
                rerank_by_token_lenght = true;
                rerank_shorter_first_by_average = true;
                rerank_longer_first_by_absorbtion = true;
                rerank_shorter_first_by_boosting = true;
                averaging_cycles = 2;
                absorbtion_cycles = 2;
                break;
            case NO:
                rerank_by_token_lenght = false;
                rerank_shorter_first_by_average = false;
                rerank_longer_first_by_absorbtion = false;
                rerank_shorter_first_by_boosting = false;
                averaging_cycles = 0;
                absorbtion_cycles = 0;
                break;
            default:
                rerank_by_token_lenght = false;
                rerank_shorter_first_by_average = false;
                rerank_longer_first_by_absorbtion = false;
                rerank_shorter_first_by_boosting = false;
                averaging_cycles = 0;
                absorbtion_cycles = 0;
                break;

        }

        List<Future<Map<String, KD_keyconcept>>> futures = null;
        List<List<Resultset_Record>> parts;
        int record_id = 1;
        int tot_sentences = 1;
        int totalNumberOfToken = 0;

        StringBuffer recomposedTextFile = new StringBuffer();
        List<Resultset_Record> records = new ArrayList<Resultset_Record>();
        try {

            //DB inveDocFreQLanguageDB = DBMaker.newFileDB(new File(pathPrefix + "invdocfreq.map")).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
            if (this.hashFileKey == null) {
                this.hashFileKey = languageDB.hashMap("invdocfreq").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).open();
            }

            Double max_idf_value = 0.0;
            //System.out.println(this.hashFileKey);

            for (Object d : this.hashFileKey.values()) {
                if ((Double) d > max_idf_value) {
                    max_idf_value = (Double) d;
                }
            }


            //hashFileKey.close();
            //inveDocFreQLanguageDB.close();

            List<SortedMap<Integer, Resultset_Record>> collections = new ArrayList<SortedMap<Integer, Resultset_Record>>();
            SortedMap<Integer, Resultset_Record> initial_collection = new TreeMap<Integer, Resultset_Record>();
            collections.add(initial_collection);

            File f = new File(filePath);

            double f_kilobytes = (f.length() / 1024);
            if (f_kilobytes <= 11) {
                this.threadsNumber = 1;
                if (configuration.verbose) {
                    logger.info("File too short, thread number forced to 1");
                }
            }
            BufferedReader in = null;

            if (fileContent != null && fileContent.length() > 0) {
                InputStream is = new ByteArrayInputStream(fileContent.toString().getBytes());
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } else {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            }
            String line = "";
            String[] lineItems;


            Integer token_position = 0;
            Integer lemma_position = 0;
            Integer pos_position = 0;
            switch (column_configuration) {
                case TOKEN_LEMMA_POS:
                    token_position = 0;
                    lemma_position = 1;
                    pos_position = 2;
                    break;
                case TOKEN_POS_LEMMA:
                    token_position = 0;
                    lemma_position = 2;
                    pos_position = 1;
                    break;
                case CUSTOM:
                    token_position = configuration.token_position;
                    lemma_position = configuration.lemma_position;
                    pos_position = configuration.pos_position;
                    break;
            }

            if (configuration.verbose) {
                logger.info("Columns Selected: Token: " + token_position + ", lemma: " + lemma_position + ", pos:" + pos_position);
            }

            boolean topOfFile = true;


            while ((line = in.readLine()) != null) {
                if (line.length() > 0 && line.toLowerCase().compareTo("<eos>") != 0) {

                    if (topOfFile && line.startsWith("#")) {
                        //Skip header of textpro
                    } else {
                        topOfFile = false;
                        lineItems = line.split("\t");
                        recomposedTextFile.append(lineItems[token_position] + " ");

                        try {
                            Resultset_Record rsr = new Resultset_Record(f.getName(), tot_sentences, "", lineItems[token_position].toLowerCase(), lineItems[lemma_position].toLowerCase(), lineItems[pos_position].toUpperCase(), record_id);
                            records.add(rsr);
                            record_id++;
                        } catch (Exception e) {
                            logger.error("Wrong format line: " + line + " missing separation.....");
                        }
                    }
                } else {
                    tot_sentences++;
                }

            }

            in.close();

            if (recomposedTextFile.toString().trim().length() == 0) {
                return new LinkedList<KD_keyconcept>();
            }

            totalNumberOfToken = record_id;
            collections.clear();
            collections = null;

            if (configuration.verbose) {
                logger.info("Loaded! Now I prepare the threads...");
            }

            parts = Lists.partition(records, (records.size() / threadsNumber) + 1);
            try {
                executor = Executors.newFixedThreadPool(parts.size());
            } catch (Exception e) {
                this.threadsNumber = 1;
                parts = Lists.partition(records, (records.size() / threadsNumber) + 1);
                executor = Executors.newFixedThreadPool(parts.size());
            }
            //System.out.println("Now the parts are " + parts.size());

            Set<Callable<Map<String, KD_keyconcept>>> callables = new HashSet<Callable<Map<String, KD_keyconcept>>>();
            for (int pts = 0; pts < parts.size(); pts++) {
                callables.add(new KD_extractor("Worker" + (pts + 1), parts.get(pts), lang, max_idf_value, configuration, languageDB));
            }

            futures = executor.invokeAll(callables);
            executor.shutdown();
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

            executor.shutdownNow();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, KD_keyconcept> expressions_collected = new LinkedHashMap<String, KD_keyconcept>();
        Map<String, KD_keyconcept> ec;
        try {
            for (int i = 0; i < futures.size(); i++) {
                if (i == 0) {
                    expressions_collected = futures.get(i).get();
                } else {
                    ec = futures.get(i).get();
                    for (Entry<String, KD_keyconcept> entry : ec.entrySet()) {
                        if (expressions_collected.containsKey(entry.getKey())) {
                            expressions_collected.put(entry.getKey(), expressions_collected.get(entry.getKey()).mergeValue(entry.getValue()));
                        } else {
                            expressions_collected.put(entry.getKey(), entry.getValue());
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        records.clear();
        records = new ArrayList<Resultset_Record>();

        //////////////////////////////// Evaluation section /////////////////////////////////

        new KD_concept_sorter();

        expressions_collected = KD_concept_sorter.sort(expressions_collected, Sort.FREQ, SortDirection.DESC);
        ArrayList<String> stems = new ArrayList<String>();

        //Remove multiword with freq under the threshold
        Iterator<Entry<String, KD_keyconcept>> it = expressions_collected.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, KD_keyconcept> entry = it.next();

            entry.getValue().calculateNormFreqByToken(totalNumberOfToken);


            if ((entry.getValue().frequency < local_frequency_threshold) && !entry.getValue().isAbstract) {
                it.remove();
            }
            ////////////////////////
            ////////////////////////
            //////// check this part ////////////////
            if ((entry.getValue().frequency < local_frequency_threshold) && entry.getValue().isAbstract) {
                if (!configuration.no_abstract) {
                    if (entry.getValue().elements.size() > 1) {
                        //System.out.println(entry.getValue().elements.toString());
                        entry.getValue().scoreBoost -= 0;
                    }
                }
                if (!configuration.force_abstract) {
                    it.remove();
                }
            }
            //////////////////////
            //////////////////////
            //////////////////////

        }
        if (this.hashFileStoplistDB == null) {
            this.hashFileStoplistDB = languageDB.hashMap("stoplist").keySerializer(Serializer.JAVA).valueSerializer(Serializer.DOUBLE).open();
        }
        it = expressions_collected.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, KD_keyconcept> entry = it.next();
            if (entry.getValue().frequency <= 0 || hashFileStoplistDB.containsKey(entry.getKey())) {
                //totalNumberOfToken -= entry.getValue().frequency;
                it.remove();
            }
        }
        //System.out.println(" -> " + totalNumberOfToken);

        //---------------------------- Ricalcolo Frequenze --------------------//


        if (!configuration.skipFrequencyAbsorption) {
            if (expressions_collected.size() <= 0) {
                return new LinkedList<KD_keyconcept>();
            }

            LinkedList<String> keysList = new LinkedList<String>(expressions_collected.keySet());
            List<List<String>> partsOfKeySet = Lists.partition(keysList, (keysList.size() / threadsNumber) + 1);
            executor = Executors.newFixedThreadPool(partsOfKeySet.size());
            ConcurrentHashMap<String, Map<String, KD_keyconcept>> all_related_expressions = new ConcurrentHashMap<String, Map<String, KD_keyconcept>>();
            List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();

            for (int pts = 0; pts < partsOfKeySet.size(); pts++) {
                tasks.add(Executors.callable(new RelatedExpressionExtractorThread(partsOfKeySet.get(pts), expressions_collected, all_related_expressions)));
            }

            try {
                // lancia tutti i thread e attende fine
                executor.invokeAll(tasks);
                executor.shutdown();
                executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            AtomicInteger ttoken = new AtomicInteger(totalNumberOfToken);

            p = new ForkJoinPool(threadsNumber);
            p.invoke(new Recounter(expressions_collected, ttoken, all_related_expressions, 0));
            totalNumberOfToken = ttoken.get();//System.out.println(" -> " + totalNumberOfToken);
        }

        //----------------------------////////////////////--------------------//

        //--------------------------- synonymns block ------------------------//

        if (!no_syn) {
            if (this.hashFileSynonyms == null) {
                this.hashFileSynonyms = languageDB.hashMap("synonyms").keySerializer(Serializer.JAVA).valueSerializer(Serializer.DOUBLE).open();
            }
            for (Object objIterator : hashFileSynonyms.keySet()) {
                ArrayList<String> synonyms_line = (ArrayList<String>) objIterator;
                KD_keyconcept sinonimo = new KD_keyconcept();
                String keyToBeAdded = "";
                Integer maxChainLenght = 0;

                for (String synonyms_exp : synonyms_line) {
                    //System.out.println(synonyms_exp);
                    if (expressions_collected.containsKey(synonyms_exp)) {

                        for (String synonyms_exp_max : synonyms_line) {
                            if (maxChainLenght < getStringFromKey(synonyms_exp_max).split(" ").length) {
                                maxChainLenght = getStringFromKey(synonyms_exp_max).split(" ").length;
                            }
                        }
                        if (sinonimo.elements.size() == 0) {
                            keyToBeAdded = synonyms_exp;
                            sinonimo.elements.addAll(expressions_collected.get(synonyms_exp).elements);
                            sinonimo.elementsLemma.addAll(expressions_collected.get(synonyms_exp).elementsLemma);
                            sinonimo.elementsToken.addAll(expressions_collected.get(synonyms_exp).elementsToken);
                            sinonimo.elementsStem.addAll(expressions_collected.get(synonyms_exp).elementsStem);
                            sinonimo.frequency = expressions_collected.get(synonyms_exp).frequency;
                            sinonimo.position_in_text.addAll(expressions_collected.get(synonyms_exp).position_in_text);
                            sinonimo.idf = expressions_collected.get(synonyms_exp).idf;
                            sinonimo.chainlenght = maxChainLenght;
                            sinonimo.scoreBoost = expressions_collected.get(synonyms_exp).scoreBoost;
                            sinonimo.synonyms.add(expressions_collected.get(synonyms_exp).getString());
                        } else {
                            sinonimo.synonyms.add(expressions_collected.get(synonyms_exp).getString());
                            sinonimo.frequency += expressions_collected.get(synonyms_exp).frequency;
                            sinonimo.position_in_text.addAll(expressions_collected.get(synonyms_exp).position_in_text);

                            if (sinonimo.scoreBoost < expressions_collected.get(synonyms_exp).scoreBoost) {
                                sinonimo.scoreBoost = expressions_collected.get(synonyms_exp).scoreBoost;
                            }
                        }
                    }
                    expressions_collected.remove(synonyms_exp);
                }

                if (keyToBeAdded.length() > 0) {
                    expressions_collected.put(keyToBeAdded, sinonimo);
                }
            }
        }

        //---------------------------end  synonymns block ------------------------//

        //--------------------------- Lucene configuration ----------------------//


        Directory lucene_index = new RAMDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig lucene_config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
        IndexSearcher searcher = null;
        TopScoreDocCollector collector = null;
        if (use_lucene) {
            try {
                IndexWriter indexWriter = new IndexWriter(lucene_index, lucene_config);
                Document doc = new Document();
                doc.add(new StringField("id", "file", Field.Store.YES));
                doc.add(new TextField("text", recomposedTextFile.toString(), Field.Store.YES));
                indexWriter.addDocument(doc);
                indexWriter.close();
                searcher = new IndexSearcher(DirectoryReader.open(lucene_index));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        //--------------------------- end Lucene configuration -----------------//


        //--------------------------- score computation -------------------------//


        List<String> acronyms = new ArrayList<String>();

        if (configuration.boost_acronyms) {
            it = expressions_collected.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, KD_keyconcept> entry = it.next();
                if (entry.getValue().isAcronym) {
                    acronyms.add(entry.getValue().getString());
                }
            }
        }

        it = expressions_collected.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, KD_keyconcept> entry = it.next();
            entry.getValue().score = (entry.getValue().frequency / (double) totalNumberOfToken) * 1000;

            if (rerank_by_token_lenght) {
                entry.getValue().score *= entry.getValue().chainlenght;
            }


            if (use_lucene) {
                try {
                    Query q = new QueryParser("text", analyzer).parse(entry.getValue().getString());
                    collector = TopScoreDocCollector.create(1, true);
                    searcher.search(q, collector);

                    entry.getValue().score += (1 + collector.topDocs().getMaxScore());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //entry.getValue().score *= Math.log( totalNumberOfToken / ((double) entry.getValue().frequency +1 ));

            entry.getValue().score *= entry.getValue().scoreBoost;
            entry.getValue().score *= entry.getValue().idf;


            /* local idf*/


            if (rerank_by_position) {
                Collections.sort(entry.getValue().sentences_in_text);
                Collections.sort(entry.getValue().position_in_text);
                double positionFactor = 1;

                /* token based */
                if (entry.getValue().position_in_text.get(0) < (totalNumberOfToken / 2)) {
                    double pow_arg = (totalNumberOfToken - entry.getValue().position_in_text.get(0)) / (double) totalNumberOfToken;
                    positionFactor = 1 + (Math.pow(pow_arg, 3));
                }
                /**/



				/* sentence based */
                //double pow_arg = (tot_sentences - entry.getValue().sentences_in_text.get(0)) / (double) tot_sentences;
                //double positionFactor = 1 + (Math.pow(pow_arg, 3));
                /**/


                entry.getValue().score *= positionFactor;
            }


            // *** acronym block

            if (configuration.boost_acronyms) {
                if (entry.getValue().isAcronym && entry.getValue().frequency > 4) {
                    entry.getValue().score *= 8;
                }
            /*deboost multoword with acronym inside*/
                if (entry.getValue().chainlenght > 1) {
                    List<String> common = new ArrayList<String>(acronyms);
                    common.retainAll(entry.getValue().elements);
                    if (common.size() > 0) {
                        // System.out.println(entry.getValue().getString());
                        entry.getValue().score *= 0.4;


                    }
                }
            }

            //********
            if (configuration.use_pattern_weight) {
                entry.getValue().score *= entry.getValue().patternScoreBoost;
            }
        }


        expressions_collected = KD_concept_sorter.sort(expressions_collected, Sort.SCORE, SortDirection.ASC);
        //--------------------------- end score computation ---------------------------//


        //--------------------------------rerank block-----------------------------------//
        if (!no_rerank) {
            KD_rerank_methods reRanker;

            if (rerank_shorter_first_by_average) {
                reRanker = new KD_rerank_methods(expressions_collected, Method.SHORTER_FIRST_BY_AVERAGE, averaging_cycles, rerank_shorter_first_by_boosting);
                reRanker.rerank();
                expressions_collected = KD_concept_sorter.sort(reRanker.base, Sort.SCORE, SortDirection.DESC);
            }
            if (rerank_longer_first_by_absorbtion) {
                reRanker = new KD_rerank_methods(expressions_collected, Method.LONGER_FIRST_BY_ABSORBTION, absorbtion_cycles);
                reRanker.rerank();
                expressions_collected = reRanker.base;
            }
        }
        //--------------------------------end rerank block-------------------------------//


        expressions_collected = KD_concept_sorter.sort(expressions_collected, Sort.SCORE, SortDirection.DESC);


        for (Entry<String, KD_keyconcept> entry : expressions_collected.entrySet()) {

            entry.getValue().score = MathUtils.round(entry.getValue().score, 3);

            //output.append(entry.getValue() + "\n");
            if (only_multiword) {
                if (entry.getValue().chainlenght > 1) {
                    if (entry.getValue().frequency > 0) {
                        outputList.add(entry.getValue());
                        if (numberOfConcepts >= 0) {
                            numberOfConcepts--;
                            if (numberOfConcepts == 0) {
                                break;
                            }
                        }
                    }
                }
            } else {
                if (entry.getValue().frequency > 0) {
                    outputList.add(entry.getValue());
                    if (numberOfConcepts >= 0) {
                        numberOfConcepts--;
                        if (numberOfConcepts == 0) {
                            break;
                        }
                    }
                }
            }
        }

        if (configuration.verbose) {
            logger.info(" -- Done!");
        }
        try {
            executor.shutdown();
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

            executor.shutdownNow();

            p.shutdown();
            p.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

            p.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return outputList;

    }

    private String getStringFromKey(String s) {
        ArrayList<String> elements = new ArrayList<String>();
        String[] tokens = s.split("÷•÷");
        for (String tkn : tokens) {
            if (tkn.trim().length() > 0) {
                elements.add(tkn.trim());
            }
        }
        return Joiner.on(" ").join(elements);
    }

    protected void finalize() throws Throwable {
        try {
            close();        // close open files
            this.languageDB.close();
        } finally {
            super.finalize();
        }
    }
}





class Resultset_Record {
    String file_name = "";
    Integer sentence_id = 0;
    String entity = "";
    String token = "";
    Integer token_number = 0;
    String lemma = "";
    String pos = "";

    /**
     * <p>Constructor for Resultset_Record.</p>
     *
     * @param file_name a {@link java.lang.String} object.
     * @param sentence_id a {@link java.lang.Integer} object.
     * @param entity a {@link java.lang.String} object.
     * @param token a {@link java.lang.String} object.
     * @param lemma a {@link java.lang.String} object.
     * @param pos a {@link java.lang.String} object.
     * @param token_idx a {@link java.lang.Integer} object.
     */
    public Resultset_Record(String file_name, Integer sentence_id, String entity, String token, String lemma, String pos, Integer token_idx) {
        this.file_name = file_name;
        this.sentence_id = sentence_id;
        this.entity = entity;
        this.token = token;
        this.lemma = lemma;
        this.pos = pos;
        this.token_number = token_idx;
/**
 * <p>Constructor for RelatedExpressionExtractorThread.</p>
 *
 * @param my_part a {@link java.util.List} object.
 * @param expressions_collected a {@link java.util.Map} object.
 * @param related_exp a {@link java.util.concurrent.ConcurrentHashMap} object.
 */

    }

}

class RelatedExpressionExtractorThread implements Runnable {
/** {@inheritDoc} */

    Map<String, KD_keyconcept> expressions_collected;
    ConcurrentHashMap<String, Map<String, KD_keyconcept>> relatedExpressionCollected;
    List<String> my_part_of_keys;

    public RelatedExpressionExtractorThread(List<String> my_part, Map<String, KD_keyconcept> expressions_collected, ConcurrentHashMap<String, Map<String, KD_keyconcept>> related_exp) {
        this.expressions_collected = expressions_collected;
        this.my_part_of_keys = my_part;
        this.relatedExpressionCollected = related_exp;
    }

    @Override
    public void run() {
        for (String key : my_part_of_keys) {
            KD_keyconcept ex = expressions_collected.get(key);
            Iterator<Entry<String, KD_keyconcept>> it_sub = expressions_collected.entrySet().iterator();
            Map<String, KD_keyconcept> relatedExpressionForAKey = new HashMap<String, KD_keyconcept>();
            while (it_sub.hasNext()) {
                Entry<String, KD_keyconcept> entry_sub = it_sub.next();
                KD_keyconcept ex_sub = entry_sub.getValue();
                if ((ex_sub.chainlenght > ex.chainlenght) && (KD_utils.findArray(ex_sub.elements, ex.elements) >= 0)) {
                    relatedExpressionForAKey.put(entry_sub.getKey(), ex_sub);
                }
            }
            relatedExpressionCollected.put(key, relatedExpressionForAKey);
        }

    }

}

class Recounter extends RecursiveAction {

    Map<String, KD_keyconcept> expressions_collected;
    AtomicInteger totalOfTokens;
    ConcurrentHashMap<String, Map<String, KD_keyconcept>> allrelatedExpressions;
    int level;
    public Integer frequencyToSubtract = new Integer(0);

    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for Recounter.</p>
     *
     * @param expressions_collected a {@link java.util.Map} object.
     * @param totalOfTokens a {@link java.util.concurrent.atomic.AtomicInteger} object.
     * @param all_related_expressions a {@link java.util.concurrent.ConcurrentHashMap} object.
     * @param level a int.
     */
    public Recounter(Map<String, KD_keyconcept> expressions_collected, AtomicInteger totalOfTokens, ConcurrentHashMap<String, Map<String, KD_keyconcept>> all_related_expressions, int level) {
        this.expressions_collected = expressions_collected;
        this.totalOfTokens = totalOfTokens;
        this.level = level;
        this.allrelatedExpressions = all_related_expressions;
    }

    /** {@inheritDoc} */
    @Override
    protected void compute() {
        Map<String, KD_keyconcept> relatedExpressionCollected = new LinkedHashMap<String, KD_keyconcept>();
        Iterator<Entry<String, KD_keyconcept>> it = expressions_collected.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, KD_keyconcept> entry = it.next();
            if (!entry.getValue().frequencyrecounted) {

                relatedExpressionCollected = allrelatedExpressions.get(entry.getKey());
            }

            if (relatedExpressionCollected.size() > 0) {
                List<Recounter> subtasks = new ArrayList<Recounter>();
                subtasks.add(new Recounter(relatedExpressionCollected, totalOfTokens, allrelatedExpressions, level + 1));
                invokeAll(subtasks);

                Integer value = subtasks.get(0).frequencyToSubtract;
                entry.getValue().frequency -= value;
                totalOfTokens.decrementAndGet();
                frequencyToSubtract += entry.getValue().frequency;
                entry.getValue().frequencyrecounted = true;
                relatedExpressionCollected = new LinkedHashMap<String, KD_keyconcept>();
            } else {
                frequencyToSubtract += entry.getValue().frequency;
                entry.getValue().frequencyrecounted = true;
            }

        }

    }

}
