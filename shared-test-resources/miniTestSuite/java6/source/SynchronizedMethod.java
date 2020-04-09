class SenderMethod
{
    public synchronized void send(String msg)
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {

        }
        System.out.println(msg + "Sent");
    }
}

public class SynchronizedMethod {
    private String msg;
    SenderMethod sender;

    SynchronizedMethod(String m,  SenderMethod obj)
    {
        msg = m;
        sender = obj;
    }

    public void run()
    {
        sender.send(msg);
    }
}


