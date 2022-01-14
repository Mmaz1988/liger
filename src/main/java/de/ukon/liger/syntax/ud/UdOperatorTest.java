package de.ukon.liger.syntax.ud;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.SyntaxOperator;
import org.junit.jupiter.api.Test;

public class UdOperatorTest {


    @Test
    void testCompareParses()
    {
        UDoperator op = new UDoperator();

        LinguisticStructure in = op.parseSingle("John loves Mary.");
        LinguisticStructure out = op.parseSingle("John has loved Mary.");

        op.compareParses(in,out);

    }

}
