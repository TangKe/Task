package ke.tang.task.executor;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public class RxJavaSingleTaskExecutor implements TaskExecutor<Single<?>> {
    private ConcurrentHashMap<TaskJob<?, Single<?>>, Disposable> mExecutingTasks = new ConcurrentHashMap<>();

    @Override
    public <Result> void execute(TaskJob<Result, Single<?>> job, TaskExecuteResult<Result> result) throws Exception {
        Single<Result> single = (Single<Result>) job.getJob();
        single.observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .subscribeOn(Schedulers.io())
                .subscribe(new RxJavaTaskObserver<>(result, job));
    }

    @Override
    public void abort(TaskJob<?, Single<?>> job) {
        final Disposable disposable = mExecutingTasks.get(job);
        if (null != disposable) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        mExecutingTasks.remove(job);
    }

    private class RxJavaTaskObserver<Result> implements SingleObserver<Result> {
        private TaskExecuteResult<Result> mExecuteResult;
        private TaskJob<Result, Single<?>> mJob;

        public RxJavaTaskObserver(TaskExecuteResult<Result> executeResult, TaskJob<Result, Single<?>> job) {
            mExecuteResult = executeResult;
            mJob = job;
        }

        @Override
        public void onSubscribe(Disposable d) {
            mExecutingTasks.put(mJob, d);
        }

        @Override
        public void onSuccess(Result result) {
            mExecuteResult.deliverResult(result);
            mExecuteResult.complete();
            mExecutingTasks.remove(mJob);
        }

        @Override
        public void onError(Throwable e) {
            mExecuteResult.deliverError(e);
            mExecuteResult.complete();
            mExecutingTasks.remove(mJob);
        }
    }
}
