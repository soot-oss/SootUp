class Sender 
{ 
    public void send(String msg) 
    { 
        try
        { 
            Thread.sleep(100);
        } 
        catch (Exception e) 
        { 

        } 
        System.out.println(msg + "Sent"); 
    } 
} 

public class SynchronizedBlock {
    private String msg; 
    Sender sender;

    SynchronizedBlock(String string,  Sender sender)
    { 
        this.msg = string;
        this.sender = sender;
    } 
  
    public void run() 
    { 
        synchronized(sender) 
        { 
            sender.send(msg); 
        } 
    } 
} 

    
