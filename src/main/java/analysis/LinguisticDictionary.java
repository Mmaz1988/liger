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

package analysis;

import com.google.gson.Gson;
import utilities.PathVariables;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static HashMap<String, HashMap<String, List<String>>> ld;


    public LinguisticDictionary() {
try {
   this.ld = initializeLinguisticDictionary();
}catch(Exception e)
{
    e.printStackTrace();
    System.out.println("Failed to initialize dictionary!");
}
    }
    //For testing purposes


    public static HashMap<String, HashMap<String, List<String>>> initializeLinguisticDictionary() throws IOException {
        HashMap<String, HashMap<String, List<String>>> dict = new HashMap<>();

        try {
            return text2Dict(PathVariables.dictPath, "ling_dict_eng.txt");
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //TODO write Method that loads in external lexicon; JSON?
    public static HashMap<String, HashMap<String, List<String>>>  text2Dict(String path_to_txt, String file_name) throws IOException {

        HashMap<String, HashMap<String, List<String>>> lingDict = new HashMap<>();
        //String tense = file_name;
        //A linguistic dictionary is a collection  of dictionaries for different categories, e.g. tense,
        // within a dictionary specific tokens are associated with a label, i.e. hashmap that has a label as key and a list of possible values as value.
        //==> type HashMap<String,HashMap<String,List<String>>>


        List<String> verbs = new ArrayList();
        HashMap<String, List<String>> map_verbs = new HashMap<>();

        //File file = new File("C:\\Users\\User\\Documents\\Uni\\HiWi-Java\\ling_dict_eng.txt");
        File file = new File(path_to_txt + file_name);
        BufferedReader br = new BufferedReader(new FileReader(file));
        //Key has to start with #
        //Lines should not end with ,
        // Block have to end with *
        String line = null;

        while ((line = br.readLine()) != null) {
            String key = "";

            String lexKey ="";

            if (line.startsWith("//"))
            {
                continue;
            }

            if (line.startsWith(">"))
            {
                 lexKey = line.substring(1).strip();
                 map_verbs = new HashMap<>();
                 lingDict.put(lexKey,map_verbs);
            }

//            while ((line = br.readLine()) != null && !line.startsWith(">")) {

                if (line.startsWith("#")) {
                    key = line.replace("#", "");
                    map_verbs.put(key, verbs);
                } else if (line.contains(",")) {
                    String[] lines = line.split(", ");
                    for (String ss : lines) {
                        verbs.add(ss.strip());
                    }
                } else if (line.contains("*")) {
                    verbs = new ArrayList<String>();
                }
     //           lexKey = line.substring(1).strip();
  //          }
         //   future_verbs.put(lexKey, map_verbs);
        }

        //writing to json
        //write2json(future_verbs);
        //json2hashmap("C:/Users/User/Documents/Uni/HiWi-Java/future_map.json");
        return lingDict;
    }


    public static void write2json(HashMap<String, HashMap<String, ArrayList<String>>> future_verbs) {
        String outpath = "C:\\Users\\User\\Documents\\Uni\\HiWi-Java\\future_map.json";
        // Instantiate a new Gson instance.
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(outpath)) {
            gson.toJson(future_verbs, writer);
        } catch (Exception e) {
            System.out.print("Something went wrong while writing to Json");
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


