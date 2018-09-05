package ke.tang.task.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
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
                for (int index = 0; index < 5; index++) {
                    Task.start(this,
                            TaskJob.createRxJavaJob(Flowable.just("Task Test").delay((long) (Math.random() * 10000), TimeUnit.MILLISECONDS)),
                            r -> {
                            }, e -> {
                            }, false);
                }
                break;
        }
    }
}
