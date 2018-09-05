package ke.tang.task;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static ke.tang.task.Task.LOG_TAG;


public class TaskExecuteResult<Result> {
    private Queue<Result> mPendingResults = new LinkedList<>();
    private Queue<Throwable> mPendingErrors = new LinkedList<>();

    private Task<Result, ?> mTask;

    private boolean mPendingComplete;

    TaskExecuteResult(Task<Result, ?> task) {
        mTask = task;
    }

    void setTask(Task<Result, ?> task) {
        mTask = task;
    }

    public void deliverError(Throwable error) {
        if (mTask.isResumed()) {
            final OnTaskErrorListener errorListener = mTask.getErrorListener();
            if (null != errorListener) {
                errorListener.onTaskError(error);
            }
        } else {
            mPendingErrors.offer(error);
        }
    }

    public void deliverResult(Result result) {
        if (mTask.isResumed()) {
            final OnTaskResultListener<? super Result> resultListener = mTask.getResultListener();
            if (null != resultListener) {
                resultListener.onResult(result);
            }
        } else {
            mPendingResults.offer(result);
        }
    }

    /**
     * Should always call this method
     */
    public void complete() {
        if (!hasPendingResults() && !hasPendingErrors()) {
            Log.i(LOG_TAG, "Task@" + mTask.hashCode() + " complete");
            mTask.stop();
        } else {
            Log.i(LOG_TAG, "Task@" + mTask.hashCode() + " complete, but has pending results, will re deliver results to listener when Task next resumed");
            mPendingComplete = true;
        }
    }

    public void deliverAllPending() {
        if (mTask.isResumed()) {
            if (hasPendingResults()) {
                Log.i(LOG_TAG, "Deliver all pending results to Task@" + mTask.hashCode());
                final Iterator<Result> iterator = mPendingResults.iterator();
                while (iterator.hasNext()) {
                    final Result result = iterator.next();
                    deliverResult(result);
                    iterator.remove();
                }
            }
            if (hasPendingErrors()) {
                Log.i(LOG_TAG, "Deliver all pending errors to Task@" + mTask.hashCode());
                final Iterator<Throwable> iterator = mPendingErrors.iterator();
                while (iterator.hasNext()) {
                    final Throwable result = iterator.next();
                    deliverError(result);
                    iterator.remove();
                }
            }

            if (mPendingComplete && !hasPendingResults() && !hasPendingErrors()) {
                Log.i(LOG_TAG, "All pending results and errors were delivered to Task@" + mTask.hashCode() + ", complete it now");
                complete();
            }
        }
    }

    private boolean hasPendingResults() {
        return mPendingResults.size() != 0;
    }

    private boolean hasPendingErrors() {
        return mPendingErrors.size() != 0;
    }

    public void clear() {
        mPendingResults.clear();
        mPendingErrors.clear();
    }

}
