package eu.fbk.dh.kd.annotator;

import eu.fbk.dh.kd.lib.KD_core;

/**
 * Created by giovannimoretti on 14/09/16.
 *
 * @author giovannimoretti
 * @version $Id: $Id
 */
public class DigiKDModel {

    private static KD_core kd;

    /**
     * <p>getInstance.</p>
     *
     * @param t a {@link eu.fbk.dh.kd.lib.KD_core.Threads} object.
     * @return a {@link eu.fbk.dh.kd.lib.KD_core} object.
     */
    public static KD_core getInstance(KD_core.Threads t) {
        if (kd == null) {
            kd = new KD_core(t);
        }
        return kd;
    }

}
