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
}
