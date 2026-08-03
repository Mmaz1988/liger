package de.ukon.liger.test;

import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.PackedAlternativeAssembler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackedSemanticRuleApplicationTest {
    @Test
    void ruleAnnotationStaysInMatchingSemanticContext() {
        LinguisticStructure first = alternative("m1");
        LinguisticStructure second = alternative("m2");
        LinguisticStructure packed = PackedAlternativeAssembler.pack(List.of(first, second));

        RuleParser parser = new RuleParser(
                new ArrayList<>(Collections.singletonList(packed)),
                "#m DRS 'm1' ==> #m MARK +.");
        parser.addAnnotation2(packed);

        assertEquals(1, packed.annotation.size());
        assertTrue(packed.annotation.get(0).getReading().contains(new ChoiceVar("M1")));
    }

    private LinguisticStructure alternative(String value) {
        LinguisticStructure structure = new LinguisticStructure();
        structure.local_id = value;
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        GraphConstraint fact = new GraphConstraint();
        fact.setFsNode("m");
        fact.setRelationLabel("DRS");
        fact.setFsValue(value);
        fact.setReading(new LinkedHashSet<>(Collections.singleton(new ChoiceVar("1"))));
        structure.constraints.add(fact);
        return structure;
    }
}
