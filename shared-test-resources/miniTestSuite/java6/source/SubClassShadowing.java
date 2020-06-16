public class SubClassShadowing extends SuperClass{

    String info = "sub";

    public void printInfo(String info){

        System.out.println(info);
        System.out.println(this.info);
        System.out.println(super.info);
    }

    public void testls(){
        /**
        int i  = 0;
        if (i>0){
            if(i>2) {
                i = i + 0;
            }else{
                i = i + 1;
            }
        }else{
            i = i +2;
        }*/
        int i  = 0;
        while(i<10){
            i = i +1;
            i = i +1;
        }
    }

}