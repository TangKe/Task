package ke.tang.task.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import ke.tang.task.Task;
import ke.tang.task.TaskHost;
import ke.tang.task.TaskJob;

public class MainActivity extends AppCompatActivity implements TaskHost, View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onAttachTask(Task<?, ?> task) {
        getSupportFragmentManager().beginTransaction().add(task, task.toString()).commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.executes:
                Task.start(this,
                        TaskJob.createRxJavaJob(Observable.just("Task Test").delay((long) (Math.random() * 10000), TimeUnit.MILLISECONDS)),
                        r -> {
                        }, e -> {
                        }, () -> {
                            Log.e("Tank", "complete");
                        }, true);
                break;
        }
    }
}
