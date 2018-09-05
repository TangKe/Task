package ke.tang.task;

public interface Progress {
    void show(Task owner);

    void hide();

    void setOnProgressCancelListener(OnProgressCancelListener listener);
}
