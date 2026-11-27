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

    interface Runner {
        void run(Job job); // modeled to call job.work()
    }

    static class LibRunner implements Runner {
        public void run(Job job) {} // a library method the analysis may not follow
    }

    static class Picked implements Job {
        public void work() {}
    }

    static Job passed() {
        return new Passed();
    }

    public static void main(String[] args) {
        Job other = new NotPassed();
        new Scheduler().submit(passed()); // a Job: CHA calls the work() of every Job
        Runner runner = new LibRunner();
        runner.run(new Picked());
    }

}
