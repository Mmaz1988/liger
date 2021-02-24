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

package semantics;

import glueSemantics.linearLogic.Premise;
import glueSemantics.linearLogic.Sequent;
import glueSemantics.parser.GlueParser;
import glueSemantics.semantics.LexicalEntry;
import main.Settings;
import packing.ChoiceVar;
import prover.LLProver2;
import syntax.GraphConstraint;
import syntax.SyntacticStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GlueSemantics {

    public GlueParser glueParser = new GlueParser(true );
    public LLProver2 llprover;

    public GlueSemantics()
    {}

    public String calculateSemantics(SyntacticStructure fs) {
        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();

        for (Set<ChoiceVar> choice : fs.cp.choices) {
            if (!choice.equals(fs.cp.rootChoice)) {
                unpackedSem.put(choice, new ArrayList<>());
            }
        }

        List<String> meaningConstructorStrings = new ArrayList<>();
        for (GraphConstraint c : fs.annotation) {
            if (c.getRelationLabel().equals("GLUE")) {
                if (unpackedSem.containsKey(c.getReading())) {
                    unpackedSem.get(c.getReading()).add(c.getFsValue().toString());
                } else {
                    for (Set<ChoiceVar> key : unpackedSem.keySet()) {
                        unpackedSem.get(key).add(c.getFsValue().toString());
                    }
                }
                //  System.out.println(c.getFsValue().toString());
            }
        }

        //Unpacked semantic calculation
        StringBuilder solutionBuilder = new StringBuilder();


        for (Set<ChoiceVar> key : unpackedSem.keySet()) {

            llprover = new LLProver2(new Settings(), new StringBuilder());

            List<LexicalEntry> lexicalEntries = new ArrayList<>();

            for (String mc : unpackedSem.get(key)) {
                System.out.println(mc);

                try {
                    LexicalEntry le = glueParser.parseMeaningConstructor(mc);
                    lexicalEntries.add(le);
                } catch (Exception e) {
                    System.out.println("Failed to parse meaning constructor " + mc);
                    e.printStackTrace();
                }
            }

            Sequent s = new Sequent(lexicalEntries);

            try {
                llprover.deduce(s);
            } catch (Exception e) {
                System.out.println("Failed to deduce a meaning from f-structure: " + fs.local_id);
                e.printStackTrace();
            }


            for (Premise p : llprover.getSolutions()) {
                solutionBuilder.append(p.toString());
                solutionBuilder.append(System.lineSeparator());
            }

            System.out.println(llprover.getProofBuilder().toString());


        }
        return solutionBuilder.toString();
    }

    /*
       public LexicalEntry parseMeaningConstructor(String mc) throws ParserInputException {
        String[] mcList = mc.split(":");
        if (mcList.length != 2) {
            throw new ParserInputException("Error parsing formula '" + mc + "'. " +
                    "Meaning side and glue side need to be separated with a ':'");
        }
        LexicalEntry entry = new LexicalEntry();
        LLTerm glue = llparser.parse(mcList[1]);
        SemanticRepresentation sem = null;
        if (!PARSESEMANTCS) {
            sem = new MeaningRepresentation(mcList[0].trim());
        } else
        {
           sem = semParser.parseExpression(mcList[0]);
           semParser.resetParser();
        }
        entry.setLlTerm(glue);
        entry.setSem(sem);

        return entry;
    }
     */


}
