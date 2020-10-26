package syntax.xle.Prolog2Java;

import utilities.VariableHandler;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFsProlog implements Serializable {

    public String sentence;
    public List<String> prolog;
    public String sentenceID;
   public VariableHandler vh;


    public ReadFsProlog(String sentenceID, String sentence, List<String> fsConstraints, VariableHandler vh)
    {
        this.sentenceID = sentenceID;
        this.sentence = sentence;
        this.prolog = fsConstraints;
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
            System.out.println("Invalid prolog file name: Could not retrieve sentence ID");
        }

        if (sentenceID == null)
        {
            sentenceID = vh.returnNewVar(VariableHandler.variableType.SENTENCE_ID,null);
        }



        BufferedReader br = null;

        // This list will contain our f-structure constraints
        String inSentence = "";
        List<String> fsConstraints = new ArrayList<String>();

        try {
            // reads in File
            br = new BufferedReader(new FileReader(inFile));

            String strLine;
      //      int  counter = 0;
            // matches all constraints of the syntactic input
            Pattern constraints = Pattern.compile("(cf\\(.*\\))");
            // Mark up free sentence
            Pattern sentence = Pattern.compile( "'markup_free_sentence'\\((.*?)\\)");
            // matches c-structure constraints (this is very ugly but maybe enough)
       //     Pattern cstructure = Pattern.compile("((surfaceform|semform_data)\\(.+\\))");

            while ((strLine = br.readLine()) != null)
            {

                Matcher sentenceMatcher = sentence.matcher(strLine);
                Matcher constraintMatcher = constraints.matcher(strLine);
   //             Matcher cstructureMatcher = cstructure.matcher(strLine);

                if (sentenceMatcher.find()) {
                    inSentence = sentenceMatcher.group(1);
                }

                if (constraintMatcher.find())
                {
                    // Material that we want to translate into java objects is stored in arrayList
                    fsConstraints.add(constraintMatcher.group(1));
                //    counter++;

//                if (cstructureMatcher.find())
//                {
//                    fsConstraints.add(cstructureMatcher.group(1));
//                }

                } else
                {
                    continue;
                }
            }
          //  System.out.println(counter);


            // Print out f-structure facts for test purposes
            for (int i = 0; i < fsConstraints.size(); i++) {
                System.out.println(fsConstraints.get(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        fsConstraints = contractFstructure(fsConstraints);
        fsConstraints = removeEqualities(fsConstraints);

        ReadFsProlog Fstructure = new ReadFsProlog(sentenceID, inSentence, fsConstraints,vh);
        return Fstructure;
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
                    System.out.println(varMatcher.group(1));

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
