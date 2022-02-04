package de.ukon.liger.webservice.rest;

import de.ukon.liger.claimanalysis.ClassifierRuleMapping;
import de.ukon.liger.cuepaq.claimanalysis.ClaimAnalysis;
import de.ukon.liger.cuepaq.claimanalysis.Classifier;
import de.ukon.liger.cuepaq.claimanalysis.ClassifierProperties;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.webservice.rest.dtos.ClassifierRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/rules")
public class RuleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleController.class);

    @RequestMapping("/")
    public ClassifierRuleMapping getRules() throws IOException {
        return ClaimAnalysis.readClassifierRuleMapping();
    }

    @RequestMapping("/save")
    public boolean savevRules(ClassifierRules[] rules) throws IOException {
        Map<Classifier, ClassifierProperties> ruleMap = new HashMap<>();
//        Arrays.stream(rules).forEach(r -> ruleMap.put(r.getCl(), new ClassifierProperties(r.cl, r.path, r.query)));
        final ClassifierRuleMapping classifierRuleMapping = ClaimAnalysis.readClassifierRuleMapping();
        classifierRuleMapping.setClassifiers(ruleMap);
        ClaimAnalysis.writeClassifierMap(classifierRuleMapping);
        return true;
    }
}
