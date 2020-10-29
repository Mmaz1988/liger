package semantics;

import glueSemantics.linearLogic.Premise;
import glueSemantics.linearLogic.Sequent;
import glueSemantics.parser.GlueParser;
import glueSemantics.semantics.LexicalEntry;
import main.Settings;
import org.apache.xml.serialize.LineSeparator;
import prover.LLProver2;
import syntax.SyntacticStructure;
import syntax.xle.Prolog2Java.GraphConstraint;

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


        StringBuilder solutionBuilder = new StringBuilder();

        for (Premise p : llprover.getSolutions()) {
            solutionBuilder.append(p.toString());
            solutionBuilder.append(System.lineSeparator());
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
