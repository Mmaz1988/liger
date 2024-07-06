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
import de.ukon.liger.syntax.xle.Fstructure;
import de.ukon.liger.utilities.HelperMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class GlueSemantics {


    private final static Logger LOGGER = LoggerFactory.getLogger(GlueSemantics.class);

    public GlueSemantics() {
    }


    /**
     * @Param xlePlusGlueVersion: 1 = xleplusglue prolog version, 2 = xleplus java version Only 2 is compatible
     * with LiGER. Thus, only then attempt hybrid
    TODO modify this so that it can deal with those modalities: XLE+Glue Prlog version, XLE+Glue Liger version, and
    multistage proving. XLE+Glue options should be mutually exlusive
     */
    public String returnMeaningConstructors(LinguisticStructure fs, boolean prolog, boolean multistage) {

        //Unpacked Semantics corresponds to the information that comes from LiGER
        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();

        for (Set<ChoiceVar> choice : fs.cp.choices) {

            unpackedSem.put(choice, new ArrayList<>());
        }

        StringBuilder sb = new StringBuilder();

            if (!fs.annotation.isEmpty()) {
                //extract from LiGER
                for (GraphConstraint c : fs.annotation) {
                    if (c.getRelationLabel().equals("GLUE")) {
                        if (!HelperMethods.isInteger(c.getFsValue())) {
                            if (unpackedSem.containsKey(c.getReading())) {
                                unpackedSem.get(c.getReading()).add(c.getFsValue().toString());
                            }
                        }
                    }
                }
            }

        //Remove elements with empty values from unpackedSem
        unpackedSem.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        HashMap<Set<ChoiceVar>, Set<String>> grammarSem = new HashMap<>();

        //Check if any constraint in fs.constraints has the relation label "GLUE"
        boolean hasGlue = false;
        for (GraphConstraint c : fs.constraints) {
            if (c.getRelationLabel().equals("GLUE")) {
                hasGlue = true;
            }
        }

        String prologMCs = "";
        //extract from Grammar
        if (!prolog) {
            grammarSem = translateMeaningConstructors(fs);
        } else if (hasGlue)
        {
          prologMCs =  extractMCsFromFs(((Fstructure) fs).prologString);
        }


        //Unpack Liger
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

        //Unpack grammar
        boolean relevantChoiceGrammar = false;

        for (Set<ChoiceVar> choice : grammarSem.keySet()) {
            if (!choice.equals(fs.cp.rootChoice) && !grammarSem.get(choice).isEmpty()) {
                relevantChoiceGrammar = true;
                grammarSem.get(choice).addAll(grammarSem.get(fs.cp.rootChoice));
            }
        }
        if (relevantChoiceGrammar) {
            grammarSem.remove(fs.cp.rootChoice);
        }


        List<Set<ChoiceVar>> keyListLiger = new ArrayList<>();
        List<Set<ChoiceVar>> keyListGrammar = new ArrayList<>();

        if (!unpackedSem.keySet().isEmpty())
        {
         keyListLiger = new ArrayList<>(unpackedSem.keySet());
        }

        if (!grammarSem.keySet().isEmpty()) {
            keyListGrammar = new ArrayList<>(grammarSem.keySet());
        }

         for (int i = 0; i < keyListLiger.size(); i++) {
             Set<ChoiceVar> keyLiger = keyListLiger.get(i);
            // for (int j = 0; i < keyListGrammar.size(); i++) {

              if (keyListGrammar.contains(keyLiger))
              {
                 Set<ChoiceVar> keyGrammar = keyLiger;

                 if (keyLiger.equals(keyGrammar)) {

                     sb.append("{");
                     sb.append("\n");

                     if (!unpackedSem.get(keyLiger).isEmpty()) {
                         sb.append("//Liger");
                         sb.append("\n");


                         for (String s : unpackedSem.get(keyLiger)) {
                             sb.append(s);
                             sb.append("\n");
                         }
                     }


                     if (!grammarSem.get(keyGrammar).isEmpty()) {
                         sb.append("//Grammar");
                         sb.append("\n");

                         for (String s : grammarSem.get(keyGrammar)) {
                             sb.append(s);
                             sb.append("\n");
                         }
                     }

                     sb.append("}");

                     if (i < keyListLiger.size() - 1 || keyListLiger.size() -1 < keyListGrammar.size() - 1) {
                         sb.append("\n");
                     }
                 }

                 }

              else {
                  for (int j = 0; j < keyListGrammar.size(); j++) {
                      Set<ChoiceVar> keyGrammar = keyListGrammar.get(j);

                      sb.append("{");
                      sb.append("\n");

                      if (!unpackedSem.get(keyLiger).isEmpty()) {
                          sb.append("//Liger");
                          sb.append("\n");


                          for (String s : unpackedSem.get(keyLiger)) {
                              sb.append(s);
                              sb.append("\n");
                          }
                      }

                      if (!grammarSem.get(keyGrammar).isEmpty()) {
                          sb.append("//Grammar");
                          sb.append("\n");

                          for (String s : grammarSem.get(keyGrammar)) {
                              sb.append(s);
                              sb.append("\n");
                          }
                      }

                      sb.append("}");

                      if (i < keyListGrammar.size() - 1 || keyListGrammar.size() -1 < keyListLiger.size() - 1) {
                          sb.append("\n");
                      }
                  }
              }
             }


         if (prolog && unpackedSem.isEmpty())
         {
             return prologMCs;
         }
        return sb.toString();
    }


    /**
     * method for testing multiStageProving without packing
      * @param fs
     * @return
     */
    public String returnMultiStageMeaningConstructors(LinguisticStructure fs)
    {
        try {

            GraphConstraint rootNode = null;

            try {
                rootNode = ((Fstructure) fs).cStructureFacts.stream().filter(c -> c.isRoot()).findFirst().get();
            } catch (Exception e) {
                rootNode = ((Fstructure) fs).cStructureFacts.get(0);
                LOGGER.error("No root node found for c-stucture");
            }

            if (rootNode != null) {
                String rootId = rootNode.getFsNode();
                LinkedHashMap<String, Object> cstr = ((Fstructure) fs).builtCstructureTree(rootId);

                // CStructureTraverser ctr = new CStructureTraverser();

                CStructureTraverser ctr = new CStructureTraverser(rootId, (Fstructure) fs);

                //Start time for next step
                long startTime = System.currentTimeMillis();

                /*
                ctr.traverseCStructure(cstr, null, rootId, null);
                ctr.visitedProofNodes = new HashSet<>();
                */

                ctr.traverseCstructure2(cstr,null);

                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                LOGGER.info("Traversed C-Structure in " + duration + " ms");
                LOGGER.info("Full mapping from proof tree to cstr:\n" + ctr.prooftreeToCStructure);

                //String glueRoot = ctr.findGlueTreeRoot();

                StringBuilder sb = new StringBuilder();

                sb.append("//" + ctr.printGlueTree() + "\n");

                sb.append("{\n");

                /*
                Integer addedMCs = 0;
                if (ctr.associatedMCs.containsKey("null"))
                {
                    for (String glueNode : ctr.associatedMCs.get("null"))
                    {
                        sb.append(parseMCfromProlog(glueNode,fs.returnFullGraph()) + "\n");
                        addedMCs++;
                    }
                }
                 */

                List<Object> flattenedGlueTree = new ArrayList<>();

                ctr.translateGlueTreeToList("root",flattenedGlueTree);

                flattenedGlueTree.remove(0);

                for (Object string : flattenedGlueTree)
                {
                    if (HelperMethods.isInteger(string))
                    {
                        sb.append(parseMCfromProlog((String) string, fs.returnFullGraph()) + "\n");
                    } else {
                        sb.append(string + "\n");
                    }
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to extract MCs from Cstructure");
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<Set<ChoiceVar>, Set<String>> translateMeaningConstructors(LinguisticStructure fs) {

        HashMap<String,HashMap<Set<ChoiceVar>, List<String>>> disjunctiveSem = new HashMap<>();
        List<GraphConstraint> ls = new ArrayList<>(fs.returnFullGraph());
        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();
        HashMap<String,Set<ChoiceVar>> glueIndices = new HashMap<>();

            for (GraphConstraint c : ls) {
                if (c.getRelationLabel().equals("GLUE")) {
                    if (HelperMethods.isInteger(c.getFsValue())) {
                        Set<GraphConstraint> glueSetElements = ls.stream().filter(c2 -> Integer.parseInt(c2.getFsNode()) ==
                                Integer.parseInt((String) c.getFsValue())).collect(Collectors.toSet());

                        for (GraphConstraint c3 : glueSetElements) {
                            if (c3.getRelationLabel().equals("in_set")) {
                                glueIndices.put((String) c3.getFsValue(), c3.getReading());
                            }
                            if (!unpackedSem.containsKey(c3.getReading())) {
                                unpackedSem.put(c.getReading(), new ArrayList<>());
                            }
                        }
                    }
                }
            }
            for (String i : glueIndices.keySet()) {
                HashMap<Set<ChoiceVar>,String> testMap = parseMCfromPackedProlog(i, ls);

                if (!disjunctiveSem.containsKey(i))
                {
                    disjunctiveSem.put(i,new HashMap<>());
                }
                    for (Set<ChoiceVar> key : testMap.keySet()) {
                        if (!disjunctiveSem.get(i).containsKey(key)) {
                            disjunctiveSem.get(i).put(key, new ArrayList<>());
                        }
                        disjunctiveSem.get(i).get(key).add(testMap.get(key));
                    }
                String mc = parseMCfromProlog(i, ls);
                unpackedSem.get(glueIndices.get(i)).add(mc);
            }

            HashMap<Set<ChoiceVar>, Set<String>> unpackedSem2 = new HashMap<>();
            Set<ChoiceVar> defaultReading = Collections.singleton(new ChoiceVar());
            unpackedSem2.put(defaultReading, new HashSet<>());


        // Use streams to partition the map entries


        // Extract the singleton and multi-element maps from the partitioned map
        Map<String, HashMap<Set<ChoiceVar>, List<String>>> singletonMap = new HashMap<>();
        Map<String, HashMap<Set<ChoiceVar>, List<String>>> multiElementMap = new HashMap<>();

        for (String key : disjunctiveSem.keySet()){
            if (disjunctiveSem.get(key).keySet().size() == 1)
            {
                singletonMap.put(key,disjunctiveSem.get(key));
            } else
            {
                multiElementMap.put(key,disjunctiveSem.get(key));
            }
        }

            for (String i : singletonMap.keySet())
            {
                unpackedSem2.get(defaultReading).addAll(singletonMap.get(i).get(defaultReading));
            }

            //raise packed mcs to conjunctive normal form
            for (String i : multiElementMap.keySet())
            {
                 for (Set<ChoiceVar> choice : multiElementMap.get(i).keySet())
                 {
                     if (!choice.equals(defaultReading)) {
                         if (!unpackedSem2.containsKey(choice)) {
                             unpackedSem2.put(choice, new HashSet<>());
                         }
                         unpackedSem2.get(choice).addAll(multiElementMap.get(i).get(choice));


                         for (String j : multiElementMap.keySet()) {
                             if (!j.equals(i)) {
                                 if (multiElementMap.get(j).containsKey(choice)) {
                                     unpackedSem2.get(choice).addAll(multiElementMap.get(j).get(choice));
                                 } else {
                                     unpackedSem2.get(choice).addAll(multiElementMap.get(j).get(defaultReading));
                                 }
                             }
                         }
                     }
                 }
            }
            return unpackedSem2;
    }



    //Extracts a XLE+Glue version 2 mc from Prolog
    //Ignores any choice that is not the default choice (choice 1)
    public String parseMCfromProlog(String glueNode, List<GraphConstraint> ls)
    {
        String meaning = "";

        List<GraphConstraint> glueConstraints = ls.stream().filter(c -> c.getFsNode().equals(glueNode)).collect(Collectors.toList());
        //Find graph constraint in glueConstraints with label GLUE

        List<GraphConstraint> meaningConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("MEANING")).collect(Collectors.toList());

        if (!meaningConstraint.isEmpty()){

            meaningConstraint = meaningConstraint.stream().filter(c -> c.getReading().equals(Collections.singleton(new ChoiceVar()))).collect(Collectors.toList());

            meaning = (String) meaningConstraint.stream().findAny().get().getFsValue();
            //Strip single quotes of meaning
            meaning = meaning.substring(1,meaning.length()-1);
            //replace \' with '
            meaning = meaning.replace("\\'","'");

            //TODO ? possibly remove constraints that have already been covered?
        }

        // For parameters like noscope
        List<String> params = new ArrayList<>();

        List<GraphConstraint> noscopeConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("NOSCOPE")).collect(Collectors.toList());
        noscopeConstraint = noscopeConstraint.stream().filter(c -> c.getReading().equals(Collections.singleton(new ChoiceVar()))).collect(Collectors.toList());

        if (!noscopeConstraint.isEmpty())
        {
            params.add("noscope");
        }

        List<GraphConstraint> resourceConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("RESOURCE")).collect(Collectors.toList());
        resourceConstraint = resourceConstraint.stream().filter(c -> c.getReading().equals(Collections.singleton(new ChoiceVar()))).collect(Collectors.toList());

        if (!resourceConstraint.isEmpty())
        {
            //Resource is atomic
            String resource = "";
            String type = "" ;
            List<GraphConstraint> typeConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("TYPE")).collect(Collectors.toList());
            typeConstraint = typeConstraint.stream().filter(c -> c.getReading().equals(Collections.singleton(new ChoiceVar()))).collect(Collectors.toList());

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
                antecedent = antecedent.stream().filter(c -> c.getReading().equals(Collections.singleton(new ChoiceVar()))).collect(Collectors.toList());
                GraphConstraint ant = antecedent.stream().findAny().get();
                antString = parseMCfromProlog( (String) ant.getFsValue(),ls);
            }

            if (!consequent.isEmpty())
            {
                consequent = consequent.stream().filter(c -> c.getReading().equals(Collections.singleton(new ChoiceVar()))).collect(Collectors.toList());
                GraphConstraint cons = consequent.stream().findAny().get();
                consString = parseMCfromProlog((String) cons.getFsValue(),ls);
            }

            String paramString = "";

            if (!params.isEmpty())
            {
                paramString = " || " + params.stream().collect(Collectors.joining(", "));
            }
            if (meaning.equals(""))
            {
                return  "(" + antString + " -o " + consString + ")";
            } else
            {
                return meaning + " : " + "(" + antString + " -o " + consString + ")" + paramString;
            }

        }
    }

   // private HashMap<Set<ChoiceVar>,String> unpackedMCs = new HashMap<>();
    public HashMap<Set<ChoiceVar>,String> parseMCfromPackedProlog(String glueNode, List<GraphConstraint> ls)
    {
        HashMap<Set<ChoiceVar>,String> unpackedMeaningConstructors = new HashMap<>();
        //String meaning = "";
        HashMap<Set<ChoiceVar>,String> unpackedMeanings = new HashMap<>();

        Set<ChoiceVar> defaultContext = Collections.singleton(new ChoiceVar());


        List<GraphConstraint> glueConstraints = ls.stream().filter(c -> c.getFsNode().equals(glueNode)).collect(Collectors.toList());
        //Find graph constraint in glueConstraints with label GLUE

        Set<Set<ChoiceVar>> relevantChoices = glueConstraints.stream().map(GraphConstraint::getReading).collect(Collectors.toSet());
        for (Set<ChoiceVar> choice : relevantChoices)
        {
            if (!unpackedMeaningConstructors.containsKey(choice))
            {
                unpackedMeaningConstructors.put(choice,"");
            }
        }

        List<GraphConstraint> meaningConstraints = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("MEANING")).collect(Collectors.toList());

        if (!meaningConstraints.isEmpty()){
            for (GraphConstraint meaningConstraint : meaningConstraints)
            {
                String currentMeaning =  (String) meaningConstraint.getFsValue();

                //Strip single quotes of meaning
                currentMeaning = currentMeaning.substring(1,currentMeaning.length()-1);
                //replace \' with '
                currentMeaning = currentMeaning.replace("\\'","'");
                unpackedMeanings.put( meaningConstraint.getReading(), currentMeaning);
            }
            //TODO ? possibly remove constraints that have already been covered?
        }

        // For parameters like noscope
        List<String> params = new ArrayList<>();

        List<GraphConstraint> noscopeConstraint = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("NOSCOPE")).collect(Collectors.toList());

        if (!noscopeConstraint.isEmpty())
        {
            params.add("noscope");
        }

        List<GraphConstraint> resourceConstraints = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("RESOURCE")).collect(Collectors.toList());

        if (!resourceConstraints.isEmpty())
        {
            HashMap<Set<ChoiceVar>,String> currentResourceStrings = new HashMap<>();
            HashMap<Set<ChoiceVar>,String> currentTypeStrings = new HashMap<>();

            for (GraphConstraint resourceConstraint : resourceConstraints) {

                //Resource is atomic
                String resource = "";
                resource = (String) resourceConstraint.getFsValue();
                currentResourceStrings.put(resourceConstraint.getReading(),resource);
            }
                List<GraphConstraint> typeConstraints = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("TYPE")).collect(Collectors.toList());
                if (!typeConstraints.isEmpty()) {
                    for (GraphConstraint typeConstraint : typeConstraints) {
                        String type = "";
                        type = (String) typeConstraint.getFsValue();
                        type = type.substring(1, type.length() - 1);
                        currentTypeStrings.put(typeConstraint.getReading(), type);
                    }
                }

                for (Set<ChoiceVar> choice : relevantChoices)
                {
                    String resource = currentResourceStrings.get(choice);

                    //Default type t
                    String type = "t";

                    if (currentTypeStrings.containsKey(choice)) {
                        type = currentTypeStrings.get(choice);
                    }
                    else{
                        type = currentTypeStrings.get(defaultContext);
                    }
                    String meaning = null;

                    if (unpackedMeanings.containsKey(choice)) {
                        meaning = unpackedMeanings.get(choice);
                    } else {
                        meaning =  unpackedMeanings.get(Collections.singleton(new ChoiceVar("1")));
                    }

                    if (meaning == null) {
                        unpackedMeaningConstructors.put(choice, resource + "_" + type);

                    } else {
                        unpackedMeaningConstructors.put(choice, meaning + " : " + resource + "_" + type);
                    }
                }

                return unpackedMeaningConstructors;

        } else {
            //resource is non-atomic
            HashMap<Set<ChoiceVar>,String> possibleAntecedentStrings = new HashMap<>();
            HashMap<Set<ChoiceVar>,String> possibleConsequentStrings = new HashMap<>();

            List<GraphConstraint> antecedent = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("ANT")).collect(Collectors.toList());
            List<GraphConstraint> consequent = glueConstraints.stream().filter(c -> c.getRelationLabel().equals("CONS")).collect(Collectors.toList());

            if (!antecedent.isEmpty())
            {
                GraphConstraint ant = antecedent.stream().findAny().get();
            //    antString = parseMCfromProlog( (String) ant.getFsValue(),ls);
                possibleAntecedentStrings = parseMCfromPackedProlog((String) ant.getFsValue(),ls);
            }

            if (!consequent.isEmpty())
            {
                GraphConstraint cons = consequent.stream().findAny().get();
                //consString = parseMCfromProlog((String) cons.getFsValue(),ls);
                possibleConsequentStrings = parseMCfromPackedProlog((String) cons.getFsValue(),ls);
            }


            for (Set<ChoiceVar> key : possibleAntecedentStrings.keySet()) {

                String paramString = "";

                if (!params.isEmpty()) {
                    paramString = " || " + params.stream().collect(Collectors.joining(", "));
                }

                StringBuilder newMC = new StringBuilder();


                if (!unpackedMeanings.isEmpty()) {
                    if (unpackedMeanings.containsKey(key)) {
                        newMC.append(unpackedMeanings.get(key));
                    } else {
                     newMC.append(unpackedMeanings.get(defaultContext));
                    }
                    newMC.append(" : ");
                }

                newMC.append("(");

                newMC.append(possibleAntecedentStrings.get(key));

                newMC.append(" -o ");

                if (possibleConsequentStrings.containsKey(key))
                {
                    newMC.append(possibleConsequentStrings.get(key));
                } else
                {
                    newMC.append(possibleConsequentStrings.get(defaultContext));
                }
          // "(" + antString + " -o " + consString + ")" + paramString;

                newMC.append(")");
                newMC.append(paramString);

                unpackedMeaningConstructors.put(key,newMC.toString());

                }

            for (Set<ChoiceVar> key : possibleConsequentStrings.keySet()) {

                if (!possibleAntecedentStrings.containsKey(key)) {

                    String paramString = "";

                    if (!params.isEmpty()) {
                        paramString = " || " + params.stream().collect(Collectors.joining(", "));
                    }

                    StringBuilder newMC = new StringBuilder();

                    if (!unpackedMeanings.isEmpty()) {
                        if (unpackedMeanings.containsKey(key)) {
                            newMC.append(unpackedMeanings.get(key));
                        } else {
                            newMC.append(unpackedMeanings.get(defaultContext));
                        }
                        newMC.append(" : ");
                    }

                    newMC.append("(");

                    newMC.append(possibleAntecedentStrings.get(defaultContext));

                    newMC.append(" -o ");

                    newMC.append(possibleConsequentStrings.get(key));

                    newMC.append(")");
                    newMC.append(paramString);

                    unpackedMeaningConstructors.put(key, newMC.toString());
                }
            }
            return unpackedMeaningConstructors;
        }
    }



    //For XLE+Glue version 1. Uses Prolog to extract MCs
    public String extractMCsFromFs(String fs) {
        List<String> drtSolutions = new ArrayList<>();
        //create a file that includes all Strings in solutions line  by line
        //run swipl with the file as input

        LOGGER.info("Creating temporary files...");
        //create temporary directory gswb_resources/tmp
        File tmpDir = new File("liger_resources/tmp/prolog");

        if (tmpDir.exists()) {
            File[] files = tmpDir.listFiles();
            for (File file : files) {
                file.delete();
            }
            tmpDir.delete();
        }

        tmpDir.mkdir();

        File prologFS = new File("liger_resources/tmp/prolog/prolog.pl");

        try {
            if (prologFS.createNewFile()) {
                LOGGER.info("File created successfully!");
            } else {
                LOGGER.error("File already exists!");
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while creating the file: " + e.getMessage() + "\n");
        }

        File xleTransferOutput = new File("liger_resources/tmp/prolog/xle_prolog_mcs.txt");
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
                    return null;
                }
            } catch (Exception e) {
                // If the process takes more than 5 seconds, destroy it
                process.destroyForcibly();
                LOGGER.error("\nProcess timed out and was forcibly terminated.\n");
                return null;
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
