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

package syntax.ud;

// Base class for creating glue semantic representations with a meaning side and a glue side

import analysis.RuleParser.RuleParser;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import packing.ChoiceSpace;
import packing.ChoiceVar;
import syntax.GraphConstraint;
import syntax.SyntacticStructure;
import syntax.SyntaxOperator;
import test.QueryParserTest;
import utilities.VariableHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//import edu.stanford.nlp.pipeline;
//TODO Methoden sortieren
public class UDoperator extends SyntaxOperator {

    public static void main(String[] args) // throws VariableBindingException
    {

        List<String> testSentences = new ArrayList<String>(Arrays.asList(args));

        UDoperator udops = new UDoperator();

        if (testSentences.isEmpty()) {
            Scanner s = new Scanner(System.in);
            String input;
            while (true) {
                System.out.println("Enter sentence to be analyzed or enter 'quit'.");
                input = s.nextLine();
                if (input.equals("quit"))
                    break;
                SyntacticStructure out = udops.parseSingle(input);
                System.out.println(out.constraints);

            }
        }

    }

    private final DependencyParser dp;
    private static VariableHandler vh;


    public UDoperator()
    {
        dp = new DependencyParser();
        vh = new VariableHandler();
    }

    public UDoperator(VariableHandler variable_handler)
    {
        dp = new DependencyParser();
        vh = variable_handler;
    }

    @Override
    public SyntacticStructure parseSingle(String sentence) {

        //VariableHandler vh = new VariableHandler();

        List<GraphConstraint> out = new ArrayList<>();

        List<GrammaticalStructure> stanfordParse = dp.generateParses(Collections.singletonList(sentence));

        Morphology morphology = new Morphology();

        ChoiceSpace cp = new ChoiceSpace();

        for (GrammaticalStructure parse : stanfordParse) {
            //Dependency list
            List<GraphConstraint> dh = new ArrayList<>();

            for (TypedDependency dep : parse.typedDependencies()) {

                Set<ChoiceVar> context = new HashSet<>();
                context.add(new ChoiceVar("1"));
                cp.choices.add(context);

                GraphConstraint dc = new GraphConstraint(context, dep.gov().index(),
                        dep.reln().toString(),
                        String.valueOf(dep.dep().index()));

                GraphConstraint dcDep = new GraphConstraint(context, dep.dep().index(), "TOKEN", dep.dep().word().toString());
                GraphConstraint dcPos = new GraphConstraint(context, dep.dep().index(), "POS", dep.dep().tag().toString());

                String lemma = morphology.lemma(dep.dep().word(),dep.dep().tag());

                GraphConstraint dcLemma = new GraphConstraint(context, dep.dep().index(), "LEMMA", lemma);

                out.add(dc);
                out.add(dcDep);
                out.add(dcPos);
                out.add(dcLemma);

                /*
                Morphology m = new Morphology();

                System.out.println(dep.gov().word());
                System.out.println(dep.gov().tag());

                System.out.println(dep.dep().word());
                System.out.println(dep.dep().tag());

                System.out.println(Morphology.lemmaStatic(dep.dep().word(),dep.dep().tag()));

*/
            }
        }


        UDstructure ud = new UDstructure(vh.returnNewVar(VariableHandler.variableType.SENTENCE_ID,null),sentence,out);
        ud.cp = cp;

        return ud;
    }

    public LinkedHashMap<String, SyntacticStructure> parseTestfile (String test_file) {

        File f = new File(test_file);
        LinkedHashMap<String, SyntacticStructure> parsed_sentences_map = new LinkedHashMap<>();
        List<String> sentences = new ArrayList<>();

        if (f.isDirectory()) {
            //System.out.println("UD requires a UD testfile. (.txt file with sentences every other line)");
            // pathnames = Arrays.asList(Objects.requireNonNull(f.list(filter)));
        } else
        {
            try{
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String line;

                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        sentences.add(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (String sentence : sentences)
        {
            SyntacticStructure parsed_sentence = parseSingle(sentence);
            parsed_sentences_map.put(parsed_sentence.local_id,parsed_sentence);

        }
        return parsed_sentences_map;
    }

    public void applyRulefile(LinkedHashMap<String,SyntacticStructure> input, String path)
    {

    }

    //TODO return String with sentence -- modal --
    /*public String extractModals (SyntacticStructure fs) {

        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);
        RuleParser rp = new RuleParser(fsList, QueryParserTest.testFolderPath + "testRulesUD5.txt");
        rp.addAnnotation2(fs);
        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch(Exception e)
        {
            System.out.println("Sorting annotation failed.");
        }
        for (GraphConstraint g : fs.annotation) {
            System.out.println(g);
        }
        List<List<GraphConstraint>> substructure = fs.getSubstructures("FEATURES");
        ArrayList<String> sent_modals = new ArrayList<>();
        sent_modals.add(fs.sentence);
        for (List<GraphConstraint> sstr : substructure)
        {
            for (GraphConstraint g : sstr)
            {
                if (g.getRelationLabel().equals("MODAL"))
                {
                    sent_modals.add(g.getFsValue().toString());
                }
            }
        }

        //return this

        StringBuilder sb = new StringBuilder();


        for(int i = 0; i < sent_modals.size(); i++) {
            sb.append(sent_modals.get(i));
        }
        System.out.println("\nDone");

        return sb.toString();
    }*/


    public static List<String> extractModals (SyntacticStructure fs) {

        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);
        RuleParser rp = new RuleParser(fsList, QueryParserTest.testFolderPath + "testRulesUD5.txt");
        rp.addAnnotation2(fs);
        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch(Exception e)
        {
            System.out.println("Sorting annotation failed.");
        }
        for (GraphConstraint g : fs.annotation) {
            //System.out.println(g);
        }
        List<List<GraphConstraint>> substructure = fs.getSubstructures("FEATURES");
        List<String> sent_modals = new ArrayList<>();
        sent_modals.add(fs.sentence);
        for (List<GraphConstraint> sstr : substructure)
        {
            for (GraphConstraint g : sstr)
            {
                if (g.getRelationLabel().equals("MODAL"))
                {
                    sent_modals.add(g.getFsValue().toString());
                }
            }
        }
        System.out.println("\nDone");
        //return this

        return sent_modals;
    }


    //TODO collect all sentence + modal strings in a list;
    public List<List<String>> extractModalsFromTestfile(String testfile)
    {
      LinkedHashMap<String,SyntacticStructure> sentences =  parseTestfile(testfile);

      List<List<String>> output = new ArrayList<>();

      for (String key : sentences.keySet())
      {
          output.add(extractModals(sentences.get(key)));
      }
      return output;
    }


    public LinkedHashMap<IndexedWord,List<GraphConstraint>> generateDependencyMap(GrammaticalStructure gs)
    {
        LinkedHashMap<IndexedWord,List<GraphConstraint>> dependencyMap = new LinkedHashMap<>();

        List<GraphConstraint> fsList = new ArrayList<>();


        for (TypedDependency structure : gs.typedDependencies())
        {

            if (structure.reln().equals("root")) {

            }

            // GraphConstraint fsC = new GraphConstraint("1",structure.gov().hashCode(),structure.reln(), );



            //new entry if no key for the respective pred is available
            if (dependencyMap.get(structure.gov()) == null)

            {
                List<GraphConstraint> values = new ArrayList<>();
           //     values.add(new GraphConstraint("1",i,structure.reln().toString(),structure.dep().toString()));
                dependencyMap.put(structure.gov(), values);
            }
            else
            {
             //Jo   dependencyMap.get(structure.gov()).add(new GraphConstraint(structure.reln().toString(),structure.dep()));
            }
        }
        return dependencyMap;
    }



    public LinkedHashMap<String,SyntacticStructure> ud2Java(String inputPath)
    {
        //In
        //VariableHandler vh = new VariableHandler();
        File f = new File(inputPath);

        //Out
        LinkedHashMap<String, SyntacticStructure> out = new LinkedHashMap<>();

// This filter will only include files ending with .py

// This is how to apply the filter

        List<String> sentences = new ArrayList<>();

        if (f.isDirectory()) {
            //System.out.println("UD requires a UD testfile. (.txt file with sentences every other line)");
           // pathnames = Arrays.asList(Objects.requireNonNull(f.list(filter)));
        } else
        {
            try{
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String line;

                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        sentences.add(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String pn : sentences)
        {

            UDstructure ud = (UDstructure) parseSingle(pn);

            out.put(ud.local_id,ud);

        }

        return out;
    }
}
