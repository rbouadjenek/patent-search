latex ICAIL2015.tex 
bibtex ICAIL2015.aux
dvips -t letter ICAIL2015.dvi
latex ICAIL2015.tex 
bibtex ICAIL2015.aux
dvips -t letter ICAIL2015.dvi
latex ICAIL2015.tex 
bibtex ICAIL2015.aux
dvips -t letter ICAIL2015.dvi
ps2pdf -dMaxSubsetPct=100 -dSubsetFonts=true -dEmbedAllFonts=true -dPDFSETTINGS=/printer -dUseCIEColor ICAIL2015.ps
#pdffonts ICAIL2015.pdf
