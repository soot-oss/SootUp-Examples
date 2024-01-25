package sootup.examples;

import java.util.*;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
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
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation("src/test/resources/Classhierarchy/binary"));
    inputLocations.add(new DefaultRTJarAnalysisInputLocation()); // add rt.jar

    JavaView view = new JavaView(inputLocations);

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
