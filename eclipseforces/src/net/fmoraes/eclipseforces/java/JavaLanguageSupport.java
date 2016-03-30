package net.fmoraes.eclipseforces.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import net.fmoraes.eclipseforces.Contest;
import net.fmoraes.eclipseforces.EclipseForcesPlugin;
import net.fmoraes.eclipseforces.ProblemStatement;
import net.fmoraes.eclipseforces.ProblemStatementView;
import net.fmoraes.eclipseforces.languages.CodeGenerator;
import net.fmoraes.eclipseforces.languages.LanguageSupport;
import net.fmoraes.eclipseforces.util.Utilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaUI;
import org.osgi.framework.Version;

/**
 * For more information on the JDT java model, see:
 * http://www.eclipsecon.org/2005/presentations/EclipseCON2005_Tutorial29.pdf
 */
public class JavaLanguageSupport extends LanguageSupport {

  /** TopCoder supports java 1.8. */
  private static String eclipseAndJvmSupportedJavaVersion() {
    boolean jvm18Installed = false;
    for(IVMInstallType vm : JavaRuntime.getVMInstallTypes()) {
      for(IVMInstall inst : vm.getVMInstalls()) {
        if(inst instanceof IVMInstall2) {
          String jvmVersion = ((IVMInstall2) inst).getJavaVersion();
          String[] jvmVersionParts  = jvmVersion.split("\\.");
          int major = Integer.parseInt(jvmVersionParts[0]);
          int minor = Integer.parseInt(jvmVersionParts[1]);
          if((major == 1 && minor >= 8) || major >=2) {
            jvm18Installed = true;
          }
        }
      }
    }

    Version jdtVersion = JavaCore.getJavaCore().getBundle().getVersion();
    boolean jdtSupports18 = jdtVersion.getMajor() >= 4
        || (jdtVersion.getMajor() == 3 && jdtVersion.getMinor() >= 10)
        || (jdtVersion.getMajor() == 3 && jdtVersion.getMinor() >= 9 && jdtVersion.getMicro() >= 50);

    return jvm18Installed && jdtSupports18 ? "1.8" : "1.7";
  }

  @Override
  protected CodeGenerator createCodeGenerator(List<ProblemStatement> problems) {
    return new JavaCodeGenerator(problems);
  }

  @Override
  public IFile createLanguageProject(IProject project, ProblemStatement prob, Contest contest) throws CoreException, JavaModelException, IOException {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IJavaProject javaProject = JavaCore.create(project);
    
    getCodeGenerator().setProject(project);

    IProjectDescription newProjectDescription = workspace.newProjectDescription(project.getName());
    newProjectDescription.setNatureIds(new String[] { JavaCore.NATURE_ID });
    project.setDescription(newProjectDescription, null);

    final String JAVA_VERSION = eclipseAndJvmSupportedJavaVersion();

    IClasspathEntry sourceEntry = JavaCore.newSourceEntry(javaProject.getPath());
    IClasspathEntry conEntry = JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER
        + "/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-" + JAVA_VERSION));
    IClasspathEntry junitEntry = JavaCore.newContainerEntry(new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/4"));
    
    IClasspathEntry pluginEntry = JavaCore.newVariableEntry(new Path("ECLIPSEFORCES"), null, null);

    javaProject.setRawClasspath(new IClasspathEntry[] { sourceEntry, conEntry, junitEntry, pluginEntry }, null);

    javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, JAVA_VERSION);
    javaProject.setOption(JavaCore.COMPILER_SOURCE, JAVA_VERSION);
    javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JAVA_VERSION);

    IFile sourceFile = null;
    
    IEclipsePreferences prefs = EclipseForcesPlugin.getProjectPrefs(project);
    prefs.put(ProblemStatementView.CONTEST_KEY, contest.name);
    
    for(ProblemStatement ps : getProblems()) {
      prefs.put(ps.getClassName() + ".java", String.format("http://www.codeforces.com/contest/%s/problem/%s", contest.id, ps.getID()));
      prefs.put(ps.getClassName() + "Test.java", String.format("http://www.codeforces.com/contest/%s/problem/%s", contest.id, ps.getID()));
      prefs.put("timeout." + ps.getClassName() + "Test.java", ps.getTimeLimit());

      IFile testsFile = project.getFile(ps.getClassName() + "Test.java");
      testsFile.create(new ByteArrayInputStream(getCodeGenerator().getTestsSource(ps).getBytes()), false, null);
  
      IFile taskSourceFile = project.getFile(getSolutionFileName(ps));
      taskSourceFile.create(new ByteArrayInputStream(getInitialSource(ps).getBytes()), true, null);
  
      IJavaElement javaElement = javaProject.findElement(new Path(ps.getClassName()
          + "Test.java"));
  
      if (javaElement != null && sourceFile == null) {
        // Run initial JUnit test run
        ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
        Utilities.buildAndRun(project, new JUnitLauncher(compilationUnit, ps.getMemoryLimit()));
        sourceFile = taskSourceFile;
      }
      
      try {
        prefs.flush();
      }
      catch(Exception e) {
        Utilities.showException(e);
      }
    }

    return sourceFile;
  }

  @Override
  public String getCodeEditorID() {
    return JavaUI.ID_CU_EDITOR;
  }

  @Override
  public String getCodeTemplate() {
    return EclipseForcesPlugin.getDefault().getCodeTemplate();
  }

  @Override
  public String getLanguageName() {
    return LanguageSupport.LANGUAGE_NAME_JAVA;
  }

  @Override
  public String getPerspectiveID() {
    return JavaUI.ID_PERSPECTIVE;
  }

  @Override
  protected String getSolutionFileName(ProblemStatement ps) {
    return ps.getClassName() + ".java";
  }

}
