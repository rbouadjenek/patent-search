#latex main.tex 
#bibtex main.aux
#dvips -t letter main.dvi
#latex main.tex 
#bibtex main.aux
#dvips -t letter main.dvi
#latex main.tex 
#bibtex main.aux
#dvips -t letter main.dvi
#ps2pdf -dMaxSubsetPct=100 -dSubsetFonts=true -dEmbedAllFonts=true -dPDFSETTINGS=/printer -dUseCIEColor main.ps
#pdffonts main.pdf
-----------------
latex CameraReady.tex 
bibtex CameraReady.aux
dvips -t letter CameraReady.dvi
latex CameraReady.tex 
bibtex CameraReady.aux
dvips -t letter CameraReady.dvi
latex CameraReady.tex 
bibtex CameraReady.aux
dvips -t letter CameraReady.dvi
#ps2pdf -dPDFSETTINGS=/prepress -dSubsetFonts=true -dEmbedAllFonts=true -dMaxSubsetPct=100 -dCompatibilityLevel=1.3 CameraReady.ps
 ps2pdf -dMaxSubsetPct=100 -dSubsetFonts=true -dEmbedAllFonts=true -dPDFSETTINGS=/printer -dUseCIEColor CameraReady.ps
#pdffonts CameraReady.pdf
#pdftops CameraReady.pdf
#ps2pdf14 -dPDFSETTINGS=/prepress CameraReady.ps
