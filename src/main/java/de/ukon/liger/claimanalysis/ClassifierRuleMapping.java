package de.ukon.liger.claimanalysis;

import de.ukon.liger.cuepaq.claimanalysis.Classifier;
import de.ukon.liger.cuepaq.claimanalysis.ClassifierProperties;

import java.util.Map;

public class ClassifierRuleMapping {
    String name;
    String version;
    Map<Classifier, ClassifierProperties> classifiers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<Classifier, ClassifierProperties> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(Map<Classifier, ClassifierProperties> classifiers) {
        this.classifiers = classifiers;
    }
}
