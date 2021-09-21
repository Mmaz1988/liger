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

package test;

import analysis.RuleParser.RuleParser;
import com.opencsv.CSVWriter;
import main.DbaMain;
import org.junit.jupiter.api.Test;
import syntax.GraphConstraint;
import syntax.SyntacticStructure;
import syntax.ud.UDoperator;
import syntax.xle.XLEoperator;
import utilities.PathVariables;
import utilities.VariableHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ParserTest {


    private final static Logger LOGGER = Logger.getLogger(DbaMain.class.getName());

    @Test
    void testParser()
    {

        PathVariables.workingDirectory = "C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\";
        PathVariables.initializePathVariables();
        String test_file = PathVariables.testPath + "manifesto_test.txt";
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
                RuleParser rp = new RuleParser(fsList, PathVariables.testPath + "testRulesUD5.txt");
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
        String filePath = PathVariables.testPath + "manifesto_test_j2.csv";
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
        String filePath = PathVariables.testPath + "manifesto_test_xle.csv";
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

