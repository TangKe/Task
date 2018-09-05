package ke.tang.task;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class DefaultProgress extends DialogFragment implements Progress {
    private OnProgressCancelListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new ProgressDialog(getActivity());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (null != mListener) {
            mListener.onCancel();
        }
    }

    @Override
    public void show(Task owner) {
        super.show(owner.getChildFragmentManager(), toString());
    }

    @Override
    public void hide() {
        if (null != getFragmentManager()) {
            dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void setOnProgressCancelListener(OnProgressCancelListener listener) {
        mListener = listener;
    }
}
