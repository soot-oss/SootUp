class VolatileVariable{
    public volatile int counter = 0;

    public int increaseCounter(){
        return counter++;
    }
}