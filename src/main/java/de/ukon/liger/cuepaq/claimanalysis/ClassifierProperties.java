package de.ukon.liger.cuepaq.claimanalysis;

public class ClassifierProperties {
    public Classifier cl;
    public String rules;
    public String query;

    public ClassifierProperties() {}

    public ClassifierProperties(String cl, String rules, String query) {
        this.cl = Classifier.valueOf(cl);
        this.rules = rules;
        this.query = query;
    }

    public ClassifierProperties(Classifier cl, String rules, String query) {
        this.cl = cl;
        this.rules = rules;
        this.query = query;
    }

    public ClassifierProperties(ClassifierProperties cp) {
        this.cl = cp.cl;
        this.rules = cp.rules;
        this.query = cp.query;
    }

    public Classifier getCl() {
        return cl;
    }

    public void setCl(Classifier cl) {
        this.cl = cl;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
