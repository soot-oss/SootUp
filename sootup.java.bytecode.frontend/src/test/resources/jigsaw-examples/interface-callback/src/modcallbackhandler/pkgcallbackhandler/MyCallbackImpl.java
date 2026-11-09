package pkgcallbackhandler;

import pkgcallee.ICallback;

public class MyCallbackImpl implements ICallback {
    @Override
    public void doCallback(String message)  {
        System.out.println("MyCallbackImpl: Received callback with message: " + message);
    }
}