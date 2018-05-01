package Solarlux;

import Solarlux.Genetic.Pair;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Created by Raphael on 04.12.2017.
 */
public class FitAlgos {

    /**
     * First Fit Decreasing: sorts the elements of the CuttingStockInstance in a decreasing order by calling sortIndices() and returning the solution from bestFit()
     *
     * @param instance the CuttingStockInstance that will be solved
     * @return the Solution in form of an ArrayList of Bins
     */
    public static ArrayList<Bin> bestFitDecreasing(CuttingStockInstance instance){
        int[] sortedIndices = sortIndices(instance);
        return bestFit(instance, sortedIndices);
    }

    /**
     * Sorts the indices of elements of the CuttingStockInstance in a way that the elements that belong to
     * the returned order of indices are sorted in a decreasing way.
     *
     * @param instance the CuttingStockInstance of which items you want to know the decreasing order
     * @return an int Array which contains the indices in a sorted way
     */
    private static int[] sortIndices(CuttingStockInstance instance) {
        int[] elementLength = instance.getElementLength();
        ArrayList<Pair> pairs = new ArrayList<>(elementLength.length);
        for (int i = 0; i < elementLength.length; i++) {
            pairs.add(new Pair(i, elementLength[i]));
        }
        pairs.sort((p1, p2) -> p2.getLength() - p1.getLength()); //sort by length decreasing
        int[] indices = new int[pairs.size()];
        for (int i = 0; i < pairs.size(); i++) {
            indices[i] = pairs.get(i).getIndex();
        }
        return indices;
    }

    /**
     * Calls the bestFit() function without changing the order in which the elements are processed
     * @param instance CuttingStockInstance that will be solved with the bestFit algorithm
     * @return the solution of the bestFit algorithm as an ArrayList of Bins
     */
    public static ArrayList<Bin> bestFit(CuttingStockInstance instance){
        return bestFit(instance, IntStream.range(0, instance.getElementLength().length).toArray());
    }

    /**
     * Does the bestFit algorithm for a given CuttingStockInstance chronological or in a special order
     * given in the orderOfItems Array
     *
     * @param instance     the CuttingStockInstance that will be solved with the bestFit algorithm
     * @param orderOfItems the order in which the objects are processed
     * @return the solution of the bestFit algorithm as an ArrayList of Bins
     */
    private static ArrayList<Bin> bestFit(CuttingStockInstance instance, int[] orderOfItems){
        ArrayList<Bin> bestBins = new ArrayList<>();
        int[] elementLength = instance.getElementLength();
        int[] elementCount = instance.getElementCount();
        int[] elementOrderNumber = instance.getElementOrderNumber();

        for (int index : orderOfItems) {                         //for each object (in order of orderOfItems)
            for (int i = 0; i < elementCount[index]; i++) {     //for each demand of that object in ElementCount
                int bestIndexToAdd = -1;
                int minimalRest = Integer.MAX_VALUE;

                int binsize = bestBins.size();
                for (int j = 0; j < binsize; j++) {
                    Bin checkingBin = bestBins.get(j);
                    if (checkingBin.fitting(elementLength[index])){
                        int newRestLength = checkingBin.newRestLength(elementLength[index]);
                        if(newRestLength < minimalRest){
                            bestIndexToAdd = j;
                            minimalRest = newRestLength;
                        }
                    }
                }
                if (bestIndexToAdd == -1) {                                   //if the item doesnt fit in any bin a new one
                    Bin newBin = new Bin(instance.getStaticWaste(), instance.getDynamicWaste(), instance.getMaterialLength());
                    //is created, where the element gets added
                    if (!newBin.fitting(elementLength[index])) newBin = new Bin(0, 0, instance.getMaterialLength());
                    //extra rule for Items that are just a bit to large with the new waste rule - the aditional waste is ignored

                    newBin.add(elementLength[index], elementOrderNumber[index]);
                    bestBins.add(newBin);
                }
                else{
                    bestBins.get(bestIndexToAdd).add(elementLength[index], elementOrderNumber[index]);
                }

            }
        }
        for (int i = bestBins.size() - 1; i >= 0; i--) {
            Bin testBin = bestBins.get(i);
            if (testBin.isHalveable()) {     //only one bin can be less than half full
                testBin.halve();
                break;
            }
        }
        return bestBins;
    }


    /**
     * Calls the functions for the BestFit Decreasing and FirstFit Decreasing algorithms and returns the better solution
     * @param instance the CuttingStockInstance that will be solved with the bestFit Decreasing
     *                 and firstFit Decreasing algorithms
     * @return the better solution from the decreasing algorithms as an ArrayList of Bins
     */
    public static ArrayList<Bin> bothFitDecreasing(CuttingStockInstance instance){
        int[] sortedIndices = sortIndices(instance);
        ArrayList<Bin> firstBins = firstFit(instance, sortedIndices);
        ArrayList<Bin> bestBins = bestFit(instance, sortedIndices);
        //comare which has best solution
        if(firstBins.size() == bestBins.size()){
            if(ordersPerBin(firstBins) < ordersPerBin(bestBins)){
                return firstBins;
            } else{
                return bestBins;
            }
        }
        if(firstBins.size() < bestBins.size()){
            return firstBins;
        } else{
            return bestBins;
        }

    }

    /**
     * Does the FirstFit Decreasing algorithm for a CuttingStockInstance
     * @param instance the CuttingStockInstance that will be solved with the FirstFit Decreasing algorithm
     * @return the solution of the FirstFit Decreasing algorithm as an ArrayList of Bins
     */
    public static ArrayList<Bin> firstFitDecreasing(CuttingStockInstance instance){
        int[] sortedIndices = sortIndices(instance);
        return firstFit(instance, sortedIndices);
    }

    /**
     * Does the standard FirstFit algorithm (chronologically without sorting)
     * @param instance the CuttingStockInstance that will be solved with the FirstFit algorithm
     * @return the solution of the FirstFit algorithm as an ArrayList of Bins
     */
    public static ArrayList<Bin> firstFit(CuttingStockInstance instance){
        return firstFit(instance, IntStream.range(0, instance.getElementLength().length).toArray());
    }

    /**
     * Solves a CuttingStockInstance with the FirstFit algorithm with the item order that is
     * given in the orderOfItems array
     * @param instance the CuttingStockInstance that will be solved with the FirstFit algorithm
     * @param orderOfItems the order in which the objects are processed
     * @return the solution of the FirstFit algorithm as an ArrayList of Bins
     */
    public static ArrayList<Bin> firstFit(CuttingStockInstance instance, int[] orderOfItems){
        ArrayList<Bin> firstBins = new ArrayList<>();
        int[] elementLength = instance.getElementLength();
        int[] elementCount = instance.getElementCount();
        int[] elementOrderNumber = instance.getElementOrderNumber();


        for (int index : orderOfItems) {                         //for each object (in order of orderOfItems)
            for (int i = 0; i < elementCount[index]; i++) {   //for each demand of that object in ElementCount
                boolean added = false;
                int binsize = firstBins.size();
                for (int j = 0; j < binsize && !added; j++) {
                    if (firstBins.get(j).fitting(elementLength[index])) {
                        firstBins.get(j).add(elementLength[index], elementOrderNumber[index]);
                        added = true;
                    }
                }
                if (!added) {                                               //if the item doesnt fit in any bin a new one
                    Bin newBin = new Bin(instance.getStaticWaste(), instance.getDynamicWaste(), instance.getMaterialLength());
                    if (!newBin.fitting(elementLength[index])) newBin = new Bin(0, 0, instance.getMaterialLength());
                    //extra rule for Items that are just a bit to large with the new waste rule - the aditional waste is ignored

                    newBin.add(elementLength[index], elementOrderNumber[index]);
                    firstBins.add(newBin);
                }

            }
        }
        for (int i = firstBins.size() - 1; i >= 0; i--) {
            Bin testBin = firstBins.get(i);
            if (testBin.isHalveable()) {
                testBin.halve();
                break;
            }
        }
        return firstBins;
    }

    /**
     * Given an ArrayList of bins the average number of ordernumbers is calculated
     *
     * @param binList a BPP solution as ArrayList of Bins
     * @return the average of different ordernumbers per bin
     */
    private static double ordersPerBin(ArrayList<Bin> binList){
        double count = 0;
        for (Bin b: binList) {
            count += b.countOrders();
        }
        return count / binList.size();
    }

}
