#Diese Datei ist fuer das Verarbeiten der Instanzen mit n=1:
#Einlesen der Parameter aus Datei
param n := read file as "1n" skip 0 use 1 comment "#";
param c := read file as "2n" skip 0 use 1 comment "#";
param bmax := read file as "3n" skip 0 use 1 comment "#";
param bplus := bmax + 1;

param firstRow := read file as "1n" skip 1 use n comment "#";
param secondRow := read file as "2n" skip 1 use n comment "#";
param orderNr := read file as "3n" skip 1 use n comment "#";
set Bins := {1..bplus}; 
param chalf := c/2;
param w[Bins] := <1> chalf default c; 

# variables
var x[Bins] integer >= 0;
var y[Bins] binary;

# objective
# minimiere Summe ueber alle Bins
minimize objName: sum <j> in Bins : y[j]*w[j];

# constraints
# Gewichtsbeschraenkung
subto constName1: forall <j> in Bins : firstRow*x[j] <= w[j]*y[j];

#Elemente muessen zugeordnet sein
subto constName3: sum <j> in Bins : x[j] ==  secondRow;

#nicht negative zuordnung
subto constName2: forall <j> in Bins do x[j] >= 0;

