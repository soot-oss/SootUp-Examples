package sootup.examples.targetcode;

public class FunctionPropagatesTaint {
    private String id(String s){
        return s;
    }

    public void entryPoint() {
        String a = "SECRET";
        String b = id(a);
        SinkClass sc = new SinkClass();
        sc.sink(b);
    }

}
