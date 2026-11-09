class LocalNameCollisionsWithDebugNamesAndScopes {
    void foo(){
        // same Variable names In different scopes having different local indices should not result in the same Local (-name)
        {
            int candidate = 42;
            System.out.println(candidate);
        }
        {
            int theresAnother = 666;
            String candidate = "banana";
            System.out.println(candidate);
            System.out.println(theresAnother);
        }
    }

    void bar(){
        // same Variable names In different scopes having same local indices should not result in the same Local
        // -> use scopes to determine valid regions - only exists when debug info is available
        {
            int candidateA = 42;
            System.out.println(candidateA);
        }
        {
            String candidateB = "banana";
            System.out.println(candidateB);
        }
    }
}