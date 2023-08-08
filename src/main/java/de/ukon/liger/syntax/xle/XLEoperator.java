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

package de.ukon.liger.syntax.xle;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.SyntaxOperator;
import de.ukon.liger.syntax.xle.avp_elements.AttributeValuePair;
import de.ukon.liger.syntax.xle.prolog2java.FsProlog2Java;
import de.ukon.liger.syntax.xle.prolog2java.ReadFsProlog;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.utilities.XLEStarter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.ukon.liger.syntax.ud.UDoperator.extractModals;


public class XLEoperator extends SyntaxOperator {

    public VariableHandler vh;

    public XLEStarter.OS os;

    //for Mac
    public String xlebashcommand = Paths.get(PathVariables.workingDirectory,  "tmp" , "xlebash.sh").toString();

    //For Windows
    //public String xlebashcommand = "/mnt/c/Users/Celeste/IdeaProjects/LiGER/liger_resources/xlebash_win.sh";
    private final static Logger LOGGER = Logger.getLogger(XLEoperator.class.getName());


    public static void main(String[] args) throws IOException // throws VariableBindingException
    {

        PathVariables.initializePathVariables();

        String xlePath = "/bin/xle";
        String grammarPath = "/mnt/d/Resources/english_pargram/index/main.lfg";
        XLEStarter.OS os = XLEStarter.OS.WINDOWS;

        XLEStarter xleStarter = new XLEStarter(xlePath, grammarPath, os);

        xleStarter.generateXLEStarterFile();

        List<String> testSentences = new ArrayList<String>(Arrays.asList(args));

        XLEoperator xleops = new XLEoperator(new VariableHandler(),os);

        if (testSentences.isEmpty()) {
            Scanner s = new Scanner(System.in);
            String input;
            while (true) {
                System.out.println("Enter sentence to be analyzed or enter 'quit'.");
                input = s.nextLine();
                if (input.equals("quit"))
                    break;
                LinguisticStructure out = xleops.parseSingle(input);
                System.out.println(out.constraints);

            }
        }

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
                LOGGER.warning("Failed to delete tmp directory");
            }
        }


    }


    public XLEoperator(VariableHandler vh)
    {
        this.vh = vh;
    }

    public XLEoperator(VariableHandler vh, XLEStarter.OS os)
    {
        this.vh = vh;
        this.os = os;
    }

    public void parseSentences(String testFile)
    {
        File f = new File(testFile);
        try
        {
            // ProcessBuilder proc = new ProcessBuilder(xlebashcommand);

            //For Windows

//            proc.start().waitFor();

            String processString = xlebashcommand;

            if (this.os.equals(XLEStarter.OS.WINDOWS)) {
                processString = "wsl $(wslpath " + processString + ")";
            }

            ProcessBuilder proc = new ProcessBuilder(processString);

            proc.start().waitFor();


            f.delete();


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void parseSentences(List<String> sentences){


        File testdir = new File( Paths.get(PathVariables.workingDirectory,"tmp","parser_output").toString());

        if (testdir.exists() && testdir.isDirectory())
        {
            try {
                Files.walk(testdir.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }catch(Exception e)
            {
                LOGGER.warning("Failed to delete output directory");
            }
        }
        new File( Paths.get(PathVariables.workingDirectory,"tmp","parser_output").toString()).mkdirs();

        // new File("output").mkdirs();

        File f = new File(Paths.get(PathVariables.workingDirectory,"tmp","testfile.lfg").toString());

        if (f.exists())
        {
            f.delete();
        }

        try {
            f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (
                FileWriter fw = new FileWriter(f, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
        {
            for (String sentence : sentences) {
                out.println(sentence);
                out.println(System.lineSeparator());
            }
        }
        catch(Exception e)
        {
            LOGGER.warning("Something went wrong while setting temporary files for parsing.\n" + e.getMessage());
        }
        try
        {
            // For windows
            //ProcessBuilder proc = new ProcessBuilder("wsl",xlebashcommand);
            //For mac
            String processString = xlebashcommand;

            if (this.os.equals(XLEStarter.OS.WINDOWS)) {

                //Translate windows path to unix path
                // trans late [A-Z]: to /mnt/[a-z]

                processString = HelperMethods.formatWslString(processString);

             //   processString = "wsl" + processString;
            }

            ProcessBuilder proc = null;

            if (this.os.equals(XLEStarter.OS.WINDOWS)) {
            proc = new ProcessBuilder("wsl", processString);
            } else
            {
                proc = new ProcessBuilder(processString);
            }

            proc.start().waitFor();

          //  f.delete();




        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //TODO parse multiple
    /*
    public HashMap<Integer,LinguisticStructure> parseMultiple(LinkedHashMap<Integer,String> sentences)
    {
        List<String> sentenceList = new ArrayList<>();
        sentenceList.addAll(sentences.values());

        parseSentences(sentenceList);



        File fsFile = new File(Paths.get(PathVariables.workingDirectory,"tmp","parser_output").toString());

        if (fsFile.isDirectory()) {
            File[] files = fsFile.listFiles((d, name) -> name.endsWith(".pl"));

            for (int i = 0; i < files.length; i++) {
                LinkedHashMap<String, LinguisticStructure> fsRef = fs2Java(files[i].getPath());
                //close fsFile

            }

        }
            return null;
    }

     */

    @Override
    public LinguisticStructure parseSingle(String sentence) {
        List<String> singletonList = new ArrayList<>();
        singletonList.add(sentence);
        parseSentences(singletonList);

        //File fsFile = new File("/Users/red_queen/IdeaProjects/abstract-syntax-annotator-web/parser_output");

        File fsFile = new File(Paths.get(PathVariables.workingDirectory,"tmp","parser_output").toString());

        if (fsFile.isDirectory()) {
            File[] files = fsFile.listFiles((d, name) -> name.endsWith(".pl"));

            for (int i = 0; i < files.length; i++) {
                LinkedHashMap<String, LinguisticStructure> fsRef = fs2Java(files[i].getPath());
                //close fsFile


                return fsRef.get(fsRef.keySet().iterator().next()) ;
            }
        }
        return null;
    }

    public String parse2Prolog(String sentence) {
        List<String> singletonList = new ArrayList<>();
        singletonList.add(sentence);
        parseSentences(singletonList);

        //File fsFile = new File("/Users/red_queen/IdeaProjects/abstract-syntax-annotator-web/parser_output");

        File fsFile = new File(Paths.get(PathVariables.workingDirectory,"tmp","parser_output").toString());

        if (fsFile.isDirectory()) {
            File[] files = fsFile.listFiles((d, name) -> name.endsWith(".pl"));

            for (int i = 0; i < files.length; i++) {
                //read in files.get(i)

                try {
                    return Files.readString(Paths.get(files[i].getPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return null;
    }

    public LinkedHashMap loadPrologFstructures()
    {
        VariableHandler vh = new VariableHandler();
        File folder = new File("/Users/red_queen/IdeaProjects/xlebatchparsing/src/test/prolog/");
        List<Path> listOfPrologFiles = new ArrayList<>();
        try {
            listOfPrologFiles = Files.list(Paths.get(folder.getPath())).filter(Files::isRegularFile).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(listOfPrologFiles, new Comparator<Path>() {
            public int compare(Path f1, Path f2) {
                Pattern treeBankFile = Pattern.compile(".*S(\\d+)");
                Matcher f1Matcher = treeBankFile.matcher(f1.toString());
                Matcher f2Matcher = treeBankFile.matcher(f2.toString());

                try {
                    if (f1Matcher.find() && f2Matcher.find()) {
                        int i1 = Integer.parseInt(f1Matcher.group(1));
                        int i2 = Integer.parseInt(f2Matcher.group(1));

                        return i1 - i2;
                    }
                } catch (IllegalStateException e) {
                    throw new AssertionError(e);
                }
                return 0;
            }

        });

        LinkedHashMap<String, LinguisticStructure> prologsJavaObjects = new LinkedHashMap<>();

        for (int i = 0; i < listOfPrologFiles.size(); i++) {

            String prologFilePath = listOfPrologFiles.get(i).toString();
            LinkedHashMap<String, LinguisticStructure> prologJavaObject = fs2Java(prologFilePath);

            for (String key : prologJavaObject.keySet())
            {
                prologsJavaObjects.put(key,prologJavaObject.get(key));
            }
        }

        return prologsJavaObjects;
    }

    public List<List<String>> extractModalsFromXLE(LinkedHashMap<String, LinguisticStructure> sentences)
    {
        List<List<String>> output = new ArrayList<>();

        for (String key : sentences.keySet())
        {
            output.add(extractModals(sentences.get(key)));
        }
        return output;
    }




    public LinkedHashMap<String, LinguisticStructure> fs2Java(String inputPath)
    {
        //In
        File f = new File(inputPath);

        //Out
        LinkedHashMap<String, LinguisticStructure> out = new LinkedHashMap<>();

// This filter will only include files ending with .py
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".pl");
            }
        };

// This is how to apply the filter
        List<String> pathnames = null;
        if (f.isDirectory()) {
            pathnames = Arrays.asList(Objects.requireNonNull(f.list(filter)));
        } else
        {
            String[] path = {f.getPath()};
            pathnames = Arrays.asList(Objects.requireNonNull(path));
        }

        for (String pn : pathnames)
        {
            File sentence = new File(pn);

            ReadFsProlog fs2pl = ReadFsProlog.readPrologFile(sentence,vh);

            LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs2pl);
            List<GraphConstraint> fsList = FsProlog2Java.fsHash2List(fsHash);

            Fstructure fs = new Fstructure(fs2pl.sentenceID,fs2pl.sentence,fsList,fs2pl.cp);

            out.put(fs2pl.sentenceID,fs);

        }

        return out;
    }


    public LinkedHashMap<String, LinguisticStructure> fsString2Java(String prologString,String id)
    {
            LinkedHashMap<String,LinguisticStructure> out = new LinkedHashMap<>();

            ReadFsProlog fs2pl = ReadFsProlog.readPrologString(prologString,id,vh);

            LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs2pl);
            List<GraphConstraint> fsList = FsProlog2Java.fsHash2List(fsHash);

            Fstructure fs = new Fstructure(fs2pl.sentenceID,fs2pl.sentence,fsList,fs2pl.cp);

            out.put(fs2pl.sentenceID,fs);

        return out;
    }



    //Load single xle structure as a syntactic structure:
    public LinguisticStructure xle2Java(String inputPath) throws IOException {
        //In
        File f = new File(inputPath);

        //Out
        LinkedHashMap<String, LinguisticStructure> out = new LinkedHashMap<>();

// This filter will only include files ending with .py
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".pl");
            }
        };

// This is how to apply the filter
        List<String> pathnames = null;
        if (!f.isDirectory() && f.toString().endsWith(".pl")) {

            File sentence = new File(f.getPath());

            ReadFsProlog fs2pl = ReadFsProlog.readPrologFile(sentence,vh);

            LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs2pl);
            List<GraphConstraint> fsList = FsProlog2Java.fsHash2List(fsHash);

            return new Fstructure(fs2pl.sentenceID,fs2pl.sentence,fsList,fs2pl.cp);

        } else
        {throw new IOException("Input path must be a prolog structure as produced by the XLE");}

    }


}




