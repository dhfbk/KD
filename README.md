KD: Keyphrase Digger
====================


Keyphrase Digger (KD) is a rule-based system for keyphrase extraction. It is a Java re-implementation of KX tool (Pianta and Tonelli, 2010) with a new architecture and new features. KD combines statistical measures with linguistic information given by PoS patterns to identify and extract weighted keyphrases from texts.

#### Main Features:

* Extraction of multi-words
* Multilinguality (EN, IT, FR and DE)
* Easily extendible to other languages
* Higher customizability than KX
* High processing speed
* Clustering of keyphrases under the same lemma
* Various accepted formats and PoS tagsets: Stanford PoS Tagger (EN,FR), TreeTagger (IT, DE, FR and EN), TextPro (IT and EN)
* Boost of specific PoS patterns




Introduction
------------

This document describes the API for starting and using in your code the KD tool for the keyphrase extraction. The tool uses both statistical measures and linguistic information to detect a weighted list of n-grams representing the most important concepts of a text.
Example and Tutorial


#### Requirements:

Java 1.8+ is needed


#### Input files:

There are 2 possible input formats depending on the language to process:
RAW TEXT: available only for English and with the -us (use Stanford) option.
CONLL format (i.e. tab separated): available for both English and Italian and for all tagsets. The format must include at least 3 columns: token, PoS, and lemma. It's possible to specify the column position through the column_configuration parameter, see the help for more information

#### How to Run:

open command line shell
go to the KD folder (the folder containing KD.jar)
java -jar KD.jar -lang ENGLISH -p WEAK -us -v -n 50 -m 6 <Folder or File to be processed>

#### Hints:

drag folder containing data to the command line shell in order to obtain the correct and "wrapped" path to the files.
run the tool with -STDOUT option in order to check the output directly from the console without write new file.

Parameter description used in the example:

-lang ENGLISH is the main language of the file  
-p give a boost to more specific key-concept (ie. multi-token expressions). You can change the value of this option to have more or less multi-token expressions: NO | WEAK | MEDIUM | STRONG  
-us use stanford tokenizer,lemmatizer and pos tagger (included in the tool) - only for English  
-n is the number of concepts/key-phrases to be extracted, in the example above is set to 50  
-m is the maximum length of the multi-token expressions to be extracted  

#### For more information on the parameters, run: 
java -jar KD.jar -h

#### Configuration and tuning:

Configuration files are in the following folder and are in txt format:
~/.kd/languages/{Language}/configuration_files

Please, do not change the folder hierarchy!
This folder contains all the files used by the tool to increase performances and to obtain better results.
The file name are self explaining and its format is really understandable and easy to use.

If you use the tool in your code remember to use the KD_loader object in order to update the serialized data file.
e.g : KD_loader.run_the_updater(lang, configuration.languagePackPath);

#### How to use in your code:

Below an example of code integration:

```java
import java.util.LinkedList;
import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.lib.KD_core.Language;
import eu.fbk.dh.kd.lib.KD_keyconcept;
import eu.fbk.dh.kd.lib.KD_loader;

public class Main {

    public static void main(String[] args) {
        String languagePackPath = args[0]; //taken from command line
        String pathToFIle = args[1]; //taken from command line

        Language lang = Language.ITALIAN; //Specify language
        KD_configuration configuration = new KD_configuration(); //Creates a new instance of KD_Configuration object

        // Configuration Setup
        configuration.numberOfConcepts = 20;
        configuration.max_keyword_length = 4;
        configuration.local_frequency_threshold = 2;
        configuration.prefer_specific_concept = KD_configuration.Prefer_Specific_Concept.MEDIUM;
        configuration.skip_proper_noun = false;
        configuration.skip_keyword_with_proper_noun = false;
        configuration.rerank_by_position = false;
        configuration.lemmatization = KD_configuration.Lemmatize.NONE;
        configuration.column_configuration = KD_configuration.ColumExtraction.TOKEN_POS_LEMMA;
        configuration.only_multiword = false;
        configuration.tagset = KD_configuration.Tagset.TEXTPRO;

        configuration.languagePackPath = languagePackPath;//Overrides the default path with the new one taken from the command line parameter

        KD_loader.run_the_updater(lang, configuration.languagePackPath); //Updates the configuration file if something is changed

        KD_core kd_core = new KD_core(KD_core.Threads.TWO);//Create an instance of the KD core

        LinkedList<KD_keyconcept> concept_list = kd_core.extractExpressions(lang, configuration, pathToFIle, null);
        for (KD_keyconcept k : concept_list) { //loop over the extracted key_phrases and print the results
            System.out.println(k.getString() + "\t" + k.getSysnonyms() + "\t" + k.score + "\t" + k.frequency);
        }
    }
}
```    

### Support

This software is provided as it is. For new versions and updates please check the project web page at : KD Key-Phrases Digger at DH FBK


### License:
Keyphrase Digger (KD_Lib) is released under Apache License 2.0.

If you want to use KD-Runner and KD-StanfordAnnotator you have to apply GPLv3 or later due to Stanford CoreNLP license extension. 

### Acknowledgment:
The French patterns have been kindly provided by Tien-Duc Cao (Inria Saclay) and Xavier Tannier (Sorbonne Université). 


### Reference:

Moretti, G., Sprugnoli, R., Tonelli, S. "[Digging in the Dirt: Extracting Keyphrases from Texts with KD](https://iris.unito.it/retrieve/handle/2318/1532924/75495/Accademia_University_Press_978-88-99200-62-6.pdf#page=200)". In Proceedings of the Second Italian Conference on Computational Linguistics (CLiC-it 2015), Trento, Italy.