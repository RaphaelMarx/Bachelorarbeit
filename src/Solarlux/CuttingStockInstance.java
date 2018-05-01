package Solarlux;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by Raphael on 30.11.2017.
 */
public class CuttingStockInstance {
    private int[] elementLength;        //the length of the item with its index
    private int[] elementCount;         //the demand of the item with its index
    private int[] elementOrderNumber;   //the ordernumber of the item with its index
    private String matId;               //material ID for the profile in this instance
    private String colorId;             //color ID of this instance
    private int staticWaste;            //added waste for each profile
    private int dynamicWaste;           //added waste for each item


    private double weightPerMeter;      //the weight per meter of this MATID
    private double surfacePerMeter;     //the surface per meter of this MATID

    private int materialLength;         //binlength of this instance

    private double oldMaterialUsed;     //how much material was used in the real production process

    private LocalDate startDate;        //real starting day for the production

    /**
     * Constructor for a CuttinStockInstance
     *
     * @param elementLength      the length of the item with its index
     * @param elementCount       the demand of the item with its index
     * @param elementOrderNumber the ordernumber of the item with its index
     * @param staticWaste        added waste for each profile
     * @param dynamicWaste       added waste for each item
     * @param matId              material ID for the profile in this instance
     * @param colorId            color ID of this instance
     * @param materialLength     binlength of this instance
     * @param startDate          real starting day for the production / the start date for this instance
     * @param weightPerMeter     the weight per meter of this MATID
     * @param surfacePerMeter    the surface per meter of this MATID
     * @param oldMaterialUsed    how much material was used in the real production process
     */
    public CuttingStockInstance(int[] elementLength, int[] elementCount, int[] elementOrderNumber, int staticWaste, int dynamicWaste, String matId, String colorId, int materialLength, LocalDate startDate, double weightPerMeter, double surfacePerMeter, double oldMaterialUsed){
        this.elementLength = elementLength;
        this.elementCount = elementCount;
        this.elementOrderNumber = elementOrderNumber;
        this.matId = matId;
        this.colorId = colorId;
        this.materialLength = materialLength;
        //initializeCutWaste(staticWaste);
        this.staticWaste = staticWaste;
        this.dynamicWaste = dynamicWaste;

        this.startDate = startDate;
        this.weightPerMeter = weightPerMeter;
        this.surfacePerMeter = surfacePerMeter;

        this.oldMaterialUsed = oldMaterialUsed;
    }


    /**
     * toString function that lists the important features of the instance
     *
     * @return String that contains the length, demand and ordernumber of each item in this instance
     */
    public String toString() {
        return "Length: " + Arrays.toString(elementLength) + "\n \n" + "Number: " + Arrays.toString(elementCount) + "\n \n" + "Ordernumber: " + Arrays.toString(elementOrderNumber);
    }

    /**
     * Sums up the number of items in this instance
     *
     * @return the number of items in this instance
     */
    public int getNumberOfElements() {
        return IntStream.of(elementCount).sum();
    }

    public int[] getElementLength(){
        return this.elementLength;
    }
    public int[] getElementCount(){
        return this.elementCount;
    }
    public int[] getElementOrderNumber(){
        return this.elementOrderNumber;
    }
    public String getMatId(){
        return this.matId;
    }
    public String getColorId(){
        return this.colorId;
    }
    public int getStaticWaste(){
        return this.staticWaste;
    }
    public int getDynamicWaste() {return this.dynamicWaste; }
    public int getMaterialLength() { return this.materialLength; }
    public LocalDate getStartDate() {
        return startDate;
    }
    public double getWeightPerMeter() {
        return weightPerMeter;
    }
    public double getSurfacePerMeter() {
        return surfacePerMeter;
    }
    public double getOldMaterialUsed() { return oldMaterialUsed; }
}

