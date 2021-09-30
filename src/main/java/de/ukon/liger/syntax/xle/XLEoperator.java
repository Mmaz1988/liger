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

package syntax.xle;

import main.DbaMain;
import packing.ChoiceVar;
import syntax.GraphConstraint;
import syntax.SyntacticStructure;
import syntax.SyntaxOperator;
import syntax.xle.FstructureElements.AttributeValuePair;
import syntax.xle.Prolog2Java.FsProlog2Java;
import syntax.xle.Prolog2Java.ReadFsProlog;
import utilities.VariableHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static syntax.ud.UDoperator.extractModals;


public class XLEoperator extends SyntaxOperator {

    public VariableHandler vh;

    public String xlebashcommand = "/Users/red_queen/IdeaProjects/xlebatchparsing/resources/xlebash.sh";
    private final static Logger LOGGER = Logger.getLogger(DbaMain.class.getName());


    public XLEoperator(VariableHandler vh)
    {
        this.vh = vh;
    }

    public void parseSentences(String testFile)
    {
        File f = new File(testFile);
        try
        {
            ProcessBuilder proc = new ProcessBuilder(xlebashcommand);

            proc.start().waitFor();

            f.delete();


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void parseSentences(List<String> sentences){


        File testdir = new File("parser_output");

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
            new File("parser_output").mkdirs();

       // new File("output").mkdirs();

        File f = new File("testfile.lfg");
        try (
                FileWriter fw = new FileWriter(f, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
        {
            for (String sentence : sentences) {
                out.println(sentence);
            }
        }
        catch(Exception e)
        {
            LOGGER.warning("Something went wrong.\n" + e.getMessage());
        }
        try
        {
            ProcessBuilder proc = new ProcessBuilder(xlebashcommand);

            proc.start().waitFor();

            f.delete();


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public SyntacticStructure parseSingle(String sentence) {
        List<String> singletonList = new ArrayList<>();
        singletonList.add(sentence);
        parseSentences(singletonList);

        File fsFile = new File("/Users/red_queen/IdeaProjects/syntax-annotator-glue/parser_output");

        if (fsFile.isDirectory()) {
            File[] files = fsFile.listFiles((d, name) -> name.endsWith(".pl"));

            for (int i = 0; i < files.length; i++) {
                LinkedHashMap<String, SyntacticStructure> fsRef = fs2Java(files[i].getPath());
                return fsRef.get(fsRef.keySet().iterator().next()) ;
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

        LinkedHashMap<String, SyntacticStructure> prologsJavaObjects = new LinkedHashMap<>();

        for (int i = 0; i < listOfPrologFiles.size(); i++) {

                String prologFilePath = listOfPrologFiles.get(i).toString();
                LinkedHashMap<String, SyntacticStructure> prologJavaObject = fs2Java(prologFilePath);

                for (String key : prologJavaObject.keySet())
                {
                    prologsJavaObjects.put(key,prologJavaObject.get(key));
                }
            }

        return prologsJavaObjects;
    }

    public List<List<String>> extractModalsFromXLE(LinkedHashMap<String, SyntacticStructure> sentences)
    {
        List<List<String>> output = new ArrayList<>();

        for (String key : sentences.keySet())
        {
            output.add(extractModals(sentences.get(key)));
        }
        return output;
    }




    public LinkedHashMap<String, SyntacticStructure> fs2Java(String inputPath)
    {
        //In
        File f = new File(inputPath);

        //Out
        LinkedHashMap<String,SyntacticStructure> out = new LinkedHashMap<>();

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

    //Load single xle structure as a syntactic structure:
    public SyntacticStructure xle2Java(String inputPath) throws IOException {
        //In
        File f = new File(inputPath);

        //Out
        LinkedHashMap<String,SyntacticStructure> out = new LinkedHashMap<>();

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




