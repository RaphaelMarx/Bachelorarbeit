package Solarlux.Genetic;

/**
 * Created by Raphael on 02.02.2018.
 * Used for sorting the items without changing the original arrays
 */
public class Pair {
    private int index;
    private int length;

    /**
     * constuctor for the pair
     *
     * @param index  index of the item
     * @param length length of the item
     */
    public Pair(int index, int length){
        this.index = index;
        this.length = length;
    }

    public int getIndex(){
        return index;
    }
    public int getLength(){
        return length;
    }

}
