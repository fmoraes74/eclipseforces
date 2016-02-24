package net.fmoraes.eclipseforces.java;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import net.fmoraes.eclipseforces.EclipseForcesPlugin;
import net.fmoraes.eclipseforces.ProblemStatement;
import net.fmoraes.eclipseforces.util.Utilities;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AddTestCase extends AbstractHandler {
  /**
   * The constructor.
   */
  public AddTestCase() {
  }

  /**
   * the command has been executed, so extract extract the needed information
   * from the application context.
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    AddTestCaseDialog dlg = new AddTestCaseDialog(window.getShell());
    if(dlg.open() == Dialog.OK) {
      ICompilationUnit res = getResource(event);
      if(res != null) {
        try {
          String i = dlg.getInput();
          String o = dlg.getOutput();
          IProject project = null;
          IJavaElement p = res;
          while(p != null) {
            if(p instanceof IJavaProject) {
              project = ((IJavaProject)p).getProject();
              break;
            }
            p = p.getParent();
          }
          IType type = res.getAllTypes()[0];
          TreeSet<Integer> existing = new TreeSet<>();
          Pattern pat = Pattern.compile("testCase(\\d+)");
          for(IMethod m : type.getMethods()) {
            String name = m.getElementName();
            Matcher mat = pat.matcher(name);
            if(mat.matches()) {
              int num = Integer.parseInt(mat.group(1));
              existing.add(num);
            }
          }
          int next = existing.last() + 1;
          IEclipsePreferences prefs = EclipseForcesPlugin.getProjectPrefs(project);
          int timeLimit = prefs.getInt("timeout." + res.getElementName(), 1);
          JavaCodeGenerator gen = new JavaCodeGenerator(new ArrayList<ProblemStatement>());
          StringBuilder result = new StringBuilder();
          gen.getTestCaseSource(result, timeLimit, next, i, o);
          type.createMethod(result.toString(), null, false, null);
        }
        catch(Exception e) {
          Utilities.showException(e);
        }
      }
    }
    return null;
  }
  
  private ICompilationUnit getResource(ExecutionEvent event) {
    ICompilationUnit resource = getSelectionResource(event);
    if (resource==null) {
      resource = getEditorInputResource(event);
    }
    return resource;
  }

  private ICompilationUnit getSelectionResource(ExecutionEvent event) {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    if ((selection == null) || (selection.isEmpty())
        || (!(selection instanceof IStructuredSelection))) {
      return null;
    }

    Object selectedObject = ((IStructuredSelection) selection)
        .getFirstElement();
    return ((ICompilationUnit)selectedObject);
  }

  private ICompilationUnit getEditorInputResource(ExecutionEvent event) {
    IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
    if (!(activePart instanceof IEditorPart)) {
      return null;
    }
    IEditorInput input = ((IEditorPart)activePart).getEditorInput();
    if (input instanceof IFileEditorInput) {
      return JavaCore.createCompilationUnitFrom(((IFileEditorInput)input).getFile());
    }
    return null;
  }
}
