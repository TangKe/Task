package ke.tang.task.executor;

import org.reactivestreams.Subscription;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public class RxJavaTaskExecutor implements TaskExecutor<Flowable<?>> {
    private ConcurrentHashMap<TaskJob<?, Flowable<?>>, Subscription> mExecutingTasks = new ConcurrentHashMap<>();

    @Override
    public <Result> void execute(TaskJob<Result, Flowable<?>> job, TaskExecuteResult<Result> result) throws Exception {
        Flowable<Result> flowable = (Flowable<Result>) job.getJob();
        flowable.observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .subscribeOn(Schedulers.io())
                .subscribe(new RxJavaTaskSubscriber<>(result, job));
    }

    @Override
    public void abort(TaskJob<?, Flowable<?>> job) {
        final Subscription subscription = mExecutingTasks.get(job);
        if (null != subscription) {
            subscription.cancel();
        }
        mExecutingTasks.remove(job);
    }

    private class RxJavaTaskSubscriber<Result> implements FlowableSubscriber<Result> {
        private TaskExecuteResult<Result> mExecuteResult;
        private TaskJob<Result, Flowable<?>> mJob;

        public RxJavaTaskSubscriber(TaskExecuteResult<Result> executeResult, TaskJob<Result, Flowable<?>> job) {
            mExecuteResult = executeResult;
            mJob = job;
        }

        @Override
        public void onSubscribe(Subscription s) {
            s.request(1000);
            mExecutingTasks.put(mJob, s);
        }

        @Override
        public void onNext(Result o) {
            mExecuteResult.deliverResult(o);
        }

        @Override
        public void onError(Throwable t) {
            mExecuteResult.deliverError(t);
        }

        @Override
        public void onComplete() {
            mExecuteResult.complete();
            mExecutingTasks.remove(mJob);
        }
    }
}
