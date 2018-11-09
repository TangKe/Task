package ke.tang.task.executor;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public class RxJavaMaybeTaskExecutor implements TaskExecutor<Maybe<?>> {
    private ConcurrentHashMap<TaskJob<?, Maybe<?>>, Disposable> mExecutingTasks = new ConcurrentHashMap<>();

    @Override
    public <Result> void execute(TaskJob<Result, Maybe<?>> job, TaskExecuteResult<Result> result) throws Exception {
        Maybe<Result> maybe = (Maybe<Result>) job.getJob();
        maybe.observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .subscribeOn(Schedulers.io())
                .subscribe(new RxJavaTaskObserver<>(result, job));
    }

    @Override
    public void abort(TaskJob<?, Maybe<?>> job) {
        final Disposable disposable = mExecutingTasks.get(job);
        if (null != disposable) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        mExecutingTasks.remove(job);
    }

    private class RxJavaTaskObserver<Result> implements MaybeObserver<Result> {
        private TaskExecuteResult<Result> mExecuteResult;
        private TaskJob<Result, Maybe<?>> mJob;

        public RxJavaTaskObserver(TaskExecuteResult<Result> executeResult, TaskJob<Result, Maybe<?>> job) {
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
        }

        @Override
        public void onError(Throwable e) {
            mExecuteResult.deliverError(e);
            mExecuteResult.complete();
            mExecutingTasks.remove(mJob);
        }

        @Override
        public void onComplete() {
            mExecuteResult.complete();
            mExecutingTasks.remove(mJob);
        }
    }
}
