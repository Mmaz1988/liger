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

    /** The group's words as they read in the sentence. Words between two of the group's tokens
     *  are kept when none of them has a source index of its own -- "every ITEL computer", not
     *  "every computer", since ITEL's meaning comes from a LiGER rule -- and replaced by "…" when
     *  any of them belongs to a constructor, i.e. to some other element of the reading. */
    private LigerSourceSpan resolve(SourceTokenIndex index, Collection<Integer> sourceIndices) {
        List<LigerSourceToken> tokens = index.tokensFor(sourceIndices);
        if (tokens.isEmpty()) {
            return new LigerSourceSpan("", null, null);
        }
        Set<LigerSourceToken> unindexed = index.unindexedTokens();
        List<LigerSourceToken> all = index.allTokens();
        String sentence = index.text();
        StringBuilder text = new StringBuilder(tokens.get(0).form);
        for (int i = 1; i < tokens.size(); i++) {
            LigerSourceToken previous = tokens.get(i - 1);
            LigerSourceToken next = tokens.get(i);
            List<LigerSourceToken> between = all.stream()
                    .filter(token -> token.start >= previous.end && token.end <= next.start)
                    .toList();
            if (unindexed.containsAll(between) && previous.end <= next.start && next.start <= sentence.length()) {
                text.append(sentence, previous.end, next.start).append(next.form);
            } else {
                text.append(" … ").append(next.form);
            }
        }
        int end = tokens.stream().mapToInt(token -> token.end).max().getAsInt();
        return new LigerSourceSpan(text.toString().replaceAll("\\s+", " "), tokens.get(0).start, end, tokens);
    }
}
