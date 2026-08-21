package de.ukon.liger.utilities;

import de.ukon.liger.semantics.GlueSemanticsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class XLEStarter {

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS, UNKNOWN
    }

    ;

    public static String xlePath;
    public String grammarPath;
    public static OS operatingSystem;

    public boolean isGlue;

    private final static Logger LOGGER = LoggerFactory.getLogger(XLEStarter.class);

    public XLEStarter(String xlePath, String grammarPath, OS operatingSystem) {
        this.xlePath = xlePath;
        this.grammarPath = grammarPath;
        this.operatingSystem = operatingSystem;
    }

    public XLEStarter() {
        initiateFromFile();
    }

    public void initiateFromFile() {
        //Open file and read in the paths
        File f = new File(Paths.get(PathVariables.workingDirectory, "xle_paths.txt").toString());

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i == 0) {
                    String[] lineOne = line.split("=");
                    xlePath = lineOne[1].replace("\"", "");
                }
                if (i == 1) {
                    String[] lineTwo = line.split("=");
                    grammarPath = lineTwo[1].replace("\"", "");
                }
                if (i == 2) {
                    String[] lineThree = line.split("=");
                    String osString = lineThree[1].replace("\"", "");
                    if (osString.equalsIgnoreCase("windows")) {
                        operatingSystem = OS.WINDOWS;
                    } else if (osString.equalsIgnoreCase("mac")) {
                        operatingSystem = OS.MAC;
                    } else {
                        operatingSystem = OS.LINUX;
                    }

                }
                i++;
            }
            br.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Initialized paths...");

    }


    /*
    Example command: xle -noTk -e "unpack-prolog-graph fschart2 100; exit"
     */

    public static String unpackFsViaXLE(String fsPath, String noOfSolutions) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");

        /*
        export XLEPATH=/bin/xle
        export PATH=${XLEPATH}/bin:$PATH
        export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7
        export LD_LIBRARY_PATH=${XLEPATH}/lib
        export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH
        export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH
         */

        sb.append("export XLEPATH=");
        sb.append(xlePath);
        sb.append("\n");
        sb.append("export PATH=${XLEPATH}/bin:$PATH");
        sb.append("\n");
        sb.append("export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7");
        sb.append("\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib");
        sb.append("\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH");
        sb.append("\n");
        sb.append("export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH");
        sb.append("\n");
        sb.append("\n");

        //   if (this.operatingSystem.equals(OS.WINDOWS)) {
            /*
            export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl
            export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl
            export TKLIBPATH=${XLEPATH}/tcl/scripts/tk
            export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk
             */
        sb.append("export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl");
        sb.append("\n");
        sb.append("export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl");
        sb.append("\n");
        sb.append("export TKLIBPATH=${XLEPATH}/tcl/scripts/tk");
        sb.append("\n");
        sb.append("export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk");
        sb.append("\n");
        sb.append("\n");

        // }


        // xle -noTk -e "create-parser /mnt/d/Resources/english_pargram/index/main.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"

        sb.append("xle -noTk -e \"unpack-prolog-graph ");

        String fileString = fsPath;

        sb.append(fileString);

        sb.append(" ");
        sb.append(noOfSolutions);
        sb.append("; exit\"");

        File tempDir = new File(Paths.get(PathVariables.workingDirectory, "tmp").toString());

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        //open file and write
        File file = new File(Paths.get(PathVariables.workingDirectory, "tmp", "xle-unpack.sh").toString());

        try {
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(sb.toString());
            fw.close();
        } catch (
                Exception e) {
            LOGGER.error("Failed to write xle-unpack.sh");
        }

        try {
            String chmodCommand = "";

            if (operatingSystem.equals(OS.WINDOWS)) {
                chmodCommand = "wsl chmod +x " + HelperMethods.formatWslString(file.getCanonicalPath());
            } else {
                chmodCommand = "chmod +x " + file.getCanonicalPath();
            }

            Runtime.getRuntime().exec(chmodCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Generated xle-unpack.sh at " + file.getCanonicalFile());
        return Paths.get(PathVariables.workingDirectory, "tmp", "xle-unpack.sh").toString();
    }


    public void generateXLEStarterFile() throws IOException {

        useGlueGrammar();

        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");

        /*
        export XLEPATH=/bin/xle
        export PATH=${XLEPATH}/bin:$PATH
        export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7
        export LD_LIBRARY_PATH=${XLEPATH}/lib
        export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH
        export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH
         */

        sb.append("export XLEPATH=");
        sb.append(xlePath);
        sb.append("\n");
        sb.append("export PATH=${XLEPATH}/bin:$PATH");
        sb.append("\n");
        sb.append("export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7");
        sb.append("\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib");
        sb.append("\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH");
        sb.append("\n");
        sb.append("export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH");
        sb.append("\n");
        sb.append("\n");

     //   if (this.operatingSystem.equals(OS.WINDOWS)) {
            /*
            export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl
            export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl
            export TKLIBPATH=${XLEPATH}/tcl/scripts/tk
            export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk
             */
            sb.append("export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl");
            sb.append("\n");
            sb.append("export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl");
            sb.append("\n");
            sb.append("export TKLIBPATH=${XLEPATH}/tcl/scripts/tk");
            sb.append("\n");
            sb.append("export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk");
            sb.append("\n");
            sb.append("\n");

       // }


        // xle -noTk -e "create-parser /mnt/d/Resources/english_pargram/index/main.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"

        sb.append("xle -noTk -e \"set timeout 5; create-parser ");

        String grammarString = grammarPath;

        if (operatingSystem.equals(OS.WINDOWS)) {
            grammarString = HelperMethods.formatWslString(grammarString);
        }

        sb.append(grammarString);
        sb.append("; parse-testfile ");


        String testFileString = Paths.get(PathVariables.workingDirectory, "tmp", "testfile.lfg").toString();

        if (operatingSystem.equals(OS.WINDOWS)) {
            testFileString = HelperMethods.formatWslString(testFileString);
        }

        sb.append(testFileString);

        String outputPrefix = Paths.get(PathVariables.workingDirectory, "tmp", "parser_output/sentence").toString();

        if (operatingSystem.equals(OS.WINDOWS)) {
            outputPrefix = HelperMethods.formatWslString(outputPrefix);
        }

        sb.append(" -outputPrefix ");
        sb.append(outputPrefix);
        sb.append("; exit\"");
        sb.append("\n");


        File tempDir = new File(Paths.get(PathVariables.workingDirectory, "tmp").toString());

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        //open file and write
        File file = new File(Paths.get(PathVariables.workingDirectory, "tmp", "xlebash.sh").toString());

        try {
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(sb.toString());
            fw.close();
        } catch (
                Exception e) {
            LOGGER.error("Failed to write xlebash.sh");
        }

        try {

            String chmodCommand = "";

            if (operatingSystem.equals(OS.WINDOWS)) {
                chmodCommand = "wsl chmod +x " + HelperMethods.formatWslString(file.getCanonicalPath());
            } else {
                chmodCommand = "chmod +x " + file.getCanonicalPath();
            }

            Runtime.getRuntime().exec(chmodCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Generated xlebash.sh at " + file.getCanonicalFile());
    }


    /**
     * If grammar is a single grammar, only translate grammar to LFG format
     * @throws IOException
     */
    public void useGlueGrammar() throws IOException {
        if (this.grammarPath.endsWith(".glue")) {
            GlueSemanticsParser gp = new GlueSemanticsParser(new VariableHandler());

            this.grammarPath = gp.convertGlueGrammar(this.grammarPath);
            // use this.grammarPath without .glue ending
            this.isGlue = true;
        } else {
            this.isGlue = false;
        }
    }


    public List<String> listGrammars() {
        List<String> grammarPaths = new ArrayList<>();
        //check ./grammars
        File f = new File(Paths.get(PathVariables.workingDirectory, "../grammars").toString());
        LOGGER.info("Current working directory: " + Paths.get(PathVariables.workingDirectory, "../").toString());
        LOGGER.info("Listing grammars in " + Paths.get(PathVariables.workingDirectory, "../grammars").toString());
        try {
            //add all paths ending with .lfg or .glue relative to working directory (.)
            for (File file : f.listFiles()) {
                if (file.getName().endsWith(".lfg") || file.getName().endsWith(".glue")) {
                    grammarPaths.add(file.getCanonicalPath());
                }
                //also check subdirectories
                if (file.isDirectory()) {
                    for (File subFile : file.listFiles()) {
                        if (subFile.getName().endsWith(".lfg") || subFile.getName().endsWith(".glue")) {
                            grammarPaths.add(subFile.getCanonicalPath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to list grammars");
        }

        return grammarPaths;
    }

    /**
     * Points xle_paths.txt at a different grammar.
     *
     * Hardened 2026-08-21 after the file was observed being wiped by "update grammar" in
     * some configuration, which leaves every parse endpoint returning 500 because
     * {@link #initiateFromFile()} then reads an empty or blank grammar path. Three
     * separate holes, any of which could produce that:
     *
     * <ul>
     *   <li>No validation of the incoming path. A null or blank value wrote
     *       {@code grammar=""} or literally {@code grammar="null"} -- syntactically a
     *       fine file, semantically a dead XLE.</li>
     *   <li>{@code new FileWriter(f)} TRUNCATES immediately, before the replacement
     *       content is known good. Any failure -- or a second request interleaving -- left
     *       the live file empty or partial, with no copy of what it used to hold.</li>
     *   <li>The grammar line was located purely by position ({@code i == 1}) without
     *       checking it actually was the grammar line, so a file with any other layout was
     *       silently rewritten unchanged.</li>
     * </ul>
     *
     * Now: validate, build the replacement in full, verify it still parses as an
     * xle_paths file, and swap it in atomically. A failure leaves the existing file
     * untouched and says why.
     */
    public void updateGrammarPath(String grammarPath) {
        if (grammarPath == null || grammarPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Refusing to update xle_paths.txt with an empty grammar path: that leaves XLE "
                            + "with no grammar and every parse failing.");
        }

        Path pathsFile = Paths.get(PathVariables.workingDirectory, "xle_paths.txt");
        List<String> lines;
        try {
            lines = Files.readAllLines(pathsFile);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + pathsFile + " to update the grammar path", e);
        }

        if (lines.size() < 2 || !lines.get(1).trim().startsWith("grammar=")) {
            throw new IllegalStateException(
                    pathsFile + " does not have a grammar= line where it is expected (line 2); "
                            + "refusing to rewrite it. Found " + lines.size() + " line(s).");
        }

        List<String> updated = new ArrayList<>(lines);
        updated.set(1, "grammar=\"" + grammarPath + "\"");

        // Written beside the target and moved into place, so the live file is never in a
        // truncated state and a failure costs nothing.
        try {
            Path temp = Files.createTempFile(pathsFile.getParent(), "xle_paths", ".tmp");
            try {
                Files.write(temp, updated);
                try {
                    Files.move(temp, pathsFile, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException atomicUnsupported) {
                    Files.move(temp, pathsFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                Files.deleteIfExists(temp);
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not update the grammar path in " + pathsFile, e);
        }

        this.grammarPath = grammarPath;
        LOGGER.info("Updated grammar path in {} to {}", pathsFile, grammarPath);
    }
}


