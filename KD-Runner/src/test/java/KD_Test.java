import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.models.KD_Model;

/**
 * Created by giovannimoretti on 28/02/17.
 */
public class KD_Test {
    public static void main(String[] args) {

        KD_configuration configuration = new KD_configuration();

        KD_Model model = new KD_Model(configuration.getPath());
        KD_core kd_core = new KD_core(KD_core.Threads.TWO);


    }
}
