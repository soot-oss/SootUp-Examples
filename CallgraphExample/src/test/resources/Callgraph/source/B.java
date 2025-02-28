public class B {
  public void calc(A a) {
    a.calc(1); // can be A.calc, B.calc, C.calc
  }
  public void foo(int x) {
    // conditional instantiation check
    A a = new A();
    a.bar();
    System.out.println("Foo");
  }
}