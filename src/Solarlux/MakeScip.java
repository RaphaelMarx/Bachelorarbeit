package Solarlux;


import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;



/**
 *
 * Created by Raphael on 11.01.2018.
 */
public class MakeScip {

    public static void main(String[] args) {
        makeInstanceFiles();
    }


    private static void makeInstanceFiles() {
        String dirPath =  "D:\\Raphael\\Desktop\\";
        ArrayList<CuttingStockInstance> instances = IOData.makeInstancesFromFile();
        for(CuttingStockInstance inst : instances) {
            int[] elementLength = inst.getElementLength();
            int[] elementCount = inst.getElementCount();
            int[] elementOrderNr = inst.getElementOrderNumber();
            int number = elementLength.length;
            int dynamic = inst.getDynamicWaste();
            int staticW = inst.getStaticWaste();

            CSVWriter write = null;
            PrintStream writeZPL = null;
            try {
                String pathVar = dirPath + inst.getMatId() +"_"+ inst.getColorId() +"_"+ inst.getStartDate();
                if(number == 1) pathVar = dirPath + "one\\" + inst.getMatId() +"_"+ inst.getColorId() +"_"+ inst.getStartDate();
                                                                        //Elemente mit nur einer Länge in eigenen Ordner
                                                                        //seperate Verarbeitung

                write = new CSVWriter(new FileWriter(new File(pathVar +".dat")));
                writeZPL = new PrintStream(new FileOutputStream(pathVar +".zpl"));
                writeZPL.print("param file := \"" + inst.getMatId() +"_"+ inst.getColorId() +"_"+ inst.getStartDate() +".dat\";");
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                writeZPL.close();
            }
            String[] line = new String[3];
            line[0] = Integer.toString(number);
            line[1] = Integer.toString(inst.getMaterialLength() - staticW);
            line[2] = Integer.toString(FitAlgos.bothFitDecreasing(inst).size());
            write.writeNext(line);
            for (int i = 0; i < number; i++) {
                line[0] = Integer.toString(elementLength[i] + dynamic);
                line[1] = Integer.toString(elementCount[i]);
                line[2] = Integer.toString(elementOrderNr[i]);
                write.writeNext(line);
            }
            try {
                write.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}