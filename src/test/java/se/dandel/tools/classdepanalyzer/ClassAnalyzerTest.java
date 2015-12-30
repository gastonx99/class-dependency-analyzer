package se.dandel.tools.classdepanalyzer;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassAnalyzerTest {

    @Mock
    private DependencyTracker tracker;

    @Mock
    private Settings settings;

    @Mock
    private PlantUMLWriter writer;

    @InjectMocks
    private ClassAnalyzer analyzer;

    @Captor
    private ArgumentCaptor<List<ClassDefinition>> definitionsCaptor;

    @Captor
    private ArgumentCaptor<List<ClassPackage>> packagesCaptor;

    @Before
    public void before() {
        when(settings.getIncludes()).thenReturn("org.acme");
    }

    @Test
    public void oneClassNode() throws Exception {
        String classname = "org.acme.foo.ApexPredator";
        ClassTreeNode root = createNode(classname);
        when(tracker.track()).thenReturn(root);

        analyzer.analyze();

        verify(writer).write(any(ClassTreeNode.class), packagesCaptor.capture(), definitionsCaptor.capture());

        assertContains(definitionsCaptor.getValue(), classname);
        assertContainsPackage(packagesCaptor.getValue(), getPackagename(classname));
    }

    private void assertContainsPackage(List<ClassPackage> packages, String packagename) {
        for (ClassPackage packaze : packages) {
            if (packagename.equals(packaze.getName())) {
                return;
            }
        }
        fail("Expected " + packagename + " in " + packages);
    }

    private void assertContains(List<ClassDefinition> definitions, String classname) {
        for (ClassDefinition definition : definitions) {
            if (classname.equals(definition.getClassname())) {
                return;
            }
        }
        fail("Expected " + classname + " in " + definitions);
    }

    private ClassTreeNode createNode(String classname) {
        return new ClassTreeNode(null, createDefinition(classname));
    }

    private ClassDefinition createDefinition(String classname) {
        ClassDefinition definition = mock(ClassDefinition.class);
        when(definition.getClassname()).thenReturn(classname);
        when(definition.getPackagename()).thenReturn(getPackagename(classname));
        when(definition.toString()).thenReturn(classname);
        return definition;
    }

    private String getPackagename(String classname) {
        return classname.substring(0, classname.lastIndexOf("."));
    }

}
