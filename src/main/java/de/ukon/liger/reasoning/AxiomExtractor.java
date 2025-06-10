package de.ukon.liger.reasoning;


import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
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

    public String extractAxiomsFromString(String inputString) {
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

        return translatePrologAxioms(axiomBuilder.toString());
    }


    public List<String> extractAxiomsFromLigerAnnotations(LinguisticStructure linguisticStructure) {
        StringBuilder axiomBuilder = new StringBuilder();

        List<GraphConstraint> allConstraints = linguisticStructure.returnFullGraph();
        for (GraphConstraint constraint : allConstraints) {
            if (constraint.getRelationLabel().equals("AXIOM")) {
                //Check if Prolog or tftf
                axiomBuilder.append(String.format("axiom(%s).", constraint.getFsValue()));
                axiomBuilder.append(System.lineSeparator());
            }
        }

        if (axiomBuilder.length() == 0) {
            LOGGER.warn("No axioms found in linguistic structure.");
            return null;
        }

        String axiomsString = translatePrologAxioms(axiomBuilder.toString());

        List<String> axioms = Arrays.stream(axiomsString.split("\\.\\n"))
                .map(String::trim)
                .collect(Collectors.toList());

        return axioms;
    }

    public String translatePrologAxioms(String prologAxioms) {
        LOGGER.info("Translating Prolog axioms to tftf representation");
        File tmpDir = new File("./liger_resources/tmp/axiom_extractor_tmp");

        // Clean and prepare temp dir
        if (tmpDir.exists()) {
            for (File file : tmpDir.listFiles()) file.delete();
            tmpDir.delete();
        }
        tmpDir.mkdir();

        /*
        try {
            if (gswbFile.createNewFile()) {
                LOGGER.fine("File created successfully!");
            } else {
                LOGGER.warning("File already exists!");
            }
        } catch (
                IOException e) {
            LOGGER.warning("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(gswbFile));
            for (String solution : solutions) {
                writer.write(solution);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
         */


        File axiomFile = new File(tmpDir, "axiomFile.txt");
        File axiomOutputFile = new File(tmpDir, "axiomOutputFile.txt");


        //Create new file
        try {
            if (axiomFile.createNewFile()) {
                LOGGER.info(String.format("File %s created successfully!", axiomFile.getAbsolutePath()));
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
                LOGGER.info(String.format("File %s created successfully!", axiomOutputFile.getAbsolutePath()));
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
                    axiomOutputFile.getAbsolutePath()
            };

            LOGGER.info("Executing Prolog command: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(false); // keep stderr separate

            LOGGER.info("Executing Prolog process...");
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
                BufferedReader reader = new BufferedReader(new FileReader(axiomOutputFile));
                result = reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.joining("\n"));
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                int exitCode = task.get(5, TimeUnit.SECONDS);

                stderrThread.join(); // wait for stderr reading to finish

                if (exitCode != 0) {
                    LOGGER.error("Prolog exited with code {}", exitCode);
                    LOGGER.error("Prolog stderr:\n{}", errorOutput.toString());
                    throw new RuntimeException("Prolog failed:\n" + errorOutput.toString());
                }

                // Read result from output file
                LOGGER.info("Translated axioms:\n{}", result);
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


