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

package de.ukon.liger.test;


import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import glueSemantics.linearLogic.Premise;
import org.junit.jupiter.api.Test;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.Fstructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class QueryParserTest {

    public static String testFolderPath;
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryParserTest.class);

    public LinkedHashMap<String, LinguisticStructure> loadFs(int i)
    {
        PathVariables.workingDirectory = "C:\\Users\\Celeste\\IdeaProjects\\LiGER\\liger_resources";
        PathVariables.initializePathVariables();
        testFolderPath = PathVariables.testPath;
        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);

        List<LinkedHashMap<String, LinguisticStructure>> fsList = new ArrayList<>();


        fsList.add(xle.fs2Java(testFolderPath + "testdirS1.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS2.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS3.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS4.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS5.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS6.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS7.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS8.pl"));
        fsList.add(xle.fs2Java(testFolderPath + "testdirS11.pl"));
        return fsList.get(i);
    }


    @Test
    void testPrologPrint() throws IOException {
        PathVariables.initializePathVariables();
        testFolderPath = PathVariables.testPath;
        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);
        Fstructure fs = (Fstructure) xle.xle2Java(testFolderPath + "testdirS1.pl");
        System.out.println(fs.writeToProlog(false));
    }

    /**
     * Queryparser tests
     */
    @Test
    void testQueryParser()
{
    LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

    for (String key : fs.keySet())
    {
        QueryParser qp = new QueryParser("#g TENSE 'past' & #h PERF '-_'",fs.get(key));

        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertEquals(4,qpr.result.keySet().size());
    }

}

    @Test
    void testQueryParsera()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("SUBJ #a",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }

    }


    @Test
    void testQueryParser2()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TENSE 'past' & #g TENSE 'past'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser3()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TENSE 'past' & #h PERF '-_' & #j SUBJ #i",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(12,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser4a()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TNS-ASP #h TENSE 'past' & #h PERF '-_'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser4b()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' & #h MOOD 'indicative'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser4c()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' & #h MOOD 'indicative'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }
    }


    @Test
    void testQueryParser5()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g !(COMP*>TNS-ASP) #h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser5a()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g !(COMP*>TNS-ASP) #h",fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            RuleParser rp = new RuleParser(new ArrayList<>());

            Rule r = new Rule("#g !(COMP*>TNS-ASP) #h ==> #g TMP-DOM #h");

            rp.getRules().add(r);

            rp.addAnnotation2(fs.get(key));

            assertEquals(3,fs.get(key).annotation.size());

            assertEquals(3,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser6()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g !(SUBJ) #h CASE 'nom'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4,qpr.result.keySet().size());

        }

    }

    @Test
    void testQueryParser7()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g !(SUBJ>NTYPE) #h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser8()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g ^(COMP*) #h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser8b()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g ^(COMP*>%) #h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser9()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TENSE %g",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser10()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TENSE %g & #h TENSE %g",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser11()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TENSE %g & #h TENSE %h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser12()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g TENSE %g & %g != 'past' & #h PERF '-_'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser13()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g PRED %g & strip(%g) == 'John' & #h PERF '-_'",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser14()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(7);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g ^(SUBJ) #h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser15()
    {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(8);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g ADJUNCT #h & #h in_set #i",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            RuleParser rp = new RuleParser(new ArrayList<>());
            rp.setReplace(true);

            Rule r1 = new Rule("#g ADJUNCT #h & #h in_set #i & #g TNS-ASP #j ==> #i ADJ-SEM '#g -o #i'.");
            Rule r2 = new Rule("#g ADJUNCT #h & #h in_set #i & #g NTYPE #a ==> #i ADJ-SEM '#g -o #i'.");

            rp.getRules().add(r1);
            rp.getRules().add(r2);


            String key2 = fs.keySet().stream().findAny().get();

            rp.addAnnotation2(fs.get(key2));

            assertEquals(2, fs.get(key2).annotation.size());

        //    assertEquals(2,qpr.result.keySet().size());
        }

    }


    /**
     * analysis.RuleParser tests
     */

    @Test
    void testRuleParser() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #h SEM 'event'");

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(1, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser2() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #i SEM 'event'");

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(1, fslist.get(0).annotation.size());

    }
    @Test
    void testRuleParser3() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #i SEM 'event'");
        Rule r2 = new Rule("#i SEM 'event' ==> #i PAST 'past'");

        rp.getRules().add(r1);
        rp.getRules().add(r2);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(4, fslist.get(0).annotation.size());

    }


    @Test
    void testRuleParser4() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(3);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist, Paths.get(testFolderPath + "testRules.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(11, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser5() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);
        rp.setReplace(true);

        Rule r1 = new Rule("#i PRED %i ==> #i SEM 'strip(%i)'");

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(5, fslist.get(0).annotation.size());

    }


    @Test
    void testRuleParser6() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(0);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);
        rp.setReplace(true);

        Rule r1 = new Rule("#i PRED %i ==> #i SEM 'strip(%i)'");
        Rule r2 = new Rule("#i PRED %i ==> #i SEM 'This string replaced: strip(%i)'");


        rp.getRules().add(r1);
        rp.getRules().add(r2);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(5, fslist.get(0).annotation.size());

    }


    @Test
    void testRuleParser7() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(3);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist, Paths.get(testFolderPath + "testRules2.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(11, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser8() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(3);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist, Paths.get(testFolderPath + "testRules3.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(14, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser9() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(3);

        GlueSemantics sem = new GlueSemantics();

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,Paths.get( testFolderPath + "testRules3.txt"));

        rp.addAnnotation2(fslist.get(0));

        sem.calculateSemantics(fslist.get(0));

        assertEquals(14, fslist.get(0).annotation.size());
        assertEquals(2,sem.llprover.getSolutions().size());

        for (Premise p : sem.llprover.getSolutions())
        {
            System.out.println(p.toString());
        }

    }

    @Test
    void testRuleParser10() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(5);

        GlueSemantics sem = new GlueSemantics();

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,Paths.get(testFolderPath + "testRules5.txt"));

        rp.addAnnotation2(fslist.get(0));

        sem.calculateSemantics(fslist.get(0));

        assertEquals(27,fslist.get(0).annotation.size());
        assertEquals(2,sem.llprover.getSolutions().size());

        for (Premise p : sem.llprover.getSolutions())
        {
            System.out.println(p.toString());
        }

    }

    @Test
    void testRuleParser11() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(6);

        GlueSemantics sem = new GlueSemantics();

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,Paths.get(testFolderPath + "testRules5.txt"));

        rp.addAnnotation2(fslist.get(0));

        sem.calculateSemantics(fslist.get(0));

        assertEquals(32,fslist.get(0).annotation.size());
        assertEquals(1,sem.llprover.getSolutions().size());

        for (Premise p : sem.llprover.getSolutions())
        {
            System.out.println(p.toString());
        }

    }


    @Test
    void testRuleParserRewrite1() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' =-> #i SEM 'event'",true);

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(1, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParserRewrite2() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs(2);

        List<LinguisticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h =-> 0",true);

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(79, fslist.get(0).constraints.size());
    }

    /**
     *Other tests
     */
    @Test
    void testStrip2(){
        assertEquals("sssasssa", HelperMethods.stripValeue2("sssstrip(semform('a',5,[],[]))sssstrip(semform('a',5,[],[]))"));
    }

}
