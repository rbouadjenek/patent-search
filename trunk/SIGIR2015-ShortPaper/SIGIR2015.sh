latex main.tex 
bibtex main.aux
dvips -t letter main.dvi
latex main.tex 
bibtex main.aux
dvips -t letter main.dvi
latex main.tex 
bibtex main.aux
dvips -t letter main.dvi
ps2pdf -dMaxSubsetPct=100 -dSubsetFonts=true -dEmbedAllFonts=true -dPDFSETTINGS=/printer -dUseCIEColor main.ps
#pdffonts main.pdf
