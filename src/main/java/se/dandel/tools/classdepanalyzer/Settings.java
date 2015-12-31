package se.dandel.tools.classdepanalyzer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

public class Settings {

    private String classname;
    private String includes;
    private String outputFilename;
    protected boolean traceEnabled;
    private ClassLoader targetClassloader;

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

    public ClassLoader getTargetClassloader() {
        return targetClassloader == null ? getClass().getClassLoader() : targetClassloader;
    }

    public void setTargetClasspath(String cp) {
        try {
            String[] split = cp.split(File.pathSeparator);
            Collection<URL> urls = new ArrayList<>();
            for (String string : split) {
                urls.add(new File(string).toURI().toURL());
            }
            targetClassloader = new URLClassLoader(urls.toArray(new URL[] {}), getClass().getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
