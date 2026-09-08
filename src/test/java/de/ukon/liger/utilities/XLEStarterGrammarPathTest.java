package de.ukon.liger.utilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * `xle_paths.txt` was observed being wiped by "update grammar", which leaves XLE with no
 * grammar and every parse endpoint returning 500. These pin the three ways that could
 * happen: an unvalidated path, a truncate-before-write, and a positional edit applied to
 * a file that is not shaped the way it assumes.
 */
class XLEStarterGrammarPathTest {

    private Path writePathsFile(Path dir, String... lines) throws IOException {
        Path file = dir.resolve("xle_paths.txt");
        Files.write(file, List.of(lines));
        return file;
    }

    private <T> T inWorkingDirectory(Path dir, java.util.function.Supplier<T> body) {
        String previous = PathVariables.workingDirectory;
        PathVariables.workingDirectory = dir.toString();
        try {
            return body.get();
        } finally {
            PathVariables.workingDirectory = previous;
        }
    }

    @Test
    void updatesTheGrammarLineAndLeavesEverythingElseAlone(@TempDir Path dir) throws IOException {
        Path file = writePathsFile(dir, "xle=\"/opt/xle\"", "grammar=\"/old.lfg\"", "os=\"mac\"");

        inWorkingDirectory(dir, () -> {
            new XLEStarter().updateGrammarPath("/new.lfg");
            return null;
        });

        assertEquals(List.of("xle=\"/opt/xle\"", "grammar=\"/new.lfg\"", "os=\"mac\""),
                Files.readAllLines(file));
    }

    @Test
    void refusesABlankGrammarPathInsteadOfWritingAnUnusableFile(@TempDir Path dir) throws IOException {
        Path file = writePathsFile(dir, "xle=\"/opt/xle\"", "grammar=\"/old.lfg\"", "os=\"mac\"");
        List<String> before = Files.readAllLines(file);

        inWorkingDirectory(dir, () -> {
            XLEStarter starter = new XLEStarter();
            assertThrows(IllegalArgumentException.class, () -> starter.updateGrammarPath("  "));
            assertThrows(IllegalArgumentException.class, () -> starter.updateGrammarPath(null));
            return null;
        });

        assertEquals(before, Files.readAllLines(file), "the file must survive a rejected update");
    }

    @Test
    void refusesToRewriteAFileThatIsNotShapedLikeXlePaths(@TempDir Path dir) throws IOException {
        // Previously this was silently rewritten with the grammar line never replaced,
        // because the edit was located purely by line index.
        Path file = writePathsFile(dir, "xle=\"/opt/xle\"");

        inWorkingDirectory(dir, () -> {
            XLEStarter starter = new XLEStarter("/opt/xle", "/old.lfg", XLEStarter.OS.MAC);
            assertThrows(IllegalStateException.class, () -> starter.updateGrammarPath("/new.lfg"));
            return null;
        });

        assertEquals(List.of("xle=\"/opt/xle\""), Files.readAllLines(file),
                "a refused update must not truncate the file");
    }

    @Test
    void reportsRatherThanClobbersWhenTheFileIsMissing(@TempDir Path dir) {
        Path missing = dir.resolve("nowhere");
        inWorkingDirectory(missing, () -> {
            XLEStarter starter = new XLEStarter("/opt/xle", "/old.lfg", XLEStarter.OS.MAC);
            assertThrows(RuntimeException.class, () -> starter.updateGrammarPath("/new.lfg"));
            return null;
        });
    }
}
