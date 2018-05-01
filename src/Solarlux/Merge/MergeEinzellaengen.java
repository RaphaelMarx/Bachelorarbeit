package Solarlux.Merge;

import com.opencsv.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Raphael on 15.12.2017.
 */
public class MergeEinzellaengen {
    public static final int RECEIPTNR = 0;
    public static final int MATID = 2;
    public static final int PROFILLAENGE = 3;
    public static final int EINZELLAENGE = 4;
    public static final int MENGE =5;

    public static final int PBA_RECEIPTNR = 2;
    public static final int PBA_MATID = 4;
    public static final int PBA_STARTDATE = 6;
    public static final int PBA_COLOR = 7;
    public static final int PBA_STATUS = 17; //only BW45 is accepted
    public static final int PBA_OLDNUMBERUSED = 15;

    public static final int MERGED_RECEIPTNR = 0;
    public static final int MERGED_MATID = 1;
    public static final int MERGED_PROFILLAENGE = 2;
    public static final int MERGED_EINZELLAENGE = 3;
    public static final int MERGED_MENGE = 4;
    public static final int MERGED_STARTDATE = 5;
    public static final int MERGED_COLOR = 6;
    public static final int MERGED_OLDNUMBERUSED = 7;


    private static String mergedPath;


    /**
     * starts the merge process,
     * sets the path to the direcories where the merge is taking place and makes the folders if they don't exist
     * after that the merge is prepared and the merge process starts
     */
    public static void startMerge() {
        File f1 = new File("");
        String thisPath = f1.getAbsolutePath();     //the path of this program


        String einzelPath = thisPath + "\\Profilbedarf.CSV";
        String pbaTerminePath = thisPath + "\\Meistercockpit.CSV";
        mergedPath = thisPath + "\\data\\MergeDir\\Merged\\";
        String matIdDirPath = thisPath + "\\data\\MergeDir\\MATID";

        File matIdDir = new File(matIdDirPath);

        File mergedDir = new File(mergedPath);
        if(!(mergedDir).exists() || !mergedDir.isDirectory())  mergedDir.mkdirs();
        if(!matIdDir.exists())  matIdDir.mkdir();
        cleanDirectory(new File(mergedPath));
        cleanDirectory(matIdDir);
        long timeStart = System.currentTimeMillis();


        prepare(pbaTerminePath, matIdDirPath);
        long prepare = System.currentTimeMillis()-timeStart;
        long merge = System.currentTimeMillis();
        mergeFiles(einzelPath, matIdDirPath);
        long mergeEnd = System.currentTimeMillis()-merge;


        System.out.println("Prepare: " + prepare/1000 + "s und Merge: " + (mergeEnd/1000)/60 +" Minuten");
    }

    /**
     * checks if two lines from the CSV-files can be merged and in that case merges them
     *
     * @param einzel     String with the path to the "Einzellaengen" file
     * @param pbaDirPath String with the path to the directory where the splitted MATID-files are located
     */
    private static void mergeFiles(String einzel, String pbaDirPath) {
        CSVParser parser = null;
        CSVReader readEinzel = null;
        CSVReaderBuilder readBuildEinzel = null;

        try {
            parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();

            readBuildEinzel = new CSVReaderBuilder(new FileReader(einzel));
            readEinzel = readBuildEinzel.withCSVParser(parser).build();
        } catch (Exception e) {
            System.out.println("Fehler: Benötigte Dateien konnten nicht gefunden oder nicht gelesen werden. " + einzel);
            System.exit(1);
        }
        String[] lineEinzel;
        try {
            while(!readEinzel.readNext()[RECEIPTNR].equals("ReceiptNr")){ }         //read until it gets to the values (after reading the header)
            while((lineEinzel = readEinzel.readNext()) != null){
                String receiptnr = lineEinzel[RECEIPTNR];
                String matID = lineEinzel[MATID];
                String pathToPBA = pbaDirPath + "\\" + matID + ".CSV";     //make a file for each Profile of the items that shall be produced
                File pbaFile = new File(pathToPBA);
                if(pbaFile.exists()) {
                    CSVReaderBuilder readBuildPBA = new CSVReaderBuilder(new FileReader(pbaFile));
                    CSVReader readPBA = readBuildPBA.withCSVParser(parser).build();
                    String[] linePBA;
                    boolean added = false;
                    while (((linePBA = readPBA.readNext()) != null ) && !added) {
                        if (linePBA[PBA_RECEIPTNR].equals(receiptnr) && checkProfillaenge(lineEinzel[PROFILLAENGE], linePBA[PBA_MATID])) { //goes through the "Meistercockpit" file
                            // if the ordernumber and length matches: they can be merged
                            String[] sameValues;
                            double add = 0;
                            while ((sameValues = readPBA.readNext()) != null) { //implicit check on the length through MATID, adding up the values for real material usage in the production process
                                if(sameValues[PBA_MATID].equals(linePBA[PBA_MATID]) && sameValues[PBA_RECEIPTNR].equals(linePBA[PBA_RECEIPTNR])) add  += Double.parseDouble(sameValues[PBA_OLDNUMBERUSED].replace(',', '.'));
                            }
                            if(add > 0){
                                double sum = add + Double.parseDouble(linePBA[PBA_OLDNUMBERUSED].replace(',', '.'));
                                linePBA[PBA_OLDNUMBERUSED] = Double.toString(sum);
                            }
                            String pathWrite = mergedPath + matID + ".CSV";
                            CSVWriter writer = new CSVWriter(new FileWriter(pathWrite, true), ';', '"', '\\', "\n");
                            writer.writeNext(mergeLines(lineEinzel, linePBA));
                            writer.flush();
                            added = true;   //because of this each element is only added once - for some items there are multiple dates where it could
                            // be added. In that case it gets added to the earliest (first in file) date
                            writer.close();
                        }
                    }
                }

            }


        } catch (IOException e) {
            System.out.println("Fehler beim mergen der Dateien aufgetreten.");
            System.exit(1);
        }

    }

    /**
     * Check if the length of the items in both files are the same/belong to the same order
     * @param profillaenge length of the item
     * @param matIdPBA the complete MATID that has a length parameter
     * @return true if they can be merged, false otherwise
     */
    private static boolean checkProfillaenge(String profillaenge, String matIdPBA) {
        int indexSplit = profillaenge.lastIndexOf(',');
        if (indexSplit == -1) {
            indexSplit = profillaenge.lastIndexOf('.');
            if (indexSplit == -1) {
                System.out.println("Fehler: Weder deutsche noch englische Zahlenformatierung konnte erkannt werden. Bitte überprüfen Sie die Daten.");
                System.exit(1);
            }
        }
        String lengthCheck = profillaenge.substring(0, indexSplit - 1);
        int matIdSplt = matIdPBA.lastIndexOf("-");
        return matIdPBA.endsWith(lengthCheck) || matIdSplt == -1 || (Integer.parseInt(matIdPBA.substring(matIdSplt)) == Integer.parseInt(lengthCheck) * 2);
        //only if the length are the same, no length is given or the length is exactly
        //the half of the other one then they can be merged

    }


    /**
     * splits the big CSV into small CSV-Files with one MATID per File
     *
     * @param filepath path to the file that is plitted into smaller csv files
     * @param toPath   the path to the directory where the smaller files will be written to
     */
    private static void prepare(String filepath, String toPath){
        CSVParser parser = null;
        CSVReader reader = null;

        CSVReaderBuilder readBuild = null;
        try {
            parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();

            readBuild = new CSVReaderBuilder(new FileReader(filepath));


            reader = readBuild.withCSVParser(parser).build();

        } catch (Exception e) {
            System.out.println("Fehler: Benötigte Dateien nicht gefunden!");
            System.exit(1);
        }
        String[] line;
        try {
            while (!reader.readNext()[PBA_RECEIPTNR].equals("Kundenauftrag")) {
            }     //Go to the line, where the data starts
            CSVWriter writer;
            while ((line = reader.readNext()) != null) {
                int indexSplit = line[PBA_MATID].lastIndexOf('-');
                if ((indexSplit != -1) && line[PBA_STATUS].equals("BW45")) {
                    String matID = line[PBA_MATID].substring(0, indexSplit);

                    String length = line[PBA_MATID].substring(indexSplit + 1);


                    String pathToFile = toPath + "\\" + matID + ".CSV";
                    File f = new File(pathToFile);
                    writer = new CSVWriter(new FileWriter(f, true), ';', '"', '\\', "\n");
                    writer.writeNext(line);
                    writer.flush();
                }

            }
        } catch (IOException e) {
            System.out.println("Fehler beim Sortieren der Daten.");
            System.exit(1);
        }


    }

    /**
     * deletes every file in the given directory
     *
     * @param dir directory in which the files will be deleted
     */
    private static void cleanDirectory(File dir) {
        try {
            for (File file : dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
        } catch (Exception e) {
            System.out.println("Beim Löschen der Dateien im Verzeichnis " + dir.getAbsolutePath() + " ist ein Fehler aufgetreten.");
        }
    }

    /**
     * this function merges two lines in the needed way to get one line that can be written in the merged files
     *
     * @param einzel line from the csv file "Einzellängen"
     * @param pba    line from the csv file "Meistercockpit"
     * @return a String array of the merged line
     */
    private static String[] mergeLines(String[] einzel, String[] pba){
        String[] merged = new String[9];
        merged[MERGED_RECEIPTNR] = einzel[RECEIPTNR];
        merged[MERGED_MATID] = einzel[MATID];
        merged[MERGED_PROFILLAENGE] = einzel[PROFILLAENGE];
        merged[MERGED_EINZELLAENGE] = einzel[EINZELLAENGE];
        merged[MERGED_MENGE] = einzel[MENGE];
        merged[MERGED_STARTDATE] = pba[PBA_STARTDATE];
        merged[MERGED_COLOR] = pba[PBA_COLOR];
        merged[MERGED_OLDNUMBERUSED] = pba[PBA_OLDNUMBERUSED];

        return merged;
    }

}
