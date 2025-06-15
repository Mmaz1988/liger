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

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.SyntaxOperator;
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
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

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
                List<LinguisticStructure> out = xleops.parseSingle(input);
                System.out.println(out.get(0).constraints);

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



    public void parseSentences(List<String> sentences, boolean unpack){


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
        try {

            LOGGER.info("Parsing following sentences: " + String.join("\n", sentences));

            String processString = xlebashcommand;

            if (this.os.equals(XLEStarter.OS.WINDOWS)) {
                processString = HelperMethods.formatWslString(processString);
            }

            ProcessBuilder proc = this.os.equals(XLEStarter.OS.WINDOWS)
                    ? new ProcessBuilder("wsl", processString)
                    : new ProcessBuilder(processString);

            Process process = proc.start();

            // Log the process output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.info(line);
                }

                while ((line = errorReader.readLine()) != null) {
                    LOGGER.severe(line);
                }
            }

            int exitCode = process.waitFor();
            LOGGER.info("Process exited with code: " + exitCode);

            if (exitCode == 0 && unpack){
                String unpackProcessString =
                        XLEStarter.unpackFsViaXLE(
                                Paths.get(PathVariables.workingDirectory,"tmp","parser_output","sentence1").toString(),
                                "100");

                if (this.os.equals(XLEStarter.OS.WINDOWS)) {
                    unpackProcessString = HelperMethods.formatWslString(unpackProcessString);
                }

                ProcessBuilder unpackProc = this.os.equals(XLEStarter.OS.WINDOWS)
                        ? new ProcessBuilder("wsl", unpackProcessString)
                        : new ProcessBuilder(unpackProcessString);

                Process unpackProcess = unpackProc.start();

                // Log the process output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(unpackProcess.getInputStream()));
                     BufferedReader errorReader = new BufferedReader(new InputStreamReader(unpackProcess.getErrorStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info(line);
                    }

                    while ((line = errorReader.readLine()) != null) {
                        LOGGER.severe(line);
                    }
                }

                int unpackExitCode = unpackProcess.waitFor();
                if (unpackExitCode == 0) {
                    LOGGER.info("Unpacking process exited with code: " + unpackExitCode);
                    //Delete original file
                    File fo = new File(Paths.get(PathVariables.workingDirectory,"tmp","parser_output","sentence1.pl").toString());
                    if (fo.exists()) {
                        fo.delete();
                    }
                } else {
                    LOGGER.warning("Unpacking process exited with code: " + unpackExitCode);
                }

            }
        } catch (Exception e) {
            LOGGER.warning("Failed to execute process: " + e.getMessage());
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

    public List<LinguisticStructure> parseSingle(String sentence)
    {
        return parseSingle(sentence,true);
    }

    @Override
    public List<LinguisticStructure> parseSingle(String sentence, boolean unpack) {
        List<String> singletonList = new ArrayList<>();
        singletonList.add(sentence);
        parseSentences(singletonList, unpack);

        List<LinguisticStructure> out = new ArrayList<>();
        //File fsFile = new File("/Users/red_queen/IdeaProjects/abstract-syntax-annotator-web/parser_output")
        File fsFile = new File(Paths.get(PathVariables.workingDirectory,"tmp","parser_output").toString());

        if (fsFile.isDirectory()) {
            List<File> files = new ArrayList<>(Arrays.asList(fsFile.listFiles((d, name) -> name.endsWith(".pl"))));

            if (files.size() > 1) {
                // Delete files with same string contents
                Map<String, File> seenFiles = new HashMap<>();
                Iterator<File> iterator = files.iterator();

                while (iterator.hasNext()) {
                    File file = iterator.next();
                    try {
                        String hash = HelperMethods.computeSHA256(file);
                        if (seenFiles.containsKey(hash)) {
                            file.delete();  // Delete duplicate
                            iterator.remove();  // Remove from list
                        } else {
                            seenFiles.put(hash, file);
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // Process remaining files
            for (File file : files) {
                LinkedHashMap<String, LinguisticStructure> fsRef = fs2Java(file.getPath());
                for (String key : fsRef.keySet()) {
                    out.add(fsRef.get(key));
                }
            }
            return out;
        }
        return null;
    }

    public String parse2Prolog(String sentence) {
        List<String> singletonList = new ArrayList<>();
        singletonList.add(sentence);
        parseSentences(singletonList, true);

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

    public LinkedHashMap<String,LinguisticStructure> fs2Java(String inputPath)
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

           // LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs2pl);
            List<GraphConstraint> fsList = FsProlog2Java.fs2List(fs2pl);

            Fstructure fs = new Fstructure(fs2pl.sentenceID,fs2pl.sentence,fsList,fs2pl.cp);

            fs.prologString = fs2pl.prologString;

            out.put(fs2pl.sentenceID,fs);

        }

        return out;
    }

    public LinkedHashMap<String, LinguisticStructure> fsString2Java(String prologString,String id)
    {
            LinkedHashMap<String,LinguisticStructure> out = new LinkedHashMap<>();

            ReadFsProlog fs2pl = ReadFsProlog.readPrologString(prologString,id,vh);

          //  LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs2pl);
            List<GraphConstraint> fsList = FsProlog2Java.fs2List(fs2pl);

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

           // LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash = FsProlog2Java.fs2Hash(fs2pl);
            List<GraphConstraint> fsList = FsProlog2Java.fs2List(fs2pl);

            return new Fstructure(fs2pl.sentenceID,fs2pl.sentence,fsList,fs2pl.cp);

        } else
        {throw new IOException("Input path must be a prolog structure as produced by the XLE");}

    }


}




