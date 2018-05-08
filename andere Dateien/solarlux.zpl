#Einlesen der Datei:
param n := read file as "1n" skip 0 use 1 comment "#";
param c := read file as "2n" skip 0 use 1 comment "#";
param maxBins := read file as "3n" skip 0 use 1 comment "#";

#Zusaetzlicher Bin, da einer nur halbiert
param np := maxBins + 1;
set N := {1..n};
param firstRow[N] := read file as "1n" skip 1 use n comment "#";
param secondRow[N] := read file as "2n" skip 1 use n comment "#";
param orderNr[N] := read file as "3n" skip 1 use n comment "#";
set Bins := {1..np}; 
param chalf := c/2;
param w[Bins] := <1> chalf default c; 


# print data (optional)
#do print "first";
#do forall <i> in N do print i, " ", firstRow[i];
#do print "second";
#do forall <i> in N do print i, " ", secondRow[i];
#do print "last";
#do forall <i> in N do print i, " ", orderNr[i];

# variables
var x[N*Bins] integer >= 0;
var y[Bins] binary;

# objective
# minimiere Summe ueber alle Bins
minimize objName: sum <j> in Bins : y[j]*w[j];

# constraints
#Einhalten der Kapazitaet
subto constName1: forall <j> in Bins do sum <i> in N : firstRow[i]*x[i,j] <= w[j]*y[j];

#Jedes Element einpacken
subto constName3: forall <i> in N do sum <j> in Bins : x[i,j] ==  secondRow[i];

#x nicht negativ
subto constName2: forall <i,j> in N*Bins do x[i,j] >= 0;

