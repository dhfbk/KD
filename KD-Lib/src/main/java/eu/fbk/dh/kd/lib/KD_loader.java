package eu.fbk.dh.kd.lib;

import eu.fbk.dh.kd.lib.KD_core.Language;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;


/**
 * This object updates the serialized configuration files with the data specified by the user in the configuration txt files
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class KD_loader {

    Map<String, Double> base;


    /**
     * This method regenerate the serialized object from the configuration files
     *
     * @param lang             The language to be updated with the fresh data.
     * @param languagePackPath The path to the languages folder to be updated.
     * @param languagePackPath The path to the languages folder to be updated.
     */
    public static synchronized void run_the_updater(Language lang, String languagePackPath) {

        try {

            String pathPrefix = languagePackPath + File.separator + lang + File.separator;


            if (!new File(pathPrefix + "config.properties").exists()) {
                Properties prop = new Properties();
                OutputStream output = null;
                try {
                    output = new FileOutputStream(pathPrefix + "config.properties");

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
                    System.err.println("Error during the  \"config.properties\" file creation");
                }
            }


            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(pathPrefix + "config.properties");
                prop.load(input);
                input.close();
            } catch (Exception e) {
                System.err.println("Error while loading \"config.properties\" file ");
            }


            if (prop.getProperty("keyconcept_yes_md5") == null) {
                prop.setProperty("keyconcept_yes_md5", "");
            }
            if (prop.getProperty("keyconcept_no_md5") == null) {
                prop.setProperty("keyconcept_no_md5", "");
            }
            if (prop.getProperty("lemma_list_md5") == null) {
                prop.setProperty("lemma_list_md5", "");
            }
            if (prop.getProperty("lemma_blacklist_md5") == null) {
                prop.setProperty("lemma_blacklist_md5", "");
            }
            if (prop.getProperty("stop_list_md5") == null) {
                prop.setProperty("stop_list_md5", "");
            }
            if (prop.getProperty("pos_blacklist_md5") == null) {
                prop.setProperty("pos_blacklist_md5", "");
            }
            if (prop.getProperty("synonyms_md5") == null) {
                prop.setProperty("synonyms_md5", "");
            }
            if (prop.getProperty("invFreq_md5") == null) {
                prop.setProperty("invFreq_md5", "");
            }
            if (prop.getProperty("properNounPosList_md5") == null) {
                prop.setProperty("properNounPosList_md5", "");
            }


            if (!(new File(pathPrefix + "configuration_files" + File.separator +"keyconcept-yes.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"keyconcept-yes.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"keyconcept-no.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"keyconcept-no.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }

            if (!(new File(pathPrefix + "configuration_files" + File.separator +"lemmalist.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"lemmalist.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"lemma-no.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"lemma-no.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"pos-no.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"pos-no.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"stoplist.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"stoplist.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"synonyms.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"synonyms.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"idf_lang.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"idf_lang.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }
            if (!(new File(pathPrefix + "configuration_files" + File.separator +"properNounPosList.txt")).exists()) {
                String filePath = pathPrefix + "configuration_files" + File.separator +"properNounPosList.txt";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
                out.write("");
                out.close();
            }


            if (prop.getProperty("keyconcept_yes_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"keyconcept-yes.txt")) != 0) {
                prop.setProperty("keyconcept_yes_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"keyconcept-yes.txt"));
                System.out.println("Something is changed in the keyconcept white list... I'm working on that!");
                KD_loader.make_whitelist(pathPrefix + "configuration_files" + File.separator +"keyconcept-yes.txt", lang);
            }

            if (prop.getProperty("keyconcept_no_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"keyconcept-no.txt")) != 0) {
                prop.setProperty("keyconcept_no_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"keyconcept-no.txt"));
                System.out.println("Something is changed in the keyconcept black list... I'm working on that!");
                KD_loader.make_blacklist(pathPrefix + "configuration_files" + File.separator +"keyconcept-no.txt", lang);
            }

            if (prop.getProperty("lemma_list_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"lemmalist.txt")) != 0) {
                prop.setProperty("lemma_list_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"lemmalist.txt"));
                System.out.println("Something is changed in the lemma list... I'm working on that!");
                KD_loader.make_lemma_list(pathPrefix + "configuration_files" + File.separator +"lemmalist.txt", lang);
            }


            if (prop.getProperty("lemma_blacklist_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"lemma-no.txt")) != 0) {
                prop.setProperty("lemma_blacklist_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"lemma-no.txt"));
                System.out.println("Something is changed in the lemma black list... I'm working on that!");
                KD_loader.make_lemma_blacklist(pathPrefix + "configuration_files" + File.separator +"lemma-no.txt", lang);
            }


            if (prop.getProperty("pos_blacklist_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"pos-no.txt")) != 0) {
                prop.setProperty("pos_blacklist_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"pos-no.txt"));
                System.out.println("Something is changed in the pos black list... I'm working on that!");
                KD_loader.make_pos_blacklist(pathPrefix + "configuration_files" + File.separator +"pos-no.txt", lang);
            }


            if (prop.getProperty("stop_list_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"stoplist.txt")) != 0) {
                prop.setProperty("stop_list_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"stoplist.txt"));
                System.out.println("Something is changed in the stopword list... I'm working on that!");
                KD_loader.make_stoplist(pathPrefix + "configuration_files" + File.separator +"stoplist.txt", lang);
            }

            if (prop.getProperty("synonyms_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"synonyms.txt")) != 0) {
                prop.setProperty("synonyms_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"synonyms.txt"));
                System.out.println("Something is changed in the synonyms list... I'm working on that!");
                KD_loader.make_synonyms(pathPrefix + "configuration_files" + File.separator +"synonyms.txt", lang);
            }


            if (prop.getProperty("invFreq_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"idf_lang.txt")) != 0) {
                prop.setProperty("invFreq_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"idf_lang.txt"));
                System.out.println("Something is changed in the idf file... I'm working on that!");
                KD_loader.make_invFreqFile(pathPrefix + "configuration_files" + File.separator +"idf_lang.txt", lang);
            }

            if (prop.getProperty("properNounPosList_md5").compareTo(KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"properNounPosList.txt")) != 0) {
                prop.setProperty("properNounPosList_md5", KD_utils.md5(pathPrefix + "configuration_files" + File.separator +"properNounPosList.txt"));
                System.out.println("Something is changed in the proper noun pos file... I'm working on that!");
                KD_loader.make_properNounPosList(pathPrefix + "configuration_files" + File.separator +"properNounPosList.txt", lang);
            }

            try {
                OutputStream output = new FileOutputStream(pathPrefix + "config.properties");
                prop.store(output, null);
            } catch (Exception e) {
                System.err.println("Error while storing \"config.properties\" file ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("mmm... somthing goes wrong in update cofiguration process... try to re-launch");
        }
    }

    private static void make_invFreqFile(String filePath, Language lang) {

        File f = new File(filePath);


        File dbFile = new File(f.getParentFile().getParent().toString() + File.separator + lang.toString() + ".map");

        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();
        //faccio puntare il db ad un oggetto che posso trattare come un hash

        HTreeMap<String, Double> invfreq = db.hashMap("invdocfreq").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();


        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put(lineItems[0], Double.parseDouble(lineItems[1]));
            }

            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

        invfreq.close();
        db.close();
    }

    private static void make_lemma_list(String filePath, Language lang) {

        File f = new File(filePath);


        File dbFile = new File(f.getParentFile().getParent().toString() + File.separator + lang.toString() + ".map");


        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("lemmata").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put(lineItems[0].trim().toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        invfreq.close();
        db.close();


    }

	/*
    public static void make_pattern_list(String filePath) {

		File dbFile = new File("pattern.map");

		if (dbFile.exists()) {
			dbFile.delete();
		}

		DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
		//faccio puntare il db ad un oggetto che posso trattare come un hash
		HTreeMap<String[], String> patterns = db.getHashMap("pattern");

		File f = new File(filePath);
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

			String line = "";
			
			while ((line = in.readLine()) != null) {
				
				if ( !line.trim().startsWith("#") && line.trim().length() > 0){
					
					StringBuilder SB = new StringBuilder(line.replace("\"", "").trim().toUpperCase()).reverse();
					String revsb = SB.toString();
					patterns.put(revsb.split(","),"");
				}
				
			}
			db.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	*/


    private static void make_blacklist(String filePath, Language lang) {


        File f = new File(filePath);
        File dbFile = new File(f.getParentFile().getParent().toString() + File.separator + lang.toString() + ".map");

        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put("÷•÷" + lineItems[0].trim().replace(" ", "÷•÷").replace("_", "÷•÷").toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        invfreq.close();
        db.close();
    }


    private static void make_whitelist(String filePath, Language lang) {


        File f = new File(filePath);
        File dbFile = new File(f.getParentFile().getParent().toString() +  File.separator + lang.toString() + ".map");

        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("whitelist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put(lineItems[0].trim().toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        invfreq.close();
        db.close();
    }


    private static void make_properNounPosList(String filePath, Language lang) {
        File f = new File(filePath);
        File dbFile = new File(f.getParentFile().getParent().toString() +  File.separator + lang.toString() + ".map");
        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("properNounPos_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put(lineItems[0].toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        invfreq.close();
        db.close();
    }


    private static void make_stoplist(String filePath, Language lang) {


        File f = new File(filePath);


        File dbFile = new File(f.getParentFile().getParent().toString() +  File.separator + lang.toString() + ".map");


       // DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db  = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("stoplist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();

        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put("÷•÷" + lineItems[0].trim().replace(" ", "÷•÷").replace("_", "÷•÷").toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        invfreq.close();
        db.close();
    }


    private static void make_lemma_blacklist(String filePath, Language lang) {

        File f = new File(filePath);
        File dbFile = new File(f.getParentFile().getParent().toString() +  File.separator + lang.toString() + ".map");


        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db  = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("lemma_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put(lineItems[0].toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        invfreq.close();
        db.close();
    }


    private static void make_pos_blacklist(String filePath, Language lang) {

        File f = new File(filePath);
        File dbFile = new File(f.getParentFile().getParent().toString() +  File.separator + lang.toString() + ".map");


        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db  = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<String, Double> invfreq = db.hashMap("pos_blacklist").keySerializer(Serializer.STRING).valueSerializer(Serializer.DOUBLE).createOrOpen();
        invfreq.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;

            while ((line = in.readLine()) != null) {
                lineItems = line.split("\t");
                invfreq.put(lineItems[0].toLowerCase(), 0.0);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        invfreq.close();
        db.close();
    }


    private static void make_synonyms(String filePath, Language lang) {
        File f = new File(filePath);


        File dbFile = new File(f.getParentFile().getParent().toString() +  File.separator + lang.toString() + ".map");


        //DB db = DBMaker.newFileDB(dbFile).mmapFileEnable().transactionDisable().closeOnJvmShutdown().make();
        DB db  = DBMaker.fileDB(dbFile).fileMmapEnable().fileMmapEnableIfSupported().fileMmapPreclearDisable().closeOnJvmShutdown().make();

        //faccio puntare il db ad un oggetto che posso trattare come un hash
        HTreeMap<ArrayList<String>, Integer> synonyms = db.hashMap("synonyms").keySerializer(Serializer.JAVA).valueSerializer(Serializer.INTEGER).createOrOpen();
        synonyms.clear();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String[] lineItems;
            String lineModded = "";
            while ((line = in.readLine()) != null) {
                lineModded = line;
                lineModded = "÷•÷" + lineModded;
                lineModded = lineModded.replace(" ", "|÷•÷").replace("_", "÷•÷");
                lineItems = lineModded.split("\\|");
                ArrayList<String> synonymsEntries = new ArrayList<String>();
                for (String s : lineItems) {
                    synonymsEntries.add(s);
                }
                synonyms.put(synonymsEntries, 0);

            }
            in.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        synonyms.close();
        db.close();
    }

}
