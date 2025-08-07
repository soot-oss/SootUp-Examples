package sootup.examples.targetcode;

public class FunctionReturnsTaint {

    private String source(){
        return "SECRET";
    }

    private void sink(String s){

    }

    public void entryPoint() {
        String a = source();
        String b = a;
        sink(b);
    }
}
