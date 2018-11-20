import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.kd.annotator.DigiKDAnnotations;
import eu.fbk.dh.kd.annotator.DigiKDResult;
import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.lib.KD_loader;
import eu.fbk.dh.kd.models.KD_Model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class StanfordKDAnnotatorTest {
    public static void main(String[] args) {
        String text = "Obama was born in 1961 in Honolulu, Hawaii, two years after the territory was admitted to the Union as the 50th state. After graduating from Columbia University in 1983, he worked as a community organizer in Chicago. In 1988, he enrolled in Harvard Law School, where he was the first black president of the Harvard Law Review. After graduating, he became a civil rights attorney and a professor, teaching constitutional law at the University of Chicago Law School from 1992 to 2004. He represented the 13th district for three terms in the Illinois Senate from 1997 to 2004, when he ran for the U.S. Senate. He received national attention in 2004 with his March primary win, his well-received July Democratic National Convention keynote address, and his landslide November election to the Senate. In 2008, he was nominated for president a year after his campaign began and after a close primary campaign against Hillary Clinton. He was elected over Republican John McCain and was inaugurated on January 20, 2009. Nine months later, he was named the 2009 Nobel Peace Prize laureate.\n" +
                "\n" +
                "During his first two years in office, Obama signed many landmark bills into law. The main reforms were the Patient Protection and Affordable Care Act (often referred to as \"Obamacare\", shortened as the \"Affordable Care Act\"), the Dodd–Frank Wall Street Reform and Consumer Protection Act, and the Don't Ask, Don't Tell Repeal Act of 2010. The American Recovery and Reinvestment Act of 2009 and Tax Relief, Unemployment Insurance Reauthorization, and Job Creation Act of 2010 served as economic stimulus amidst the Great Recession. After a lengthy debate over the national debt limit, he signed the Budget Control and the American Taxpayer Relief Acts. In foreign policy, he increased U.S. troop levels in Afghanistan, reduced nuclear weapons with the United States–Russia New START treaty, and ended military involvement in the Iraq War. He ordered military involvement in Libya in opposition to Muammar Gaddafi; Gaddafi was killed by NATO-assisted forces. He also ordered the military operations that resulted in the deaths of Osama bin Laden and suspected Yemeni Al-Qaeda operative Anwar al-Awlaki.\n" +
                "\n" +
                "After winning re-election by defeating Republican opponent Mitt Romney, Obama was sworn in for a second term in 2013. During this term, he promoted inclusiveness for LGBT Americans. His administration filed briefs that urged the Supreme Court to strike down same-sex marriage bans as unconstitutional (United States v. Windsor and Obergefell v. Hodges); same-sex marriage was fully legalized in 2015 after the Court ruled that a same-sex marriage ban was unconstitutional in Obergefell. He advocated for gun control in response to the Sandy Hook Elementary School shooting, indicating support for a ban on assault weapons, and issued wide-ranging executive actions concerning climate change and immigration. In foreign policy, he ordered military intervention in Iraq in response to gains made by ISIL after the 2011 withdrawal from Iraq, continued the process of ending U.S. combat operations in Afghanistan in 2016, promoted discussions that led to the 2015 Paris Agreement on global climate change, initiated sanctions against Russia following the invasion in Ukraine and again after Russian interference in the 2016 United States elections, brokered a nuclear deal with Iran, and normalized U.S. relations with Cuba. During his term in office, America's reputation in global polling significantly improved.[2] Evaluations of his presidency among historians and the general public place him among the upper tier of American presidents. Obama left office in January 2017 and currently resides in Washington, D.C.[3][4]";


        Properties props = new Properties();
        props.setProperty("customAnnotatorClass.keyphrase", "eu.fbk.dh.kd.annotator.DigiKDAnnotator");
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, keyphrase");

        StanfordCoreNLP pipeline  = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<DigiKDResult> r = annotation.get(DigiKDAnnotations.KeyphrasesAnnotation.class);
        System.out.println(r);

    }
}
