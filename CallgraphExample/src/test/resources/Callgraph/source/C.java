public class C extends A {
  public int calc(int a) {
    B b = new B();
    b.foo(a);
    return a + 2;
  }

  public static void main(String[] args) {
    C c = new C();
    c.calc(1);
  }
}