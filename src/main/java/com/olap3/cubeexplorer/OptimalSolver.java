package com.olap3.cubeexplorer;

import com.google.common.collect.Sets;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.measures.Jaccard;
import com.olap3.cubeexplorer.optimize.AprioriMetric;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class OptimalSolver {
    @Data @AllArgsConstructor
    static class Solution{
        List<InfoCollector> ic;
        double interest, distance;
    }

    static Solution bestDistance(Set<Solution> candidates){
        Solution best = null; double d = Double.MAX_VALUE;
        for (Solution c : candidates){
            if (c.distance < d){
                d = c.distance;
                best = c;
            }
        }
        return best;
    }

    static Solution bestIm(Set<Solution> candidates){
        Solution best = null; double im = -1;
        for (Solution c : candidates){
            if (c.distance > im){
                im = c.distance;
                best = c;
            }
        }
        return best;
    }

    public static Set<List<InfoCollector>> optimalSolver(Set<InfoCollector> candidates, AprioriMetric im, long timebudget){
        Set<Solution> dominantes = new HashSet<>();

        Sets.powerSet(candidates).stream()
                .filter(s -> s.size() > 0)
                .filter(s -> s.stream().mapToLong(InfoCollector::estimatedTime).sum() <= timebudget)
                .peek(s -> {
                    if(s.size()== candidates.size())
                        System.out.println("\n!!! DEBUG !!!\n");
                })
                .forEach(s -> {
                    var tmp = new ArrayList<>(s);
                    StreamedPermutation.generatePerm(tmp)
                            .stream().filter(p -> p.size() > 1)
                            .forEach(p -> {
                        double interst = 0, dist = 0;
                        for (int i = 0; i < p.size() - 1; i++) {
                            interst += im.rate(p.get(i));
                            dist += 1 - Jaccard.similarity(p.get(i).getDataSource().getInternal(), p.get(i+1).getDataSource().getInternal());
                        }
                        interst += im.rate(p.get(p.size() - 1));
                        Solution us = new Solution(p, interst, dist);

                        Set<Solution> subs = new HashSet<>(); boolean addUs = false;
                        //System.out.println(dominantes);
                        if (dominantes.size() != 0 && (bestDistance(dominantes).distance > us.distance || bestIm(dominantes).interest < us.interest)) {
                            addUs = true;
                            for (Solution dom : dominantes) {
                                if (dom.distance > us.distance && us.interest > dom.interest) {
                                    subs.add(dom);//Dom become a sub so I guess it's a switch ?
                                    //System.out.println(us);
                                }
                            }
                        }

                        if (addUs) { //easy way to avoid concurrent modification
                            dominantes.removeAll(subs);
                            dominantes.add(us);
                        }
                        if (dominantes.size() == 0)// Init case
                            dominantes.add(us);
                    });
                });
        //System.out.println(dominantes.size());
        return dominantes.stream().map(Solution::getIc).collect(Collectors.toSet());
    }


    private static class StreamedPermutation<E> implements Iterator<List<E>> {
        List<E> original;

        public StreamedPermutation(List<E> original) {
            this.original = original;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public List<E> next() {
            return null;
        }

        public static <E> List<List<E>> generatePerm(List<E> original) {
            if (original.size() == 0) {
                List<List<E>> result = new ArrayList<List<E>>();
                result.add(new ArrayList<E>());
                return result;
            }

            E firstElement = original.remove(0);

            List<List<E>> returnValue = new ArrayList<>();
            List<List<E>> permutations = generatePerm(original);

            for (List<E> smallerPermutated : permutations) {
                for (int index=0; index <= smallerPermutated.size(); index++) {
                    List<E> temp = new ArrayList<E>(smallerPermutated);
                    temp.add(index, firstElement);
                    returnValue.add(temp);
                }
            }

            return returnValue;
        }
    }

    public static void main(String[] args) {
        List<Integer> ints = new ArrayList<>(List.of(1,2,3,4));
        Sets.powerSet(new HashSet<>(ints)).forEach(s -> System.out.println(s));
        System.out.println(StreamedPermutation.generatePerm(ints));
    }

}
