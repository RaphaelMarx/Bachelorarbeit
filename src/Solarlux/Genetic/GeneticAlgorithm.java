package Solarlux.Genetic;

import Solarlux.CuttingStockInstance;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by Raphael on 11.01.2018.
 */
public class GeneticAlgorithm {
    private ArrayList<Solution> solutionList;
    private GeneticInstance instance;

    private Solution bestSolution = null;
    private double bestFitness = -1;

    /**
     * creates a genetic algorithm with a random initial solution
     *
     * @param instance Genetic instance that will be solved solved
     */
    public GeneticAlgorithm(CuttingStockInstance instance) {
        this(new GeneticInstance(instance), 10);
    }

    /**
     * creates a genetic algorithm with a random initial solution
     *
     * @param instance Genetic instance that will be solved solved
     * @param initSize size of the initial solution
     */
    public GeneticAlgorithm(GeneticInstance instance, int initSize) {
        this(instance, generateSolutions(instance, initSize));
    }

    /**
     * creates a genetic algorithm with a given population
     * @param instance Genetic instance that will be solved solved
     * @param initialPop the initial population
     */
    public GeneticAlgorithm(GeneticInstance instance, ArrayList<Solution> initialPop){    //makes it possible to redo the algorithm with the same start by saving the initial population
        this.instance = instance;
        this.solutionList = initialPop;

        for (Solution sol: solutionList
                ) {
            if(sol.getFitnessValue() > bestFitness){
                bestFitness = sol.getFitnessValue();
                bestSolution = sol;
            }
        }
    }

    /**
     * Generates the random initial solutions for the population
     *
     * @param instance Genetic instance that will be solved
     * @param initSize size of the initial population
     * @return the initial Population as an ArrayList of Solutions
     */
    private static ArrayList<Solution> generateSolutions(GeneticInstance instance, int initSize) {
        ArrayList<Solution> initPop = new ArrayList<>(120);
        for (int i = 0; i < initSize; i++) {
            initPop.add(new Solution(instance.getElementLength(), instance.getElementOrderNumber(), instance.getMaterialLength()));
        }
        int[] ffdBinRep = new int[instance.getElementLength().length];
        for (int i = 0; i < ffdBinRep.length; i++) {
            ffdBinRep[i] = -1;
        }
        initPop.add(new Solution(instance.getElementLength(), instance.getElementOrderNumber(), ffdBinRep, new TIntArrayList(10), instance.getMaterialLength()));
        return initPop;
    }

    /**
     * solves the problem instance with the genetic algorithm
     * @return the best found solution
     */
    public Solution solve() {
        final int ABBRUCHKRITERIUM = 1000;
        final double MUTATIONPROBABILITY = 0.04;
        final int maxSize = 100;
        final int numberDelete = 10;
        final int numberOfParents = 10;

        for (int i = 0; i < ABBRUCHKRITERIUM; i++) {
            Solution[] parents = getParents(numberOfParents);

            Solution[] child = crossoverFalkenauer(parents);
            child[0].mutate(MUTATIONPROBABILITY);
            child[1].mutate(MUTATIONPROBABILITY);
            integrateChild(child, maxSize, numberDelete);
        }
        return bestSolution.convertSolutionFromWithExtra(instance);
    }

    /**
     * gets 2 parents out of the solution by tournament selection
     * @param participants number of solutions in the tournament
     * @return the best two solutions in the tournament
     */
    private Solution[] getParents(int participants){
        // gets two Solutions from the Population that act as parents for the in the next step generated child
        if (participants < 2) {
            System.out.println("Interner Fehler bei der Auswahl der Eltern. Dieser Fehler sollte nicht auftreten.");
            System.exit(1);
        }
        double parentFitness1 = -1;
        int parentIndex1 = -1;
        double parentFitness2 = -1;
        int parentIndex2 = -1;
        int[] randomIndices = getShuffledIndex(participants);


        for (int randomIndex : randomIndices) {
            double currentFitness = solutionList.get(randomIndex).getFitnessValue();
            if (currentFitness > parentFitness1) {
                parentFitness2 = parentFitness1;
                parentIndex2 = parentIndex1;
                parentFitness1 = currentFitness;
                parentIndex1 = randomIndex;
            } else if (currentFitness > parentFitness2) {
                parentFitness2 = currentFitness;
                parentIndex2 = randomIndex;
            }
        }
        Solution[] parents = new Solution[2];
        parents[0] = solutionList.get(parentIndex1);
        parents[1] = solutionList.get(parentIndex2);
        return parents;
    }

    /**
     * selects the participants of the tournament randomly by ordering the indices of the items in a
     * random way and selecting the last ones
     * @param participants number of parents in the tournament
     * @return indices of the parents in the tournament
     */
    private int[] getShuffledIndex(int participants) {  //fisher yates variant - each element can only get picked once
        int[] partshuffled = IntStream.range(0, solutionList.size()).toArray();
        int[] solutionIndex = new int[participants];
        Random rand = new Random();
        for (int i = 0; i < participants; i++) {
            int indexShuffle = rand.nextInt(partshuffled.length - i); //because in rand.nextInt(x) the number x is exclusive
            int value = partshuffled[indexShuffle];
            partshuffled[indexShuffle] = partshuffled[partshuffled.length - 1 - i];
            solutionIndex[i] = value;
        }
        return solutionIndex;
    }

    /**
     * performes the corssoveroperation with two parents to generate two children
     * @param parents the parent solutions
     * @return the child solutions
     */
    private Solution[] crossoverFalkenauer(Solution[] parents) {


        Solution[] children = new Solution[2];

        //does a crossover on basis of Falkenauer returns the generated childSolution
        TIntArrayList binGrouping1 = parents[0].getBinGrouping();      //ParentBinGroupings
        TIntArrayList binGrouping2 = parents[1].getBinGrouping();


        int[] crossingSites = generateCrossingSites(binGrouping1.size(), binGrouping2.size());    //generate random crossingPoints for each parent


        int numberOfElements = parents[0].getElementLength().length;

        TIntArrayList searchForBin1 = new TIntArrayList();           //contains all all Binrepresentations that will be taken from Parent1
        TIntArrayList searchForBin2 = new TIntArrayList();           //same for Parent2


        TIntArrayList childBinGrouping1 = new TIntArrayList();
        TIntArrayList childBinGrouping2 = new TIntArrayList();


        int[] binRep1 = parents[0].getElementBinRepresentation();
        int[] binRep2 = parents[1].getElementBinRepresentation();


        for (int i = crossingSites[0]; i < crossingSites[1]; i++) {       //get all bins from P1 that will be given to the child
            int binNumber = binGrouping1.get(i);
            searchForBin1.add(binNumber);
        }
        for (int i = crossingSites[2]; i < crossingSites[3]; i++) {       //get all bins from P2 that will be given to the child
            int binNumber = binGrouping2.get(i);
            searchForBin2.add(binNumber);
        }


        int[] child1BinRepresentation = makeChildBinRepresentation(numberOfElements, binRep1, binRep2, searchForBin1, searchForBin2, childBinGrouping1);
        int[] child2BinRepresentation = makeChildBinRepresentation(numberOfElements, binRep2, binRep1, searchForBin2, searchForBin1, childBinGrouping2);


        children[0] = new Solution(Arrays.copyOf(parents[0].getElementLength(), numberOfElements), Arrays.copyOf(parents[0].getElementOrderNumber(), numberOfElements), child1BinRepresentation, childBinGrouping1, parents[0].getMaterialLength());
        children[1] = new Solution(Arrays.copyOf(parents[0].getElementLength(), numberOfElements), Arrays.copyOf(parents[0].getElementOrderNumber(), numberOfElements), child2BinRepresentation, childBinGrouping2, parents[0].getMaterialLength());
        //the empty values in the child representation are filled when the solution gets constructed

        return children;
    }

    /**
     * Generates random CrossingSites for each parent and returns them in for each parent in the right order.
     * @param binGroupingSize1  the number of bins used in parent1
     * @param binGroupingSize2  the number of bins used in parent2
     * @return  the int-Array crossingSites which contains 4 numbers, where the crossover will be made (in the binGrouping).
     *          the first two values are for the first parent and the other two for the second parent.
     */
    private int[] generateCrossingSites(int binGroupingSize1, int binGroupingSize2){
        int[] crossingSites = new int[4];

        Random rand = new Random();
        int oneP1 = rand.nextInt(binGroupingSize1);
        int twoP1 = rand.nextInt(binGroupingSize1);
        int oneP2 = rand.nextInt(binGroupingSize2);
        int twoP2 = rand.nextInt(binGroupingSize2);

        if (oneP1 < twoP1){
            crossingSites[0] = oneP1;
            crossingSites[1] = twoP1;
        } else{
            crossingSites[0] = twoP1;
            crossingSites[1] = oneP1;
        }

        if (oneP2 < twoP2){
            crossingSites[2] = oneP2;
            crossingSites[3] = twoP2;
        } else{
            crossingSites[2] = twoP2;
            crossingSites[3] = oneP2;
        }
        return crossingSites;
    }

    /**
     * Creates the bin representation for the child
     *
     * @param numberOfElements number of elemnents in the instance
     * @param binRep1          bin representation of parent 1
     * @param binRep2          bin representation of parent 2
     * @param searchForBin1    items which assignment should be transferred from parent 1 to the child
     * @param searchForBin2    items which assignment should be transferred from parent 2 to the child
     * @param childBinGrouping the bin grouping of the child
     * @return the bin representation of the child solution
     */
    private int[] makeChildBinRepresentation(int numberOfElements, int[] binRep1, int[] binRep2, TIntArrayList searchForBin1, TIntArrayList searchForBin2, TIntArrayList childBinGrouping) {
        int[] childBinRepresentation = new int[numberOfElements];
        for (int i = 0; i < childBinRepresentation.length; i++) {           //set the default Value for each Element to -1 which means that the Element is not assigned to a bin
            childBinRepresentation[i] = -1;
        }


        int bin1SearchSize = searchForBin1.size();
        for (int i = 0; i < bin1SearchSize; i++) {
            boolean added = false;
            int search = searchForBin1.get(i);
            for (int j = 0; j < numberOfElements; j++) {

                if (childBinRepresentation[j] == -1 && binRep1[j] == search) {
                    if (!childBinGrouping.contains(i)) { //if an item gets added, the number gets also added to the bin grouping
                        childBinGrouping.add(i);
                    }
                    added = true;
                    childBinRepresentation[j] = i;
                }
            }
            if (!added) {
                System.out.println("Der Crossover-Operator ist fehlgeschlagen. Diese Fehler sollte nicht auftreten.");
                System.exit(1);
            }
        }
        int binNumberAdd = bin1SearchSize;
        for (int i = 0; i < searchForBin2.size(); i++) {

            int search = searchForBin2.get(i);

            boolean added = false;
            for (int j = 0; j < numberOfElements; j++) {
                if (childBinRepresentation[j] == -1 && binRep2[j] == search) {
                    //if(j == 65) System.out.println("Nr2: " + i + binNumberAdd);
                    childBinRepresentation[j] = (i + binNumberAdd);
                    if (!childBinGrouping.contains(i + binNumberAdd)) {
                        childBinGrouping.add(i + binNumberAdd);
                    }

                    added = true;
                }

            }
            if (!added) binNumberAdd--;          //if no item of the new bin is added for the next element the
            // childBinRepresentation number and bin grouping size stays the
            // same and the next bin in the search space will be done
        }

        return childBinRepresentation;
    }

    /**
     * adds the child solutions to the poulations and does the selection process if the size is bigger than maxSize
     * @param child the child solutions that are added to the population
     * @param maxSize maximum population size
     * @param numberDelete number of parents that are deleted in the selection process
     */
    private void integrateChild(Solution[] child, int maxSize , int numberDelete) {

        for (Solution aChild : child) {
            if (aChild.getFitnessValue() > bestFitness) {
                bestFitness = aChild.getFitnessValue();
                bestSolution = aChild;
            }
            solutionList.add(aChild);
        }
        if (solutionList.size() > maxSize) {
            if (numberDelete * 2 > solutionList.size()) {
                System.out.println("Interner Fehler bei der Löschung aus der Population.");
                System.exit(1);
            }

            for (int i = 0; i < numberDelete; i++) {
                if(solutionList.get(i).getFitnessValue() > solutionList.get(i+1).getFitnessValue()){
                    solutionList.remove(i+1);
                } else{
                    solutionList.remove(i);
                }
            }
        }
    }



}
