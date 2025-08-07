package sootup.examples.targetcode;

public class BasicTaintSanitized {

    public void entryPoint() {
        String a = "SECRET";
        String b = a;
        b = "...";
        SinkClass sc = new SinkClass();
        sc.sink(b);
    }
}
