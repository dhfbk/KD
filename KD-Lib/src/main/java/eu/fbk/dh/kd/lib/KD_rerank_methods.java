package eu.fbk.dh.kd.lib;


import eu.fbk.dh.kd.lib.KD_concept_sorter.Sort;
import eu.fbk.dh.kd.lib.KD_concept_sorter.SortDirection;

import java.util.*;
import java.util.Map.Entry;

/**
 * Util re-rank Class of the tool.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */

class KD_rerank_methods {

    public enum Method {
        SHORTER_FIRST_BY_AVERAGE, LONGER_FIRST_BY_ABSORBTION
    }

    private Integer averaging_cycles = 1;
    private Integer absorbtion_cycles = 1;
    Map<String, KD_keyconcept> base = new LinkedHashMap<String, KD_keyconcept>();
    LinkedHashSet<KD_keyconcept> valuesOfBase = null;
    LinkedHashSet<KD_keyconcept> valuesOfBaseByLength = null;
    Method sort_method = Method.SHORTER_FIRST_BY_AVERAGE;
    boolean rerank_shorter_first_by_boosting = false;

    /**
     * <p>Constructor for KD_rerank_methods.</p>
     *
     * @param base a {@link java.util.Map} object.
     * @param m a {@link eu.fbk.dh.kd.lib.KD_rerank_methods.Method} object.
     * @param cycles a {@link java.lang.Integer} object.
     */
    public KD_rerank_methods(Map<String, KD_keyconcept> base, Method m, Integer cycles) {
        this.base.putAll(base);
        this.sort_method = m;
       // this.valuesOfBase = new LinkedHashSet<KD_keyconcept>(this.base.values());
        this.averaging_cycles = cycles;
        this.absorbtion_cycles = cycles;

    }

    /**
     * <p>Constructor for KD_rerank_methods.</p>
     *
     * @param base a {@link java.util.Map} object.
     * @param m a {@link eu.fbk.dh.kd.lib.KD_rerank_methods.Method} object.
     * @param cycles a {@link java.lang.Integer} object.
     * @param rerank_shorter_first_by_boosting a boolean.
     */
    public KD_rerank_methods(Map<String, KD_keyconcept> base, Method m, Integer cycles, boolean rerank_shorter_first_by_boosting) {
        this.base.putAll(base);
        this.sort_method = m;
      //  this.valuesOfBase = new LinkedHashSet<KD_keyconcept>(this.base.values());
        this.averaging_cycles = cycles;
        this.absorbtion_cycles = cycles;
        this.rerank_shorter_first_by_boosting = rerank_shorter_first_by_boosting;
    }

    /**
     * <p>getTheHigherConcept.</p>
     *
     * @param currentConceptOBJ a {@link eu.fbk.dh.kd.lib.KD_keyconcept} object.
     * @param distance a {@link java.lang.Integer} object.
     * @return a {@link eu.fbk.dh.kd.lib.KD_keyconcept} object.
     */
    public KD_keyconcept getTheHigherConcept(KD_keyconcept currentConceptOBJ, Integer distance) {
        valuesOfBase.remove(currentConceptOBJ);
        KD_keyconcept out = null;
        Iterator<KD_keyconcept> it_sub = valuesOfBase.iterator();
        while (it_sub.hasNext()) {
            KD_keyconcept higherConcept = it_sub.next();
            if (KD_utils.findArray(currentConceptOBJ.elements, higherConcept.elements) >= 0) {
                out = higherConcept;
            }
            if (distance == 0) {
                break;
            }
            distance--;
        }
        return out;
    }

    /**
     * <p>getTheHigherConceptVariations.</p>
     *
     * @param currentConceptOBJ a {@link eu.fbk.dh.kd.lib.KD_keyconcept} object.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getTheHigherConceptVariations(KD_keyconcept currentConceptOBJ) {
        int variation = 0;
        Iterator<KD_keyconcept> it_sub = valuesOfBaseByLength.iterator();
        while (it_sub.hasNext()) {
            KD_keyconcept higherConcept = it_sub.next();
            if (KD_utils.findArray(higherConcept.elements, currentConceptOBJ.elements) >= 0) {
               /// System.out.println("L: " + currentConceptOBJ.getString()+ " - " + currentConceptOBJ.score +  "    V: " + higherConcept.getString() + " - " + higherConcept.score);
                variation++;
            }

        }
        return variation-1;
    }




    /**
     * <p>getTheLongerHigherConcept.</p>
     *
     * @param currentConceptOBJ a {@link eu.fbk.dh.kd.lib.KD_keyconcept} object.
     * @return a {@link eu.fbk.dh.kd.lib.KD_keyconcept} object.
     */
    public KD_keyconcept getTheLongerHigherConcept(KD_keyconcept currentConceptOBJ) {
        valuesOfBase.remove(currentConceptOBJ);
        KD_keyconcept out = null;
        Iterator<KD_keyconcept> it_sub = valuesOfBase.iterator();
        while (it_sub.hasNext()) {
            KD_keyconcept higherConcept = it_sub.next();
            if (KD_utils.findArray(currentConceptOBJ.elements, higherConcept.elements) >= 0) {
                out = higherConcept;
            }
        }
        return out;

    }

    /**
     * <p>rerank.</p>
     *
     * @return a int.
     */
    public int rerank() {


        switch (this.sort_method) {
            case SHORTER_FIRST_BY_AVERAGE:

                Map<String, KD_keyconcept> reorderedByLength = new HashMap<String, KD_keyconcept>();
                reorderedByLength.putAll(KD_concept_sorter.sort(this.base, Sort.LENGTH, SortDirection.ASC));


                this.valuesOfBaseByLength = new LinkedHashSet<KD_keyconcept>(reorderedByLength.values());


                for (int i = 1; i <= this.averaging_cycles; i++) {
                    this.base = KD_concept_sorter.sort(this.base, Sort.SCORE, SortDirection.ASC);
                    Map<KD_keyconcept,Double> deboost_coefficient = new HashMap<KD_keyconcept, Double>();
                    this.valuesOfBase = new LinkedHashSet<KD_keyconcept>(this.base.values());
                    Iterator<Entry<String, KD_keyconcept>> it = this.base.entrySet().iterator();
                    while (it.hasNext()) {

                        Entry<String, KD_keyconcept> entry = it.next();
                        //Integer currentLen = entry.getValue().elements.size();
                        if (entry.getValue().score > 0) {

                            //Integer variations = getTheHigherConceptVariations (entry.getValue());

                            //System.out.println(entry.getValue().getString() +" vartiations" +variations);

                            KD_keyconcept highConcept = getTheHigherConcept(entry.getValue(), 15);

                            if (highConcept != null && highConcept.score > 0) {
                                if (rerank_shorter_first_by_boosting) {
                                    entry.getValue().score = highConcept.score ;
                                } else {
                                    if (deboost_coefficient.containsKey(highConcept)){
                                        deboost_coefficient.put(highConcept,deboost_coefficient.get(highConcept)+ 0.3);
                                    }else{
                                        deboost_coefficient.put(highConcept,0.2);
                                    }

                                    //System.out.println("L: " + entry.getValue().getString() + " score " + entry.getValue().score + "     H: " + highConcept.getString() + " score " + highConcept.score );



                                    double averageScore = (entry.getValue().score + highConcept.score) / 2;
                                    highConcept.score -= entry.getValue().score * deboost_coefficient.get(highConcept);
                                    entry.getValue().score += averageScore;

                                    /*
                                    double averageScore = (entry.getValue().score + highConcept.score) / 2;
                                    highConcept.score -= averageScore;
                                    entry.getValue().score = averageScore;
                                    */
                                    //System.out.println("L: " + entry.getValue().getString() + " score " + entry.getValue().score + "     H: " + highConcept.getString() + " score " + highConcept.score );



                                }
                            }

                        } else {
                            this.valuesOfBase.remove(entry.getValue());
                        }
                    }
                    this.valuesOfBase = new LinkedHashSet<KD_keyconcept>(this.base.values());
                }
                break;
            case LONGER_FIRST_BY_ABSORBTION:

                for (int i = 1; i <= this.absorbtion_cycles; i++) {
                    this.base = KD_concept_sorter.sort(this.base, Sort.LENGTH, SortDirection.DESC);

                    this.valuesOfBase = new LinkedHashSet<KD_keyconcept>(this.base.values());
                    //System.out.println("ciclo");
                    Iterator<Entry<String, KD_keyconcept>> it = this.base.entrySet().iterator();

                    while (it.hasNext()) {
                        Entry<String, KD_keyconcept> entry = it.next();
                        //Integer currentLen = entry.getValue().elements.size();

                        if (entry.getValue().score > 0) {
                            KD_keyconcept higherLongerConcept = getTheLongerHigherConcept(entry.getValue());

                            if (higherLongerConcept != null && higherLongerConcept.score > 0) {
                                //System.out.println("LONGER current concept: " + entry.getValue() + " higherconcept: " + higherLongerConcept);

                                // if rerank by position available soon
                                //else
                               // System.out.println("  LONGER "+i+" current concept: " + entry.getValue().getString() + " higherconcept: " + higherLongerConcept.getString());

                                double newScore = (entry.getValue().score + higherLongerConcept.score);
                                entry.getValue().score += newScore;
                                higherLongerConcept.score =  higherLongerConcept.score/3.0;
                                //System.out.println("LONGER current concept: " + entry.getValue() + " higherconcept: " + higherLongerConcept);
                            }

                        } else {
                            this.valuesOfBase.remove(entry.getValue());
                        }
                    }
                    this.valuesOfBase = new LinkedHashSet<KD_keyconcept>(this.base.values());

                }
                break;
        }

        return 0;
    }

}
