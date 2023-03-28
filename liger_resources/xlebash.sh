#!/bin/bash
#xle
export XLEPATH=/Applications/xle-intelmac-2009-09-18
export PATH=${XLEPATH}/bin:$PATH
export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7
export LD_LIBRARY_PATH=${XLEPATH}/lib
export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH
export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH

xle -noTk -e "create-parser /users/red_queen/Desktop/AKR-Semantics/SVN/english-asker-2007-10-23/grammar/english.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"

#xle -noTk -e "create-parser /Users/red_queen/Desktop/german_grammar/german.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"




#xle -e "create-parser /users/red_queen/Desktop/AKR-Semantics/SVN/english-asker-2007-10-23/grammar/english.lfg; parse {John loves Jesus.}; print-fs-as-prolog thisistheone.pl; exit"

#xle "create-parser /Users/red_queen/Desktop/AKR-Semantics/SVN/english-asker-2007-10-23/grammar/english.lfg"
