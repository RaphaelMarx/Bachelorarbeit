package Solarlux;

import Solarlux.Genetic.GeneticAlgorithm;
import Solarlux.Genetic.Solution;
import Solarlux.Merge.MergeEinzellaengen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainProgram {
    /**
     * starts the main program that takes input from the command prompt and starts the merging process
     * or the genetic algorithm
     *
     * @param args not used
     */
    public static void main(String[] args) {

        try (BufferedReader cin = new BufferedReader(new InputStreamReader(System.in))) {    //to take input in the command prompt
            System.out.println("Version 01.00.00");
            System.out.println("Wollen Sie einen neuen Datensatz anlegen? Dieses kann einige Zeit in Anspruch nehmen.");
            System.out.println("Bestätigen Sie mit \"Yes\" oder lehnen Sie mit \"No\" ab.");
            int firstInput = 0;
            do {
                String test = cin.readLine();
                if(test.toLowerCase().equals("yes")) firstInput = 1;
                if(test.toLowerCase().equals("no")) firstInput = -1;
            } while (firstInput == 0);

            if(firstInput == 1){
                System.out.println("Legen Sie die Datei mit den Einzellängen mit dem Namen \"Profilbedarf.CSV\" und die Datei mit den Meistercockpitdaten als \"Meistercockpit.CSV\" in den gleichen Ordner wie diese Jar-Datei.");
                System.out.println("Bestätigen Sie erneut mit \"Yes\" oder brechen Sie durch eine beliebige andere Eingabe ab.");
                if(cin.readLine().toLowerCase().equals("yes")) {
                    MergeEinzellaengen.startMerge();
                } else {
                    System.exit(0);
                }
            }

            System.out.println("Das Programm kann in der \"input.CSV\" Datei konfiguriert werden. Dort können unter anderem die zu untersuchenden MATIDs, FarbIDs, der Betrachtungszeitraum und die Anzahl an Tagen, in der Aufträge zusammengefasst werden, eingestellt werden.");
            System.out.println("Bei der ersten Auführung des Programms wird diese input-Datei erstellt.");
            System.out.println("");
            System.out.println("Stellen Sie sicher, dass Sie die Dateien \"Profildimension - Verschnittzugabe.CSV\" und \"Profilgeometrie und Gewicht.CSV\" im gleichen Ordner abgelegt haben.");
            System.out.println("Bestätigen Sie erneut mit \"Yes\" oder brechen Sie durch eine beliebige andere Eingabe ab.");
            if(cin.readLine().toLowerCase().equals("yes")) {
                startGeneticAlgorithm();
            }

            cin.close();
        } catch (IOException e) {
            System.out.println("Fehler beim Schereiben und Lesen auf der Konsole.");
            System.exit(1);
        }
    }

    /**
     * Starts the genetic algorithm with instances that are declared in the input.CSV and writes the
     * solution for each instance into the output files
     */
    private static void startGeneticAlgorithm() {
        long starttime = System.currentTimeMillis();
        ArrayList<CuttingStockInstance> instances = IOData.makeInstancesFromFile();
        long endInstanceTime = System.currentTimeMillis();
        if (instances == null) {
            System.out.println("Fehler bei der Instanzgenerierung.");
            System.exit(1);
        }
        IOData.writeOutputHeader();
        for (CuttingStockInstance inst : instances
                ) {
            GeneticAlgorithm gen = new GeneticAlgorithm(inst);
            Solution sol = gen.solve();
            ArrayList<Bin> solution = sol.getBins();
            IOData.writeOutput(solution, inst);
        }
        System.out.println("Dauer der Instanzgenerierung: " + (((endInstanceTime - starttime) / 1000) + "s"));
        long endtime = System.currentTimeMillis();
        System.out.println("Dauer der Optimierung: " + ((endtime - endInstanceTime) / 1000) + "s");
        System.out.println("Gesamtdauer: " + (((endtime - starttime) / 1000) + "s"));

    }
}
