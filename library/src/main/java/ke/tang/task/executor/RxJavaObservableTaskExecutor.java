package ke.tang.task.executor;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public class RxJavaObservableTaskExecutor implements TaskExecutor<TaskExecutor<Observable<?>>> {
    private ConcurrentHashMap<TaskJob<?, Observable<?>>, Disposable> mExecutingTasks = new ConcurrentHashMap<>();

    @Override
    public <Result> void execute(TaskJob<Result, TaskExecutor<Observable<?>>> job, TaskExecuteResult<Result> result) throws Exception {
        Observable<Result> flowable = (Observable<Result>) job.getJob();
        flowable.observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .subscribeOn(Schedulers.io())
                .subscribe(new RxJavaTaskObserver(result, job));
    }

    @Override
    public void abort(TaskJob<?, TaskExecutor<Observable<?>>> job) {
        final Disposable disposable = mExecutingTasks.get(job);
        if (null != disposable) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        mExecutingTasks.remove(job);
    }

    private class RxJavaTaskObserver<Result> implements Observer<Result> {
        private TaskExecuteResult<Result> mExecuteResult;
        private TaskJob<Result, Observable<?>> mJob;

        public RxJavaTaskObserver(TaskExecuteResult<Result> executeResult, TaskJob<Result, Observable<?>> job) {
            mExecuteResult = executeResult;
            mJob = job;
        }

        @Override
        public void onSubscribe(Disposable d) {
            mExecutingTasks.put(mJob, d);
        }

        @Override
        public void onNext(Result result) {
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
