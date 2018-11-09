package ke.tang.task;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

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
        return new TaskJob<>(flowable, JobTypes.RXJAVA_FLOWABLE);
    }

    public static <Result> TaskJob<Result, Observable<Result>> createRxJavaJob(Observable<Result> observable) {
        return new TaskJob<>(observable, JobTypes.RXJAVA_OBSERVABLE);
    }

    public static <Result> TaskJob<Result, Maybe<Result>> createRxJavaJob(Maybe<Result> maybe) {
        return new TaskJob<>(maybe, JobTypes.RXJAVA_MAYBE);
    }

    public static <Result> TaskJob<Result, Single<Result>> createRxJavaJob(Single<Result> single) {
        return new TaskJob<>(single, JobTypes.RXJAVA_SINGLE);
    }

    public static TaskJob<?, Comparable> createRxJavaJob(Completable completable) {
        return new TaskJob(completable, JobTypes.RXJAVA_COMPLETABLE);
    }

    public static <Result> TaskJob<Result, Callable<Result>> createAsyncTaskJob(Callable<Result> callback) {
        return new TaskJob<>(callback, JobTypes.ASYNC_TASK);
    }
}
