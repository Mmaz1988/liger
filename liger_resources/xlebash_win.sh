#!/bin/bash
#xle
export XLEPATH=/bin/xle
export PATH=${XLEPATH}/bin:$PATH
export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7
export LD_LIBRARY_PATH=${XLEPATH}/lib
export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH
export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH

export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl
export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl
export TKLIBPATH=${XLEPATH}/tcl/scripts/tk
export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk

xle -noTk -e "create-parser /mnt/d/Resources/english_pargram/index/main.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"

#xle -noTk -e "create-parser /Users/red_queen/Desktop/german_grammar/german.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"




#xle -e "create-parser /users/red_queen/Desktop/AKR-Semantics/SVN/english-asker-2007-10-23/grammar/english.lfg; parse {John loves Jesus.}; print-fs-as-prolog thisistheone.pl; exit"

#xle "create-parser /Users/red_queen/Desktop/AKR-Semantics/SVN/english-asker-2007-10-23/grammar/english.lfg"
