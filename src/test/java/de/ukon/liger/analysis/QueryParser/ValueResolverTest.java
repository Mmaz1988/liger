package de.ukon.liger.analysis.QueryParser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueResolverTest {

    @Test
    void comparesNumericSuffixesForSameTypedNamespace() {
        assertEquals(-1, ValueResolver.compareIds("f10", "f25"));
        assertEquals(0, ValueResolver.compareIds("d07", "d7"));
        assertEquals(1, ValueResolver.compareIds("c30", "c4"));
    }

    @Test
    void rejectsComparisonsAcrossTypedNamespaces() {
        assertThrows(IllegalArgumentException.class,
                () -> ValueResolver.compareIds("f10", "c10"));
    }

    @Test
    void comparesLegacyNumericIdsByTheirNumericValues() {
        assertEquals(-1, ValueResolver.compareIds("10", "25"));
    }
}
