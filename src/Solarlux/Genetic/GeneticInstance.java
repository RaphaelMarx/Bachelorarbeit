package Solarlux.Genetic;

import Solarlux.CuttingStockInstance;
import gnu.trove.list.array.TIntArrayList;

import java.time.LocalDate;

/**
 * Created by Raphael on 12.01.2018.
 */
public class GeneticInstance {
    private int[] elementLength;
    private int[] elementOrderNumber;
    private String matId;
    private String colorId;
    private int staticWaste;
    private int dynamicWaste;

    private int materialLength;
    private LocalDate startDate;
    private TIntArrayList itemsThatAreToBig;    //indices of items that are to big

    /**
     * Converts a CuttingStockInstance (with itemlegth, item demand, item ordernumber) into a BPP instance ready for the genetic algorithm
     * where each item is stored separately, and adds the extra waste to the values
     *
     * @param cutInstance the CuttingStockInstance that is converted
     */
    public GeneticInstance(CuttingStockInstance cutInstance){
        this.matId = cutInstance.getMatId();
        this.colorId = cutInstance.getColorId();
        this.staticWaste = cutInstance.getStaticWaste();
        this.dynamicWaste = cutInstance.getDynamicWaste();
        this.materialLength = cutInstance.getMaterialLength() - this.staticWaste;       //the binsize gets reduced by the static Waste#
        this.startDate = cutInstance.getStartDate();
        this.itemsThatAreToBig = new TIntArrayList();


        int numberOfElements = cutInstance.getNumberOfElements();

        elementLength = new int[numberOfElements];
        elementOrderNumber = new int[numberOfElements];

        int[] oldElementLength = cutInstance.getElementLength();
        int[] oldElementCount = cutInstance.getElementCount();
        int count = 0;
        for (int i = 0; i < oldElementLength.length; i++) {
            boolean tooBig = false;
            if(oldElementLength[i] > materialLength-dynamicWaste) tooBig = true;
            for (int j = 0; j < oldElementCount[i]; j++) {
                if(tooBig){
                    elementLength[count] = oldElementLength[i] - staticWaste;
                    itemsThatAreToBig.add(count);
                }
                else{
                    elementLength[count] = oldElementLength[i]+dynamicWaste;    //the dynamic Waste is added here

                }
                elementOrderNumber[count] = cutInstance.getElementOrderNumber()[i];
                count++;
            }
        }
    }

    public int[] getElementLength(){ return elementLength;  }
    public int[] getElementOrderNumber(){ return elementOrderNumber; }
    public String getMatId(){ return matId; }
    public String getColorId(){ return colorId;
    }

    public int getStaticWaste() {
        return staticWaste;
    }      //these Values are working with added Waste
    public int getDynamicWaste(){ return dynamicWaste; }
    public int getMaterialLength(){ return materialLength; }
    public LocalDate getStartDate(){ return startDate; }
    public TIntArrayList getItemsThatAreToBig() { return itemsThatAreToBig; }
}
