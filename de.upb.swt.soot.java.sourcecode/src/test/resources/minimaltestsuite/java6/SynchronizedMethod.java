public class Sender 
{ 
    public void send(String msg) 
    { 
        try
        { 
            Thread.sleep(1000); 
        } 
        catch (Exception e) 
        { 
            System.out.println("Thread  interrupted."); 
        } 
        System.out.println(msg + "Sent"); 
    } 
} 

public class SynchronizedMethod {
    private String msg; 
    Sender sender;
  
    SynchronizedMethod(String m,  Sender obj) 
    { 
        msg = m; 
        sender = obj; 
    } 
  
    public void run() 
    { 
        synchronized(sender) 
        { 
            sender.send(msg); 
        } 
    } 
} 

    
