package ke.tang.task.executor;

import java.util.concurrent.ConcurrentHashMap;

import ke.tang.task.JobTypes;
import ke.tang.task.TaskJob;

public class TaskExecutors {
    private static ConcurrentHashMap<String, TaskExecutor<?>> sExecutors = new ConcurrentHashMap<>();

    static {
        registerTaskExecutor(JobTypes.RXJAVA_FLOWABLE, new RxJavaFlowableTaskExecutor());
        registerTaskExecutor(JobTypes.RXJAVA_OBSERVABLE, new RxJavaObservableTaskExecutor());
        registerTaskExecutor(JobTypes.RXJAVA_COMPLETABLE, new RxJavaCompletableTaskExecutor());
        registerTaskExecutor(JobTypes.RXJAVA_SINGLE, new RxJavaSingleTaskExecutor());
        registerTaskExecutor(JobTypes.RXJAVA_MAYBE, new RxJavaMaybeTaskExecutor());
        registerTaskExecutor(JobTypes.ASYNC_TASK, new AsyncTaskExecutor());
    }

    public static void registerTaskExecutor(String type, TaskExecutor<?> executor) {
        sExecutors.put(type, executor);
    }

    public static <Job> TaskExecutor<Job> getExecutor(TaskJob<?, Job> job) {
        final TaskExecutor<Job> jobTaskExecutor = (TaskExecutor<Job>) sExecutors.get(job.getType());
        if (null == jobTaskExecutor) {
            throw new IllegalArgumentException("Can not found executor to execute job");
        }
        return jobTaskExecutor;
    }
}
