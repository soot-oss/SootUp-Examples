package sootup.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import org.eclipse.core.internal.resources.Project;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.Project;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.VoidType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
//import sootup.java.core.JavaProject;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class CallgraphExample {

  public static void main(String[] args) {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory


    JavaLanguage language = new JavaLanguage(8);
    List<AnalysisInputLocation<JavaSootClass>> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/Callgraph/binary"));
    inputLocations.add(new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "/lib/rt.jar"));

    // Assuming the correct way to initialize a JavaProject with multiple AnalysisInputLocation instances
    JavaProject project = JavaProject.builder(language)
            .addInputLocation(inputLocations.get(0)) // Add the first location
            .addInputLocation(inputLocations.get(1)) // Add the second location
            .build();

    JavaView view = project.createView();

    // Get a MethodSignature
    JavaClassType classTypeA = (JavaClassType) view.getIdentifierFactory().getClassType("A");
    JavaClassType classTypeB = (JavaClassType) view.getIdentifierFactory().getClassType("B");
    MethodSignature entryMethodSignature =
            JavaIdentifierFactory.getInstance()
                    .getMethodSignature(
                            classTypeB,
                            JavaIdentifierFactory.getInstance()
                                    .getMethodSubSignature(
                                            "calc", VoidType.getInstance(), Collections.singletonList(classTypeA)));

    // Create type hierarchy and CHA
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    System.out.println(typeHierarchy.subclassesOf(classTypeA));
    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);

    // Create CG by initializing CHA with entry method(s)
    CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));

    cg.callsFrom(entryMethodSignature).forEach(System.out::println);
  }


}
