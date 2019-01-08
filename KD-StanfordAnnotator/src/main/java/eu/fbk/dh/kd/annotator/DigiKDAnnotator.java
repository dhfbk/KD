package eu.fbk.dh.kd.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.lib.KD_keyconcept;
import eu.fbk.dh.kd.lib.KD_loader;

import java.util.*;

/**
 * Created by giovannimoretti on 22/05/16.
 *
 * @author giovannimoretti
 * @version $Id: $Id
 */
public class DigiKDAnnotator implements Annotator {

    private KD_configuration configuration = new KD_configuration();
    private KD_core.Language lang = KD_core.Language.ENGLISH;
    private KD_core kd;


    /**
     * Stanford Annotator for KD Keyphrase extractor
     * List of properties that can be used to configure KD
     * <ul>
     * <li>languageFolder - String</li>
     * <li>language - String</li>
     * <li>prefer_specific_concept - String</li>
     * <li>group_by - String</li>
     * <li>numberOfConcepts - Integer</li>
     * <li>local_frequency_threshold - Integer</li>
     * <li>max_keyword_length - Integer</li>
     * <li>update - Boolean</li>
     * <li>no_abstract - Boolean</li>
     * <li>skip_proper_noun - Boolean</li>
     * <li>skip_keyword_with_proper_noun - Boolean</li>
     * <li>skip_keyword_with_not_allowed_words - Boolean</li>
     * <li>skipFrequencyAbsorption - Boolean</li>
     * <li>use_pattern_weight - Boolean</li>
     * <li>rerank_by_position - Boolean</li>
     * </ul>
     *
     * @param annotatorName a {@link java.lang.String} object.
     * @param prop a {@link java.util.Properties} object.
     */
    public DigiKDAnnotator(String annotatorName, Properties prop) {


        this.lang = KD_core.Language.valueOf(prop.getProperty(annotatorName + ".language", "ENGLISH").toUpperCase());

        if (prop.getProperty(annotatorName + ".language", "ENGLISH").toUpperCase().equals("CUSTOM")){
            this.lang = KD_core.Language.CUSTOM;
            this.lang.set_Custom_Language(prop.getProperty(annotatorName + ".languageName", "ENGLISH").toUpperCase());
        }



        configuration.languagePackPath = prop.getProperty(annotatorName + ".languageFolder", KD_configuration.getDefault_laguage_pack_localtion());

        configuration.only_multiword = Boolean.parseBoolean(prop.getProperty(annotatorName + ".only_multiword", "false"));

        configuration.numberOfConcepts = Integer.parseInt(prop.getProperty(annotatorName + ".numberOfConcepts", "20"));
        configuration.local_frequency_threshold = Integer.parseInt(prop.getProperty(annotatorName + ".local_frequency_threshold", "2"));
        configuration.max_keyword_length = Integer.parseInt(prop.getProperty(annotatorName + ".max_keyword_length", "4"));

        configuration.prefer_specific_concept = KD_configuration.Prefer_Specific_Concept.valueOf(prop.getProperty(annotatorName + ".prefer_specific_concept", "MEDIUM").toUpperCase());
        configuration.tagset = KD_configuration.Tagset.STANFORD;
        configuration.column_configuration = KD_configuration.ColumExtraction.TOKEN_LEMMA_POS;
        configuration.group_by = KD_configuration.Group.valueOf(prop.getProperty(annotatorName + ".group_by", "NONE").toUpperCase());

        configuration.no_syn = Boolean.parseBoolean(prop.getProperty(annotatorName + ".no_syn", "false"));
//        configuration.use_lucene = Boolean.parseBoolean(prop.getProperty(annotatorName + ".use_lucene", "false"));
        configuration.no_idf = Boolean.parseBoolean(prop.getProperty(annotatorName + ".no_idf", "false"));
        configuration.use_pattern_weight = Boolean.parseBoolean(prop.getProperty(annotatorName + ".use_pattern_weight", "false"));
        configuration.capitalize_pos = Boolean.parseBoolean(prop.getProperty(annotatorName + ".capitalize_pos", "false"));
        configuration.boost_acronyms = Boolean.parseBoolean(prop.getProperty(annotatorName + ".boost_acronyms", "false"));


        configuration.skipFrequencyAbsorption = Boolean.parseBoolean(prop.getProperty(annotatorName + ".skipFrequencyAbsorption", "false"));

        configuration.skip_keyword_with_proper_noun = Boolean.parseBoolean(prop.getProperty(annotatorName + ".skip_keyword_with_proper_noun", "true"));
        configuration.skip_proper_noun = Boolean.parseBoolean(prop.getProperty(annotatorName + ".skip_proper_noun", "true"));
        configuration.use_pattern_weight = Boolean.parseBoolean(prop.getProperty(annotatorName + ".use_pattern_weight", "false"));
        configuration.no_abstract = Boolean.parseBoolean(prop.getProperty(annotatorName + ".no_abstract", "true"));
        configuration.rerank_by_position = Boolean.parseBoolean(prop.getProperty(annotatorName + ".rerank_by_position", "false"));



        configuration.skip_keyword_with_not_allowed_words = Boolean.parseBoolean(prop.getProperty(annotatorName + ".skip_keyword_with_not_allowed_words", "true"));
        configuration.verbose = false;

        if (Boolean.parseBoolean(prop.getProperty(annotatorName + ".update", "false"))) {
            KD_loader.run_the_updater(this.lang, configuration.languagePackPath);
        }

        int cores = Runtime.getRuntime().availableProcessors();
        KD_core.Threads t;
        switch (cores) {
            case 1:
                t = KD_core.Threads.ONE;
                break;
            case 2:
                t = KD_core.Threads.TWO;
                break;
            case 4:
                t = KD_core.Threads.FOUR;
                break;
            case 6:
                t = KD_core.Threads.SIX;
                break;
            case 8:
                t = KD_core.Threads.EIGHT;
                break;
            case 10:
                t = KD_core.Threads.TEN;
                break;
            case 12:
                t = KD_core.Threads.TWELVE;
                break;
            default:
                t = KD_core.Threads.TWO;
                break;
        }
        if (cores > 12) {
            t = KD_core.Threads.TWELVE;
        }

        this.kd = DigiKDModel.getInstance(t);

    }

    /** {@inheritDoc} */
    public void annotate(Annotation annotation) {

        StringBuffer doc = new StringBuffer();


        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    doc.append(c.word() + "\t" + c.get(CoreAnnotations.LemmaAnnotation.class) + "\t" + c
                            .get(CoreAnnotations.PartOfSpeechAnnotation.class) + "\n");
                }
                doc.append("\n");
            }
        }
        List<DigiKDResult> listOfKeys = new ArrayList<>();

        try {
            for (KD_keyconcept k : kd.extractExpressions(this.lang, this.configuration, null, doc)) {
                listOfKeys
                        .add(new DigiKDResult(k.getString(), k.frequency, k.score, k.getLemmaArray(), k.getTokenArray(),
                                k.getPosList(),k.getStemArray(),k.getSysnonymsArray(),k.getIdf(),k.getScoreBoost(),k.getPatternBoost(),k.getTokenChainLength()));
            }

        } catch (NullPointerException n) {

        }
        annotation.set(DigiKDAnnotations.KeyphrasesAnnotation.class, listOfKeys);

    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DigiKDAnnotations.KeyphrasesAnnotation.class);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class
        )));
    }

}

