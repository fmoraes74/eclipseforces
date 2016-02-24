package net.fmoraes.eclipseforces.languages;

import java.util.List;
import java.util.regex.Pattern;

import net.fmoraes.eclipseforces.ProblemStatement;

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

  public CodeGenerator(List<ProblemStatement> problems) {
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
   * @param timeLimit the time limit for the test
   * @param number the test number
   * @param input the input to the test
   * @param output the expect output to the test
   */
  public abstract void getTestCaseSource(StringBuilder result, int timeLimit, int number, String input, String output);

  /**
   * Returns the modulo string, appropriate for the language.
   * 
   * @return modulo string, or empty string if none or not implemented
   */
  public String getModuloString(ProblemStatement ps) 
  {
    return "";
  }
}
