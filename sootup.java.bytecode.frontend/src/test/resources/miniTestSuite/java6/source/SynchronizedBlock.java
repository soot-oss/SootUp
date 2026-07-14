public class SynchronizedBlock {
    private String msg;
    SynchronizedBlock(String str)
    { 
        this.msg = str;
    } 
  
    public void run() 
    { 
        synchronized(msg)
        {
            System.out.println(msg);
        } 
    } 
} 

    
