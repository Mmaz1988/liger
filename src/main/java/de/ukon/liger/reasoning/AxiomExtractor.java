package de.ukon.liger.reasoning;


import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.webservice.rest.dtos.GrammarAxioms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Extracts axioms from Linguistic structures.
 * Determines whether axioms are stored as Prolog or tftf and producees string representations for each formula.
 */
public class AxiomExtractor {


    private static final Logger LOGGER = LoggerFactory.getLogger(AxiomExtractor.class);

    //Constructor
    public AxiomExtractor() {
        // Initialization if needed
    }

    public String extractAxiomsFromString(String inputString, String logicType) {
        StringBuilder axiomBuilder = new StringBuilder();

        // 1. Filter out comment lines and collect the rest
        List<String> nonCommentLines = Arrays.stream(inputString.split("\\R")) // \R = any line break
                .map(String::trim)
                .filter(line -> !line.startsWith("//") && !line.isEmpty())
                .collect(Collectors.toList());

        // 2. Combine the lines into a single string
        String cleanedInput = String.join("\n", nonCommentLines);

        // 3. Split the combined string on dots (.) that end a clause
        String[] clauses = cleanedInput.split("\\.\\s*");

        // 4. Append trimmed, non-empty clauses back with proper period
        for (String clause : clauses) {
            clause = clause.trim();
            if (!clause.isEmpty()) {
                axiomBuilder.append(clause).append(".\n");
            }
        }

        return translatePrologAxioms(axiomBuilder.toString(),logicType);
    }


    /** Rule label of an axiom written as a Prolog FOL/DRS term. */
    public static final String PROLOG_AXIOM_LABEL = "AXIOM";
    /** Rule label of an axiom written as a TPTP formula (escape its conjunctions as \\&). */
    public static final String TPTP_AXIOM_LABEL = "TPTP_AXIOM";
    /** Rule label of a TFF type declaration, written as its body: {@code big: ($i * $int) > $o}.
     *  Only symbols over a sort other than $i need one -- an undeclared symbol takes $i
     *  arguments and returns $o (predicate) or $i (function). */
    public static final String TFF_TYPE_LABEL = "TFF_TYPE";

    /**
     * The axiom annotations of a structure, untranslated: AXIOM values as Prolog terms,
     * TPTP_AXIOM values as TPTP formulas, TFF_TYPE values as type declaration bodies. All are encoded for a logic (fof/tff) only in the
     * Vampire service, with the logic type of the run that uses them -- an axiom encoded at
     * parse time stays fixed to whatever logic was selected then.
     */
    public GrammarAxioms extractGrammarAxioms(LinguisticStructure linguisticStructure) {
        GrammarAxioms axioms = new GrammarAxioms();
        for (GraphConstraint constraint : linguisticStructure.returnFullGraph()) {
            String label = constraint.getRelationLabel();
            List<String> target = PROLOG_AXIOM_LABEL.equals(label) ? axioms.prolog
                    : TPTP_AXIOM_LABEL.equals(label) ? axioms.tptp
                    : TFF_TYPE_LABEL.equals(label) ? axioms.tffTypes : null;
            if (target == null) continue;
            String value = String.valueOf(constraint.getFsValue()).trim();
            if (!value.isEmpty() && !target.contains(value)) {
                target.add(value);
            }
        }
        return axioms;
    }

    public String translatePrologAxioms(String prologAxioms,String logicType) {
        LOGGER.debug("Translating Prolog axioms to tftf representation");
        // A directory of its own per call. One fixed axiom_extractor_tmp, emptied and refilled
        // by every call, let concurrent requests read each other's axioms -- the same race the
        // XLE working directory had (xleplusglue docs/bug_reports/liger_xle_shared_tmp_dir_race.md).
        File tmpDir;
        try {
            java.nio.file.Path base = java.nio.file.Paths.get("./liger_resources/tmp");
            java.nio.file.Files.createDirectories(base);
            tmpDir = java.nio.file.Files.createTempDirectory(base, "axioms-").toFile();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Could not create a directory for the axiom translation", e);
        }

        File axiomFile = new File(tmpDir, "axiomFile.txt");
        File axiomOutputFile = new File(tmpDir, "axiomOutputFile.txt");


        //Create new file
        try {
            if (axiomFile.createNewFile()) {
                LOGGER.debug(String.format("File %s created successfully!", axiomFile.getAbsolutePath()));
            } else {
                LOGGER.error("File already exists!");
            }
        } catch (
                IOException e) {
            LOGGER.error("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }

        // Write axioms to file
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(axiomFile));
            writer.write(prologAxioms);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create output file
        try {
            if (axiomOutputFile.createNewFile()) {
                LOGGER.debug(String.format("File %s created successfully!", axiomOutputFile.getAbsolutePath()));
            } else {
                LOGGER.error("File already exists!");
            }
        } catch (
                IOException e) {
            LOGGER.error("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }

        try {

            // Prepare command
            String[] command = {
                    "swipl",
                    "-q",
                    "-f", "liger_resources/lambdaDRT.pl",
                    "-t", "pl2Tftf.",
                    "--",
                    axiomFile.getAbsolutePath(),
                    axiomOutputFile.getAbsolutePath(),
                    logicType
            };

            LOGGER.debug("Executing Prolog command: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(false); // keep stderr separate

            LOGGER.debug("Executing Prolog process...");
            Process process = processBuilder.start();

            // Capture stderr output in separate thread
            StringBuilder errorOutput = new StringBuilder();
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    LOGGER.error("Error reading Prolog stderr", e);
                }
            });
            stderrThread.start();

            // Use a thread pool to safely apply a timeout
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> task = executor.submit(() -> process.waitFor());



            String result = "";

            // Write axioms to file

            try {
                int exitCode = task.get(5, TimeUnit.SECONDS);

                stderrThread.join(); // wait for stderr reading to finish

                LOGGER.debug("Prolog exited with code {}", exitCode);
                if (exitCode != 0) {

                    LOGGER.error("Prolog stderr:\n{}", errorOutput.toString());
                    throw new RuntimeException("Prolog failed:\n" + errorOutput.toString());
                }

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(axiomOutputFile));
                    result = reader.lines()
                            .map(String::trim)
                            .filter(line -> !line.isEmpty())
                            .collect(Collectors.joining("\n"));
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Read result from output file
                LOGGER.debug("Translated axioms:\n{}", result);
                return result;

            } catch (TimeoutException e) {
                LOGGER.error("Prolog process timed out. Terminating...");
                process.destroyForcibly();
                task.cancel(true);
                throw new RuntimeException("Prolog process timed out and was forcibly terminated", e);
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                throw new RuntimeException("Process execution failed or was interrupted", e);
            } finally {
                executor.shutdownNow();
            }

        } catch (IOException e) {
            LOGGER.error("I/O error during Prolog invocation: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // Cleanup
            axiomFile.delete();
            axiomOutputFile.delete();
            tmpDir.delete();
        }
    }

}


