class TrapBlockCheck{
    public void test(){
        int a = 0;
        try{
            int b = 0;
            if(a == b){
                a++;
            }else {
                b++;
            }
        }catch (Exception e){
            throw new RuntimeException("error rises!");
        }
    }

    public void test2(){
        int a = 0;
        int b = 0;
        if(a == b){
            a++;
        }else {
            b++;
        }
    }
}
