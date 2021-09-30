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
import de.ukon.liger.syntax.SyntacticStructure;
import de.ukon.liger.syntax.xle.FstructureElements.AttributeValuePair;
import de.ukon.liger.syntax.xle.Prolog2Java.FsProlog2Java;
import de.ukon.liger.syntax.xle.Prolog2Java.ReadFsProlog;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.VariableHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ChoiceTest {


    String testfile = "C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\testFiles\\testdirS11.pl";
    //C:\Users\Celeste\IdeaProjects\SpringDemo\resources\testFiles\testdirS11.pl


    @Test
    public void testChoiceParsing()
    {
        XLEoperator xle = new XLEoperator(new VariableHandler());

        List<LinkedHashMap<String, SyntacticStructure>> fsList = new ArrayList<>();

        fsList.add(xle.fs2Java(testfile));

        ReadFsProlog fs = ReadFsProlog.readPrologFile(new File(testfile),new VariableHandler());
       LinkedHashMap<Set<ChoiceVar>,LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs);



        System.out.println(fs.cp.toString());
        System.out.println(fs.cp.choices);
        assertEquals(1,fs.cp.choiceNodes.size());
    }


}
