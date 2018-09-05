package ke.tang.task;

/**
 * Listener for retrieve task execute error
 */
public interface OnTaskErrorListener {
    /**
     * Called when task execute error occur, may call multi times
     *
     * @param e
     */
    void onTaskError(Throwable e);
}
