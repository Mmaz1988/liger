package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

public record LigerBatchVariant(String sentenceId, String syntaxVariantId, String solutionKey,
                                LinkedHashMap<String, Object> structureJson, LigerWebGraph graph,
                                String meaningConstructors, int sourceIndex) {}
