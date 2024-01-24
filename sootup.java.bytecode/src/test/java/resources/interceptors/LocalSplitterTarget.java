public class LocalSplitterTarget{

    void case0(){
        int a = 0;
        int b = 1;
        a = b + 1;
        b = a + 1;
    }

    void case1(){
        int a = 0;
        int b = 1;
        a = a + 1;
        b  = b + 1;
    }

    int case2(){
        int a = 0;
        if(a<0){
            a = a + 1;
        } else {
            a = a - 1;
            a = a + 2;
        } 
        return a;
    }

    int case3(){
        int a = 0;
        if(a<0){
            a = a + 1;
            a = a + 2;
            a = a + 3;
        } else {
            a = a - 1;
            a = a - 2;
            a = a - 3;
        } 
        return a;
    }

    int case4(){
        int a = 0;
        if(a<0){
            a = a + 1;
            a = a + 2;
        } else {
            a = a - 1;
            a = a - 2;
        } 

        if(a>1){
            a = a + 3;
            a = a + 5;
        } else {
            a = a - 3;
            a = a - 5;
        } 

        return a;
    }

    int case5(){
        int a = 0;
        if(a<0){
            a = a + 1;
            a = a + 2;
        } else if(a<5) {
            a = a - 1;
            a = a - 2;
        } else{
            a = a * 1;
            a = a * 2;
        }
        
        return a;
    }

    int case6(){
        int a = 0;
        for(int i = 0; i < 10; i++){
            i = i + 1;
            a++;
        }
        return a;
    }

}