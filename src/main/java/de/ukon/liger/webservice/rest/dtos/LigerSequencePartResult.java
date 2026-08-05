package de.ukon.liger.webservice.rest.dtos;

public record LigerSequencePartResult(int sourceIndex, String sentenceId, String syntaxVariantId,
                                      String solutionKey, String meaningConstructors, int sourceIndexOffset,
                                      String rootId) {}
