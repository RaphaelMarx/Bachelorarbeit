package Solarlux;

public class ProductionUnit {
    private String startDate;
    private int orderNumber;
    private double oldLength;
    private double oldMaterialCount;
    private String colorId;

    /**
     * Constuctor for a production unit which contains all identifying information
     * so that each data (of the old used materials) is only counted once
     *
     * @param startDate        poduction start of the item
     * @param orderNumber      ordernumber of the item
     * @param oldLength        length of the old rods that were used to produce the items
     * @param oldMaterialCount number of rods used
     * @param colorId          colorid of that production unit
     */
    public ProductionUnit(String startDate, int orderNumber, double oldLength, double oldMaterialCount, String colorId){
        this.startDate = startDate;
        this.orderNumber = orderNumber;
        this.oldLength = oldLength;
        this.oldMaterialCount = oldMaterialCount;
        this.colorId = colorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductionUnit that = (ProductionUnit) o;

        if (Double.compare(that.oldLength, oldLength) != 0 && Double.compare(that.oldLength/2, oldLength) != 0) return false;
        if (Double.compare(that.oldMaterialCount, oldMaterialCount) != 0) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;
        if (colorId != null ? !colorId.equals(that.colorId) : that.colorId != null) return false;
        if( orderNumber != that.orderNumber) return false;
        if(Double.compare(that.oldLength *2 , oldLength ) == 0) throw new RuntimeException("failed double value in compare/equals");
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + Integer.hashCode(orderNumber);
        temp = Double.doubleToLongBits(oldLength);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(oldMaterialCount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * @return the data of the production unit as a string
     */
    public String toString(){
        return "startDate: " + startDate +"; - orderNumber: " + orderNumber + "; - oldLength: " + oldLength + "; - oldMaterialCOunt: " +oldMaterialCount;
    }

}
