class Issue911_Aggregator {
    void foo(String len) {
        byte[] arr;
        try {
            arr = len.getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (byte a : arr) {}
    }
}