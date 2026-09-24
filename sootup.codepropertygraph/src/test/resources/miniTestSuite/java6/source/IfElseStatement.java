
/** @author Hasitha Rajapakse */



public class IfElseStatement {

    public int ifStatement(int a){
        int val = 0;
        if(a < 42){
            val = 1;
        }
        return val;
    }

    public int ifElseStatement(int a){
        int val = 0;
        if(a < 42){
            val = 1;
        }else{
            val = 2;
        }
        return val;
    }

    public int ifElseIfStatement(int a){
        int val = 0;
        if(a < 42){
            val = 1;
        }else if( a > 123){
            val = 2;
        }else{
            val = 3;
        }
        return val;
    }

    public int ifElseCascadingStatement(int a){
        int val = 0;
        if(a < 42){
            if(a < 42){
                val = 11;
            }else{
                val = 12;
            }
        }else{
            val = 3;
        }
        return val;
    }

    public int ifElseCascadingInElseStatement(int a){
        int val = 0;
        if(a < 42){
            val = 1;
        }else{
            if(a < 42){
                val = 21;
            }else{
                val = 22;
            }
        }
        return val;
    }


    public int ifElseCascadingElseIfStatement(int a){
        int val = 0;
        if(a < 42){
            if(a < 42){
                val = 11;
            }else if(a > 123){
                val = 12;
            }else{
                val = 13;
            }
        }else{
            val = 2;
        }
        return val;
    }

    public int ifElseCascadingElseIfInElseStatement(int a){
        int val = 0;
        if(a < 42){
            val = 1;
        }else{
            if(a < 42){
                val = 21;
            }else if(a > 123){
                val = 22;
            }else{
                val = 23;
            }
        }
        return val;
    }

}
