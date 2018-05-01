package Solarlux;

import gnu.trove.list.array.TIntArrayList;

/**
 * Created by Raphael on 30.11.2017.
 */
public class Bin {
    private TIntArrayList containsLength;       //the length of the items that will be produced
    private TIntArrayList countOfLength;        //the demand of the item with the same index
    private TIntArrayList containsOrderNumber;  //the ordernumber of the item with the same index

    private int maxCapacity;    //the maximum capacity of one bin
    private int restCapacity;   //the free capacity of the bin
    private int staticWaste;    //added waste for each profile
    private int perCutWaste;    //added waste for each item


    /**
     * constructor for a bin which can contain items
     * @param staticWaste added waste that will be deducted from the maximum capacity of the bin
     * @param perCutWaste added waste that will be added to each item in the bin
     * @param maxCapacity capacity of the bin
     */
    public Bin(int staticWaste, int perCutWaste, int maxCapacity) {
        this.maxCapacity = maxCapacity - staticWaste;
        this.restCapacity = this.maxCapacity;

        containsLength = new TIntArrayList();
        countOfLength = new TIntArrayList();
        this.containsOrderNumber = new TIntArrayList();
        this.staticWaste = staticWaste;
        this.perCutWaste = perCutWaste;
    }

    /**
     * adds a given item (length and ordernumber) to the bin
     *
     * @param length      the length of the item that is added to this bin
     * @param orderNumber the ordernumber of the item
     * @throws RuntimeException if the free capacity isn't enough for the item
     */
    public void add(int length, int orderNumber) {
        if (!fitting(length)) {
            System.out.println(staticWaste + " - " + perCutWaste);
            System.out.println("The given Element doesn't fit here!" + length + " in " + restCapacity + " (" + maxCapacity + ") ");
            System.exit(1);
        }
        int combinedLength = length + perCutWaste;
        int index = containsLength.indexOf(combinedLength);

        if (index == -1) {
            containsLength.add(combinedLength);
            countOfLength.add(1);
        } else {
            int old = countOfLength.get(index);
            countOfLength.set(index, old + 1);
        }
        restCapacity -= combinedLength;
        if (!containsOrderNumber.contains(orderNumber))
            containsOrderNumber.add(orderNumber); //each order number is only saved once
    }

    /**
     * checks if the remaining capacity is enough for the given length
     * @param length the length that is checked
     * @return true if a given length fits in this bin, false otherwise
     */
    public boolean fitting(int length) {
        return restCapacity >= length + perCutWaste;
    }

    /**
     * Returns the new restCapacity after the given length would be added
     * @param length the length of the element that may be added to this bin
     * @return -1 if the length doesn't fit in the bin, otherwise the restCapacity if the length (extra waste is included) is added to this bin
     */
    public int newRestLength(int length) {
        int rest = restCapacity - length - perCutWaste;
        return (rest < 0) ? -1 : rest;
    }

    /**
     * the amount of different ordernumbers in this bin
     * @return the nuber of different ordernumbers in this bin
     */
    public int countOrders() {
        return containsOrderNumber.size();
    }

    /**
     * @return the remaining capacity
     */
    public int getRestCapacity() {
        return restCapacity;
    }

    /**
     * The true waste of this bin - the extra waste is considered
     * @return the real waste of this bin
     */
    public int getWaste() {
        return (countOfLength.sum() * perCutWaste) + restCapacity + staticWaste;
    }

    /**
     * Calculates the fullness of the bin - the extra waste is not considered as waste in this function
     * It is used to calculate the fitness value of the bin/solution in the genetic algorithm
     * @return the fullness of the bin
     */
    public double fullnessWithExtra() {
        return (double) (maxCapacity - restCapacity) / maxCapacity;
    }

    /**
     * checks if the bin uses equal or less than half  of its capacity
     * @return true if the maximum bin capacity can be halved
     */
    public boolean isHalveable() {
        return restCapacity >= (double) maxCapacity / 2;
    }

    /**
     * Halves the capacity of this bin
     * @throws RuntimeException if the bin uses more than half of it's capacity so that it can't be halved
     */
    public void halve() {
        if (!isHalveable()) throw new RuntimeException("This bin doesn't have enough space for this.");

        restCapacity -= maxCapacity / 2;
        maxCapacity = maxCapacity / 2;
        staticWaste = staticWaste / 2;
    }

    /**
     * @return the official capacity of the bin, the deducted staticWaste is readded
     */
    public int getMaxCapacity() {
        return maxCapacity + staticWaste;
    }

    /**
     * @return the static waste of this bin
     */
    public int getStaticWaste() {
        return staticWaste;
    }

    /**
     * @return the content of the bin in form of a string
     */
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < containsLength.size(); i++) {
            strBuilder.append("(").append(String.valueOf(containsLength.get(i) - perCutWaste)).append(" | ").append(String.valueOf(countOfLength.get(i))).append(") ");
        }
        return strBuilder.toString();
    }

    /**
     * Getter for the TIntArrayList with all lengths in this bin
     * @return the TIntArrayList with all lengths in this bin
     */
    public TIntArrayList getContainsLength() {
        return containsLength;
    }

    /**
     * Getter for the TIntArrayList with the amount of every item in this bin
     * @return the TIntArrayList with the amount of every item in this bin
     */
    public TIntArrayList getCountOfLength() {
        return countOfLength;
    }

    /**
     * Getter fot the TIntArrayList with all ordernumbers in this bin
     * @return the TIntArrayList with all ordernumbers in this bin
     */
    public TIntArrayList getContainsOrderNumber() {
        return containsOrderNumber;
    }

    /**
     * @return the real used capacity of the bin when the extra waste is considered as waste
     */
    public int getUsedCapacity() {
        int used = 0;
        for (int i = 0; i < containsLength.size(); i++) {
            used += (containsLength.get(i) - perCutWaste) * countOfLength.get(i);
        }
        return used;
    }


}
