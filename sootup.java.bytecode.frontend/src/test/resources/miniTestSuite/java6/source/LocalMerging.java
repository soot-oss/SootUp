public class LocalMerging {
    public void localMergingWithConstant(int n) {
        String a = "one";
        // The branch returns either a local or a constant.
        // Because of the divergence neither `a` nor `"two"` should be inlined,
        // but a stack local variable should be created for holding the result of the branch.
        System.out.println(n == 1 ? a : "two");
    }

    public void localMergingWithOtherLocal(int n) {
        String a = "one";
        String b = "two";
        // The branch returns either a local or a different local.
        // Because of the divergence neither `a` nor `b` should be inlined,
        // but a stack local variable should be created for holding the result of the branch.
        System.out.println(n == 1 ? a : b);
    }

    public void localMergingWithDuplicateValue(int n) {
        String a = "one";
        // One of the branches for the first argument contains the constant "two"
        // and the second argument also contains the constant "two".
        // This test ensures that when the first argument gets replaced by a stack local,
        // the second argument isn't replaced as well.
        System.setProperty(n == 1 ? a : "two", "two");
    }
}
