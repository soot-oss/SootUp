public class ArrayDefsDemos {

    // a is an array of one of two element types, depending on the branch taken
    public void storeIntoArrayOfTwoTypes() {
        Object[] a;
        if (System.nanoTime() > 0) {
            a = new String[1];
        } else {
            a = new Integer[1];
        }
        a[0] = null;
    }

    public void loadFromArrayOfTwoTypes() {
        Object[] a;
        if (System.nanoTime() > 0) {
            a = new String[1];
        } else {
            a = new Integer[1];
        }
        Object o = a[0];
        System.out.println(o);
    }
}
