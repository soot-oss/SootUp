class ArrayExceptions{

    public void testException(){
        Number [] arr = new Integer[5];
        //array write
        for(int i=0; i<arr.length; i++){
            arr[i] = i;
        }
        Double b = 100.01;
        arr[4] = b;
        //array load
        for(int i=0; i<arr.length; i++){
            System.out.println(arr[i]);
        }
        Number c = arr[3];

    }
}