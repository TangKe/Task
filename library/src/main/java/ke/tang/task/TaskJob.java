package ke.tang.task;

import java.util.concurrent.Callable;

import io.reactivex.Flowable;

public class TaskJob<Result, Job> {
    private Job mJob;
    private String mType;

    public TaskJob(Job job, String type) {
        mJob = job;
        mType = type;
    }

    public Job getJob() {
        return mJob;
    }

    public String getType() {
        return mType;
    }

    public static <Result> TaskJob<Result, Flowable<Result>> createRxJavaJob(Flowable<Result> flowable) {
        return new TaskJob<>(flowable, JobTypes.RXJAVA);
    }

    public static <Result> TaskJob<Result, Callable<Result>> createAsyncTaskJob(Callable<Result> callback) {
        return new TaskJob<>(callback, JobTypes.ASYNC_TASK);
    }
}
