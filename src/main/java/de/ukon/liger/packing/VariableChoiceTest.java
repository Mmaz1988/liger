/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.packing;

import org.junit.jupiter.api.Test;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.avp_elements.AttributeValuePair;
import de.ukon.liger.syntax.xle.prolog2java.FsProlog2Java;
import de.ukon.liger.syntax.xle.prolog2java.ReadFsProlog;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class VariableChoiceTest {




    @Test
    public void testVariableReturn()
    {
        PathVariables.initializePathVariables();

        String testfile = PathVariables.testPath + "testdirS12.pl";

        XLEoperator xle = new XLEoperator(new VariableHandler());

        List<LinkedHashMap<String, LinguisticStructure>> fsList = new ArrayList<>();

        fsList.add(xle.fs2Java(testfile));

        ReadFsProlog fs = ReadFsProlog.readPrologFile(new File(testfile),new VariableHandler());
       LinkedHashMap<Set<ChoiceVar>,LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs);


        System.out.println("All Variables: " + fs.cp.allVariables);
        // Correct Amount... I didn't test that the correct ones are present bc there are so many
        assertEquals(475,fs.cp.allVariables.size());
        //last Variable is the expected one
        assertEquals("RL", fs.cp.lastVar());
        //returning new Variables
        Set<ChoiceVar> newVars = fs.cp.returnNewChoiceVars(5);
        String result = newVars.toString();
        System.out.println("New Variables: " + result);
        //comparing the strings bc I struggled to compare the actual output of ChoiceVars at first
        assertEquals(result, "[RM5, RM2, RM1, RM4, RM3]");
        //Checking that it was added to all variables
        assertEquals(476,fs.cp.allVariables.size());
        assertEquals("RM", fs.cp.lastVar());
        //generating another set of variables and checking that it was added
        Set<ChoiceVar> newVars2 = fs.cp.returnNewChoiceVars(3);
        System.out.println("New Variables: "+newVars2);
        // and perhaps a more sensible test that the ouput is correct
        List<String> result2 = new ArrayList<>();
        for (ChoiceVar var : newVars2){
            String variable = var.choiceID;
            result2.add(variable);
        }
        assertEquals(true, result2.contains("RN3"));
        assertEquals(false,result2.contains("RN4"));
        assertEquals(477,fs.cp.allVariables.size());
        assertEquals("RN", fs.cp.lastVar());

    }

}
