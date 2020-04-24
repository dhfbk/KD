import java.util.LinkedList;
import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.lib.KD_core.Language;
import eu.fbk.dh.kd.lib.KD_keyconcept;
import eu.fbk.dh.kd.lib.KD_loader;

public class Example {

	public static void main(String[] args) {
		String pathToFIle = args[0]; //taken from command line

		Language lang = Language.ENGLISH; //Specify language
		KD_configuration configuration = new KD_configuration(); //Creates a new instance of KD_Configuration object

		// Configuration Setup
		configuration.numberOfConcepts = 20;
		configuration.max_keyword_length = 4;
		configuration.local_frequency_threshold = 2;
		configuration.prefer_specific_concept = KD_configuration.Prefer_Specific_Concept.MEDIUM;
		configuration.skip_proper_noun = false;
		configuration.skip_keyword_with_proper_noun = false;
		configuration.rerank_by_position = false;
		configuration.group_by = KD_configuration.Group.ALL_LEMMA;
		configuration.column_configuration = KD_configuration.ColumExtraction.TOKEN_POS_LEMMA;
		configuration.only_multiword = false;
		configuration.tagset = KD_configuration.Tagset.TREETAGGER;

		//configuration.languagePackPath = <languagePackPath String> ;//Overrides the default path with the new one taken from the command line parameter

		//Create an instance of the KD core.
		// Write this code line before the KD_loader.run_the_updater in order to create the dafualt language folder on the first run
		KD_core kd_core = new KD_core(KD_core.Threads.ONE);

		KD_loader.run_the_updater(lang, configuration.languagePackPath); //Updates the configuration file if something is changed



		LinkedList<KD_keyconcept> concept_list = kd_core.extractExpressions(lang, configuration, pathToFIle, null);
		for (KD_keyconcept k : concept_list) { //loop over the extracted key_phrases and print the results
			System.out.println(k + " all_collected_values:" + k.getAllTheVariationsArray());
		}

	}

}
