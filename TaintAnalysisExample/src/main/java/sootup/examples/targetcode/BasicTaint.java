package sootup.examples.targetcode;

public class BasicTaint {

    static String c;

    public void entryPoint() {
        String a = "SECRET";
        String b = a;
        c = b;
        SinkClass sc = new SinkClass();
        sc.sink(c);
    }
}
