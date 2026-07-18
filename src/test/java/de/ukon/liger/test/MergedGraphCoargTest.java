package de.ukon.liger.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ukon.liger.analysis.QueryParser.HierarchyParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergedGraphCoargTest {

    @Test
    void testCoargQueryMatchesMergedGraph() throws Exception {
        Path mergedGraph = Paths.get("merged-graph.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "GF := SUBJ | OBJ | OBL . " +
                "COARG-PATH(#a,#b,#c) := #a ^(@GF*:~(->PRED)) #b ^(@GF) #c . " +
                "COARG(#a,#b) := @COARG-PATH(#a,#r,#s) & #s !(@GF) #b & id(#r) != id(#b) .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBJ2 > OBL .");

        QueryParser qp = new QueryParser("@COARG(#a,#b)", fs, templateRegistry, hierarchyRegistry);
        QueryParserResult qpr = qp.parseQueryWithTemplates("@COARG(#a,#b)").get(0);

        assertTrue(qpr.isSuccess);
        assertEquals(6, qpr.result.keySet().size());
    }
}
