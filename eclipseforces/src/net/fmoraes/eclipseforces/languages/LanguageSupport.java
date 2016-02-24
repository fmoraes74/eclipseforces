package net.fmoraes.eclipseforces.languages;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.fmoraes.eclipseforces.Contest;
import net.fmoraes.eclipseforces.ProblemStatement;
import net.fmoraes.eclipseforces.util.Utilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Each implementation of this class provides support for using a programming language with EclipseCoder.
 * 
 * LanguageSupport has two separate tasks:
 * <ol>
 * <li>Support the creation of appropriate Eclipse projects.</li>
 * <li>Support generation of test cases and formatting source code.</li>
 * </ol>
 */
public abstract class LanguageSupport {

  public static final String LANGUAGE_NAME_CPP = "C++";
  public static final String LANGUAGE_NAME_CSHARP = "C#";
  public static final String LANGUAGE_NAME_JAVA = "Java";
  public static final String LANGUAGE_NAME_PYTHON = "Python";
  public static final String LANGUAGE_NAME_VB = "VB";

  public static String getDefaultProjectName(Contest contest, List<ProblemStatement> problems, String languageName) {
    return (contest.name + "-" + languageName).toLowerCase();
  }

  protected CodeGenerator codeGenerator;

  List<ProblemStatement> problems;
  
  Contest contest;

  private String projectName;

  /**
   * The file containing the solution which is to be submitted.
   */
  IFile sourceFile;

  protected abstract CodeGenerator createCodeGenerator(List<ProblemStatement> tasks);

  /**
   * Implementations implement this method to create the language-specific parts of a project.
   * 
   * @return The file containing the problem class.
   * @throws Exception
   */
  protected abstract IFile createLanguageProject(IProject project, ProblemStatement problem, Contest contest) throws Exception;

  /**
   * Create a new project.
   * 
   * Must be called in the SWT display thread.
   * 
   * @param task
   *            the problem statement to create a project for
   */
  public final CreatedProject createProject(Contest contest, final List<ProblemStatement> theProblems) {
    // this must run in the display thread
    Assert.isNotNull(Display.getCurrent());

    try {
      problems = theProblems;
      codeGenerator = createCodeGenerator(problems);
      this.contest = contest;

      IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
      IWorkbench workbench = PlatformUI.getWorkbench();
      IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
      final IProject myProject = workspaceRoot.getProject(getProjectName());

      if (myProject.exists()) {
        ProblemStatement ps = problems.get(0);
        sourceFile = myProject.getFile(getSolutionFileName(ps));
        if (sourceFile.exists()) {
          Utilities.setPerspective(getPerspectiveID());

          CreatedProject result = new CreatedProject(myProject, sourceFile, getCodeEditorID());
          result.openSourceFileInEditor();
          return result;
        }

        workbenchWindow.getShell().forceActive();
        if (Utilities.showOkCancelDialog("Malformed project exists", "The project \"" + getProjectName()
            + "\" already exists but lacks the expected source file \"" + getSolutionFileName(ps)
            + "\"!\n\nPress Ok if you want to delete the project and create a new one.")) {
          myProject.delete(true, true, null);
          return createProject(contest, problems);
        }

        return null;
      }

      WorkspaceModifyOperation projectCreationOperation = new WorkspaceModifyOperation() {
        @Override
        protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
        InterruptedException {
          myProject.create(null);
          myProject.open(null);
        }
      };
      projectCreationOperation.run(null);

      // below prefs must be set outside the projectCreationOperation?
      try {
        sourceFile = createLanguageProject(myProject, problems.get(0), contest);
      } catch (Exception e) {
        Utilities.showException(e);
      }

      Utilities.setPerspective(getPerspectiveID());

      CreatedProject result = new CreatedProject(myProject, sourceFile, getCodeEditorID());
      result.openSourceFileInEditor();
      return result;
    } catch (Exception e) {
      Utilities.showException(e);
      return null;
    }
  }

  /**
   * Implementations should return the ID of the editor that should be used to open the problem statement source code
   * file.
   * 
   * @return The appropriate editor ID.
   */
  protected abstract String getCodeEditorID();

  public final CodeGenerator getCodeGenerator() {
    return codeGenerator;
  }

  protected abstract String getCodeTemplate();

  public final String getInitialSource(ProblemStatement ps) {
    return getSolutionStub(ps);
  }

  /**
   * Get the solution stub for the problem (not including test cases).
   * 
   * The tags from the code template are replaced with the actual contents.
   * 
   * @return The solution stub for the problem.
   */
  private String getSolutionStub(ProblemStatement ps) {
    return getCodeGenerator().getSolutionStub(getCodeTemplate(), ps);
  }

  /**
   * Implementations should return the name of the programming language that they provide support for. If the support
   * is for one of the TopCoder-supported languages C++, C#, Java or VB the named defined by the *_LANGUAGE_NAME
   * static fields of this interface must be used.
   * 
   * @return the name of the programming language that the implementation supports.
   */
  public abstract String getLanguageName();

  /**
   * Implementations should return the ID of the perspective to set when working on the problem in the current
   * language.
   * 
   * @return The appropriate perspective ID.
   */
  public abstract String getPerspectiveID();

  /** For subclasses */
  protected final List<ProblemStatement> getProblems() {
    return problems;
  }

  /**
   * Get the name of the project created for the problem statement.
   * 
   * @return the name of the newly created project.
   */
  public final String getProjectName() {
    if (projectName == null) {
      return getDefaultProjectName(contest, getProblems(), getLanguageName());
    }

    return projectName;
  }

  protected abstract String getSolutionFileName(ProblemStatement ps);

  public final void setProjectName(String projectName) {
    this.projectName = projectName;
  }
}
