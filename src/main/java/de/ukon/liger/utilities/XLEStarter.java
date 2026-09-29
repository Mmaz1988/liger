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

        LOGGER.debug("Initialized paths...");

    }


    /** The grammar the last {@link #generateXLEStarterFile()} resolved (a {@code .glue} grammar's
     *  converted {@code .lfg}), which every parse script uses. The loaded grammar is LiGER-wide
     *  state already (xle_paths.txt), so one value serves every request. */
    private static volatile String activeGrammarPath;

    /** The environment every XLE script sets up. */
    private static String scriptHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");
        sb.append("export XLEPATH=").append(xlePath).append("\n");
        sb.append("export PATH=${XLEPATH}/bin:$PATH\n");
        sb.append("export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH\n");
        sb.append("export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH\n");
        sb.append("\n");
        sb.append("export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl\n");
        sb.append("export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl\n");
        sb.append("export TKLIBPATH=${XLEPATH}/tcl/scripts/tk\n");
        sb.append("export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk\n");
        sb.append("\n");
        return sb.toString();
    }

    private static String scriptPath(Path path) {
        String p = path.toString();
        return operatingSystem == OS.WINDOWS ? HelperMethods.formatWslString(p) : p;
    }

    /** Writes an executable script. Made executable in-process: the former
     *  {@code Runtime.exec("chmod +x ...")} ran asynchronously, so a script could be started
     *  before it was executable. */
    private static Path writeScript(Path file, String content) throws IOException {
        Files.writeString(file, content);
        if (operatingSystem == OS.WINDOWS) {
            try {
                new ProcessBuilder("wsl", "chmod", "+x", HelperMethods.formatWslString(file.toFile().getCanonicalPath()))
                        .start().waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while making " + file + " executable", e);
            }
        } else if (!file.toFile().setExecutable(true)) {
            throw new IOException("Could not make " + file + " executable");
        }
        return file;
    }

    /*
    Example command: xle -noTk -e "unpack-prolog-graph fschart2 100; exit"
     */

    /** The script that unpacks {@code fsPath}'s packed f-structures, written into {@code workDir}:
     *  one per parse, so concurrent parses do not overwrite each other's script. */
    public static Path writeUnpackScript(Path workDir, String fsPath, String noOfSolutions) throws IOException {
        String command = "xle -noTk -e \"unpack-prolog-graph " + fsPath + " " + noOfSolutions + "; exit\"";
        Path file = writeScript(workDir.resolve("xle-unpack.sh"), scriptHeader() + command);
        LOGGER.debug("Generated xle-unpack.sh at " + file);
        return file;
    }

    /**
     * Resolves the grammar to parse with (converting a {@code .glue} grammar) and makes it the one
     * {@link #writeParseScript} uses. The script itself is no longer written here: a single
     * tmp/xlebash.sh, tmp/testfile.lfg and tmp/parser_output were shared by every request, so
     * concurrent parses read each other's output.
     */
    public void generateXLEStarterFile() throws IOException {
        useGlueGrammar();
        activeGrammarPath = grammarPath;
    }

    // xle -noTk -e "create-parser /mnt/d/Resources/english_pargram/index/main.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"

    /** The script that parses {@code workDir}/testfile.lfg into {@code workDir}/parser_output,
     *  with the grammar {@link #generateXLEStarterFile()} resolved. */
    public static Path writeParseScript(Path workDir) throws IOException {
        String grammar = activeGrammarPath;
        if (grammar == null) {
            throw new IllegalStateException("No grammar resolved: call generateXLEStarterFile() first");
        }
        if (operatingSystem == OS.WINDOWS) {
            grammar = HelperMethods.formatWslString(grammar);
        }
        String command = "xle -noTk -e \"set timeout 5; create-parser " + grammar
                + "; parse-testfile " + scriptPath(workDir.resolve("testfile.lfg"))
                + " -outputPrefix " + scriptPath(workDir.resolve("parser_output").resolve("sentence"))
                + "; exit\"\n";
        Path file = writeScript(workDir.resolve("xlebash.sh"), scriptHeader() + command);
        LOGGER.debug("Generated xlebash.sh at " + file);
        return file;
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


