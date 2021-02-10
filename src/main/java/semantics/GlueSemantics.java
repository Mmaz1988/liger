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
import prover.LLProver2;
import syntax.SyntacticStructure;
import syntax.GraphConstraint;

import java.util.ArrayList;
import java.util.List;

public class GlueSemantics {

    public GlueParser glueParser = new GlueParser(true );
    public LLProver2 llprover = new LLProver2(new Settings(), new StringBuilder());

    public GlueSemantics()
    {}

    public String calculateSemantics(SyntacticStructure fs)
    {
        List<String> meaningConstructorStrings = new ArrayList<>();
        for (GraphConstraint c : fs.annotation)
        {
            if (c.getRelationLabel().equals("GLUE"))
            {
                meaningConstructorStrings.add(c.getFsValue().toString());
              //  System.out.println(c.getFsValue().toString());
            }
        }
        List<LexicalEntry> lexicalEntries = new ArrayList<>();

        for (String mc : meaningConstructorStrings)
        {
          //  System.out.println(mc);

            try {
                LexicalEntry le = glueParser.parseMeaningConstructor(mc);
                lexicalEntries.add(le);
            }catch(Exception e)
            {
                System.out.println("Failed to parse meaning constructor " + mc);
                e.printStackTrace();
            }
        }

        Sequent s = new Sequent(lexicalEntries);

        try {
            llprover.deduce(s);
        }catch(Exception e)
        {
            System.out.println("Failed to deduce a meaning from f-structure: " + fs.local_id);
            e.printStackTrace();
        }

        StringBuilder solutionBuilder = new StringBuilder();

        for (Premise p : llprover.getSolutions()) {
            solutionBuilder.append(p.toString());
            solutionBuilder.append(System.lineSeparator());
        }

        System.out.println(llprover.getProofBuilder().toString());

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
