package test;

import analysis.RuleParser.RuleParser;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.Test;
import syntax.SyntacticStructure;
import syntax.ud.UDoperator;
import syntax.xle.Prolog2Java.GraphConstraint;
import syntax.xle.XLEoperator;
import utilities.VariableHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static test.QueryParserTest.testFolderPath;

public class ParserTest {


    @Test
    void testParser()
    {
        String test_file = "C:\\Users\\User\\Documents\\Uni\\HiWi-Java\\manifesto_test.txt";
        VariableHandler var_handler = new VariableHandler();
        UDoperator parser = new UDoperator(var_handler);
        List<List<String>> sentences = parser.extractModalsFromTestfile(test_file);
        Integer number_of_sentences = 13;
        //compare number of files in parser_output vs. expected (number of sentences)
        //assertEquals(number_of_sentences, sentences.size());ó
        // create a List which contains String array
        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { "Sentence", "Subject", "Modal" });
        for (List<String> sentence : sentences)
        {
            for (String j: sentence )
            {
                SyntacticStructure fs = parser.parseSingle(j);
                List<SyntacticStructure> fsList = new ArrayList<>();
                fsList.add(fs);
                RuleParser rp = new RuleParser(fsList, testFolderPath + "testRulesUD5.txt");
                rp.addAnnotation2(fs);
                try {
                    fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
                } catch(Exception e)
                {
                    System.out.println("Sorting annotation failed.");
                }

                List<String[]> listModals = new ArrayList<>();
                String subject = "";
                String modal = "";
                HashMap<String, Integer> occurence_fsvalues = new HashMap<String, Integer>();

                String[][] sentenceRep;

                if (fs.annotation.isEmpty()) {
   //                 data.add(new String[] { j, subject, modal});

                    sentenceRep = new String[1][3];
                    sentenceRep[0][0] = j;
                    sentenceRep[0][1] = "";
                    sentenceRep[0][2] = "";
                }
                else {
                    for (GraphConstraint g : fs.annotation) {
                        if (g.getRelationLabel().equals("MODAL"))
                        {
                            String[] modalPlusSubj = new String[2];
                            modalPlusSubj[1] = g.getFsValue().toString();

                            modal = g.getFsValue().toString();
                            String node_identifier = g.getFsNode();
                            if (occurence_fsvalues.containsKey(modal)){
                                occurence_fsvalues.put(modal, occurence_fsvalues.get(modal) + 1);
                            }
                            else {
                                occurence_fsvalues.put(modal, 1);
                            }
                            for (GraphConstraint k : fs.annotation) {
                                if (k.getFsNode().equals(node_identifier) && k.getRelationLabel().equals("SUBJ")){
                                    subject = k.getFsValue().toString();
                                    modalPlusSubj[0] = k.getFsValue().toString();
                                }
                            }
                            //to avoid duplicate entries in data
                            if (occurence_fsvalues.containsKey(modal) && occurence_fsvalues.get(modal)==1){
 //                               data.add(new String[] { j, subject, modal});
                            }
                            listModals.add(modalPlusSubj);
                        }

                    }



                    if (listModals.size() > 0)
                    {
                    sentenceRep = new String[listModals.size()][3];
                    sentenceRep[0][0] = j;
                    for (int i = 0; i < listModals.size();i++)
                    {
                        if (i > 0) {
                            sentenceRep[i][0] = "";
                        }
                        if (listModals.get(i)[1] != null) {
                            sentenceRep[i][1] = listModals.get(i)[0];
                        }else
                        {
                            sentenceRep[i][1] = "";
                        }
                        sentenceRep[i][2] = listModals.get(i)[1];
                    }
                    }
                    else{
                        sentenceRep = new String[1][3];
                        sentenceRep[0][0] = j;
                        sentenceRep[0][1] = "";
                        sentenceRep[0][2] = "";
                    }
                }

              for (int i2 = 0; i2 < sentenceRep.length; i2++)
              {
                  data.add(new String[] {sentenceRep[i2][0],sentenceRep[i2][1],sentenceRep[i2][2] });
              }

            }
        }
        // first create file object for file placed at location
        // specified by filepath
        String filePath = "C:\\Users\\User\\Documents\\Uni\\HiWi-Java\\manifesto_test_j.csv";
        File file = new File(filePath);

        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            writer.writeAll(data);
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void testParser2()
    {
        //VariableHandler var_handler = new VariableHandler();
        VariableHandler vh = new VariableHandler();
        XLEoperator parser = new XLEoperator(vh);
        LinkedHashMap<String, SyntacticStructure> test_file = parser.loadPrologFstructures();
        List<List<String>> sentences = parser.extractModalsFromXLE(test_file);
        Integer number_of_sentences = 13;
        //compare number of files in parser_output vs. expected (number of sentences)
        //assertEquals(number_of_sentences, sentences.size());ó
        // create a List which contains String array
        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { "Sentence", "Subject", "Modal" });
        for (String key : test_file.keySet())
        {
                SyntacticStructure fs = test_file.get(key);
                List<String[]> listModals = new ArrayList<>();
                String subject = "";
                String modal = "";
                HashMap<String, Integer> occurence_fsvalues = new HashMap<String, Integer>();

                String[][] sentenceRep;

                if (fs.annotation.isEmpty()) {
                    //                 data.add(new String[] { j, subject, modal});

                    sentenceRep = new String[1][3];
                    sentenceRep[0][0] = fs.sentence;
                    sentenceRep[0][1] = "";
                    sentenceRep[0][2] = "";
                }
                else {
                    for (GraphConstraint g : fs.annotation) {
                        if (g.getRelationLabel().equals("MODAL"))
                        {
                            String[] modalPlusSubj = new String[2];
                            modalPlusSubj[1] = g.getFsValue().toString();

                            modal = g.getFsValue().toString();
                            String node_identifier = g.getFsNode();
                            if (occurence_fsvalues.containsKey(modal)){
                                occurence_fsvalues.put(modal, occurence_fsvalues.get(modal) + 1);
                            }
                            else {
                                occurence_fsvalues.put(modal, 1);
                            }
                            for (GraphConstraint k : fs.annotation) {
                                if (k.getFsNode().equals(node_identifier) && k.getRelationLabel().equals("SUBJ")){
                                    subject = k.getFsValue().toString();
                                    modalPlusSubj[0] = k.getFsValue().toString();
                                }
                            }
                            //to avoid duplicate entries in data
                            if (occurence_fsvalues.containsKey(modal) && occurence_fsvalues.get(modal)==1){
                                //                               data.add(new String[] { j, subject, modal});
                            }
                            listModals.add(modalPlusSubj);
                        }

                    }



                    if (listModals.size() > 0)
                    {
                        sentenceRep = new String[listModals.size()][3];
                        sentenceRep[0][0] = fs.sentence;
                        for (int i = 0; i < listModals.size();i++)
                        {
                            if (i > 0) {
                                sentenceRep[i][0] = "";
                            }
                            if (listModals.get(i)[1] != null) {
                                sentenceRep[i][1] = listModals.get(i)[0];
                            }else
                            {
                                sentenceRep[i][1] = "";
                            }
                            sentenceRep[i][2] = listModals.get(i)[1];
                        }
                    }
                    else{
                        sentenceRep = new String[1][3];
                        sentenceRep[0][0] = fs.sentence;
                        sentenceRep[0][1] = "";
                        sentenceRep[0][2] = "";
                    }
                }

                for (int i2 = 0; i2 < sentenceRep.length; i2++)
                {
                    data.add(new String[] {sentenceRep[i2][0],sentenceRep[i2][1],sentenceRep[i2][2] });
                }


        }
        // first create file object for file placed at location
        // specified by filepath
        String filePath = "/Users/red_queen/IdeaProjects/xlebatchparsing/src/test/manifesto_test_xle.csv";
        File file = new File(filePath);

        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            writer.writeAll(data);
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

