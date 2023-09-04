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

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.Fstructure;
import de.ukon.liger.utilities.HelperMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GlueSemantics {


    private static Pattern daughterPattern = Pattern.compile("DAUGHTER(\\d+)");
    private final static Logger LOGGER = LoggerFactory.getLogger(GlueSemantics.class);

    public GlueSemantics() {
    }


    public String returnMeaningConstructors(LinguisticStructure fs) {

        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();

        for (Set<ChoiceVar> choice : fs.cp.choices) {

            unpackedSem.put(choice, new ArrayList<>());
        }

        StringBuilder sb = new StringBuilder();


        //extract from LiGER
        for (GraphConstraint c : fs.annotation) {
            if (c.getRelationLabel().equals("GLUE")) {
                if (!HelperMethods.isInteger(c.getFsValue()))
                {
                if (unpackedSem.containsKey(c.getReading())) {
                    unpackedSem.get(c.getReading()).add(c.getFsValue().toString());
                }
            }
            }
        }
        //extract from Grammar
        HashMap<Set<ChoiceVar>,List<String>> grammarSem = translateMeaningConstructors(fs);
        boolean relevantChoice = false;

        for (Set<ChoiceVar> choice : unpackedSem.keySet()) {
            if (!choice.equals(fs.cp.rootChoice) && !unpackedSem.get(choice).isEmpty()) {
                relevantChoice = true;
                unpackedSem.get(choice).addAll(unpackedSem.get(fs.cp.rootChoice));
            }
        }

        if (relevantChoice) {
            unpackedSem.remove(fs.cp.rootChoice);
        }

        List<Set<ChoiceVar>> keyList = new ArrayList<>(unpackedSem.keySet());
         for (int i = 0; i < keyList.size(); i++)
         {
             Set<ChoiceVar> key = keyList.get(i);

             boolean semGrammar= false;
             if (!grammarSem.keySet().isEmpty()) {
                 if (!grammarSem.get(key).isEmpty()) {
                     semGrammar = true;
                 }
             }

             sb.append("{");
             sb.append("\n");

             if (unpackedSem.containsKey(key) && !unpackedSem.get(key).isEmpty()) {

                 if (!unpackedSem.get(key).isEmpty()) {
                     sb.append("//Liger");
                     sb.append("\n");


                     for (String s : unpackedSem.get(key)) {
                         sb.append(s);
                         sb.append("\n");
                     }
                 }
             }

                 if (semGrammar)
                 {
                     sb.append("//Grammar");
                     sb.append("\n");

                 for (String s : grammarSem.get(key)) {
                     sb.append(s);
                     sb.append("\n");
                 }
}

                 sb.append("}");

                 if (i < keyList.size() -1){
                     sb.append("\n");
                 }
             }
        return sb.toString();
    }

    public HashMap<Set<ChoiceVar>, List<String>> translateMeaningConstructors(LinguisticStructure fs) {

        List<GraphConstraint> ls = new ArrayList<>(fs.returnFullGraph());

        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();

        HashMap<String,Set<ChoiceVar>> glueIndices = new HashMap<>();

        GraphConstraint rootNode = null;

                try {
                    rootNode = ((Fstructure) fs).cStructureFacts.stream().filter(c -> c.isRoot()).findFirst().get();
                } catch (Exception e) {
                    LOGGER.error("No root node found for c-stucture");
                }

        if (rootNode != null)
        {
            String rootId = rootNode.getFsNode();

            HashMap<String,Object> proofTree = buildProofTree((Fstructure) fs,rootId);

        }

        for (GraphConstraint c : ls) {
            if (c.getRelationLabel().equals("GLUE")){
                if (HelperMethods.isInteger(c.getFsValue()))
                {
                    Set<GraphConstraint> glueSetElements = ls.stream().filter(c2 -> Integer.parseInt(c2.getFsNode()) ==
                            Integer.parseInt((String) c.getFsValue())).collect(Collectors.toSet());

                    for (GraphConstraint c3 : glueSetElements){
                        if (c3.getRelationLabel().equals("in_set"))
                        {
                            glueIndices.put((String) c3.getFsValue(), c3.getReading());
                        }
                        if (!unpackedSem.containsKey(c3.getReading())){
                            unpackedSem.put(c.getReading(), new ArrayList<>());
                        }
                    }
                }
            }
        }
        for (String i : glueIndices.keySet()){
            unpackedSem.get(glueIndices.get(i)).add(parseMCfromProlog(i,ls));
        }
        return unpackedSem;
    }


    public HashMap<String, Object> buildProofTree(Fstructure fs, String rootNode)
    {
        QueryParser qp = new QueryParser(fs);
        qp.generateQuery("*" + rootNode + " !(phi>t::) #t");
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        if (qpr.isSuccess) {
            if (qpr.result.size() == 1) {
                String tNode = qpr.result.keySet().stream().findAny().get().stream().filter(c -> c.variable.equals("t")).map(c -> c.reference).findFirst().get();

                List<GraphConstraint> proofConstraints = fs.returnFullGraph().stream().filter(c -> c.getFsNode().equals(tNode)).collect(Collectors.toList());

                String elementConstraint = null;
                HashMap<Integer,Set<String>> daughters = new HashMap<>();
                for (GraphConstraint c : proofConstraints)
                {

                }


            } else
            {
                LOGGER.warn("Mapping from c-structure to t-structure failed. Mapping is incoherent...");
                return null;
            }
        }





        return null;
    }


    public String findMC(String cstrNode, Fstructure fs)
        {
            QueryParser qp = new QueryParser(fs);
            qp.generateQuery("*" + cstrNode + " !(cproj>g::>GLUE>in_set) #s");

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            if (qpr.isSuccess)
            {
                System.out.println("Found mc");

            }


            return null;
        }



    public String parseMCfromProlog(String glueNode, List<GraphConstraint> ls)
    {
        String meaning = "";

        List<GraphConstraint> glueConstraints = ls.stream().filter(c -> c.getFsNode().equals(glueNode)).collect(Collectors.toList());
        //Find graph constraint in glueConstraints with label GLUE

        List<GraphConstraint> meaningConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("MEANING")).collect(Collectors.toList());

        if (!meaningConstraint.isEmpty()){
            meaning = (String) meaningConstraint.stream().findAny().get().getFsValue();
            //Strip single quotes of meaning
            meaning = meaning.substring(1,meaning.length()-1);
        }

        List<GraphConstraint> resourceConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("RESOURCE")).collect(Collectors.toList());

        if (!resourceConstraint.isEmpty())
        {
            //Resource is atomic
            String resource = "";
            String type = "" ;
            List<GraphConstraint> typeConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("TYPE")).collect(Collectors.toList());
            if (!typeConstraint.isEmpty())
            {
                type = (String) typeConstraint.stream().findAny().get().getFsValue();
                type = type.substring(1,type.length()-1);
            }
            resource = (String) resourceConstraint.stream().findAny().get().getFsValue();


            if (meaning.equals(""))
            {
                return resource + "_" + type;
            } else {
                return meaning + " : " + resource + "_" + type;
            }
        } else {
            //resource is non-atomic
            String antString = "";
            String consString = "";

            List<GraphConstraint> antecedent = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("ANT")).collect(Collectors.toList());
            List<GraphConstraint> consequent = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("CONS")).collect(Collectors.toList());

            if (!antecedent.isEmpty())
            {
                GraphConstraint ant = antecedent.stream().findAny().get();
                antString = parseMCfromProlog( (String) ant.getFsValue(),ls);
            }

            if (!consequent.isEmpty())
            {
                GraphConstraint cons = consequent.stream().findAny().get();
                consString = parseMCfromProlog((String) cons.getFsValue(),ls);
            }

            if (meaning.equals(""))
            {
                return  "(" + antString + " -o " + consString + ")";
            } else
            {
                return meaning + " : " + "(" + antString + " -o " + consString + ")";

            }

        }
    }


    //For XLE+Glue version 1
    public String extractMCsFromFs(String fs) {
        List<String> drtSolutions = new ArrayList<>();
        //create a file that includes all Strings in solutions line  by line
        //run swipl with the file as input

        LOGGER.info("Creating temporary files...");
        //create temporary directory gswb_resources/tmp
        File tmpDir = new File("liger_resources/tmp");

        if (tmpDir.exists()) {
            File[] files = tmpDir.listFiles();
            for (File file : files) {
                file.delete();
            }
            tmpDir.delete();
        }

        tmpDir.mkdir();

        File prologFS = new File("liger_resources/tmp/prolog.pl");

        try {
            if (prologFS.createNewFile()) {
                LOGGER.info("File created successfully!");
            } else {
                LOGGER.error("File already exists!");
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }

        File xleTransferOutput = new File("liger_resources/tmp/xle_prolog_mcs.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(prologFS));
            writer.write(fs);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


// exec swipl -q -f  src/premises.pl -t "main." -- \
//	"$prologfile" "$outputfile"
        try {
            String[] command = {
                    "swipl",
                    "-q",
                    "-f",
                    "liger_resources/xle_glue_transfer/premises.pl",
                    "-t",
                    "main.",
                    "--",
                    prologFS.getCanonicalPath(),
                    xleTransferOutput.getCanonicalPath()
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Java join command with white space


            LOGGER.info("Executing Prolog goal to extract meaning constructors!");

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read and print the output of the external command

            // Wait for the process to complete
            // Create a separate thread to wait for the process to finish
            FutureTask<Integer> task = new FutureTask<>(process::waitFor);
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(task);

            // Wait for 5 seconds for the process to finish
            try {
                int exitCode = task.get(5, TimeUnit.SECONDS);
                if (exitCode != 0) {
                    LOGGER.error("\nFailed to read output from lambdaDRT.pl!\n");
                }
            } catch (Exception e) {
                // If the process takes more than 5 seconds, destroy it
                process.destroyForcibly();
                LOGGER.error("\nProcess timed out and was forcibly terminated.\n");
            }

            executor.shutdown();

            //read file xleTransferOutput to String

            String xleTransferOutputString = "";

            if (xleTransferOutput.exists()) {


                BufferedReader reader = new BufferedReader(new FileReader(xleTransferOutput));

                //concatenate all lines in the file to one String
                xleTransferOutputString = reader.lines().collect(Collectors.joining("\n"));
            } else {
                LOGGER.error("File " + xleTransferOutput.getCanonicalPath() + " does not exist!");

            }



            //delete temporary files
            prologFS.delete();
            xleTransferOutput.delete();
            tmpDir.delete();

            return xleTransferOutputString;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
