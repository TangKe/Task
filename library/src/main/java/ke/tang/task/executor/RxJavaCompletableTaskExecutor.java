package ke.tang.task.executor;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public class RxJavaCompletableTaskExecutor implements TaskExecutor<Completable> {
    private ConcurrentHashMap<TaskJob<?, Completable>, Disposable> mExecutingTasks = new ConcurrentHashMap<>();

    @Override
    public <Result> void execute(TaskJob<Result, Completable> job, TaskExecuteResult<Result> result) throws Exception {
        Completable maybe = job.getJob();
        maybe.observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .subscribeOn(Schedulers.io())
                .subscribe(new RxJavaTaskObserver(result, job));
    }

    @Override
    public void abort(TaskJob<?, Completable> job) {
        final Disposable disposable = mExecutingTasks.get(job);
        if (null != disposable) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        mExecutingTasks.remove(job);
    }

    private class RxJavaTaskObserver<Result> implements CompletableObserver {
        private TaskExecuteResult<Result> mExecuteResult;
        private TaskJob<Result, Completable> mJob;

        public RxJavaTaskObserver(TaskExecuteResult<Result> executeResult, TaskJob<Result, Completable> job) {
            mExecuteResult = executeResult;
            mJob = job;
        }

        @Override
        public void onSubscribe(Disposable d) {
            mExecutingTasks.put(mJob, d);
        }

        @Override
        public void onComplete() {
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
