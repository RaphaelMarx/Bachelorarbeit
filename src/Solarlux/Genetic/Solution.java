package Solarlux.Genetic;


import Solarlux.Bin;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by Raphael on 06.01.2018.
 */
public class Solution {

    private int[] elementLength;
    private int[] elementOrderNumber;
    private int[] elementBinRepresentation;
    private TIntArrayList binGrouping;
    private double fitnessValue;
    private ArrayList<Bin> binArray;
    private int materialLength;


    /**
     * Generates a random solution for the genetic algorithm (especially creating initial solutions)
     *
     * @param elementLength      the item lengths in this instance
     * @param elementOrderNumber the ordernumbers of the items in this instance
     * @param materialLength     the initial bin capaciity
     */
    public Solution(int[] elementLength, int[] elementOrderNumber, int materialLength) {   //generates a random solution
        this.elementLength = elementLength;
        this.elementOrderNumber = elementOrderNumber;

        this.materialLength = materialLength;


        binGrouping = new TIntArrayList();
        elementBinRepresentation = new int[elementLength.length];
        binArray = new ArrayList<>();

        binArray.add(new Bin(0, 0, materialLength)); //add first bin
        binGrouping.add(0);

        int[] shuffledIndices = makeShuffledArray();
        Random rand = new Random();

        for (int nextIndex : shuffledIndices) {
            int randomBin = rand.nextInt(binGrouping.size());
            if (binArray.get(randomBin).fitting(elementLength[nextIndex])) {  //if the next Item fits in the Bin: add it
                binArray.get(randomBin).add(elementLength[nextIndex], elementOrderNumber[nextIndex]);
                elementBinRepresentation[nextIndex] = randomBin;            //Index in binArray (hier: randomBin) entspricht der Binrepßresentation in BinGrouping und binRepresentation
            }
            else{
                Bin newBin = new Bin(0, 0, materialLength);
                newBin.add(elementLength[nextIndex], elementOrderNumber[nextIndex]);
                binArray.add(newBin);
                int binIndex = binArray.indexOf(newBin);
                binGrouping.add(binIndex);
                elementBinRepresentation[nextIndex] = binIndex;
            }

        }
        initilizeFitness();
    }

    /**
     * Generates a solution where some items can be assinged to bins and assignes the rest of them with a FirstFit Decreasing Algorithm
     *
     * @param elementLength            the item lengths in this instance
     * @param elementOrderNumber       the ordernumbers of the items in this instance
     * @param elementBinRepresentation the bin representation of this solution (doesn't have to be complete yet)
     * @param binGrouping              the bin grouping part of this solution (doesn't have to be complete yet)
     * @param materialLength           the initial bin size
     */
    public Solution(int[] elementLength, int[] elementOrderNumber, int[] elementBinRepresentation, TIntArrayList binGrouping, int materialLength){
        this.elementLength = elementLength;     //if there are some elements not assigned (in elementBinRepresentation) it has to be done
        this.elementOrderNumber = elementOrderNumber;
        this.elementBinRepresentation = elementBinRepresentation;
        this.binGrouping = binGrouping;
        this.materialLength = materialLength;
        makeBins(); //makes the bins and adds the items that are not assigned to a bin
        initilizeFitness();
    }

    /**
     * Creates a new Solution from the data where every item is already correctly assigned
     * it does't check the values so its a private function
     *
     * @param elementLength            the item lengths in this instance
     * @param elementOrderNumber       the ordernumbers of the items in this instance
     * @param elementBinRepresentation the complete bin representation of this solution
     * @param binGrouping              the complete bin grouping of this solution
     * @param materialLength           initial bin capacity
     * @param bins                     ArrayList wich contains the bins
     */
    private Solution(int[] elementLength, int[] elementOrderNumber, int[] elementBinRepresentation, TIntArrayList binGrouping, int materialLength, ArrayList<Bin> bins) {   //doesnt check the values!
        this.elementLength = elementLength;
        this.elementOrderNumber = elementOrderNumber;
        this.elementBinRepresentation = elementBinRepresentation;
        this.binGrouping = binGrouping;
        this.materialLength = materialLength;

        this.binArray = bins;
        initilizeFitness();
    }

    /**
     * Generates a random order of indices with the Fisher-Yates shuffle
     *
     * @return random order of indices for the items in this solution
     */
    private int[] makeShuffledArray() {
        int[] shuffled = IntStream.range(0, elementLength.length).toArray();
        Random rand = new Random();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);

            int tmp = shuffled[index];
            shuffled[index] = shuffled[i];
            shuffled[i] = tmp;
        }
        return shuffled;
    }

    /**
     * Generates the Bins and saves the not assigned items in a ArrayList of Pairs (so they can be sorted and inserted in the bins)
     */
    private void makeBins() {
        int numberOfBins = binGrouping.size();
        ArrayList<Pair> missingItems = new ArrayList<>();

        this.binArray = new ArrayList<>(numberOfBins);

        for (int i = 0; i < numberOfBins; i++) {
            binArray.add(new Bin(0, 0, materialLength) );        //in the Genetic Algo the static and perCutWaste is already added so here the Value is set to 0
        }

        for (int i = 0; i < elementLength.length; i++) {
            if(elementBinRepresentation[i] == -1){  //-1 means its not assigned to any bin yet
                missingItems.add(new Pair(i, elementLength[i]));
            } else {
                binArray.get(elementBinRepresentation[i]).add(elementLength[i], elementOrderNumber[i]); // füge das i-te Element in den zugehörigen
                // Bin (aus dem BinArray) mit jeweiliger Länge und OrderNr
            }
        }
        completeSolution(missingItems);
    }

    /**
     * Completes the new solution by inserting the missing items in the bins with a FistFit Decreasing Algorithm
     * @param missingItems the items that are not assigned to a bin in form of an ArrayList of Pairs
     */
    private void completeSolution(ArrayList<Pair> missingItems) {
        //searches for -1 inserts with heuristik:
        missingItems.sort((p1, p2) -> p2.getLength() - p1.getLength());     //sorts the missingItems by Length to do a First Fit Decreasing algorithm

        //sortiere nach value und füge die werte der reihe nach ein -> FFD
        for (Pair missingItem : missingItems) {                     //the First Fit Algorithm
            boolean added = false;
            int length = missingItem.getLength();
            int index = missingItem.getIndex();
            for (int j = 0; j < binArray.size() && !added; j++) {
                if (binArray.get(j).fitting(length)) {
                    binArray.get(j).add(length, elementOrderNumber[index]);
                    elementBinRepresentation[index] = j;

                    added = true;
                }
            }
            if (!added) {     //if there is no fitting bin a new one has to be made
                int numberOfBins = binArray.size();
                Bin newBin = new Bin(0, 0, materialLength);
                newBin.add(length, elementOrderNumber[index]);
                binArray.add(newBin);
                binGrouping.add(numberOfBins);
                elementBinRepresentation[index] = binArray.indexOf(newBin);
            }
        }
    }

    /**
     * Completes the solution after bins get deleted in the mutation process
     * @param missingItems the missing items that have to be added to the bins of the solution
     * @param binArrayReal copy of the bins in this solution where the missing items have to be inserted
     */
    private void completeSolution(ArrayList<Pair> missingItems, Bin[] binArrayReal) {    //deleted items are -1 or null
        //searches for -1 inserts with heuristik:
        missingItems.sort((p1, p2) -> p2.getLength() - p1.getLength());     //sorts the missingItems by Length to do a First Fit Decreasing algorithm

        //sortiere nach value und füge die werte der reihe nach ein -> FFD
        for (Pair missingItem : missingItems) {                     //the First Fit Algorithm
            boolean added = false;
            int length = missingItem.getLength();
            int index = missingItem.getIndex();

            for (int j = 0; j < binArrayReal.length && !added; j++) {
                if (binArrayReal[j] != null && binArrayReal[j].fitting(length)) {
                    binArrayReal[j].add(length, elementOrderNumber[index]);
                    elementBinRepresentation[index] = j;

                    added = true;
                }
            }
            if (!added) {     //if there is no fitting bin a new one has to be made
                Bin newBin = new Bin(0, 0, materialLength);
                newBin.add(length, elementOrderNumber[index]);
                for (int j = 0; j < binArrayReal.length && !added; j++) {
                    if (binArrayReal[j] == null) {
                        binArrayReal[j] = newBin;
                        binGrouping.add(j);
                        elementBinRepresentation[index] = j;
                        added = true;
                    }
                }
                if (!added) {
                    binArrayReal = Arrays.copyOf(binArrayReal, binArrayReal.length + 1);
                    int newPosition = binArrayReal.length - 1;
                    binArrayReal[newPosition] = newBin;
                    binGrouping.add(newPosition);
                    elementBinRepresentation[index] = newPosition;
                }
            }
        }
        shrinkAndIntegrate(binArrayReal);
    }

    /**
     * if there are null values in the binArray and the values in the binRepresentation and bin grouping are adjusted to the new values
     * @param binArrayReal the newly generated solution in form of a bin array wieth possible null values in between
     */
    private void shrinkAndIntegrate(Bin[] binArrayReal) {
        int[] shrinked = new int[binArrayReal.length]; //set to 0 - contains the index of the bins in binArrayReal without null values
        ArrayList<Bin> newBinArray = new ArrayList<>(binArrayReal.length);
        int positionMismatch = 0;
        for (int i = 0; i < binArrayReal.length; i++) {

            if(binArrayReal[i] == null){
                positionMismatch++;

            } else {
                shrinked[i - positionMismatch] = i;
                newBinArray.add(binArrayReal[i]);
            }
        }
        for (int i = 0; i < shrinked.length - positionMismatch; i++) {
            if(shrinked[i] != i){
                for (int j = 0; j < elementBinRepresentation.length; j++) {
                    if(elementBinRepresentation[j] == shrinked[i]){
                        elementBinRepresentation[j] = i;
                    }
                }
                binGrouping.set(binGrouping.indexOf(shrinked[i]), i);
            }
        }
        binArray = newBinArray;

    }

    /**
     * sets the fitness value of the solution, with k = 2, b = 2
     */
    private void initilizeFitness() {
        double fitness = 0;
        for (Bin aBin : binArray) {
            double factor = Math.pow(1 - 0.01 * (aBin.countOrders() - 1), 2);
            double summand = factor * Math.pow(aBin.fullnessWithExtra(), 2);
            fitness += summand;
        }
        this.fitnessValue = fitness / binArray.size();

    }

    /**
     * Mutates a solution by deleting two bins form the solution and reinserting them with
     * the first fit decreasing algorithm with a given probability
     * @param mutationprobability the prbability that the mutation occurs
     */
    public void mutate(double mutationprobability){
        Random rand = new Random();
        if((rand.nextDouble() < mutationprobability) && (binGrouping.size() > 1)){

            Bin[] binArrayReal = new Bin[binArray.size()];      //Copy of binArray
            binArray.toArray(binArrayReal);

            //mutate
            int r1 = rand.nextInt(binGrouping.size());          //index in BinGrouping
            int r2;
            do {
                r2 = rand.nextInt(binGrouping.size());
            } while(r1 == r2);
            int binIndex1 = binGrouping.get(r1);                //index in binArray = name of the bin (in binrepresentation)
            int binIndex2 = binGrouping.get(r2);
            if(binIndex1 > binIndex2){
                int tmp = binIndex1;
                binIndex1 = binIndex2;
                binIndex2 = tmp;
            }

            ArrayList<Pair> missingItems = new ArrayList<>();
            for (int i = 0; i < elementLength.length; i++) {        //add all items to the list
                if((elementBinRepresentation[i] == binIndex1) ||(elementBinRepresentation[i] == binIndex2)){
                    missingItems.add(new Pair(i, elementLength[i]));
                    //elementBinRepresentation[i] = -1;
                }
            }

            binArrayReal[binIndex2] = null;
            binArrayReal[binIndex1] = null;

            binGrouping.remove(binIndex2);      //removes the value binIndex2/1
            binGrouping.remove(binIndex1);
            binArray.remove(binIndex2);         //removes at the index binIndex1/2
            binArray.remove(binIndex1);
            completeSolution(missingItems, binArrayReal); //deleted items are readded with FFD
            initilizeFitness();
        }
    }

    /**
     * normal solutions work with the extra values added on the items and deducted form the maximum capacity
     * when the GA ends, the best found solution gets converted into a state where the extra waste is counted as waste
     * @param instance the GeneticInstance of wich this is a solution of
     * @return the converted solution
     */
    public Solution convertSolutionFromWithExtra(GeneticInstance instance) {

        int[] newElementLength = new int[elementLength.length];

        int[] newElementOrderNumber = Arrays.copyOf(elementOrderNumber, elementOrderNumber.length);
        int[] newBinrepresentation = Arrays.copyOf(elementBinRepresentation, elementBinRepresentation.length);
        TIntArrayList newBinGrouping = new TIntArrayList(binGrouping);
        TIntArrayList itemsThatAreToBig = instance.getItemsThatAreToBig();


        int staticWaste = instance.getStaticWaste();
        int dynamicWaste = instance.getDynamicWaste();
        ArrayList<Bin> newBinArray = new ArrayList<>(binArray.size());

        for (int i = 0; i < binArray.size(); i++) {
            newBinArray.add(new Bin(staticWaste, dynamicWaste, materialLength + staticWaste));
        }


        for (int i = 0; i < elementLength.length; i++) {
            if(itemsThatAreToBig.contains(i)) {
                newElementLength[i] = elementLength[i] + staticWaste;
                Bin withoutExtra = new Bin(0, 0, materialLength+staticWaste);
                withoutExtra.add(newElementLength[i], newElementOrderNumber[i]);
                newBinArray.remove(newBinrepresentation[i]);
                newBinArray.add(newBinrepresentation[i], withoutExtra);
            } else {
                newElementLength[i] = elementLength[i] - dynamicWaste;
                newBinArray.get(newBinrepresentation[i]).add(newElementLength[i], newElementOrderNumber[i]);
            }

        }
        for (int i = newBinArray.size() - 1; i >= 0; i--) {
            Bin testBin = newBinArray.get(i);
            if (testBin.isHalveable()) {
                testBin.halve();
                break;
            }
        }

        return new Solution(newElementLength, newElementOrderNumber, newBinrepresentation, newBinGrouping, materialLength+staticWaste, newBinArray);
    }

    public int[] getElementLength(){ return elementLength; }
    public int[] getElementOrderNumber(){ return elementOrderNumber; }
    public int[] getElementBinRepresentation(){ return elementBinRepresentation; }
    public TIntArrayList getBinGrouping(){ return binGrouping; }
    public double getFitnessValue(){ return fitnessValue; }
    public ArrayList<Bin> getBins(){ return binArray; }
    public int getMaterialLength(){ return materialLength; }







}
