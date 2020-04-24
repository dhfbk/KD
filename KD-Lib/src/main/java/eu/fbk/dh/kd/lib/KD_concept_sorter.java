package eu.fbk.dh.kd.lib;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Util Comparator Class of the tool.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
class KD_concept_sorter {
    public enum Sort {
        FREQ, SCORE, LENGTH
    }

    public enum SortDirection {
        ASC, DESC
    }

    /**
     * <p>sort.</p>
     *
     * @param collection a {@link java.util.Map} object.
     * @param sort_method a {@link eu.fbk.dh.kd.lib.KD_concept_sorter.Sort} object.
     * @param sort_direction a {@link eu.fbk.dh.kd.lib.KD_concept_sorter.SortDirection} object.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, KD_keyconcept> sort(Map<String, KD_keyconcept> collection, Sort sort_method, SortDirection sort_direction) {
        KD_concept_comparator bvc = new KD_concept_comparator(collection, sort_method, sort_direction);
        TreeMap<String, KD_keyconcept> sorted_expression = new TreeMap<String, KD_keyconcept>(bvc);
        sorted_expression.putAll(collection);
        Map<String, KD_keyconcept> results = new LinkedHashMap<String, KD_keyconcept>();
        results.putAll(sorted_expression);
        return results;
    }


}

/**
 * Util Comparator Class of the tool.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
class KD_concept_comparator implements Comparator<String> {


    Map<String, KD_keyconcept> base;


    KD_concept_sorter.Sort sort_method;
    KD_concept_sorter.SortDirection sort_direction = KD_concept_sorter.SortDirection.DESC;

    /**
     * <p>Constructor for KD_concept_comparator.</p>
     *
     * @param base a {@link java.util.Map} object.
     * @param sort_metohd a {@link eu.fbk.dh.kd.lib.KD_concept_sorter.Sort} object.
     */
    public KD_concept_comparator(Map<String, KD_keyconcept> base, KD_concept_sorter.Sort sort_metohd) {
        this.base = base;
        this.sort_method = sort_metohd;
    }


    /**
     * <p>Constructor for KD_concept_comparator.</p>
     *
     * @param base a {@link java.util.Map} object.
     * @param sort_metohd a {@link eu.fbk.dh.kd.lib.KD_concept_sorter.Sort} object.
     * @param direction a {@link eu.fbk.dh.kd.lib.KD_concept_sorter.SortDirection} object.
     */
    public KD_concept_comparator(Map<String, KD_keyconcept> base, KD_concept_sorter.Sort sort_metohd, KD_concept_sorter.SortDirection direction) {
        this.base = base;
        this.sort_method = sort_metohd;
        this.sort_direction = direction;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    /**
     * <p>compare.</p>
     *
     * @param a a {@link java.lang.String} object.
     * @param b a {@link java.lang.String} object.
     * @return a int.
     */
    public int compare(String a, String b) {

        switch (this.sort_direction) {
            case ASC:
                switch (this.sort_method) {
                    case FREQ:
                        if (base.get(a).frequency <= base.get(b).frequency) {
                            return -1;
                        } else {
                            return 1;
                        }
                    case SCORE:
                        if (base.get(a).score <= base.get(b).score) {
                            return -1;
                        } else {
                            return 1;
                        }

                    default:
                        if (base.get(a).chainlenght <= base.get(b).chainlenght) {
                            return -1;
                        } else {
                            return 1;
                        }
                }
            default:
                switch (this.sort_method) {
                    case FREQ:
                        if (base.get(a).frequency >= base.get(b).frequency) {
                            return -1;
                        } else {
                            return 1;
                        }
                    case SCORE:
                        if (base.get(a).score >= base.get(b).score) {
                            return -1;
                        } else {
                            return 1;
                        }

                    default:
                        if (base.get(a).chainlenght >= base.get(b).chainlenght) {
                            return -1;
                        } else {
                            return 1;
                        }
                }

        }


    }
}
