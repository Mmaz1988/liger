package semantics;

import glueSemantics.linearLogic.Sequent;
import glueSemantics.parser.GlueParser;
import glueSemantics.semantics.LexicalEntry;
import main.Settings;
import prover.LLProver2;
import syntax.SyntacticStructure;
import syntax.xle.Prolog2Java.GraphConstraint;

import java.util.ArrayList;
import java.util.List;

public class GlueSemantics {

    public GlueParser glueParser = new GlueParser();
    public LLProver2 llprover = new LLProver2(new Settings(), new StringBuilder());

    public GlueSemantics()
    {}

    public void calculateSemantics(SyntacticStructure fs)
    {
        List<String> meaningConstructorStrings = new ArrayList<>();
        for (GraphConstraint c : fs.annotation)
        {
            if (c.getRelationLabel().equals("GLUE"))
            {
                meaningConstructorStrings.add(c.getFsValue().toString());
                System.out.println(c.getFsValue().toString());

            }
        }
        List<LexicalEntry> lexicalEntries = new ArrayList<>();
        for (String mc : meaningConstructorStrings)
        {
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
