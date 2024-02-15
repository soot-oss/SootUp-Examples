package sootup.examples;

import java.util.*;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
//import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * The test files for this example consist of a class hierarchy as follows:
 *
 * <pre>
 *         |-- B
 *    A <--|
 *         |-- C <-- D
 *  </pre>
 *
 * This code example will show you how to build and examine a class hierarchy using sootup.
 */
public class ClassHierarchy {

  public static void main(String[] args) {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
//    JavaLanguage language = new JavaLanguage(8);
//    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
//    inputLocations.add(
//        new JavaClassPathAnalysisInputLocation("src/test/resources/Classhierarchy/binary"));
//    inputLocations.add(new DefaultRTJarAnalysisInputLocation()); // add rt.jar

//    JavaProject project = JavaProject.builder(language).addInputLocation((AnalysisInputLocation<JavaSootClass>) inputLocations).build();
//    JavaView view = project.createView();


    JavaLanguage language = new JavaLanguage(8);
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/Classhierarchy/binary"));

    // Assuming Java 8 or earlier, adjust for later versions
    String javaHome = System.getProperty("java.home");
    String rtJarPath = javaHome + "/lib/rt.jar"; // Adjust this path for Java 9 and later
    inputLocations.add(new JavaClassPathAnalysisInputLocation(rtJarPath));

    JavaProject project = JavaProject.builder(language)
            .addInputLocation(inputLocations.get(0))
            .addInputLocation(inputLocations.get(1))
            .build();
    JavaView view = project.createView();

    // Create type hierarchy
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

    // Specify class types we want to receive information about
    JavaClassType clazzTypeA = JavaIdentifierFactory.getInstance().getClassType("A");
    JavaClassType clazzTypeC = JavaIdentifierFactory.getInstance().getClassType("C");

    // Check direct subtypes
    Set<ClassType> subtypes = typeHierarchy.directSubtypesOf(clazzTypeC);
    boolean allSubtypesAreD = subtypes.stream().allMatch(type -> type.getClassName().equals("D"));
    boolean allSubtypesFullyQualifiedAreD =
        subtypes.stream().allMatch(type -> type.getFullyQualifiedName().equals("D"));

    if (allSubtypesAreD && allSubtypesFullyQualifiedAreD) {
      System.out.println("All direct subtypes of Class C are correctly identified as Class D.");
    } else {
      System.out.println("Direct subtypes of Class C are not correctly identified.");
    }

    // Examine super types
    List<ClassType> superClasses = typeHierarchy.superClassesOf(clazzTypeC);
    if (superClasses.equals(
        Arrays.asList(
            clazzTypeA, JavaIdentifierFactory.getInstance().getClassType("java.lang.Object")))) {
      System.out.println("Superclasses of Class C are correctly identified.");
    } else {
      System.out.println("Superclasses of Class C are not correctly identified.");
    }
  }


}
