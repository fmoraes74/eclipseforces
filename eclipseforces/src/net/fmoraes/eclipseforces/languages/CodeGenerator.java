package net.fmoraes.eclipseforces.languages;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import net.fmoraes.eclipseforces.ProblemStatement;
import net.fmoraes.eclipseforces.util.Utilities;

/**
 * Abstract base class for code generators for different programming languages.
 * 
 * A code generator should be able to map java language constructs to that of another programming language.
 * 
 * Note that this class is just for code generation - support for eclipse integration is from the appropriate language
 * support.
 */
public abstract class CodeGenerator {

  public static final String TAG_CLASSNAME = "$CLASSNAME$";
  
  public static final String TAG_CONTEST_NAME = "$CONTEST$";
  
  public static final String TAG_MODULO = "$MODULO$";
  
  public static final String TAG_PROBLEM_NAME = "$NAME$";

  protected List<ProblemStatement> problems;
  
  protected IProject project;

  public CodeGenerator(List<ProblemStatement> problems)
  {
    this.problems = problems;
  }

  /**
   * Get the code template for the language. The code template should contain the relevant tags which will be
   * substituted.
   * 
   * <p>
   * The code template should be settable from a preference page and should default to a reasonable value.
   * 
   * <p>
   * This method is not intended to be overridden by subclasses unless there is special need.
   * 
   * @return the code template with the variables replaced
   */
  public String getSolutionStub(String codeTemplate, ProblemStatement ps) 
  {
    String res =codeTemplate.replaceAll(Pattern.quote(TAG_CLASSNAME), ps.getClassName());
    res = res.replaceAll(Pattern.quote(TAG_CONTEST_NAME), ps.getContestName());
    res = res.replaceAll(Pattern.quote(TAG_MODULO), getModuloString(ps));
    res = res.replaceAll(Pattern.quote(TAG_PROBLEM_NAME), ps.getName());
    return res;
  }

  /**
   * Should return the source for a test suite file which tests the solution.
   */
  public abstract String getTestsSource(ProblemStatement ps);
  
  /**
   * Should return the source for a given test case. Used to add new testcases to an existing test file.
   * 
   * @param result where the source should be added
   * @param className the name of the class for which the testcase is being generated
   * @param timeLimit the time limit for the test
   * @param number the test number
   * @param input the input to the test
   * @param output the expect output to the test
   */
  public abstract void getTestCaseSource(StringBuilder result, String className, int timeLimit, int number, String input, String output);

  /**
   * Returns the modulo string, appropriate for the language.
   * 
   * @return modulo string, or empty string if none or not implemented
   */
  public String getModuloString(ProblemStatement ps) 
  {
    return "";
  }
  
  /**
   * Returns the Eclipse project for this code generator. Useful for creating files under the project.
   * 
   * @return IProject reference
   */
  public IProject getProject()
  {
    return project;
  }
  
  /**
   * Sets the Eclipse project reference that can be used to create files.
   * 
   * @param proj IProject reference
   */
  public void setProject(IProject proj)
  {
    project = proj;
  }
  
  /**
   * Creates a new file under the project with the given path and content.
   * 
   * @param fileName file name
   * @param contents contents of the file
   * @return true if created successfully or false otherwise
   */
  public boolean createFile(String fileName, String contents)
  {
    if(project == null)
      return false;
    
    try {
      if(fileName.indexOf('/') != -1) {
        String[] paths = fileName.split("/");
        IFolder parent = null;
        for(int i = 0; i < paths.length - 1; i++) {
          IFolder folder = (parent == null ? project.getFolder(paths[i]) : parent.getFolder(paths[i]));
          if(!folder.exists())
            folder.create(true, true, null);
          parent = folder;
        }
      }
      IFile file = project.getFile(fileName);
      if(file.exists())
        file.delete(true, false, null);
      file.create(new ByteArrayInputStream(contents.getBytes()), true, null);
    }
    catch(Exception e) {
      Utilities.showException(e);
      return false;
    }
    return true;
  }
}
