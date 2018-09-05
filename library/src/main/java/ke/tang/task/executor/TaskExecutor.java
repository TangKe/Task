package ke.tang.task.executor;

import ke.tang.task.TaskExecuteResult;
import ke.tang.task.TaskJob;

public interface TaskExecutor<Job> {
    <Result> void execute(TaskJob<Result, Job> job, TaskExecuteResult<Result> result) throws Exception;

    void abort(TaskJob<?, Job> job);
}
