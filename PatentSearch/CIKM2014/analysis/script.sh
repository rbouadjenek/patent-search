
clear
reset
#set title "Results over 1305 english queries"
set encoding iso_8859_1
set xlabel "recall" font "Helvetica,22"
set xtics nomirror rotate by -45
set ylabel "precision" font "Helvetica,22"
set size 0.5,0.5
set grid
set term postscript eps enhanced color "Courier,17"
set output "precision-recall_ByField-CLEF-IP_2010.eps" 
plot "data-CLEF-IP_2010/precision-recall_ByField.txt" using 3:2 title "Absract"   with linespoints lw 3,\
"data-CLEF-IP_2010/precision-recall_ByField.txt" using 5:4 title "Oracle" with linespoints lw 3



clear
reset
set style fill   solid 1.00 border -1
set style histogram clustered gap 1 title  offset character 0, 0, 0
#set title "Results over 1305 english queries"
set size 0.58,0.58
set grid
set key left top
set key vertical
set style data histograms
set xtics   ("MAP" 0.00000, "MRR" 1.00000, "PRES" 2.00000)
set term postscript eps enhanced color "Courier,17"
set output "MAP-MRR_ByField-CLEF-IP_2010.eps"
plot 'data-CLEF-IP_2010/MAP-MRR_ByField.txt' using 2:xticlabels(1) ti col ls 1 lc 1 lw 5,'' u 3 ti col ls 1 lc 2 lw 5
 
