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

package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class GlueSemantics {

    private final static Logger LOGGER = LoggerFactory.getLogger(GlueSemantics.class);

    public GlueSemantics() {
    }


    public String returnMeaningConstructors(LinguisticStructure fs) {

        HashMap<Set<ChoiceVar>, List<String>> unpackedSem = new HashMap<>();

        for (Set<ChoiceVar> choice : fs.cp.choices) {

            unpackedSem.put(choice, new ArrayList<>());
        }

        StringBuilder sb = new StringBuilder();

        for (GraphConstraint c : fs.annotation) {
            if (c.getRelationLabel().equals("GLUE")) {
                if (unpackedSem.containsKey(c.getReading())) {
                    unpackedSem.get(c.getReading()).add(c.getFsValue().toString());
                }
            }
        }

        boolean relevantChoice = false;

        for (Set<ChoiceVar> choice : unpackedSem.keySet()) {
            if (!choice.equals(fs.cp.rootChoice) && !unpackedSem.get(choice).isEmpty()) {
                relevantChoice = true;
                unpackedSem.get(choice).addAll(unpackedSem.get(fs.cp.rootChoice));
            }
        }

        if (relevantChoice) {
            unpackedSem.remove(fs.cp.rootChoice);
        }

        for (Set<ChoiceVar> key : unpackedSem.keySet()) {
            if (!unpackedSem.get(key).isEmpty()) {
                sb.append("{");
                sb.append(System.lineSeparator());
                for (String s : unpackedSem.get(key)) {
                    sb.append(s);
                    sb.append(System.lineSeparator());
                }
                sb.append("}");
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }


    public String extractMCsFromFs(String fs) {
        List<String> drtSolutions = new ArrayList<>();
        //create a file that includes all Strings in solutions line  by line
        //run swipl with the file as input

        LOGGER.info("Creating temporary files...");
        //create temporary directory gswb_resources/tmp
        File tmpDir = new File("liger_resources/tmp");

        if (tmpDir.exists()) {
            File[] files = tmpDir.listFiles();
            for (File file : files) {
                file.delete();
            }
            tmpDir.delete();
        }

        tmpDir.mkdir();

        File prologFS = new File("liger_resources/tmp/prolog.pl");

        try {
            if (prologFS.createNewFile()) {
                LOGGER.info("File created successfully!");
            } else {
                LOGGER.error("File already exists!");
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }

        File xleTransferOutput = new File("liger_resources/tmp/xle_prolog_mcs.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(prologFS));
            writer.write(fs);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


// exec swipl -q -f  src/premises.pl -t "main." -- \
//	"$prologfile" "$outputfile"
        try {
            String[] command = {
                    "swipl",
                    "-q",
                    "-f",
                    "liger_resources/xle_glue_transfer/premises.pl",
                    "-t",
                    "main.",
                    "--",
                    prologFS.getAbsolutePath(),
                    xleTransferOutput.getAbsolutePath()
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Java join command with white space


            LOGGER.info("Executing Prolog goal to extract meaning constructors!");

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read and print the output of the external command

            // Wait for the process to complete
            // Create a separate thread to wait for the process to finish
            FutureTask<Integer> task = new FutureTask<>(process::waitFor);
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(task);

            // Wait for 5 seconds for the process to finish
            try {
                int exitCode = task.get(5, TimeUnit.SECONDS);
                if (exitCode != 0) {
                    LOGGER.error("\nFailed to read output from lambdaDRT.pl!\n");
                }
            } catch (Exception e) {
                // If the process takes more than 5 seconds, destroy it
                process.destroyForcibly();
                LOGGER.error("\nProcess timed out and was forcibly terminated.\n");
            }

            executor.shutdown();

            //read file xleTransferOutput to String

            String xleTransferOutputString = "";

            if (xleTransferOutput.exists()) {


                BufferedReader reader = new BufferedReader(new FileReader(xleTransferOutput));

                //concatenate all lines in the file to one String
                xleTransferOutputString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            } else {
                LOGGER.error("File " + xleTransferOutput.getAbsolutePath() + " does not exist!");

            }



            //delete temporary files
            prologFS.delete();
            xleTransferOutput.delete();
            tmpDir.delete();

            return xleTransferOutputString;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
