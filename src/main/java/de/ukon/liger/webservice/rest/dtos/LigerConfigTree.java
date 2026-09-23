package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * /config_tree's answer: the files of a LiGER config or single rule file, per field, for the
 * client's workspace tree. Reading them is /load_file's job.
 *
 * Joining a section's files as LigerConfigLoader.assemble does reproduces /load_rules' text,
 * except for kind "rules": its semantics is the rule file's text alone, without a marker line,
 * and its one file's path is absolute even when the request named it relatively.
 */
public class LigerConfigTree {

    /** "config" or "rules". */
    public String kind;
    /** The path that was loaded, as requested. */
    public String source;
    /** The config's scope folder, absolute (a single rule file's own folder): where its
     *  files belong, and where the client browses for more. */
    public String scope;
    /** semantics, pragmatics, lexica, axioms, in that order; always all four. */
    public List<Section> sections = new ArrayList<>();

    public static class Section {
        public String field;
        /** False when the loaded file does not list the field; then /load_rules reports it
         *  as null (lexica: empty) and files is empty. Nothing is inherited. */
        public boolean present;
        public List<File> files = new ArrayList<>();
    }

    public static class File {
        /** As /load_rules names it in its "===== path =====" marker lines. */
        public String path;
        public String name;
        /** False for a file outside the config's scope: allowed, but worth seeing. */
        public boolean inScope = true;

        public File() {
        }

        public File(String path, String name, boolean inScope) {
            this.path = path;
            this.name = name;
            this.inScope = inScope;
        }
    }
}
