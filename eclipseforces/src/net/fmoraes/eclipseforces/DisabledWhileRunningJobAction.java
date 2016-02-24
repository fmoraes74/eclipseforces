package net.fmoraes.eclipseforces;


import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An action to run a job and disable the action action while the job is
 * running.
 */
public class DisabledWhileRunningJobAction extends Action {

  final Object actionLock = new Object();

  final Job jobToRun;

  public DisabledWhileRunningJobAction(String text, ImageDescriptor image, Job jobToRun) {
    super(text, image);

    this.jobToRun = jobToRun;

    jobToRun.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        synchronized (actionLock) {
          setEnabled(true);
        }
      }
    });

  }

  @Override
  public void run() {
    synchronized (actionLock) {
      if (!isEnabled()) {
        return;
      }
      setEnabled(false);
    }

    try {
      jobToRun.schedule();
    } catch (RuntimeException e) {
      synchronized (actionLock) {
        setEnabled(true);
      }
      throw e;
    }
  }

}
