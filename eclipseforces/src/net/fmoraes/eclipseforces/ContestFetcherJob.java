package net.fmoraes.eclipseforces;

import java.util.ArrayList;
import java.util.List;

import net.fmoraes.eclipseforces.languages.LanguageSupport;
import net.fmoraes.eclipseforces.languages.LanguageSupportFactory;
import net.fmoraes.eclipseforces.util.Utilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job to fetch a problem description from the online TopCoder problem
 * archive.
 */
public class ContestFetcherJob extends Job {

  private Contest contest;

  public ContestFetcherJob(Contest contest) {
    super("Checking out contest");
    this.contest = contest;
    setUser(true);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    monitor.beginTask("Checking out contest", 100);
    try {
      String language = EclipseForcesPlugin.getDefault().getPreferenceStore()
          .getString(EclipseForcesPlugin.PREFERENCE_LANGUAGE);
      List<String> availabe = LanguageSupportFactory.supportedLanguages();
      if (language == null || !availabe.contains(language)) {

        if (availabe.isEmpty()) {
          return new Status(IStatus.ERROR, EclipseForcesPlugin.PLUGIN_ID, IStatus.OK,
              "No language support found", null);
        }

        // fall back to first available if it exists
        language = availabe.get(0);
      }

      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;

      final String finalLanguage = language;
      CodeforcesParser parser = new CodeforcesParser();
      monitor.worked(20);
      final List<ProblemStatement> problems = new ArrayList<>();
      IStatus status = parser.parseContest(monitor, contest, problems);
      if(!status.isOK())
        return status;
      
      monitor.subTask("Downloading problems");
      for(ProblemStatement problem : problems) {
        monitor.subTask("Downloading problem "+ problem.getName());
        parser.parseProblem(problem);
        
        monitor.worked(1);
      }
      monitor.worked(15);
      monitor.subTask("Creating language support");
      final LanguageSupport languageSupport = LanguageSupportFactory.createLanguageSupport(finalLanguage);

      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;

      Utilities.runInDisplayThread(new Runnable() {
        @Override
        public void run() {
          languageSupport.createProject(contest, problems).openSourceFileInEditor();
        }
      });

      return Status.OK_STATUS;
    } catch (Exception exc) {
      Utilities.showException(exc);
      return Status.CANCEL_STATUS;
    }
  }

}
