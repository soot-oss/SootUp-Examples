package sootup.examples;

public class TypestateTestProgram {

    public static class CallSequence {
        public void foo() {}

        public void bar() {}

        public int sth() {
            return 1;
        }
    }

    public static void main(String[] args) {
        CallSequence obj = new CallSequence();
        obj.bar();
        obj.foo();

        int x = obj.sth();
    }
}

