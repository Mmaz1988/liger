package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * /config_tree's answer: the files of a LiGER config or single rule file, per field, for the
 * client's workspace tree. Reading them is /load_file's job.
 *
 * Joining a section's files as LigerConfigLoader.assemble does reproduces /load_rules' text,
 * except for kind "rules": its semantics is the rule file's text alone, without a marker line.
 */
public class LigerConfigTree {

    /** "config" or "rules". */
    public String kind;
    /** The path that was loaded, as requested. */
    public String source;
    /** semantics, pragmatics, lexica, axioms, in that order; always all four. */
    public List<Section> sections = new ArrayList<>();

    public static class Section {
        public String field;
        /** The default config's path when the field is inherited from it, "builtin" for
         *  LiGER's default lexicon, null when it is the loaded file's own. */
        public String inheritedFrom;
        /** False when neither the loaded file nor the default config has the field; then
         *  /load_rules reports it as null and files is empty. */
        public boolean present;
        public List<File> files = new ArrayList<>();
    }

    public static class File {
        /** As /load_rules names it in its "===== path =====" marker lines. */
        public String path;
        public String name;

        public File() {
        }

        public File(String path, String name) {
            this.path = path;
            this.name = name;
        }
    }
}
