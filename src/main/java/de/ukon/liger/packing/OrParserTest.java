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
import de.ukon.liger.syntax.xle.FstructureElements.AttributeValuePair;
import de.ukon.liger.syntax.xle.Prolog2Java.FsProlog2Java;
import de.ukon.liger.syntax.xle.Prolog2Java.ReadFsProlog;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.VariableHandler;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OrParserTest {

    String testfile = "/Users/red_queen/IdeaProjects/abstract-syntax-annotator-web/liger_resources/testFiles/testdirS13.pl";



    @Test
    public void testChoiceParsing()
    {

        XLEoperator xle = new XLEoperator(new VariableHandler());

        List<LinkedHashMap<String, LinguisticStructure>> fsList = new ArrayList<>();

        fsList.add(xle.fs2Java(testfile));

        ReadFsProlog fs = ReadFsProlog.readPrologFile(new File(testfile),new VariableHandler());
       LinkedHashMap<Set<ChoiceVar>,LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs);

        Set<Object> expectedMother = new HashSet<>();
        Set<ChoiceVar> part1 = new HashSet<>();
        ChoiceVar x = new ChoiceVar("B1");
        ChoiceVar y = new ChoiceVar("F1");
        ChoiceVar z = new ChoiceVar("I1");
        part1.add(x);
        part1.add(y);
        expectedMother.add(part1);
        expectedMother.add(z);
        ChoiceNode given = fs.cp.choiceNodes.get(12);
        //System.out.println(given.choiceNode);

        assertEquals(given.choiceNode,expectedMother);
    }


}
