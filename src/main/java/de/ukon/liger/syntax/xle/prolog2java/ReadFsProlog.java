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

package de.ukon.liger.syntax.xle.prolog2java;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.utilities.VariableHandler;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReadFsProlog implements Serializable {

    public String sentence;
    public List<String> fstr;
    public List<String> cstr;
    public String sentenceID;
   public VariableHandler vh;
   public ChoiceSpace cp;
   public String prologString;
    private final static Logger LOGGER = Logger.getLogger(ReadFsProlog.class.getName());

    public ReadFsProlog(String sentenceID, String sentence, List<String> fsConstraints, VariableHandler vh)
    {
        this.sentenceID = sentenceID;
        this.sentence = sentence;
        this.fstr = fsConstraints;
        this.vh = vh;

    }

    public ReadFsProlog(String sentenceID, String sentence, List<String> fsConstraints, ChoiceSpace cp, VariableHandler vh)
    {
        this.sentenceID = sentenceID;
        this.sentence = sentence;
        this.fstr = fsConstraints;
        this.cp = cp;
        this.vh = vh;

    }

    public ReadFsProlog(String sentenceID, String sentence, List<String> fsConstraints, List<String> cConstraints, ChoiceSpace cp, VariableHandler vh)
    {
        this.sentenceID = sentenceID;
        this.sentence = sentence;
        this.fstr = fsConstraints;
        this.cstr = cConstraints;
        this.cp = cp;
        this.vh = vh;

    }

    public static ReadFsProlog readPrologFile(File inFile, VariableHandler vh)
    {

        String sentenceID = null;

        try {
            Pattern idPattern = Pattern.compile("(S\\d+)\\.pl$");
            Matcher idMatcher = idPattern.matcher(inFile.getPath());

            if (idMatcher.find())
            {
                sentenceID = idMatcher.group(1);
            }
        }catch(Exception e)
        {
           LOGGER.warning("Invalid prolog file name: Could not retrieve sentence ID");
        }

        if (sentenceID == null)
        {
            sentenceID = vh.returnNewVar(VariableHandler.variableType.SENTENCE_ID,null);
        }



        BufferedReader br = null;

        // This list will contain our f-structure constraints
        String inSentence = "";
        List<String> fsConstraints = new ArrayList<String>();
        List<String> cstrFacts = new ArrayList<>();
        List<String> choiceSpace = new ArrayList<>();
        ChoiceSpace cp = null;

        try {
            // reads in File
            br = new BufferedReader(new FileReader(inFile));

            String strLine;
      //      int  counter = 0;
            // matches all constraints of the syntactic
            Pattern fstr = Pattern.compile("% Constraints:\\n\\t\\[(.+)\\n\\t\\],",Pattern.DOTALL);

            Pattern constraints = Pattern.compile("(cf\\(.*\\))");
            Pattern cStr = Pattern.compile("% C-Structure:\\n\\t\\[(.+)\\n\\t\\]\\)",Pattern.DOTALL);
            // Mark up free sentence
            Pattern sentence = Pattern.compile( "'markup_free_sentence'\\((.*?)\\)");

            Pattern choice = Pattern.compile("choice\\((.+)\\),?");

            String fstrString = br.lines().collect(Collectors.joining("\n"));

            Matcher constraintListMatcher = fstr.matcher(fstrString);
            Matcher cStrMatcher = cStr.matcher(fstrString);

            BufferedReader fStrReader = null;

            if (constraintListMatcher.find()) {
                fStrReader = new BufferedReader(new StringReader(constraintListMatcher.group(1)));
            } else
            {
                fStrReader = br;
            }
                String fline;

            //Read in f-structure information
            while ((fline = fStrReader.readLine()) != null)
            {
                Matcher constraintMatcher = constraints.matcher(fline);

                if (constraintMatcher.find()) {
                    // Material that we want to translate into java objects is stored in arrayList
                    fsConstraints.add(constraintMatcher.group(1));
                    //    counter++;

//                if (cstructureMatcher.find())
//                {
//                    fsConstraints.add(cstructureMatcher.group(1));
//                }
                }
            }

            //Read in c-structure information
            String cstr = null;
            BufferedReader cStrReader = null;

            if (cStrMatcher.find())
            {
                cstr = cStrMatcher.group(1);
                cStrReader = new BufferedReader(new StringReader(cStrMatcher.group(1)));
            } else
            { cStrReader = br;
            }


            String cline;
            while ((cline = cStrReader.readLine()) != null)
            {
                Matcher constraintMatcher = constraints.matcher(cline);

                if (constraintMatcher.find())
                {
                    cstrFacts.add(constraintMatcher.group(1));
                }

            }



            // matches c-structure constraints (this is very ugly but maybe enough)
       //     Pattern cstructure = Pattern.compile("((surfaceform|semform_data)\\(.+\\))");

            //TODO do not iterate through all lines again but only the necessary ones
            while ((strLine = br.readLine()) != null) {

                Matcher sentenceMatcher = sentence.matcher(strLine);
                Matcher choiceMatcher = choice.matcher(strLine);
                //             Matcher cstructureMatcher = cstructure.matcher(strLine);

                if (sentenceMatcher.find()) {
                    inSentence = sentenceMatcher.group(1);
                }


                if (choiceMatcher.find())
                {
                    choiceSpace.add(choiceMatcher.group(1));
                }
            }
          //  System.out.println(counter);


             cp = new ChoiceSpace(choiceSpace);

            /* Print out f-structure facts for test purposes
            for (int i = 0; i < fsConstraints.size(); i++) {
                System.out.println(fsConstraints.get(i));
            }
*/

        } catch (IOException e) {
            e.printStackTrace();
        }

        //close infile


       fsConstraints = simplifyFs(fsConstraints);
      //  fsConstraints = contractFstructure(fsConstraints);
       // fsConstraints = removeEqualities(fsConstraints);

        ReadFsProlog fstructure = new ReadFsProlog(sentenceID, inSentence, fsConstraints, cstrFacts, cp, vh);

        try {
            br = new BufferedReader(new FileReader(inFile));
            String prologString = br.lines().collect(Collectors.joining("\n"));

            fstructure.prologString = prologString;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fstructure;
    }

    public static ReadFsProlog readPrologString(String inputString, String id, VariableHandler vh)
    {

        String sentenceID = id;

        BufferedReader br = null;

        // This list will contain our f-structure constraints
        String inSentence = "";
        List<String> fsConstraints = new ArrayList<String>();
        List<String> cstrFacts = new ArrayList<>();
        List<String> choiceSpace = new ArrayList<>();
        ChoiceSpace cp = null;

        try {
            // reads in File
            br = new BufferedReader(new StringReader(inputString));

            String strLine;
            //      int  counter = 0;
            // matches all constraints of the syntactic
            Pattern fstr = Pattern.compile("% Constraints:\\n\\t\\[(.+)\\n\\t\\],",Pattern.DOTALL);

            Pattern constraints = Pattern.compile("(cf\\(.*\\))");
            Pattern cStr = Pattern.compile("% C-Structure:\\n\\t\\[(.+)\\n\\t\\]\\)",Pattern.DOTALL);
            // Mark up free sentence
            Pattern sentence = Pattern.compile( "'markup_free_sentence'\\((.*?)\\)");

            Pattern choice = Pattern.compile("choice\\((.+)\\),?");

            String fstrString = br.lines().collect(Collectors.joining("\n"));

            Matcher constraintListMatcher = fstr.matcher(fstrString);
            Matcher cStrMatcher = cStr.matcher(fstrString);

            BufferedReader fStrReader = null;

            if (constraintListMatcher.find()) {
                fStrReader = new BufferedReader(new StringReader(constraintListMatcher.group(1)));
            } else
            {
                fStrReader = br;
            }
            String fline;

            //Read in f-structure information
            while ((fline = fStrReader.readLine()) != null)
            {
                Matcher constraintMatcher = constraints.matcher(fline);

                if (constraintMatcher.find()) {
                    // Material that we want to translate into java objects is stored in arrayList
                    fsConstraints.add(constraintMatcher.group(1));
                    //    counter++;

//                if (cstructureMatcher.find())
//                {
//                    fsConstraints.add(cstructureMatcher.group(1));
//                }
                }
            }

            //Read in c-structure information
            String cstr = null;
            BufferedReader cStrReader = null;

            if (cStrMatcher.find())
            {
                cstr = cStrMatcher.group(1);
                cStrReader = new BufferedReader(new StringReader(cStrMatcher.group(1)));
            } else
            { cStrReader = br;
            }


            String cline;
            while ((cline = cStrReader.readLine()) != null)
            {
                Matcher constraintMatcher = constraints.matcher(cline);

                if (constraintMatcher.find())
                {
                    cstrFacts.add(constraintMatcher.group(1));
                }

            }



            // matches c-structure constraints (this is very ugly but maybe enough)
            //     Pattern cstructure = Pattern.compile("((surfaceform|semform_data)\\(.+\\))");

            //TODO do not iterate through all lines again but only the necessary ones
            while ((strLine = br.readLine()) != null) {

                Matcher sentenceMatcher = sentence.matcher(strLine);
                Matcher choiceMatcher = choice.matcher(strLine);
                //             Matcher cstructureMatcher = cstructure.matcher(strLine);

                if (sentenceMatcher.find()) {
                    inSentence = sentenceMatcher.group(1);
                }


                if (choiceMatcher.find())
                {
                    choiceSpace.add(choiceMatcher.group(1));
                }
            }
            //  System.out.println(counter);


            cp = new ChoiceSpace(choiceSpace);

            /* Print out f-structure facts for test purposes
            for (int i = 0; i < fsConstraints.size(); i++) {
                System.out.println(fsConstraints.get(i));
            }
*/
        } catch (IOException e) {
            e.printStackTrace();
        }

        //close infile
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fsConstraints = simplifyFs(fsConstraints);
        //  fsConstraints = contractFstructure(fsConstraints);
        // fsConstraints = removeEqualities(fsConstraints);

        ReadFsProlog Fstructure = new ReadFsProlog(sentenceID, inSentence, fsConstraints, cstrFacts, cp, vh);
        return Fstructure;
    }


    public static List<String> simplifyFs(List<String> fsConstraints)
    {
        HashMap<String,List<String[]>> varEqualities = new HashMap<>();
        HashMap<String,List<String[]>> valEqualities = new HashMap<>();

        Pattern eq = Pattern.compile("cf\\((.*),eq\\((var\\(\\d+\\)),(.*)\\)\\)");
        Pattern var = Pattern.compile("var\\(\\d+\\)");
         Pattern ambiguities = Pattern.compile("cf\\((.+?),\\w+\\(");

        ListIterator<String> iter = fsConstraints.listIterator();

        while (iter.hasNext()) {
            {
                String c = iter.next();
                Matcher eqM = eq.matcher(c);

                if (eqM.find()) {
                    String context = eqM.group(1);
                    String[] equal = {eqM.group(2), eqM.group(3)};
                    Matcher varM = var.matcher(eqM.group(3));
                    if (varM.matches()) {

                        if (!varEqualities.containsKey(context)) {
                            varEqualities.put(context, new ArrayList<>());
                        }
                        varEqualities.get(context).add(equal);
                    } else {
                        if (!valEqualities.containsKey(context)) {
                            valEqualities.put(context, new ArrayList<>());
                        }
                        valEqualities.get(context).add(equal);
                    }
                    iter.remove();
                }
            }
        }

        List<String> additionalConstraints = new ArrayList<>();

        for (String context : varEqualities.keySet())
        {
            for (int i =0; i < fsConstraints.size(); i++)
            {
                for (String[] equal : varEqualities.get(context))
                {
                    List<String> currentAdditionalConstraints = new ArrayList<>();

                    if (fsConstraints.get(i).contains(equal[0])) {
                        if (context.equals("1")) {
                            String replace = fsConstraints.get(i).replace(equal[0], equal[1]);
                            fsConstraints.set(i, replace);
                        } else {
                            Matcher ambMatcher = ambiguities.matcher(fsConstraints.get(i));
                            //replace only the first instance of ambMatcher group 1 with context and add to currentAdditionalConstraints
                            if (ambMatcher.find()) {
                                String replace = fsConstraints.get(i).replaceFirst(ambMatcher.group(1), context);
                                currentAdditionalConstraints.add(replace);
                            }
                        }
                    }
                    additionalConstraints.addAll(currentAdditionalConstraints);
                }



            }

        }

        for (String context : valEqualities.keySet()) {
            for (int i = 0; i < fsConstraints.size(); i++) {
                for (String[] equal : valEqualities.get(context)) {
                    if (fsConstraints.get(i).contains(equal[0])) {
                        String replace = fsConstraints.get(i).replace(equal[0], equal[1]);
                        fsConstraints.set(i, replace);
                    }
                }
            }
        }
        List<String> newfsConstraints = fsConstraints.stream().distinct().collect(Collectors.toList());
        newfsConstraints.addAll(additionalConstraints);
        return newfsConstraints;
    }

    // This function contracts two different constraints in the f-structure to one constraint.
    //TODO
    public static List<String> contractFstructure(List<String> fsConstraints)
    {
        List<String> contractedFstructure = new ArrayList<>();

        Set<String> fsSet = new LinkedHashSet<String>(fsConstraints);
        contractedFstructure.addAll(fsSet);

        Pattern floatingConstraints = Pattern.compile("attr\\(var\\((\\d+)\\),('.*')\\),(var\\(\\d+\\))");
        Pattern floats = Pattern.compile("eq\\((var\\(\\d+\\)),(.*)\\)\\)");
        Pattern amb = Pattern.compile("cf\\((or\\(.+?\\)|.+?),");


        ListIterator<String> it = contractedFstructure.listIterator();

        while(it.hasNext())
        {
            String constraint = it.next();
            Matcher fMatcher = floats.matcher(constraint);
            Matcher a1Matcher = floats.matcher(constraint);

            if (fMatcher.find() && a1Matcher.find())
            {
                ListIterator<String> it2 = contractedFstructure.listIterator();
                while(it2.hasNext()) {
                    String fConstraint = it2.next();
                    Matcher fcMatcher = floatingConstraints.matcher(fConstraint);
                    Matcher a2Matcher = amb.matcher(fConstraint);
                    if (fcMatcher.find() && a2Matcher.find()) {
                     if(a1Matcher.group(1).equals(a2Matcher.group(1))) {
                        if (Objects.equals(fcMatcher.group(3),fMatcher.group(1)))
                        {
                            it2.set("cf(" + a1Matcher.group(1) + ",eq(attr(var(" + fcMatcher.group(1) + ")," +
                                    fcMatcher.group(2) + ")," + fMatcher.group(2) + ")");

                        }
                    }
                    }

                }

            }

        }

        return contractedFstructure;
    }

    public static List<String> removeEqualities(List<String> fsConstraints)
    {

        List<String> equalizedConstraints = new ArrayList<String>(fsConstraints);

        Pattern var = Pattern.compile("(var\\(\\d+\\))");

        for (int i = 0; i < fsConstraints.size(); i++)
        {
            Matcher varMatcher = var.matcher(fsConstraints.get(i));

            if (varMatcher.find())
            {
                while (varMatcher.find())
                {
                    //for debugging reasons
                   // System.out.println(varMatcher.group(1));

                    String varString = varMatcher.group(1);
                    varString = varString.replaceAll("\\(","\\\\(");
                    varString = varString.replaceAll("\\)","\\\\)");


                    if (returnEqualValue(varMatcher.group(1),fsConstraints) != null)
                    {
                    String replace = returnEqualValue(varMatcher.group(1),fsConstraints);
                    String newEqualizedConstraint = equalizedConstraints.get(i).replaceAll(varString, replace);
                    equalizedConstraints.set(i,newEqualizedConstraint);
                    }

                }
            }
            else
            {
                equalizedConstraints.set(i, fsConstraints.get(i));
            }
        }


        return equalizedConstraints;
    }


    public static String returnEqualValue(String var, List<String> fsConstraints) {

        Pattern eq = Pattern.compile("eq\\((var\\(\\d+\\)),(.*)\\)\\)");
        for (int i = 0;i < fsConstraints.size(); i++)
        {
        Matcher eqMatcher = eq.matcher(fsConstraints.get(i));
        if (eqMatcher.find() && eqMatcher.group(1).equals(var))
        {
            return eqMatcher.group(2);
        }
        else continue;
        }

        return null;
    }


/*
    public static String writeConstraints(readFsProlog prolog)
    {
        List<String> constraints = prolog.prolog;

        StringBuilder builder = new StringBuilder();

        // adds the corresponding sentence as first line
        builder.append(prolog.sentence);
        builder.append(System.lineSeparator());
*/
/*
        //Sets sentence as working variable
        setCurrentSentence(prolog.sentence);
*//*

        for (int i = 0; i < constraints.size(); i++) {
            // adds f-structure constraints line by line;
            builder.append(constraints.get(i));
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
*/

/*
    public static HashMap<Integer,List<Pair<String,Object>>> fs2HashMap(List<String> fstructure)
    {


    }
*/

}
