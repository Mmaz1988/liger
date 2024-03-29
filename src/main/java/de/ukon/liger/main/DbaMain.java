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

package de.ukon.liger.main;
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


import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.SyntaxOperator;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.syntax.xle.Fstructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.DBASettings;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.utilities.XLEStarter;
import de.ukon.liger.webservice.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DbaMain {


    //  public BufferedWriter outputWriter;
    public static DBASettings settings;
    private final static Logger LOGGER = LoggerFactory.getLogger(DbaMain.class);


    public static void main(String[] args) throws IOException {

        /*
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new MyFormatter());
        handler.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
         */


        LOGGER.info("Starting rewrite system -- copyright 2021 Mark-Matthias Zymla");

        initiateArguments(args);
    }

    public static void initiateArguments(String[] args) throws IOException {
        settings = new DBASettings();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            LOGGER.info("GOT ARG: " + arg);

            switch (arg) {

                case "-web":
                    settings.web = true;
                    break;

                case "-res":
                    settings.resources = args[i + 1];
                    LOGGER.info("Setting resource directory to " + settings.resources);
                    break;
                case "-i":
                    settings.inputFile = args[i + 1];
                    settings.interactiveMode = false;
                    settings.mode = "lfg";
                    i++;
                    break;
                case "-o":
                    settings.outputFile = args[i + 1];
                    i++;
                    break;
                case "-rf":
                    settings.ruleFile = args[i + 1];
                    i++;
                    break;
                case "-dep":
                    settings.mode = "dep";
                    break;
                case "-lfg":
                    settings.mode = "lfg";
                    break;
                case "-sem":
                    settings.semanticParsing = true;
                    break;
                case "-mc":
                    settings.mcs = true;
                    break;
                case "-xle":
                    settings.xleBinary = args[i + 1];
                    i++;
                    break;
                case "-grammar":
                    settings.xleGrammar = args[i + 1];
                    i++;
                    break;

            }
        }


        runDBA();
     //   System.exit(0);
    }

    public static void runDBA() throws IOException {

        //    LinguisticDictionary ld = new LinguisticDictionary();

        if (settings.resources != null) {
            PathVariables.workingDirectory = settings.resources;
        } else
        {
            PathVariables.initializePathVariables();
        }


        LOGGER.info("Successfully initialized working directory to " + PathVariables.workingDirectory);
        PathVariables.initializePathVariables();

        LOGGER.info("dir " + PathVariables.workingDirectory);
        LOGGER.info("test " + PathVariables.testPath);
        LOGGER.info("dict " + PathVariables.dictPath);


        if (settings.web) {
            LOGGER.info("Running system as web service ... ");
            WebApplication web = new WebApplication();
            web.main(new String[0]);
        } else {
            if (settings.outputFile != null) {
                String outPath = settings.outputFile;
                try {
                    File outFile = new File(outPath);

                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    outFile.createNewFile();
                    //  outFile.createNewFile();
                    settings.setOutputWriter(outFile);
                    LOGGER.info("Created output file: " + outFile.toString());
                } catch (Exception e) {
                    LOGGER.warn("Failed to write output to:" + outPath);
                }
            }

            String ruleFile = "";

            if (settings.ruleFile == null) {
                if (settings.mode == "dep") {
                    if (settings.semanticParsing) {
                        ruleFile = PathVariables.testPath + "testRulesUD1.txt";
                    } else {
                        ruleFile = PathVariables.testPath + "testRulesUD4c.txt";
                    }
                }

                if (settings.mode == "lfg")
                {
                 ruleFile = PathVariables.testPath + "testRulesLFG9.txt";
                }
            } else {
                ruleFile = settings.ruleFile;
            }

            if (settings.mode == null) {
                settings.mode = "dep";
                if (settings.semanticParsing) {
                    ruleFile = PathVariables.testPath + "testRulesUD1.txt";
                } else {
                    ruleFile = PathVariables.testPath + "testRulesUD4c.txt";
                }
            }

            LOGGER.info("Set rule file: " + ruleFile);

            LinkedHashMap<String, HashMap<Integer, String>> result = new LinkedHashMap<>();

            if (settings.interactiveMode) {

                LOGGER.info("Starting interactive mode...\n");
                Scanner s = new Scanner(System.in);

                String input;
                while (true) {
                    LOGGER.info("Enter sentence to be analyzed or enter 'quit' to exit the program.");
                    input = s.nextLine();
                    break;
                    //if (input.equals("quit"))
                    //    break;
                }

                LinkedHashMap<String, LinguisticStructure> fs = parserInteractiveWrapper(settings.mode, input, ruleFile, result);

                if (settings.semanticParsing) {
                    semanticsInteractiveWrapper(fs, result);
                }

                if (settings.mcs)
                {
                    semanticsMeaningConstructorWrapper(fs,result);
                }

            } else {
                LinkedHashMap<String, LinguisticStructure> fs = fromFileWrapper(result);
                if (settings.semanticParsing) {
                    semanticsInteractiveWrapper(fs, result);
                }

                if (settings.mcs)
                {
                    semanticsMeaningConstructorWrapper(fs,result);
                }
            }

            if (settings.outputFile != null) {
                if (!settings.mcs) {
                    for (String key : result.keySet()) {
                        settings.outputWriter.append(result.get(key).get(0));
                        if (settings.semanticParsing) {
                            settings.outputWriter.append(result.get(key).get(1));
                        }
                        settings.outputWriter.close();
                    }
                } else
                {
                    for (String key : result.keySet())
                    {
                        settings.outputWriter.append(result.get(key).get(1));
                    }
                    settings.outputWriter.close();
                }
                }

        //    System.out.println();
        //    System.out.println("LiGER annotation complete.");

            LOGGER.info("LiGER annotation complete\n");
           LOGGER.info("Deleting temporary files ... ");

            //Delete tmp folder and contents
            File tmpdir = new File( Paths.get(PathVariables.workingDirectory,"tmp").toString());

            if (tmpdir.exists() && tmpdir.isDirectory())
            {
                try {
                    Files.walk(tmpdir.toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }catch(Exception e)
                {
                    LOGGER.warn("Failed to delete tmp directory");
                }
            }

            System.exit(0);

        }
    }

    public static void
    semanticsInteractiveWrapper(LinkedHashMap<String, LinguisticStructure> in, LinkedHashMap<String, HashMap<Integer, String>> result) {


        for (String key : in.keySet()) {
            LinguisticStructure fs = in.get(key);
            GlueSemantics sem = new GlueSemantics();
            String semantics = sem.calculateSemantics(fs);

            StringBuilder resultBuilder = new StringBuilder();

            resultBuilder.append("Result of the Glue derivation:");
            resultBuilder.append(semantics);

            result.get(key).put(1, semantics);

       //     System.out.println("Meaning constructors:");
       //     System.out.println(sem.returnMeaningConstructors(fs));

        }

        for (String key : result.keySet()) {
            LOGGER.info("The rewrite system produced the following output:\n" + result.get(key).get(0));
            LOGGER.info("The GSWB produced the following output:\n" + result.get(key).get(1));
        }

        StringBuilder report = new StringBuilder();

        report.append(System.lineSeparator());
        report.append("ID:      Added facts:     Glue solutions:\n");

        for (String key : result.keySet()) {
            report.append(String.format("%s\t\t\t%s\t\t\t%s", key, in.get(key).annotation.size(), result.get(key).get(1)));
        }

        LOGGER.info(report.toString());

        LOGGER.info("Done");
    }

    public static void
    semanticsMeaningConstructorWrapper(LinkedHashMap<String, LinguisticStructure> in, LinkedHashMap<String, HashMap<Integer, String>> result) {


        for (String key : in.keySet()) {
            StringBuilder resultBuilder = new StringBuilder();
            LinguisticStructure fs = in.get(key);
            GlueSemantics sem = new GlueSemantics();
            String semantics = sem.returnMeaningConstructors(fs);


            resultBuilder.append("LiGER generated the following meaning constructors:");
            resultBuilder.append(semantics);
            result.get(key).put(1, semantics);
        }

//            System.out.println("Meaning constructors:");
//            System.out.println(sem.returnMeaningConstructors(fs));



        /*
        for (String key : result.keySet()) {
            LOGGER.info("The rewrite system produced the following output:\n" + result.get(key).get(0));
            LOGGER.info("The GSWB produced the following output:\n" + result.get(key).get(1));
        }
         */


        LOGGER.info("Done");
    }

    public static LinkedHashMap<String, LinguisticStructure>
    parserInteractiveWrapper(String parserType, String input, String path, LinkedHashMap<String, HashMap<Integer, String>> result) {
        VariableHandler vh = new VariableHandler();
        LinguisticStructure fs = null;
        SyntaxOperator syn = null;

        switch (parserType) {
            case "dep": {
                syn = new UDoperator();
                LOGGER.info("Created new dependency parser instance...");
                break;
            }
            case "lfg": {
                XLEStarter xle = new XLEStarter(settings.xleBinary, settings.xleGrammar, settings.os);
                xle.generateXLEStarterFile();
                syn = new XLEoperator(vh,settings.os);
                LOGGER.info("Created new XLE parser instance ...");
                break;
            }
        }

        assert syn != null;
        fs = syn.parseSingle(input);
        //     System.out.println(fs.constraints);

        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(new File(path));
        StringBuilder resultBuilder = new StringBuilder();
        HashMap<Integer, String> syntaxResult = new HashMap<>();

        rp.addAnnotation2(fs);

        String sid = vh.returnNewVar(VariableHandler.variableType.SENTENCE_ID, null);

        LinkedHashMap<String, LinguisticStructure> out = new LinkedHashMap<>();
        out.put(sid, fs);

        resultBuilder.append(sid + ": " + fs.text);
        resultBuilder.append(System.lineSeparator());

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            LOGGER.warn("Sorting annotation failed.");
        }

        resultBuilder.append("Annotation output:\n");

        for (GraphConstraint g : fs.annotation) {
            resultBuilder.append(g.toString());
            resultBuilder.append(System.lineSeparator());
        }

        resultBuilder.append("End of: " + sid);

        syntaxResult.put(0, resultBuilder.toString());
        result.put(sid, syntaxResult);

        if (!settings.semanticParsing) {
            LOGGER.info(resultBuilder.toString());
        }
        return out;

    }

    /**
     * This function serves to load .pl files produced by the XLE.
     * @param result
     * @return
     */

    public static LinkedHashMap<String, LinguisticStructure>
    fromFileWrapper(LinkedHashMap<String, HashMap<Integer, String>> result) throws IOException {

        File inFile = new File(settings.inputFile);

        XLEoperator xle = new XLEoperator(new VariableHandler());

        LinkedHashMap<String, LinguisticStructure> indexedFs;


        if (inFile.isDirectory()) {
            File[] files = inFile.listFiles((d, name) -> name.endsWith(".pl"));

            indexedFs = new LinkedHashMap<String, LinguisticStructure>();

            for (int i = 0; i < files.length; i++) {
                indexedFs.putAll(xle.fs2Java(files[i].toString()));
            }
        } else {
            indexedFs = xle.fs2Java(settings.inputFile);
        }
/*
        List<SyntacticStructure> fsList = new ArrayList<>();

        fs = indexedFs.get(indexedFs.keySet().iterator().next());
*/
        //   List<SyntacticStructure> fsList = new ArrayList<>();
        //   fsList.add(fs);

        RuleParser rp = new RuleParser(new File(settings.ruleFile));


        for (String key : indexedFs.keySet()) {
            StringBuilder resultBuilder = new StringBuilder();
            HashMap<Integer, String> syntaxResult = new HashMap<>();
            LinguisticStructure fs = indexedFs.get(key);
            LOGGER.info("Now annotating structure with id " + key + "...");
            rp.addAnnotation2(fs);

            resultBuilder.append(key + ": " + fs.text);
            resultBuilder.append(System.lineSeparator());

            try {
                fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
            } catch (Exception e) {
                LOGGER.warn("Sorting annotation failed.");
            }

            /*
            for (GraphConstraint g : fs.annotation) {
                resultBuilder.append(g.toString());
                resultBuilder.append(System.lineSeparator());
            }
             */

            resultBuilder.append(System.lineSeparator());
            resultBuilder.append(((Fstructure) fs).writeToProlog(true));
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("End of: " + key + "\n");
            resultBuilder.append(System.lineSeparator());

            syntaxResult.put(0, resultBuilder.toString());
            result.put(key, syntaxResult);

        }
        if (!settings.semanticParsing) {
            for (String key : result.keySet()) {
                LOGGER.info("The rewrite system produced the following output:\n" + result.get(key).get(0));
            }
        }

        return indexedFs;
    }

}









    /*
       System.out.println("Starting interactive lfg mode...\n");
        Scanner s = new Scanner(System.in);
        String input;
        while (true) {
            System.out.println("Enter sentence to be analyzed or enter 'quit' to exit the program.");
            input = s.nextLine();
            break;
            //if (input.equals("quit"))
            //    break;
        }

        List<String> sentences = new ArrayList<>();
        sentences.add(input);

        XLEoperator xle = new XLEoperator();
        GlueSemantics sem = new GlueSemantics();

        xle.parseSentences(sentences);

        File fsFile = new File("C:\\Users\\Mark\\IdeaProjects\\xle_operator\\parser_output");

        if (fsFile.isDirectory()) {
            File[] files = fsFile.listFiles((d, name) -> name.endsWith(".pl"));


            for(int i = 0; i < files.length; i++) {

                LinkedHashMap<String, SyntacticStructure> fsRef = xle.fs2Java(files[i].getPath());

               SyntacticStructure fs = fsRef.get(fsRef.keySet().stream().findAny().get());

                List<SyntacticStructure> fsList = new ArrayList<>();
                fsList.add(fs);

                RuleParser rp = new RuleParser(fsList,"C:\\Users\\Mark\\IdeaProjects\\xle_operator\\src\\test\\testRules5.txt");

                rp.addAnnotation2(fs);

                sem.calculateSemantics(fs);

                for (Premise p : sem.llprover.getSolutions())
                {
                    System.out.println(p.toString());
                }
            }
        }

     */


