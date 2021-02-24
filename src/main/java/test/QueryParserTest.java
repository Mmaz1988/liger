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


import analysis.QueryParser.*;
import analysis.RuleParser.*;
import glueSemantics.linearLogic.Premise;
import semantics.GlueSemantics;
import syntax.SyntacticStructure;
import utilities.HelperMethods;
import syntax.xle.XLEoperator;
import org.junit.jupiter.api.Test;
import utilities.PathVariables;
import utilities.VariableHandler;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class QueryParserTest {

    public static String testFolderPath;


    public LinkedHashMap<String, SyntacticStructure> loadFs(int i)
    {
        PathVariables.initializePathVariables();
        testFolderPath = PathVariables.testPath;
        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);

        List<LinkedHashMap<String, SyntacticStructure>> fsList = new ArrayList<>();

        System.out.println("C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\testFiles\\");

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


    /**
     * Queryparser tests
     */
    @Test
    void testQueryParser()
{
    LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g !(COMP*>TNS-ASP) #h",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3,qpr.result.keySet().size());
        }

    }

    @Test
    void testQueryParser6()
    {
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(1);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(7);

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(8);

        for (String key : fs.keySet())
        {
            QueryParser qp = new QueryParser("#g ADJUNCT #h & #h inSet #i",fs.get(key));

            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            RuleParser rp = new RuleParser(new ArrayList<>());
            rp.setReplace(true);

            Rule r1 = new Rule("#g ADJUNCT #h & #h inSet #i & #g TNS-ASP #j ==> #i ADJ-SEM '#g -o #i'.");
            Rule r2 = new Rule("#g ADJUNCT #h & #h inSet #i & #g NTYPE #a ==> #i ADJ-SEM '#g -o #i'.");

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

        List<SyntacticStructure> fslist = new ArrayList<>();

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(2);

        List<SyntacticStructure> fslist = new ArrayList<>();

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

        List<SyntacticStructure> fslist = new ArrayList<>();

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(3);

        List<SyntacticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,new File(testFolderPath + "testRules.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(11, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser5() {
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

        List<SyntacticStructure> fslist = new ArrayList<>();

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(0);

        List<SyntacticStructure> fslist = new ArrayList<>();

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(3);

        List<SyntacticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist, new File(testFolderPath + "testRules2.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(11, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser8() {
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(3);

        List<SyntacticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist, new File(testFolderPath + "testRules3.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(14, fslist.get(0).annotation.size());

    }

    @Test
    void testRuleParser9() {
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(3);

        GlueSemantics sem = new GlueSemantics();

        List<SyntacticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,new File( testFolderPath + "testRules3.txt"));

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(5);

        GlueSemantics sem = new GlueSemantics();

        List<SyntacticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,new File(testFolderPath + "testRules5.txt"));

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
        LinkedHashMap<String, SyntacticStructure> fs = loadFs(6);

        GlueSemantics sem = new GlueSemantics();

        List<SyntacticStructure> fslist = new ArrayList<>();

        for (String key : fs.keySet()) {
            fslist.add(fs.get(key));
        }

        RuleParser rp = new RuleParser(fslist,new File(testFolderPath + "testRules5.txt"));

        rp.addAnnotation2(fslist.get(0));

        sem.calculateSemantics(fslist.get(0));

        assertEquals(32,fslist.get(0).annotation.size());
        assertEquals(1,sem.llprover.getSolutions().size());

        for (Premise p : sem.llprover.getSolutions())
        {
            System.out.println(p.toString());
        }

    }


    /**
     *Other tests
     */
    @Test
    void testStrip2(){
        assertEquals("sssasssa", HelperMethods.stripValeue2("sssstrip(semform('a',5,[],[]))sssstrip(semform('a',5,[],[]))"));
    }

}
