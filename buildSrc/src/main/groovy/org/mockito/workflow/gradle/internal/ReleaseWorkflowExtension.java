package org.mockito.workflow.gradle.internal;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.mockito.workflow.gradle.ReleaseWorkflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReleaseWorkflowExtension implements ReleaseWorkflow {

    final List<Task> steps = new ArrayList<Task>();
    Task previousStep;
    final List<Task> rollbacks = new ArrayList<Task>();
    private final Project project;

    public ReleaseWorkflowExtension(Project project) {
        this.project = project;
    }

    public ReleaseWorkflowExtension step(Task task, Map<String, Task> config) {
        addStep(task, config.get("rollback"));
        return this;
    }

    public ReleaseWorkflowExtension step(Task task) {
        return step(task, Collections.<String, Task>emptyMap());
    }

    private void addStep(final Task task, Task rollback) {
        //release steps must be sequential
        if (previousStep != null) {
            task.dependsOn(previousStep);
        }
        previousStep = task;

        steps.add(task);
        if (rollback == null) {
            rollback = project.task("noopRollback" + capitalize(task.getName()));
        }
        rollbacks.add(rollback);


        //rollbacks only run when one of the steps fails, by default we assume they don't fail
        if (!project.hasProperty("dryRun")) { //accommodate testing
            rollback.setEnabled(false);
        }

        //rollbacks finalize release steps
        task.finalizedBy(rollback);

        //rollbacks only run when their main task did not fail
        // when main task fails, there is nothing to rollback
        if (!project.hasProperty("dryRun")) { //accommodate testing
            rollback.onlyIf(new Spec<Task>() {
                public boolean isSatisfiedBy(Task t) {
                    return task.getState().getFailure() == null;
                }
            });
        }
    }

    private static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}