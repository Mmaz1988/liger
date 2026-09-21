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

package de.ukon.liger.analysis;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class LinguisticDictionary {

/*
    @Test
    public void testLoadDict() throws IOException {
        HashMap<String, HashMap<String, List<String>>> result;
        String filename = "zukunftsverben.txt";
        String path_to_txt = "C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\dicts\\";
        result = text2Dict(path_to_txt, filename);
        //compare number of keys
        Assertions.assertEquals(3, result.get("tense").size());
        //compare number of elements in every list
        Assertions.assertEquals(29, result.get("tense").get("FUTURE_POSSIBLE").size());
        Assertions.assertEquals(9, result.get("tense").get("FUTURE_PREDICTION").size());
        Assertions.assertEquals(16, result.get("tense").get("FUTURE_PLANNED").size());
    }
*/

    private final static Logger LOGGER = LoggerFactory.getLogger(LinguisticDictionary.class);

    /**
     * Parses a dictionary file used by the rule function {@code lex(value,dictionary)}.
     *
     * <pre>
     * >dictionary-name        starts a dictionary (the second argument of lex/2)
     * ***                     starts an entry
     * #LABEL                  the label lex/2 returns for the entry's values
     * value, value, ...       one or more value lines; one value per line is fine
     * // comment
     * </pre>
     *
     * Labels written one after the other within one entry share that entry's values.
     * Values are split on ',' and trimmed, so "she ,it" and a trailing "see,hear," both
     * yield separate values.
     *
     * Map and list order follow the file, which lex/2 relies on: the first label whose
     * values contain the looked-up value wins.
     */
    public static LinkedHashMap<String, LinkedHashMap<String, List<String>>> text2Dict(Path file) throws IOException {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> lingDict = new LinkedHashMap<>();
        LinkedHashMap<String, List<String>> labels = new LinkedHashMap<>();
        List<String> values = new ArrayList<>();

        for (String line : Files.readAllLines(file)) {
            String trimmed = line.strip();

            if (trimmed.isEmpty() || trimmed.startsWith("//")) {
                continue;
            }

            if (trimmed.startsWith(">")) {
                labels = lingDict.computeIfAbsent(trimmed.substring(1).strip(), k -> new LinkedHashMap<>());
            } else if (trimmed.startsWith("#")) {
                labels.put(trimmed.substring(1).strip(), values);
            } else if (trimmed.startsWith("*")) {
                values = new ArrayList<>();
            } else {
                for (String value : trimmed.split(",")) {
                    if (!value.isBlank()) {
                        values.add(value.strip());
                    }
                }
            }
        }
        return lingDict;
    }

    /** Legacy entry point: directory and file name given separately. */
    public static HashMap<String, HashMap<String, List<String>>> text2Dict(String path_to_txt, String file_name) throws IOException {
        HashMap<String, HashMap<String, List<String>>> out = new HashMap<>();
        text2Dict(Paths.get(path_to_txt + file_name)).forEach((dict, labels) -> out.put(dict, new HashMap<>(labels)));
        return out;
    }


    public static void write2json(HashMap<String, HashMap<String, ArrayList<String>>> future_verbs) {
        String outpath = "C:\\Users\\User\\Documents\\Uni\\HiWi-Java\\future_map.json";
        // Instantiate a new Gson instance.
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(outpath)) {
            gson.toJson(future_verbs, writer);
        } catch (Exception e) {
            LOGGER.error("Something went wrong while writing to Json");
        }
    }

    /*public static HashMap<String, Object> json2hashmap(String path_to_json) throws IOException {
        Gson gson = new Gson();
        String text = Files.readString(Paths.get(path_to_json));

        Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        HashMap<String, Object> map_of_verbs = gson.fromJson(text, type);


        return map_of_verbs;
    }*/
}


