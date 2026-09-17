package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerSourceSpan;
import de.ukon.liger.webservice.rest.dtos.LigerSourceToken;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SourceIndexResolverService {

    public LigerSourceSpan resolve(LinguisticStructure structure, Collection<Integer> sourceIndices) {
        if (structure == null || sourceIndices == null || sourceIndices.isEmpty()) {
            return new LigerSourceSpan("", null, null);
        }
        return resolve(new SourceTokenIndex(structure), sourceIndices);
    }

    public List<LigerSourceSpan> resolveGroups(LinguisticStructure structure, List<List<Integer>> sourceIndexGroups) {
        SourceTokenIndex index = new SourceTokenIndex(structure);
        return sourceIndexGroups.stream().map(group -> resolve(index, group)).collect(Collectors.toList());
    }

    private LigerSourceSpan resolve(SourceTokenIndex index, Collection<Integer> sourceIndices) {
        List<LigerSourceToken> tokens = index.tokensFor(sourceIndices);
        if (tokens.isEmpty()) {
            return new LigerSourceSpan("", null, null);
        }
        String text = tokens.stream().map(token -> token.form).collect(Collectors.joining(" "));
        int end = tokens.stream().mapToInt(token -> token.end).max().getAsInt();
        return new LigerSourceSpan(text, tokens.get(0).start, end, tokens);
    }
}
