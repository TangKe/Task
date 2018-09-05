package ke.tang.task;

public interface TaskHost {
    void onAttachTask(Task<?, ?> task);
}
