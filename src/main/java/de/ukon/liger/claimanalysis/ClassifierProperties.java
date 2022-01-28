package de.ukon.liger.claimanalysis;

public class ClassifierProperties {
    public Classifier cl;
    public String path;
    public String query;

    public ClassifierProperties() {}

    public ClassifierProperties(String cl, String path, String query) {
        this.cl = Classifier.valueOf(cl);
        this.path = path;
        this.query = query;
    }

    public ClassifierProperties(Classifier cl, String path, String query) {
        this.cl = cl;
        this.path = path;
        this.query = query;
    }

    public ClassifierProperties(ClassifierProperties cp) {
        this.cl = cp.cl;
        this.path = cp.path;
        this.query = cp.query;
    }

    public Classifier getCl() {
        return cl;
    }

    public void setCl(Classifier cl) {
        this.cl = cl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
