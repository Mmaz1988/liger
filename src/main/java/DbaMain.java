import analysis.LinguisticDictionary;
import analysis.RuleParser.RuleParser;
import main.Settings;
import semantics.GlueSemantics;
import syntax.SyntacticStructure;
import syntax.ud.SyntaxOperator;
import syntax.ud.UDoperator;
import syntax.GraphConstraint;
import syntax.xle.XLEoperator;
import utilities.DBASettings;
import utilities.PathVariables;
import utilities.VariableHandler;
import webservice.WebApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.*;


public class DbaMain {



  //  public BufferedWriter outputWriter;
    public static DBASettings settings;


    public static void main(String[] args) {

        initiateArguments(args);
    }

    public static void initiateArguments(String[] args) {
        settings = new DBASettings();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {

                case "-web":
                    settings.web = true;
                    break;

                case "-res":
                    settings.resources = args[i + 1];
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

            }
        }


        runDBA();
    }

    public static void runDBA() {

        //    LinguisticDictionary ld = new LinguisticDictionary();

        if (settings.resources != null) {
            PathVariables.workingDirectory = settings.resources;
        }
        PathVariables.initializePathVariables();


        if (settings.web)
        {
            WebApplication web = new WebApplication();
            web.main(new String[0]);
        }
        else {
            if (settings.inputFile != null) {
                try {

                    File inFile = new File(settings.inputFile);

                    if (inFile.exists()) {
                        List<String> lines = null;

                        lines = Files.readAllLines(inFile.toPath());

                    }
                } catch (Exception e1) {
                    System.out.println("Could not load rule file:" + settings.inputFile);
                }
            }

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
                } catch (Exception e) {
                    System.out.println("Failed to write output to:" + outPath);
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
                } else {
                    ruleFile = PathVariables.testPath + "testRulesLFG8.txt";
                }
            } else {
                ruleFile = settings.ruleFile;
            }


            if (settings.interactiveMode) {
                if (settings.mode == null)
                {
                    settings.mode = "dep";
                    if (settings.semanticParsing) {
                        ruleFile = PathVariables.testPath + "testRulesUD1.txt";
                    } else {
                        ruleFile = PathVariables.testPath + "testRulesUD4c.txt";
                    }
                }
                System.out.println("Starting interactive mode...\n");
                Scanner s = new Scanner(System.in);

                String input;
                while (true) {
                    System.out.println("Enter sentence to be analyzed or enter 'quit' to exit the program.");
                    input = s.nextLine();
                    break;
                    //if (input.equals("quit"))
                    //    break;
                }

                SyntacticStructure fs = parserInteractiveWrapper(settings.mode, input, ruleFile);

                if (settings.semanticParsing) {
                    semanticsInteractiveWrapper(fs);
                }
            } else {
                SyntacticStructure fs = fromFileWrapper(settings.inputFile, ruleFile);
                if (settings.semanticParsing) {
                    semanticsInteractiveWrapper(fs);
                }
            }

        }
    }

    public static void semanticsInteractiveWrapper(SyntacticStructure fs)
    {
        System.out.println(System.lineSeparator());

        System.out.println("Glue prover output:");
        GlueSemantics sem = new GlueSemantics();
        String result = sem.calculateSemantics(fs);

        System.out.println(System.lineSeparator());
        System.out.println("Result of the Glue derivation:");
        System.out.println(result);

        System.out.println("Done");
    }

    public static SyntacticStructure parserInteractiveWrapper(String parserType, String input, String path)
    {

        SyntacticStructure fs = null;
        SyntaxOperator syn = null;

        switch(parserType)
        {
            case "dep": {
                syn = new UDoperator();
                break;
            }
            case "lfg": {
                VariableHandler vh = new VariableHandler();
                syn = new XLEoperator(vh);
                break;
            }
        }

        assert syn != null;
        fs = syn.parseSingle(input);
        System.out.println(fs.constraints);

        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, new File(path));

        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            System.out.println("Sorting annotation failed.");
        }

        System.out.println("Annotation output:");

        for (GraphConstraint g : fs.annotation) {
            System.out.println(g);
        }

        return fs;

    }

    public static SyntacticStructure fromFileWrapper(String inPath, String rulePath)
    {
        SyntacticStructure fs = null;

        XLEoperator xle = new XLEoperator(new VariableHandler());

        LinkedHashMap<String,SyntacticStructure> indexedFs = xle.fs2Java(inPath);

        fs = indexedFs.get(indexedFs.keySet().iterator().next());

        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, new File(rulePath));

        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            System.out.println("Sorting annotation failed.");
        }

        System.out.println("Annotation output:");

        for (GraphConstraint g : fs.annotation) {
            System.out.println(g);
        }


        return fs;
    }

}





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


