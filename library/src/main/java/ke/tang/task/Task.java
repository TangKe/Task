package ke.tang.task;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.UUID;

import ke.tang.task.executor.TaskExecutors;

public class Task<Result, Job> extends Retain<TaskExecuteResult<Result>> implements OnProgressCancelListener {
    private static final String EXTRA_UUID = "UUID";
    private static HashMap<String, TaskState<?, ?>> sCachedStates = new HashMap<>();

    final static String LOG_TAG = "Task";
    private OnTaskResultListener<? super Result> mResultListener;
    private OnTaskErrorListener mErrorListener;
    private OnTaskCompleteListener mCompleteListener;

    private TaskJob<Result, Job> mJob;
    private boolean mIsExecuted;
    private boolean mIsStateSaved;

    @Nullable
    private Progress mProgress;
    private boolean mShowProgress;
    private String mUUID;

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskErrorListener errorListener,
                                                        OnTaskCompleteListener completeListener,
                                                        boolean showProgress,
                                                        Progress optionalProgress) {
        Task<Result, Job> task = new Task<>();
        task.setResultListener(resultListener);
        task.setErrorListener(errorListener);
        task.setCompleteListener(completeListener);
        if (null == host) {
            throw new IllegalArgumentException("You must provide a TaskHost instance");
        }
        task.setJob(job);
        task.setShowProgress(showProgress);
        task.setProgress(optionalProgress);
        host.onAttachTask(task);
        return task;
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskErrorListener errorListener,
                                                        OnTaskCompleteListener completeListener,
                                                        boolean showProgress) {
        return start(host, job, resultListener, errorListener, completeListener, showProgress, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskErrorListener errorListener,
                                                        OnTaskCompleteListener completeListener) {
        return start(host, job, resultListener, errorListener, completeListener, false, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskCompleteListener completeListener,
                                                        boolean showProgress) {
        return start(host, job, resultListener, null, completeListener, showProgress, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskCompleteListener completeListener) {
        return start(host, job, resultListener, null, completeListener, false, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskErrorListener errorListener,
                                                        boolean showProgress) {
        return start(host, job, resultListener, errorListener, null, showProgress, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        OnTaskErrorListener errorListener) {
        return start(host, job, resultListener, errorListener, null, false, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener,
                                                        boolean showProgress) {
        return start(host, job, resultListener, null, null, showProgress, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskResultListener<? super Result> resultListener) {
        return start(host, job, resultListener, null, null, false, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskCompleteListener completeListener,
                                                        boolean showProgress) {
        return start(host, job, null, null, completeListener, showProgress, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        OnTaskCompleteListener completeListener) {
        return start(host, job, null, null, completeListener, false, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job,
                                                        boolean showProgress) {
        return start(host, job, null, null, null, showProgress, null);
    }

    public static <Result, Job> Task<Result, Job> start(TaskHost host,
                                                        TaskJob<Result, Job> job) {
        return start(host, job, null, null, null, false, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState && null == mUUID) {
            //assign a uuid to this task when first create
            mUUID = UUID.randomUUID().toString();
            setData(new TaskExecuteResult<>(this));
        } else if (null != savedInstanceState && null == mUUID) {
            //because of the activity killed and recreate will not call onRetainNonConfigurationInstance method, setRetainInstance not effect
            mUUID = savedInstanceState.getString(EXTRA_UUID);
            TaskState<Result, Job> taskState = (TaskState<Result, Job>) sCachedStates.get(mUUID);
            if (null != taskState) {
                taskState.restore(this);
                if (null != mProgress) {
                    mProgress.setOnProgressCancelListener(this);
                }
                getData().setTask(this);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            mIsStateSaved = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsExecuted) {
            execute();
        }
        final TaskExecuteResult<Result> data = getData();
        if (null != data) {
            data.deliverAllPending();
        }
    }

    private void setShowProgress(boolean showProgress) {
        mShowProgress = showProgress;
    }

    private void setProgress(Progress progress) {
        mProgress = progress;
    }

    private void execute() {
        try {
            if (mShowProgress) {
                if (null == mProgress) {
                    mProgress = new DefaultProgress();
                }
                mProgress.setOnProgressCancelListener(this);
                mProgress.show(this);
            }
            mIsExecuted = true;
            Log.i(LOG_TAG, "Task@" + hashCode() + " start to execute");
            TaskExecutors.getExecutor(mJob).execute(mJob, getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_UUID, mUUID);
        TaskState<Result, Job> taskState = (TaskState<Result, Job>) sCachedStates.get(mUUID);
        if (null == taskState) {
            taskState = new TaskState<>();
        }
        taskState.save(this);
        sCachedStates.put(mUUID, taskState);
        mIsStateSaved = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mIsStateSaved) {
            TaskExecutors.getExecutor(mJob).abort(mJob);
            getData().clear();
            if (null != mProgress) {
                mProgress.hide();
            }
            sCachedStates.remove(mUUID);
            Log.i(LOG_TAG, "Task@" + hashCode() + " was destroyed");
        } else {
            Log.i(LOG_TAG, "Task@" + hashCode() + " was destroyed, but will be restored soon");
        }
    }

    private void setResultListener(OnTaskResultListener<? super Result> resultListener) {
        mResultListener = resultListener;
    }

    private void setErrorListener(OnTaskErrorListener errorListener) {
        mErrorListener = errorListener;
    }

    private void setCompleteListener(OnTaskCompleteListener completeListener) {
        mCompleteListener = completeListener;
    }

    OnTaskResultListener<? super Result> getResultListener() {
        return mResultListener;
    }

    OnTaskErrorListener getErrorListener() {
        return mErrorListener;
    }


    OnTaskCompleteListener getCompleteListener() {
        return mCompleteListener;
    }

    private void setJob(TaskJob<Result, Job> job) {
        mJob = job;
    }

    void stop() {
        final Fragment parentFragment = getParentFragment();
        final FragmentActivity activity = getActivity();
        if (null != parentFragment) {
            Log.i(LOG_TAG, "Remove Task@" + hashCode() + " from Fragment@" + parentFragment.hashCode());
            parentFragment.getChildFragmentManager().beginTransaction().remove(this).commit();
        } else if (null != activity) {
            Log.i(LOG_TAG, "Remove Task@" + hashCode() + " from Activity@" + activity.hashCode());
            activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onCancel() {
        Log.i(LOG_TAG, "Task@" + hashCode() + " was canceled by user");
        stop();
    }

    private static class TaskState<Result, Job> {
        private WeakReference<OnTaskResultListener<? super Result>> mResultListener;
        private WeakReference<OnTaskErrorListener> mErrorListener;
        private WeakReference<OnTaskCompleteListener> mOnTaskCompleteListener;
        private TaskJob<Result, Job> mJob;
        private boolean mIsExecuted;
        private Progress mProgress;
        private boolean mShowProgress;
        private TaskExecuteResult<Result> mResult;

        void restore(Task<Result, Job> task) {
            task.mResultListener = mResultListener.get();
            task.mErrorListener = mErrorListener.get();
            task.mCompleteListener = mOnTaskCompleteListener.get();
            task.mJob = mJob;
            task.mIsExecuted = mIsExecuted;
            task.mProgress = mProgress;
            task.mShowProgress = mShowProgress;
            task.setData(mResult);
        }

        void save(Task<Result, Job> task) {
            mErrorListener = new WeakReference<>(task.mErrorListener);
            mResultListener = new WeakReference<>(task.mResultListener);
            mOnTaskCompleteListener = new WeakReference<>(task.mCompleteListener);
            mResult = task.getData();
            mIsExecuted = task.mIsExecuted;
            mJob = task.mJob;
            mProgress = task.mProgress;
            mShowProgress = task.mShowProgress;
        }
    }
}
