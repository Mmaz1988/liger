package de.ukon.liger.semantics;

import de.ukon.liger.utilities.PathVariables;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Concurrent Prolog MC extractions each get their own files. All of them used to share
 * tmp/prolog, which every call emptied and refilled, so a request could read another
 * request's meaning constructors, or none (2026-11-09).
 */
class PrologMcExtractionConcurrencyTest {

    private static final int CALLS = 16;

    @Test
    void concurrentExtractionsDoNotShareFiles() throws Exception {
        assumeTrue(swiplAvailable(), "swipl not on PATH");
        Path workingDirectory = Files.createTempDirectory("liger-prolog-mcs");
        String previous = PathVariables.workingDirectory;
        PathVariables.workingDirectory = workingDirectory.toString();
        ExecutorService pool = Executors.newFixedThreadPool(CALLS);
        try {
            List<String> inputs = List.of(
                    Files.readString(Path.of("liger_resources/testFiles/hybrid_glue_test.pl")),
                    Files.readString(Path.of("liger_resources/testFiles/fs11.pl")));
            List<String> expected = new ArrayList<>();
            for (String input : inputs) {
                expected.add(new GlueSemantics().extractMCsFromFs(input));
            }
            assertFalse(expected.get(0).equals(expected.get(1)), "the two inputs must give different MCs");

            CountDownLatch start = new CountDownLatch(1);
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < CALLS; i++) {
                String input = inputs.get(i % 2);
                results.add(pool.submit(() -> {
                    start.await();
                    return new GlueSemantics().extractMCsFromFs(input);
                }));
            }
            start.countDown();
            for (int i = 0; i < CALLS; i++) {
                assertEquals(expected.get(i % 2), results.get(i).get(), "call " + i);
            }
            try (var left = Files.list(workingDirectory.resolve("tmp"))) {
                assertEquals(0, left.count(), "every call removes its directory");
            }
        } finally {
            pool.shutdownNow();
            PathVariables.workingDirectory = previous;
        }
    }

    private static boolean swiplAvailable() {
        try {
            return new ProcessBuilder("swipl", "--version").start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
