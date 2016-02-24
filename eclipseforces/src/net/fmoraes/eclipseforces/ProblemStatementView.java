package net.fmoraes.eclipseforces;

import net.fmoraes.eclipseforces.util.Utilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * A view which shows the problem statement of the CodeForces problem associated with the active editor in the workbench.
 * If no CodeForces problem is associated with the current editor a welcome text with instructions is is shown instead.
 */
public class ProblemStatementView extends ViewPart {

  // the text to show when no TopCoder problem is associated with the current
  // project
  private static String INITIAL_HTML = "<html><body><h1>Welcome to EclipseForces!</h1><ul>"
      + "<li>When a problem is selected, its problem statement will be shown here</li>"
      + "<li>Work on and solve the problem in Eclipse</li>"
      + "<li>Submit your solution and profit by copying and pasting the code into the website.</li>"
      + "</ul></body></html>";

  public static final String CONTEST_KEY = "contest";

  public static final String VIEW_ID = ProblemStatementView.class.getCanonicalName();

  Browser browser;

  private IFile lastFile;

  private String lastLocation;

  ISelectionListener pageSelectionListener = new ISelectionListener() {
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
      updateContent();
    }
  };

  @Override
  public void createPartControl(Composite parent) {
    browser = new Browser(parent, SWT.NONE);
    browser.setText(INITIAL_HTML);
    updateContent();
    hookPageSelection();
  }

  @Override
  public void dispose() {
    super.dispose();
    getSite().getPage().removePostSelectionListener(pageSelectionListener);
  }

  private void hookPageSelection() {
    getSite().getPage().addPostSelectionListener(pageSelectionListener);
  }

  @Override
  public void setFocus() {
    browser.setFocus();
  }

  /**
   * Update the content displayed in this view if the active project has changed.
   */
  void updateContent() {
    IEditorPart activeEditor = getSite().getPage().getActiveEditor();
    if (activeEditor == null) {
      if (browser.getUrl().length() == 0) {
        browser.setText(INITIAL_HTML);
      }
      return;
    }
    IEditorInput editorInput = activeEditor.getEditorInput();
    if (editorInput instanceof IFileEditorInput) {
      IFile editorFile = ((IFileEditorInput) editorInput).getFile();
      IProject project = editorFile.getProject();

      if (lastFile != editorFile && editorFile.exists()) {
        lastFile = editorFile;
        try {
          IEclipsePreferences prefs = EclipseForcesPlugin.getProjectPrefs(project);

          String location = prefs.get(editorFile.getName(), null);

          if (location == null) {
            browser.setText(INITIAL_HTML);
            setContentDescription("");
            lastLocation = null;
          } else {
            if(lastLocation == null || !lastLocation.equals(location)) {
              String contestName = prefs.get(ProblemStatementView.CONTEST_KEY, null);
              if (contestName != null) {
                setContentDescription(contestName);
              } else {
                setContentDescription("");
              }

              final String url = location;
              Utilities.runInDisplayThread(new Runnable() {
                public void run() {
                  browser.setUrl(url);
                }
              });
            }
          }
        } catch (Exception e) {
          Utilities.showException(e);
        }
      }
    }
  }
}