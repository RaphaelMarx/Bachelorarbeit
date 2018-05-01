package Solarlux;

import com.opencsv.*;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static Solarlux.Merge.MergeEinzellaengen.*;


/**
 * Created by Raphael on 27.12.2017.
 */
public class IOData {
    /**
     * Parameters that show in which column which data is located
     */
    public static final int VERSCHNITT_MATID = 2;
    public static final int VERSCHNITT_ZUGABE = 4;

    private static final int INPUT_STARTDATE = 1;
    private static final int INPUT_ENDDATE = 2;
    private static final int INPUT_COMBINEDAYS = 3;

    private static final int GEOMETRY_MATID = 0;
    private static final int GEOMETRY_WEIGHTPM = 6;
    private static final int GEOMETRY_SURFACEPM = 8;


    private static final int OUTPUT_MATID = 0;
    private static final int OUTPUT_COLORID = 1;
    private static final int OUTPUT_STARTDATE = 2;
    private static final int OUTPUT_NUMBEROFBINS = 3;
    private static final int OUTPUT_MATERIALLENGTH = 4;
    private static final int OUTPUT_OLDRATIOWASTED = 5;
    private static final int OUTPUT_OLDMETERSWASTED = 6;
    private static final int OUTPUT_OLDWEIGHTWASTED = 7;
    private static final int OUTPUT_RATIOWASTED = 8;
    private static final int OUTPUT_METERSWASTED = 9;
    private static final int OUTPUT_WEIGHTWASTED = 10;
    private static final int OUTPUT_SURFACEWASTED = 11;
    private static final int OUTPUT_ORDERSPERBIN = 12;


    private static String mergePath = (new File("")).getAbsolutePath() + "\\data\\MergeDir\\Merged\\";
    private static String wasteFilePath = (new File("")).getAbsolutePath() + "\\Profildimension - Verschnittzugabe.CSV";
    private  static String geometryPath = (new File("")).getAbsolutePath() + "\\Profilgeometrie und Gewicht.CSV";
    private static String inputPath = (new File("")).getAbsolutePath() + "\\data\\input.CSV";
    private static String outputPath = (new File("")).getAbsolutePath() + "\\data\\output.CSV";
    private static String outputShortPath = (new File("")).getAbsolutePath() + "\\data\\outputShort.CSV";

    /**
     * reads the input from the input.CSV and makes the CuttingStockInstances following the orders in the input-file
     *
     * @return an ArrayList of CuttingStockInstances that are described in the input file
     */
    public static ArrayList<CuttingStockInstance> makeInstancesFromFile() {
        File input = new File(inputPath);
        if (!input.exists()) {            //if input file doesn't exists a new File is created
            writeInputFile();
            return null;
        } else {
            ArrayList<CuttingStockInstance> allInstances = new ArrayList<>();

            CSVReader readInput = getCSVReader(inputPath);
            String[] matIDs = null;
            String[] colorIDs = null;
            String[] colorIDsReal = null;
            String[] dates = null;
            String[] line;
            String materialLengthRead = null;
            int materialLength = -1;

            try {
                readInput.readNext(); //skip the first line

                while ((line = readInput.readNext()) != null) { //gets the lines in which the important parameters are located
                    if (line[0].equals("MATID")) {              //in the case that additional things or empty lines are written into the input file
                        matIDs = line;
                    }
                    if (line[0].equals("COLORID")) {
                        colorIDs = line;        //contains the data from input file

                        colorIDsReal = line;    //contains the real COLORIDs for the special MATID
                        //(for example if every colorID of that MATID is turned into an instance '*')
                    }
                    if (line[0].equals("DATE")) {
                        dates = line;
                    }
                    if (line[0].equals("LENGTH")) {
                        materialLengthRead = line[1];
                    }
                }
            } catch (IOException e) {
                System.out.println("Fehler: Das Lesen der Datei " + inputPath + " ist fehlgeschlagen.");
                System.exit(1);
            }


            if (matIDs == null || colorIDs == null || dates == null || materialLengthRead == null) {        //if the parameters aren't found the program exits

                System.out.println("The Input-File seems to be corrupted. Please delete the File and try again.");
                System.exit(1);
            }

            if (!(materialLengthRead.equals("*") || materialLengthRead.equals(""))) {
                materialLength = Integer.parseInt(materialLengthRead);
            }
            LocalDate endDate = null;
            LocalDate startDate = null;
            long combineDays = -1;
            try {
                if (dates[INPUT_ENDDATE].equals("*")) {
                    endDate = LocalDate.now();
                } else {
                    endDate = LocalDate.parse(dates[INPUT_ENDDATE], DateTimeFormatter.ofPattern("dd.MM.uuuu"));
                }
                if (dates[INPUT_STARTDATE].equals("*")) {
                    startDate = endDate.minusYears(1);
                } else {
                    startDate = LocalDate.parse(dates[INPUT_STARTDATE], DateTimeFormatter.ofPattern("dd.MM.uuuu"));
                }
                combineDays = Long.parseLong(dates[INPUT_COMBINEDAYS]);
                if (startDate.isAfter(endDate)) {
                    System.out.println("Fehler: Das Enddatum ist vor dem Startdatum.");
                    System.exit(1);
                }
            } catch (DateTimeException e) {
                System.out.println("Fehler beim Einlesen der Zeile \"DATE\" in der input file. Möglicherweise ist ein falsches Datumsformat eingegeben worden.");
                System.exit(1);
            }
            if (combineDays < 1) {
                System.out.println("Fehler: Die Anzahl an Tagen, die für die Optimierung zusammengefasst werden sollen, muss eine ganze Zahl >= 1 sein.");
                System.exit(1);
            }
            System.out.println(startDate + "   -   " + endDate + " for " + combineDays);

            long interval = ChronoUnit.DAYS.between(startDate, endDate);
            boolean allColors = false;
            for (String cID : colorIDs
                    ) {
                if (cID.equals("*")) allColors = true;
            }
            int mAllIDs = -1;
            for (String mID : matIDs
                    ) {
                if(mID.equals("30")) mAllIDs = 30;
                if(mID.equals("5")) mAllIDs = 5;
                if(mID.equals("*")) mAllIDs = 0;
            }
            switch(mAllIDs){
                case 0:
                    matIDs = getAllMatIds("");
                    break;
                case 5:
                    matIDs = getAllMatIds(Integer.toString(5));
                    break;
                case 30:
                    matIDs = getAllMatIds(Integer.toString(30));
                    break;
            }

            for (int i = 1; i < matIDs.length; i++) { //for each MATID of which instances will be optimized
                if (!(matIDs[i] == null || matIDs[i].equals("") || matIDs[i].equals("*"))) {
                    double[] geometryData = null;
                    try {
                        geometryData = getGeometryData(matIDs[i]);  //gets the weight and surfacearea of that MATID

                    } catch (IOException e) {
                        System.out.println("Fehler: Das Lesen der Datei " + geometryPath + " ist fehlgeschlagen.");
                        System.exit(1);
                    }
                    if (geometryData == null) {
                        geometryData = new double[2];
                        geometryData[0] = -1;
                        geometryData[1] = -1;
                    }
                    int waste = readWaste(matIDs[i]);       //gets old extra waste date from file
                    int[] newWaste = newCutWaste(waste);    //and converts it to the new system

                    if (allColors) {    //if every COLORID of that MATID shall be optimized
                        try {
                            colorIDsReal = getAllColors(matIDs[i]);
                        } catch (IOException e) {
                            System.out.println("Fehler: Das Auslesen der Farben der MATID " + matIDs[i] + " ist fehlgeschlagen.");
                            System.exit(1);
                        }
                    }

                    for (int j = 1; j < colorIDsReal.length; j++) {
                        if (!(colorIDsReal[j] == null || colorIDsReal[j].equals(""))) {
                            for (long k = 0; k < interval + 1; k += combineDays) {
                                CuttingStockInstance next = null;
                                if (startDate.plusDays(k + combineDays - 1).isAfter(endDate)) {
                                    long restDays = ChronoUnit.DAYS.between(startDate.plusDays(k), endDate);
                                    if (startDate.plusDays(k + restDays).isEqual(endDate))  //either make the instance for the number of days in the input fiel or if that exeeds the
                                        // enddate only the remaining days till the enddate are used
                                        next = makeInstance(matIDs[i], colorIDsReal[j], startDate.plusDays(k), restDays, materialLength, geometryData[0], geometryData[1], newWaste);
                                } else {
                                    next = makeInstance(matIDs[i], colorIDsReal[j], startDate.plusDays(k), combineDays - 1, materialLength, geometryData[0], geometryData[1], newWaste);
                                }
                                if (next != null) allInstances.add(next);
                            }
                        }
                    }

                }
            }

            return allInstances;
        }
    }

    /**
     * gets all MATIDs that start with the given REGEX
     * @param regex REGEX to search for in the MATIDs (for example "30" or "5")
     * @return returns all MATIDs that are found with the REGEX in form of an array
     */
    private static String[] getAllMatIds(String regex) {
        ArrayList<String> matId = new ArrayList<>(150);
        matId.add("MATID");             //first space isn't read because it holds the line identifier
        File directory[] = new File(mergePath).listFiles();
        if (directory == null) {
            System.out.println("Fehler: Die Dateien in " + mergePath + " konnten nicht gefunden werden.");
            System.exit(1);
        }
        for (File file : directory
                ) {
            if (file.isFile()) {
                String filename = file.getName().substring(0, file.getName().lastIndexOf("."));
                if (filename.matches(regex + ".*")) matId.add(filename);
            }
        }
        return matId.toArray(new String[matId.size()]);
    }

    /**
     * Gets all existing COLORIDs for the given MATID
     * @param matID the MATID of which you want to know all possible colors
     * @return an Array which contains all colors of the given MATID
     * @throws IOException if teh reading of the file fails
     */
    private static String[] getAllColors(String matID) throws IOException {
        File file = new File(mergePath + matID + ".CSV");
        if (!file.exists()) {
            System.out.println("File " + file.getAbsolutePath() + " doesn't exist!");
            System.exit(1);
        }
        CSVReader reader = getCSVReader(file.getAbsolutePath());
        String[] line;
        ArrayList<String> colorIds = new ArrayList<>(200);
        colorIds.add("COLORID");
        while ((line = reader.readNext()) != null) {
            if (!colorIds.contains(line[MERGED_COLOR])) colorIds.add(line[MERGED_COLOR]);
        }

        return colorIds.toArray(new String[colorIds.size()]);
    }

    /**
     * makes a CuttingStockInstance with the given parameters
     * @param matId MATID as String
     * @param colorId COLORID as String
     * @param startDate first date of this instance
     * @param intervalRange number of days that are combined in this instance
     * @param materialLength capacity of the bins
     * @param weightPerMeter weight per meter of the MATID
     * @param surfacePerMeter surface per meter of the MATID
     * @param newWaste array wich contains the static waste and perCutWaste
     * @return the CuttingStockInstance
     */
    private static CuttingStockInstance makeInstance(String matId, String colorId, LocalDate startDate, long intervalRange, int materialLength, double weightPerMeter, double surfacePerMeter, int[] newWaste) {
        if (materialLength == -1) materialLength = 7000;
        File file = new File(mergePath + matId + ".CSV");
        if (!file.exists()) {
            System.out.println("Merged file " + mergePath + matId + ".CSV " + " doesn't exist.");
            System.exit(1);
        }

        CSVReader readFile = getCSVReader(file.getAbsolutePath());
        String[] line;

        TIntArrayList elementLength = new TIntArrayList();
        TIntArrayList elementCount = new TIntArrayList();
        TIntArrayList elementOrderNumber = new TIntArrayList();
        double oldMaterialUsed = 0;
        HashSet<ProductionUnit> alreadyCountedSet = new HashSet<>();

        try {
            while ((line = readFile.readNext()) != null) {
                if (line[MERGED_COLOR].equals(colorId) && rightDate(startDate, intervalRange, line[MERGED_STARTDATE])) {

                    elementLength.add(Integer.parseInt(line[MERGED_EINZELLAENGE]));
                    int split = line[MERGED_MENGE].indexOf(",");
                    if (split != -1) {
                        elementCount.add(Integer.parseInt(line[MERGED_MENGE].substring(0, line[MERGED_MENGE].indexOf(","))));
                    } else {
                        elementCount.add(Integer.parseInt(line[MERGED_MENGE]));
                    }
                    Integer orderNumber = Integer.parseInt(line[MERGED_RECEIPTNR]);
                    double oldLength = Double.parseDouble(line[MERGED_PROFILLAENGE].replaceAll(",", "."));
                    double oldMaterialCount = Double.parseDouble(line[MERGED_OLDNUMBERUSED].replaceAll(",", "."));
                    if (oldMaterialCount == 0.5)
                        oldMaterialCount = 1;   //because the value in PROFILLAENGE is already half of the 'normal' value;
                    // if it is bigger than one there has to be at least one bin with the full length

                    ProductionUnit prodUnit = new ProductionUnit(line[MERGED_STARTDATE], orderNumber, oldLength, oldMaterialCount, colorId);
                    //ProdUnit is used to make sure that the old material used is only
                    //counted once - because the real number of bins that were used
                    //is written in every line of the order
                    if (!alreadyCountedSet.contains(prodUnit)) {
                        oldMaterialUsed += oldMaterialCount * oldLength;  //add the aditional used length in mm if the order wasn't added already

                        alreadyCountedSet.add(prodUnit);
                    }

                    elementOrderNumber.add(Integer.parseInt(line[MERGED_RECEIPTNR]));


                }
            }
        } catch (IOException e) {
            System.out.println("Fehler: Das Lesen der Datei " + file.getAbsolutePath() + " ist fehlgeschlagen.");
            System.exit(1);
        } catch (NumberFormatException n) {
            System.out.println("Fehler: Das Lesen der Werte in " + file.getAbsolutePath() + " ist fehlgeschlagen. Möglicherweise ist ein falsches Zahlenformat dafür verantwortlich.");
            System.exit(1);
        } catch (DateTimeParseException d) {
            System.out.println("Fehler beim Lesen der Datumsangaben. Möglicherweise ist ein falsches Datumsformat dafür verantwortlich.");
            System.exit(1);
        }

        if (elementLength.isEmpty() || elementCount.isEmpty() ) {
            return null;          //if no (useable) instance can be made it returns null
        }
        int maximum = elementLength.max();

        if ((maximum > materialLength)) {     //if the instance can't be solved because some items are too big - the maximum capacity is raised
            materialLength = (((maximum - 1) / 1000) + 1) * 1000;   //Round to next 1000
            if (materialLength >7000) {
                System.out.println("Maximallänge von 7000mm überschritten. Bitte überprüfen sie die eingelesene Instanz. (MATID: " + matId + "; ColorID: " + colorId + "; Eckstart: " + startDate+") ");
                System.exit(1);
            }
            System.out.println("Die Instanz ist nicht lösbar, weil mindestens ein Element für die gewählte Maximallänge zu groß ist. Die Maximallänge der Instanz (MATID: " + matId + "; ColorID: " + colorId + "; Eckstart: " + startDate + ") wurde auf " + materialLength + "mm angehoben.");
        }
        return new CuttingStockInstance(elementLength.toArray(), elementCount.toArray(), elementOrderNumber.toArray(), newWaste[0], newWaste[1], matId, colorId, materialLength, startDate, weightPerMeter, surfacePerMeter, oldMaterialUsed);

    }

    /**
     * Converts the old extra waste values to the new system
     *
     * @param oldStaticWaste the static waste in the old system
     * @return the new extra waste values (static and perCut)
     */
    private static int[] newCutWaste(int oldStaticWaste) {
        int[] newWaste = new int[2];

        switch (oldStaticWaste) {
            case -1:    //if no info is found in the File it the extra Waste is set to zero
                newWaste[0] = 0;
                newWaste[1] = 0;
            case 70:
                newWaste[0] = 50;
                newWaste[1] = 10;
                break;
            case 71:
                newWaste[0] = 50;
                newWaste[1] = 10;
                break;
            case 100:
                newWaste[0] = 70;
                newWaste[1] = 10;
                break;
            default:
                newWaste[0] = oldStaticWaste;
                newWaste[1] = 0;
        }
        return newWaste;
    }

    /**
     * checks if the testDate is in the range of intervalRange after the startdate
     *
     * @param startDate     startdate of the instance
     * @param intervalRange number of combined days
     * @param testDate      startdate of the production of the item
     * @return if the item was produced in the timeframe given by startdate + intervalRange
     * @throws DateTimeParseException if converting the String to a date fails
     */
    private static boolean rightDate(LocalDate startDate, long intervalRange, String testDate) throws DateTimeParseException {
        LocalDate test = LocalDate.parse(testDate, DateTimeFormatter.ofPattern("dd.MM.uuuu"));
        if ((test.isAfter(startDate)) && (intervalRange == Long.MAX_VALUE)) return true;
        long interval = ChronoUnit.DAYS.between(startDate, test);
        return (interval >= 0 && interval <= intervalRange);

    }

    /**
     * generates a csv reader to read the file in the given path
     *
     * @param path path to the file
     * @return the CSVReader for the file
     */
    private static CSVReader getCSVReader(String path) {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();
        CSVReaderBuilder readBuildFile = null;
        try {
            readBuildFile = new CSVReaderBuilder(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.out.println("Fehler: Das Lesen der Datei " + path+ " ist fehlgeschlagen.");
            System.exit(1);
        }
        return readBuildFile.withCSVParser(parser).build();
    }

    /**
     * reads the old extra waste data for the given MATID
     *
     * @param matId the MATID you want to know the waste data of
     * @return the old waste data read from the file
     */
    private static int readWaste(String matId) {
        int waste = -1;

        CSVReader readFile = getCSVReader(wasteFilePath);
        String[] line;
        try {
            while ((line = readFile.readNext()) != null) {
                if (line[VERSCHNITT_MATID].equals(matId)) {
                    waste = Integer.parseInt(line[VERSCHNITT_ZUGABE]);
                }
            }
        } catch (IOException e) {
            System.out.println("Fehler: Das Lesen der Datei " + wasteFilePath + " ist fehlgeschlagen.");
            System.exit(1);
        }

        if (-1 == waste)
            System.out.println("Verschnittzugabe von " + matId + " konnte nicht gefunden werden. Überprüfen Sie die zugehörige .CSV Datei");
        return waste;
    }

    /**
     * builds the input file
     */
    private static void writeInputFile() {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(inputPath, true), ';', '"', '\\', "\n");
            writer.writeNext(new String[]{"//Diese CSV-Datei wird zum Einlesen der Parameter verwendet. Der Aufbau wird immer in der Zeile darueber beschrieben."});
            writer.writeNext(new String[]{"//Sollte diese Datei beschaedigt sein oder aus anderen Gruenden nicht funktionieren, so kann sie einfach geloescht werden."});
            writer.writeNext(new String[]{"//In der naechsten Zeile die MATIDs eintragen - ab Spalte B", "matidNr1", "matidNr2", "...", "nur ein * bedeutet alle MATIDs berechnen"});
            writer.writeNext(new String[]{"MATID", "5-20-44", "5-310-02"});
            writer.writeNext(new String[]{"//In der naechsten Zeile die COLORIDs eintragen - ab Spalte B", "coloridNr1", "coloridNr2", "...", "nur ein * bedeutet alle Farben berechnen"});
            writer.writeNext(new String[]{"COLORID", "46-0-10777-901", "46-0-10501-902"});
            writer.writeNext(new String[]{"//In der naechsten Zeile Start- und Enddatum angeben - ab Spalte B", "startdate (dd.mm.yyyy), * bedeutet genau ein Jahr berechnen", "enddate (dd.mm.yyyy), * bedeutet bis zu dem heutigen Tag berechnen", "Anzahl der Tage die zusammengefasst werden sollen > 0 (mindestens ein Tag)"});
            writer.writeNext(new String[]{"DATE", "01.01.2017", "05.10.2017", "1"});
            writer.writeNext(new String[]{"//In der naechsten Zeile in Zelle B die Lagerlaenge angeben", "Lagerlaenge in mm, * fuer 7000"});
            writer.writeNext(new String[]{"LENGTH", "6000"});
            writer.flush();
            System.out.println("The input-file can be found at: " + inputPath);
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Fehler: Das Erstellen der Datei " + inputPath + " ist fehlgeschlagen.");
            System.exit(1);
        }

    }


    /**
     * deletes the old output files and writes the header of the new files
     */
    public static void writeOutputHeader() {
        File outputFile = new File(outputPath);
        if (outputFile.exists()) outputFile.delete();

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(outputPath, true), ';', '"', '\\', "\n");
            writer.writeNext(new String[]{"matID", "colorID", "Eckstart", "Anzahl genutzte Profile", "Profillaenge", "alter Verschnitt", "alter Verschnitt (in m)", "alter Verschnitt (in kg)", "neuer Verschnitt", "Verschnitt (in m)", "Verschnitt (in kg)", "Verschnitt (in m^2)", "Durchschn. Aufträge pro Profil", "Profil1", "Auftragsnummern in Profil1", "Profil2", "Auftragsnummern in Profil2", "..."});
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Fehler: Das Schreiben in die Datei " + outputPath + " ist fehlgeschlagen.");
            System.exit(1);
        }
        File outputSFile = new File(outputShortPath);
        if (outputSFile.exists()) outputSFile.delete();

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(outputShortPath, true), ';', '"', '\\', "\n");
            writer.writeNext(new String[]{"matID", "colorID", "Eckstart", "Anzahl genutzte Profile", "Profillaenge", "alter Verschnitt", "alter Verschnitt (in m)", "alter Verschnitt (in kg)", "neuer Verschnitt", "Verschnitt (in m)", "Verschnitt (in kg)", "Verschnitt (in m^2)", "Durchschn. Aufträge pro Profil", "Auftragsnummer1", "Auftragsnummer2", "..."});
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Fehler: Das Schreiben in die Datei " + outputShortPath + " ist fehlgeschlagen.");
            System.exit(1);
        }
    }

    /**
     * calculates the data from the solutions and writes it into the output files
     *
     * @param result the solutiopn in form of an ArrayList of Bins
     * @param inst   the original CuttingStockInstance
     */
    public static void writeOutput(ArrayList<Bin> result, CuttingStockInstance inst) {
        String matId = inst.getMatId();
        String colorId = inst.getColorId();
        int materialLength = inst.getMaterialLength();
        LocalDate startDate = inst.getStartDate();
        double weightPerMeter = inst.getWeightPerMeter();
        double surfacePerMeter = inst.getSurfacePerMeter();
        TIntArrayList orderNumbers = new TIntArrayList();



        String[] nextLine = new String[(result.size() * 2) + OUTPUT_ORDERSPERBIN + 1];
        String[] nextLineShort;


        //Calculating the data and putting it in the String arrays:
        double maxCapacityUsed = 0;
        double usedCapacity = 0;
        double wastedCapacity = 0;
        int orders = 0;


        double numberOfBins = result.size();
        for (int i = 0; i < result.size(); i++) {
            Bin b = result.get(i);
            maxCapacityUsed += b.getMaxCapacity();
            wastedCapacity += b.getWaste();
            orders += b.countOrders();
            nextLine[(i * 2) + OUTPUT_ORDERSPERBIN + 1] = b.toString();
            if ((b.getMaxCapacity() * 2 == materialLength)) {
                numberOfBins -= 0.5;
            }

            TIntArrayList binOrderNumber = b.getContainsOrderNumber();
            nextLine[(i * 2) + 1 + OUTPUT_ORDERSPERBIN + 1] = binOrderNumber.toString();
            for (int j = 0; j < binOrderNumber.size(); j++) {
                if (!orderNumbers.contains(binOrderNumber.get(j))) orderNumbers.add(binOrderNumber.get(j));
            }
            usedCapacity += b.getUsedCapacity();
        }

        nextLine[OUTPUT_MATID] = matId;
        nextLine[OUTPUT_COLORID] = colorId;
        nextLine[OUTPUT_STARTDATE] = startDate.toString(); //check
        nextLine[OUTPUT_NUMBEROFBINS] = Double.toString(numberOfBins).replace('.', ',');
        nextLine[OUTPUT_MATERIALLENGTH] = Integer.toString(materialLength);
        nextLine[OUTPUT_ORDERSPERBIN] = Double.toString(round((double) orders / result.size())).replace('.', ',');
        nextLine[OUTPUT_OLDRATIOWASTED] = Double.toString(round(1 - (usedCapacity / inst.getOldMaterialUsed()))).replace('.', ',');
        nextLine[OUTPUT_OLDMETERSWASTED] = Double.toString(round((inst.getOldMaterialUsed() - usedCapacity) / 1000)).replace('.', ',');
        nextLine[OUTPUT_RATIOWASTED] = Double.toString(round(1 - (usedCapacity / maxCapacityUsed))).replace('.', ',');
        nextLine[OUTPUT_METERSWASTED] = Double.toString(round(wastedCapacity / 1000)).replace('.', ',');

        if (weightPerMeter != -1) {
            nextLine[OUTPUT_OLDWEIGHTWASTED] = Double.toString(round((inst.getOldMaterialUsed() - usedCapacity) * weightPerMeter / 1000)).replace('.', ',');
            nextLine[OUTPUT_WEIGHTWASTED] = Double.toString(round((weightPerMeter * wastedCapacity) / 1000)).replace('.', ',');
            nextLine[OUTPUT_SURFACEWASTED] = Double.toString(round((surfacePerMeter * wastedCapacity) / 1000)).replace('.', ',');
        } else {
            nextLine[OUTPUT_OLDWEIGHTWASTED] = "failed";
            nextLine[OUTPUT_WEIGHTWASTED] = "failed";
            nextLine[OUTPUT_SURFACEWASTED] = "failed";
        }

        nextLineShort = Arrays.copyOfRange(nextLine, 0, OUTPUT_ORDERSPERBIN + 1 + orderNumbers.size());
        for (int i = 0; i < orderNumbers.size(); i++) {
            nextLineShort[OUTPUT_ORDERSPERBIN + i + 1] = Integer.toString(orderNumbers.get(i));
        }

        //write the sting arrays into the files
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(outputPath, true), ';', '"', '\\', "\n");
            writer.writeNext(nextLine);
            writer.flush();
            CSVWriter writerShort = new CSVWriter(new FileWriter(outputShortPath, true), ';', '"', '\\', "\n");
            writerShort.writeNext(nextLineShort);
            writerShort.flush();
        } catch (IOException e) {
            System.out.println("Fehler: Das Schreiben der Output-Dateien ist fehlgeschlagen.");
            System.exit(1);
        }
    }

    /**
     * Reads surface and weight of the MATID
     *
     * @param matId the MATID you wanat to know the geometry data of
     * @return the geometry data of the MATID or null if no data is found
     * @throws IOException if reading of the file fails
     */
    private static double[] getGeometryData(String matId) throws IOException {
        CSVReader reader = getCSVReader(geometryPath);
        double[] data = null;
        String[] line;
        while ((line = reader.readNext()) != null) {
            String mat = line[GEOMETRY_MATID];
            int split = mat.lastIndexOf('-');
            if (split != -1) {
                if (mat.substring(0, split).equals(matId)) {
                    data = new double[2];
                    data[0] = Double.parseDouble(line[GEOMETRY_WEIGHTPM].replace(',', '.'));
                    data[1] = Double.parseDouble(line[GEOMETRY_SURFACEPM].replace(',', '.'));
                }
            }

        }
        if (data == null) {
            System.out.println("The geometry data for the matID " + matId + " in the file " + geometryPath + " could not be found.");
        }
        return data;

    }

    /**
     * rounds the double values so that in the csv excel doesn't think its a big integer
     * instead of a decimal value and its easier to read
     * @param value double value
     * @return the rounded double value
     */
    private static double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(5, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }





}
