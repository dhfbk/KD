package eu.fbk.dh.kd.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 14/09/16.
 *
 * @author Giovanni Moretti
 * @version $Id: $Id
 */
public class DigiKDAnnotations {

    @JSONLabel("keywords")
    public static class KeyphrasesAnnotation implements CoreAnnotation<List<DigiKDResult>> {
        @Override
        public Class<List<DigiKDResult>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }
}
