package se.dandel.tools.classdepanalyzer;

public class Settings {

    private String classname;
    private String includes;
    private String outputFilename;
    protected boolean traceEnabled;

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getClassname() {
        return classname;
    }

    public String getIncludes() {
        return includes;
    }

    public String getOutputFilename() {
        return outputFilename;
    }
}
