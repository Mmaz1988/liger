package de.ukon.liger.cuepaq.claimanalysis;

public class ClassifierProperties {
    public Classifier cl;
    public String path;
    public String query;

    public ClassifierProperties(String cl, String path, String query) {
        this.cl = Classifier.valueOf(cl);
        this.path = path;
        this.query = query;


    }
}
