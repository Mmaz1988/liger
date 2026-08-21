package de.ukon.liger.webservice.rest.dtos;


import java.util.HashMap;
import java.util.List;

/**
 * The pre-Stage-4 batch shape: ONE {@link LigerRuleAnnotation} per sentence, with every
 * syntactic analysis's meaning constructors concatenated and no structure of its own.
 *
 * Kept only for {@code /multistage_to_batch} and {@code /apply_rules_to_dependency_batch},
 * which have genuinely different semantics from the XLE batch parse and no test coverage
 * here. {@code /apply_rules_to_batch} moved to {@link LigerBatchParsingAnalysis}, whose
 * per-sentence payload is exactly what {@code /apply_rules_xle} returns.
 *
 * If either of these two endpoints stays in use, it should move to the same shape rather
 * than keeping this one alive -- see xleplusglue/docs/plans/SHARED_PIPELINE_PLAN.md,
 * invariant I6.
 */
public class LigerRuleAnnotationBatchAnalysis {
    public HashMap<String,LigerRuleAnnotation> annotations;
    public List<LigerGraphComponent> ruleApplicationGraph;
    public String report;

    public LigerRuleAnnotationBatchAnalysis(HashMap<String,LigerRuleAnnotation> annotations,
                                            List<LigerGraphComponent> ruleApplicationGraph,
                                            String report) {
        this.annotations = annotations;
        this.ruleApplicationGraph = ruleApplicationGraph;
        this.report = report;
    }
}
