package eu.fbk.dh.kd;

import com.google.common.base.Joiner;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_configuration.ColumExtraction;
import eu.fbk.dh.kd.lib.KD_configuration.Group;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.lib.KD_core.Language;
import eu.fbk.dh.kd.lib.KD_core.Threads;
import eu.fbk.dh.kd.lib.KD_keyconcept;
import eu.fbk.dh.kd.lib.KD_loader;
import eu.fbk.dh.kd.models.KD_Model;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * eu.fbk.dh.kd.Main runnable class
 * Please refer to the help for more information about the parameters
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class Main {

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {


        KD_configuration configuration = new KD_configuration();
        configuration.numberOfConcepts = -1;
        configuration.max_keyword_length = 4;
        configuration.local_frequency_threshold = 2;
        configuration.prefer_specific_concept = KD_configuration.Prefer_Specific_Concept.MEDIUM;
        configuration.skip_proper_noun = false;
        configuration.skip_keyword_with_proper_noun = false;
        configuration.rerank_by_position = false;
        configuration.group_by = KD_configuration.Group.NONE;
        configuration.column_configuration = KD_configuration.ColumExtraction.TOKEN_POS_LEMMA;
        configuration.only_multiword = false;
        configuration.tagset = KD_configuration.Tagset.TEXTPRO;


        Language lang = Language.ENGLISH;

        int cores = Runtime.getRuntime().availableProcessors();

        boolean useStanford = false;
        boolean save_stanford = false;

        Threads t;
        switch (cores) {
            case 1:
                t = Threads.ONE;
                break;
            case 2:
                t = Threads.TWO;
                break;
            case 4:
                t = Threads.FOUR;
                break;
            case 6:
                t = Threads.SIX;
                break;
            case 8:
                t = Threads.EIGHT;
                break;
            case 10:
                t = Threads.TEN;
                break;
            case 12:
                t = Threads.TWELVE;
                break;
            default:
                t = Threads.TWO;
                break;
        }


        boolean STDOUT = false;

        ///////////////////////////////// command line parser //////////////////////////////////////

        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        //options.addOption( "n", "number_of_concept", false, "do not hide entries starting with ." );

        options.addOption(OptionBuilder.withLongOpt("number_of_concept").withDescription("number of output keywords").withArgName("Integer").withType(Integer.class).hasArg().create("n"));

        options.addOption(OptionBuilder.withLongOpt("max_keyword_length").withDescription("maximum length of multi-word expressions").withArgName("Integer").withType(Integer.class).hasArg().create("m"));

        options.addOption(OptionBuilder.withLongOpt("local_frequency_threshold").withDescription("min number of occurrences in a text").withArgName("Integer").withType(Integer.class).hasArg().create("l"));

        options.addOption(OptionBuilder.withLongOpt("number_of_threads").withDescription("number of threads used by the program").withType(Integer.class).withArgName("ONE | TWO | FOUR | SIX | EIGHT | TEN | TWELVE").hasArg().create("t"));

        options.addOption(OptionBuilder.withLongOpt("prefer_specific_concept").withDescription("give a boost to more specific key-concept (multi-word)").withArgName("NO | WEAK | MEDIUM | STRONG | MAX").hasArg().create("p"));

        options.addOption(OptionBuilder.withLongOpt("column_configuration").withDescription("specify the input file column configuration\neg: CUSTOM_0,9,6 token is 0, lemma is 9, pos is 6").withArgName("TOKEN_LEMMA_POS | TOKEN_POS_LEMMA | CUSTOM_#token,#lemma,#pos").hasArg().create("c"));

        options.addOption(OptionBuilder.withLongOpt("language").withDescription("specify the language of the input file").withArgName("ENGLISH | ITALIAN | CUSTOM | CUSTOM_<your language name>").hasArg().create("lang"));

        options.addOption(OptionBuilder.withLongOpt("tagset").withDescription("specify the tagset of the pos tagger (default is TEXTPRO)").withArgName("TEXTPRO | STANFORD | TREETAGGER | CUSTOM").hasArg().create("ts"));

        options.addOption(OptionBuilder.withLongOpt("group").withDescription("set the group configuration").withArgName("NONE | BY_LIST | BY_STEM | ALL_LEMMA").hasArg().create("g"));

        options.addOption(OptionBuilder.withLongOpt("lang_folder").withDescription("set the language folder path").withArgName("Path to the folder").hasArg().create("lp"));

        options.addOption(OptionBuilder.withLongOpt("new_language").withDescription("create new empty language, in your language_folder").withArgName("Language name").hasArg().create("nl"));

        options.addOption(OptionBuilder.withLongOpt("new_language_folder").withDescription("create new empty language folder from scratch").withArgName("Path to new language folder").hasArg().create("nf"));



        options.addOption("STDOUT", "standard_out", false, "print results on standard out");
        options.addOption("h", "help", false, "print this message");
        options.addOption("om", "only_multiword", false, "display only multi-words");
        options.addOption("fas", "skip_frequency_absorption", false, "skip frequency absorption");
        options.addOption("wp", "use_pattern_weight", false, "use the weight of pattern");
        options.addOption("ba", "boost_acronyms", false, "boost acronyms (for scientific articles)");
        options.addOption("v", "verbose", false, "verbose output");
        options.addOption("us", "use_stanford", false, "use included stanford pos tagger (only english)");
        options.addOption("ve", "version", false, "print version and exit");
        options.addOption("s", "skip_proper_noun", false, "skip proper nouns");
        options.addOption("sk", "skip_keyword_with_proper_noun", false, "skip keyword with proper nouns");
        options.addOption("skw", "skip_keyword_with_not_allowed_words", false, "skip keywords that contain a keyconcept-no item");
        options.addOption("r", "rerank_by_position", false, "give a boost to key-concepts on the top of the document");
        options.addOption("ns", "no_synonyms", false, "disable the synonym resolution");
        options.addOption("ss", "save_stanford", false, "save stanford preprocessed file");
        options.addOption("nr", "no_rerank", false, "disable the re-rank function");
        options.addOption("cp", "capitalize_pos", false, "capitalize token with specified pos");
        options.addOption("nabs", "no_abstract_keyconcept", false, "disable the boost on abstract key-concepts");
        options.addOption("nidf", "no_idf", false, "disable the boost by the idf value");

        CommandLine line = null;
        try {
            line = parser.parse(options, args);

            //---------------------------------------boolean values
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth(500);
                formatter.printHelp("KD_Keyphrase_Digger", options);
                System.exit(0);
            }

            if (line.hasOption("version")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth(500);
                System.out.println("\n" + KD_core.getVersion() + "\n");
                System.exit(0);
            }


            if (line.hasOption("no_rerank")) {
                configuration.no_rerank = true;
            }
            if (line.hasOption("use_pattern_weight")) {
                configuration.use_pattern_weight = true;
            }
            if (line.hasOption("boost_acronyms")) {
                configuration.boost_acronyms = true;
            }

            if (line.hasOption("no_abstract_keyconcept")) {
                configuration.no_abstract = true;
            }


            if (line.hasOption("no_idf")) {
                configuration.no_idf = true;
            }

            if (line.hasOption("no_synonyms")) {
                configuration.no_syn = true;
            }

            if (line.hasOption("rerank_by_position")) {
                configuration.rerank_by_position = true;
            }

            if (line.hasOption("capitalize_pos")) {
                configuration.capitalize_pos = true;
            }

            if (line.hasOption("verbose")) {
                configuration.verbose = true;
            }


            if (line.hasOption("skip_frequency_absorption")) {
                configuration.skipFrequencyAbsorption = true;
            }

            if (line.hasOption("only_multiword")) {
                configuration.only_multiword = true;
            }

            if (line.hasOption("skip_proper_noun")) {
                configuration.skip_proper_noun = true;
            }

            if (line.hasOption("skip_keyword_with_proper_noun")) {
                configuration.skip_keyword_with_proper_noun = true;
            }

            if (line.hasOption("skip_keyword_with_not_allowed_words")) {
                configuration.skip_keyword_with_not_allowed_words = true;
            }

            if (line.hasOption("use_stanford")) {

                useStanford = true;
            }
            if (line.hasOption("save_stanford")) {

                save_stanford = true;
            }


            if (line.hasOption("standard_out")) {
                STDOUT = true;
            }

            //-------------------------------------end boolean values

            //-------------------------------------properties values
            if (line.hasOption("number_of_threads")) {
                try {
                    t = Threads.valueOf(line.getOptionValue("number_of_threads").toUpperCase());
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option number_of_threads\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }





            if (line.hasOption("lang_folder")) {
                try {
                    configuration.languagePackPath = line.getOptionValue("lang_folder");
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option lang_folder\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }




            if (line.hasOption("new_language")) {
                try {
                    String lang_name = line.getOptionValue("new_language");
                    KD_core kxc = new KD_core(t);
                    kxc.createNewEmptyLanguage(lang_name.toUpperCase(),configuration);
                    System.out.println ("\nThe new \""+lang_name.toUpperCase()+"\" language has been added to the languages.\nTo use it please specify \"CUSTOM_"+lang_name.toUpperCase()+"\" in the language parameter (-lang).");
                    System.exit(0);

                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option new_language\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }


            if (line.hasOption("new_language_folder")) {
                try {
                    String path_new_lang = line.getOptionValue("new_language_folder");
                    KD_Model model = new KD_Model(FileSystems.getDefault().getPath(path_new_lang));
                    System.out.println ("\nThe new language folder has been created in : "+model.getCurrent_language_path()+" .\nTo use it please specify \""+model.getCurrent_language_path()+"\" in the lang_folder (-lp) parameter.");
                    System.exit(0);
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option new_language_folder\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }







            if (line.hasOption("prefer_specific_concept")) {
                try {
                    configuration.prefer_specific_concept = KD_configuration.Prefer_Specific_Concept.valueOf(line.getOptionValue("prefer_specific_concept").toUpperCase());
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option prefer_speficic_concept\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

            if (line.hasOption("column_configuration")) {
                try {

                    if (line.getOptionValue("column_configuration").toUpperCase().startsWith("CUSTOM_")) {
                        configuration.column_configuration = ColumExtraction.valueOf("CUSTOM");
                        String columnPositions = line.getOptionValue("column_configuration").split("_")[1];

                        configuration.token_position = Integer.parseInt(columnPositions.split(",")[0].trim().replace("{", "").replace("}", ""));
                        configuration.lemma_position = Integer.parseInt(columnPositions.split(",")[1].trim().replace("{", "").replace("}", ""));
                        configuration.pos_position = Integer.parseInt(columnPositions.split(",")[2].trim().replace("{", "").replace("}", ""));
                    } else {
                        configuration.column_configuration = ColumExtraction.valueOf(line.getOptionValue("column_configuration").toUpperCase());
                    }
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option column_configuration\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

            if (line.hasOption("number_of_concept")) {
                try {
                    configuration.numberOfConcepts = Integer.parseInt(line.getOptionValue("number_of_concept"));
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option number_of_concept\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

            if (line.hasOption("max_keyword_length")) {
                try {
                    configuration.max_keyword_length = Integer.parseInt(line.getOptionValue("max_keyword_length"));
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option max_keyword_length\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

            if (line.hasOption("local_frequency_threshold")) {
                try {
                    configuration.local_frequency_threshold = Integer.parseInt(line.getOptionValue("local_frequency_threshold"));
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option local_frequency_threshold\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

            if (line.hasOption("group")) {
                try {
                    configuration.group_by = Group.valueOf(line.getOptionValue("group").toUpperCase());
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option group\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

            if (line.hasOption("language")) {
                try {

                    String stringlang = line.getOptionValue("language").toUpperCase().split("_")[0];
                    lang = Language.valueOf(stringlang);
                    if (line.getOptionValue("language").toUpperCase().split("_",2).length > 1){
                        String custom_lang = line.getOptionValue("language").toUpperCase().split("_",2)[1];
                        lang.set_Custom_Language(custom_lang);
                    }


                } catch (Exception e) {
                    e.printStackTrace();

                    System.out.println("\nerror: Wrong value for the option language\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }


            if (line.hasOption("tagset")) {
                try {
                    configuration.tagset = KD_configuration.Tagset.valueOf(line.getOptionValue("tagset").toUpperCase());
                } catch (Exception e) {
                    System.out.println("\nerror: Wrong value for the option tagset\n");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.setWidth(500);
                    formatter.printHelp("KD_Keyphrase_Digger", options);
                    System.exit(1);
                }
            }

        } catch (Exception exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }


        if (lang != Language.ENGLISH && useStanford) {
            System.err.println("Please specify english as language if you want to use the integrated Stanford POS Tagger");
            System.exit(1);
        }


        //////////////////////////////////// end command line parser////////////////////////////////


        KD_core kxc = new KD_core(t);

        KD_Model model = new KD_Model(Paths.get(configuration.languagePackPath ));
        KD_loader.run_the_updater(lang, configuration.languagePackPath);

        StanfordCoreNLP pipeline = null;
        System.out.println("Processor detected: " + cores + " used " + t.toString().toLowerCase());
        if (useStanford) {
            System.out.println("Load Stanford Model");

            System.out.println("Override column configuration");
            configuration.column_configuration = ColumExtraction.CUSTOM;
            configuration.token_position = 0;
            configuration.pos_position = 2;
            configuration.lemma_position = 1;

            System.out.println("Force tagset to STANFORD");
            configuration.tagset = KD_configuration.Tagset.STANFORD;
            Properties props = new Properties();

            props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
            props.setProperty("pos.model", configuration.languagePackPath +"/"+ lang.name() + "/tagger/stanford_model/english-bidirectional-distsim.tagger");
            props.setProperty("pos.nthreads", "4");



            pipeline = new StanfordCoreNLP(props);



        }


        @SuppressWarnings("unchecked")
        List<String> filePaths = line.getArgList();

        StringBuffer processed_filecontent = null;
        LinkedList<KD_keyconcept> concept_list = null;

        for (String filePath : filePaths) {
            List<File> files = new ArrayList<File>();
            if (new File(filePath).isDirectory()) {
                String[] extensions = new String[]{"txt", "txp"};
                files = (List<File>) FileUtils.listFiles(new File(filePath), extensions, true);
            } else if (new File(filePath).isFile()) {

                files.add(new File(filePath));
            }
            for (File f : files) {
                System.out.println("Starting the extraction for file:" + FilenameUtils.getBaseName(f.getAbsolutePath()));
                long startTime = System.currentTimeMillis();

                kxc = new KD_core(t);

                processed_filecontent = new StringBuffer();
                if (useStanford) {
                    if (configuration.verbose) {
                        System.out.print("Start Stanford preprocessing....");
                    }
                    long startTimeStanford = System.currentTimeMillis();
                    try {
                        processed_filecontent = new StringBuffer();


                        Annotation annotation = pipeline.process((new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())))).replace("\r\n"," ").replace("\n"," ")  );
                        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

                        for (CoreMap sentence : sentences) {
                            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                            for (CoreLabel c : tokens) {
                                processed_filecontent.append(c.get(CoreAnnotations.OriginalTextAnnotation.class) + "\t" + c.get(CoreAnnotations.LemmaAnnotation.class) + "\t" + c.get(CoreAnnotations.PartOfSpeechAnnotation.class) + "\n");
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    long estimatedTimeStanford = System.currentTimeMillis() - startTimeStanford;
                    if (configuration.verbose) {
                        System.out.println("End Stanford preprocessing in : " + estimatedTimeStanford);
                    }

                    if (save_stanford) {
                        try {
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FilenameUtils.getFullPath(f.getAbsolutePath()) + FilenameUtils.getBaseName(f.getAbsolutePath()) + "_stanford.tsv"), "UTF-8"));
                            out.write(processed_filecontent.toString());
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }


                concept_list = kxc.extractExpressions(lang, configuration, f.getAbsolutePath(), processed_filecontent);


                if (STDOUT) {
                    int r = 1;
                    StringBuffer output = new StringBuffer();
                    for (KD_keyconcept k : concept_list) {
                        k.getMostUsedVariation();
                        switch (configuration.group_by){
                            case ALL_LEMMA:
                                 output.append(r + "." + "\t" + Joiner.on(" ").join(k.getMostUsedVariation()) +" (" + k.getSysnonyms() + ")\tfrequency: " + k.frequency + " ,score: " + k.score + " idf: " + k.getIdf() + " boost " + k.getScoreBoost() + " pattern_boost " + k.getPatternBoost() + " chain_l " + k.getTokenChainLength() + " stem: "+ k.getStemArray().toString() + " lemma: "+ k.getLemmaArray().toString() +"\n");
                                break;
                            case NONE:
                                output.append(r + "." + "\t" + Joiner.on(" ").join(k.getTokenArray()) +" (" + k.getSysnonyms() + ")\tfrequency: " + k.frequency + " ,score: " + k.score + " idf: " + k.getIdf() + " boost " + k.getScoreBoost() + " pattern_boost " + k.getPatternBoost() + " chain_l " + k.getTokenChainLength() + " stem: "+ k.getStemArray().toString() + " lemma: "+ k.getLemmaArray().toString() +"\n");
                                break;
                            case BY_LIST:
                                output.append(r + "." + "\t" + Joiner.on(" ").join(k.getTokenArray()) +" (" + k.getSysnonyms() + ")\tfrequency: " + k.frequency + " ,score: " + k.score + " idf: " + k.getIdf() + " boost " + k.getScoreBoost() + " pattern_boost " + k.getPatternBoost() + " chain_l " + k.getTokenChainLength() + " stem: "+ k.getStemArray().toString() +"\n");
                                break;
                            case BY_STEM:
                                output.append(r + "." + "\t" + Joiner.on(" ").join(k.getStemArray()) +" (" + k.getSysnonyms() + ")\tfrequency: " + k.frequency + " ,score: " + k.score + " idf: " + k.getIdf() + " boost " + k.getScoreBoost() + " pattern_boost " + k.getPatternBoost() + " chain_l " + k.getTokenChainLength() + " stem: "+ k.getStemArray().toString() +"\n");
                                break;
                        }


                        r++;
                    }
                    if (output.toString().length() > 0) {
                        System.out.println(output.toString().substring(0, output.toString().length() - 1));
                    } else {
                        System.out.println("Mmmmm no keywords extracted... That's strange....");
                    }
                } else {
                    int r = 1;
                    StringBuffer output = new StringBuffer();
                    output.append("rank\tkeyword\tsynonyms\tscore\tfrequency\n");
                    for (KD_keyconcept k : concept_list) {
                        output.append(r + "\t" + k.getString() + "\t" + k.getSysnonyms() + "\t" + k.score + "\t" + k.frequency + "\n");
                        r++;
                    }


                    try {
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FilenameUtils.getFullPath(f.getAbsolutePath()) + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".tsv"), "UTF-8"));
                        out.write(output.toString());
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                long estimatedTime = System.currentTimeMillis() - startTime;
                System.out.println("Finished in: " + estimatedTime + " milliseconds\n");
            }
        }
        //kxc.key_concept_extraction();
    }

}
