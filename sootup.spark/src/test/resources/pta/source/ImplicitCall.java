public class ImplicitCall {

    interface Job {
        void work();
    }

    static class Passed implements Job {
        public void work() {}
    }

    static class NotPassed implements Job {
        public void work() {}
    }

    static class Scheduler {
        void submit(Job job) {} // modeled to call job.work()
    }

    public static void main(String[] args) {
        Job other = new NotPassed();
        new Scheduler().submit(new Passed());
    }

}
