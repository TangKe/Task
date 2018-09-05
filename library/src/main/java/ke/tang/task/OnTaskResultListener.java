package ke.tang.task;

/**
 * Listener for retrieve task execute result
 *
 * @param <Result>
 */
public interface OnTaskResultListener<Result> {
    /**
     * Called when task execute result available, may call multi times
     *
     * @param result
     */
    void onResult(Result result);
}
