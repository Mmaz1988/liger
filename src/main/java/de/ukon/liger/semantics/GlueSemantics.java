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

package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import glueSemantics.linearLogic.Premise;
import glueSemantics.linearLogic.Sequent;
import glueSemantics.parser.GlueParser;
import glueSemantics.semantics.LexicalEntry;
import main.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prover.LLProver;
import prover.LLProver2;
import utilities.MyFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;

public class GlueSemantics {

    private final static Logger LOGGER = LoggerFactory.getLogger(GlueSemantics.class);

    public GlueSemantics()
    {}


    public String returnMeaningConstructors(LinguisticStructure fs){

        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();

            for (Set<ChoiceVar> choice : fs.cp.choices) {

                unpackedSem.put(choice, new ArrayList<>());
            }

        StringBuilder sb = new StringBuilder();

        for (GraphConstraint c : fs.annotation) {
            if (c.getRelationLabel().equals("GLUE")) {
                if (unpackedSem.containsKey(c.getReading())) {
                    unpackedSem.get(c.getReading()).add(c.getFsValue().toString());
                }
            }
        }

        boolean relevantChoice = false;

        for (Set<ChoiceVar> choice : unpackedSem.keySet())
            {
                if (!choice.equals(fs.cp.rootChoice) && !unpackedSem.get(choice).isEmpty())
                {
                    relevantChoice = true;
                    unpackedSem.get(choice).addAll(unpackedSem.get(fs.cp.rootChoice));
                }
            }

        if (relevantChoice)
        {
            unpackedSem.remove(fs.cp.rootChoice);
        }

        for (Set<ChoiceVar> key : unpackedSem.keySet())
        {
            if (!unpackedSem.get(key).isEmpty()) {
                sb.append("{");
                sb.append(System.lineSeparator());
                for (String s : unpackedSem.get(key)) {
                    sb.append(s);
                    sb.append(System.lineSeparator());
                }
                sb.append("}");
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}
