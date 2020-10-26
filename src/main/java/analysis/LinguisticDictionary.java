package analysis;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LinguisticDictionary {


    @Test
    public void testLoadDict() throws IOException {
        HashMap<String, HashMap<String, List<String>>> result;
        String filename = "zukunftsverben.txt";
        String path_to_txt = "C:\\Users\\Celeste\\IdeaProjects\\xle_operator\\src\\Dicts\\";
        result = text2Dict(path_to_txt, filename);
        //compare number of keys
        Assertions.assertEquals(3, result.get(filename).size());
        //compare number of elements in every list
        Assertions.assertEquals(29, result.get(filename).get("FUTURE_POSSIBLE").size());
        Assertions.assertEquals(9, result.get(filename).get("FUTURE_PREDICTION").size());
        Assertions.assertEquals(16, result.get(filename).get("FUTURE_PLANNED").size());
    }

    public static List<String> future = Arrays.asList("will", "would", "can", "could");
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
            return text2Dict("C:\\Users\\Celeste\\IdeaProjects\\xle_operator\\src\\Dicts\\", "future_verbs.txt");
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //TODO write Method that loads in external lexicon; JSON?
    public static HashMap<String, HashMap<String, List<String>>>  text2Dict(String path_to_txt, String file_name) throws IOException {

        HashMap<String, HashMap<String, List<String>>> future_verbs = new HashMap<>();
        //String tense = file_name;
        HashMap<String, List<String>> map_verbs = new HashMap<>();
        List<String> verbs = new ArrayList();
        List<String> verbs_copy = new ArrayList();

        //File file = new File("C:\\Users\\User\\Documents\\Uni\\HiWi-Java\\future_verbs.txt");
        File file = new File(path_to_txt + file_name);
        BufferedReader br = new BufferedReader(new FileReader(file));
        //Key has to start with #
        //Lines should not end with ,
        // Block have to end with *
        String line = null;
        while ((line = br.readLine()) != null) {
            String key = "";
            if (line.startsWith("#")) {
                key = line.replace("#", "");
                map_verbs.put(key, verbs);
            } else if (line.contains(",")) {
                String[] lines = line.split(", ");
                for (String ss : lines) {
                    verbs.add(ss);
                }
            } else if (line.contains("*")) {
                verbs = new ArrayList<String>();
            }
        }
        future_verbs.put("tense", map_verbs);
        //writing to json
        //write2json(future_verbs);
        //json2hashmap("C:/Users/User/Documents/Uni/HiWi-Java/future_map.json");
        return future_verbs;
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


