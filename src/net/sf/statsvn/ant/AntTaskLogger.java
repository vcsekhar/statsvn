package net.sf.statsvn.ant;

import net.sf.statsvn.util.TaskLogger;

import org.apache.tools.ant.Task;

/**
 * This class wraps up an Ant task which is going to be used to log some text
 * when the tool is used with Ant.
 * 
 * @author Benoit Xhenseval
 */
public final class AntTaskLogger implements TaskLogger {
    /** the Ant task. */
    private Task task;

    /**
     * Constructor that will hide the specific logging mechanism.
     * 
     * @param antTask
     *            an Ant task
     */
    AntTaskLogger(final Task antTask) {
        this.task = antTask;
    }

    /**
     * Uses the Ant mechanism to log the text.
     * 
     * @param text
     *            to be logged.
     */
    public void log(final String text) {
        task.log(text);
    }
}
