package ke.tang.task.executor;

import android.os.AsyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public class AsyncTaskExecutor implements TaskExecutor<Callable<?>> {
    private ConcurrentHashMap<TaskJob<?, Callable<?>>, CallableAsyncTask<?>> mExecutingTasks = new ConcurrentHashMap<>();

    @Override
    public <Result> void execute(TaskJob<Result, Callable<?>> job, TaskExecuteResult<Result> result) throws Exception {
        CallableAsyncTask<Result> asyncTask = new CallableAsyncTask<Result>(result, job);
        mExecutingTasks.put(job, asyncTask);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void abort(TaskJob<?, Callable<?>> job) {
        final CallableAsyncTask<?> task = mExecutingTasks.get(job);
        if (null != task) {
            task.cancel(true);
        }
    }

    private class CallableAsyncTask<Result> extends AsyncTask<Void, Void, CallableAsyncTaskResult<Result>> {
        private TaskExecuteResult<Result> mResult;
        private TaskJob<Result, Callable<?>> mJob;

        public CallableAsyncTask(TaskExecuteResult<Result> result, TaskJob<Result, Callable<?>> job) {
            mResult = result;
            mJob = job;
        }

        @Override
        protected CallableAsyncTaskResult<Result> doInBackground(Void... callables) {
            CallableAsyncTaskResult<Result> result = new CallableAsyncTaskResult<>();
            try {
                result.mResult = (Result) mJob.getJob().call();
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    result.mError = e;
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(CallableAsyncTaskResult<Result> resultCallableAsyncTaskResult) {
            super.onPostExecute(resultCallableAsyncTaskResult);
            if (null != resultCallableAsyncTaskResult.mError) {
                mResult.deliverError(resultCallableAsyncTaskResult.mError);
            } else {
                mResult.deliverResult(resultCallableAsyncTaskResult.mResult);
            }
            mResult.complete();
            mExecutingTasks.remove(mJob);
        }

        @Override
        protected void onCancelled() {
            mExecutingTasks.remove(mJob);
        }
    }

    private class CallableAsyncTaskResult<R> {
        private Throwable mError;
        private R mResult;
    }
}
