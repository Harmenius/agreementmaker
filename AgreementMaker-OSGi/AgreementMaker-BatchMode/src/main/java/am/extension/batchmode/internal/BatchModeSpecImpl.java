package am.extension.batchmode.internal;

import am.extension.batchmode.api.BatchModeSpec;
import am.extension.batchmode.api.BatchModeTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchModeSpecImpl implements BatchModeSpec {
    private List<BatchModeTask> tasks = new ArrayList<>();

    @Override
    public List<BatchModeTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(BatchModeTask task) {
        tasks.add(task);
    }
}
