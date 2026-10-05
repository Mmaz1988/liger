package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * The axioms a structure's rules added, in the form the rules wrote them. Neither list is
 * encoded for a logic here: the Vampire service wraps (TPTP) or translates (Prolog) them with
 * the logic type of the run that uses them.
 *
 * <ul>
 *   <li>{@code prolog}: {@code AXIOM} values, Prolog FOL/DRS terms such as
 *       {@code all(X,imp(dog(X),animal(X)))}.</li>
 *   <li>{@code tptp}: {@code TPTP_AXIOM} values, TPTP formulas such as
 *       {@code ![X]: (dog(X) => animal(X))} -- passed through as written.</li>
 * </ul>
 */
public class GrammarAxioms {
    public List<String> prolog = new ArrayList<>();
    public List<String> tptp = new ArrayList<>();

    public GrammarAxioms() {
    }

    public GrammarAxioms(List<String> prolog, List<String> tptp) {
        this.prolog = prolog;
        this.tptp = tptp;
    }
}
