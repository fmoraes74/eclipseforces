package net.fmoraes.eclipseforces.java;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import net.fmoraes.eclipseforces.EclipseForcesPlugin;
import net.fmoraes.eclipseforces.ProblemStatement;
import net.fmoraes.eclipseforces.languages.CodeGenerator;

public class JavaCodeGenerator extends CodeGenerator {

  public static final String DEFAULT_CODE_TEMPLATE = 
      "import java.util.Scanner;\n" +
          "import java.io.InputStream;\n" +
          "import java.io.InputStreamReader;\n" +
          "import java.io.OutputStream;\n" +
          "import java.io.PrintWriter;\n" +
          "import java.io.Reader;\n\n" +
          "// " + CodeGenerator.TAG_CONTEST_NAME + "\n" +
          "// " + CodeGenerator.TAG_PROBLEM_NAME + "\n" +
          "public class " + CodeGenerator.TAG_CLASSNAME + " {\n\n" + 
          "    " + CodeGenerator.TAG_MODULO + "\n" +
          "    public static void main(String[] args)\n" + 
          "    {\n" +
          "        InputStream inputStream = System.in;\n" +
          "        OutputStream outputStream = System.out;\n" +
          "        PrintWriter out = new PrintWriter(outputStream);\n" +
          "        " + CodeGenerator.TAG_CLASSNAME + " solver = new " + CodeGenerator.TAG_CLASSNAME + "();\n" +
          "        solver.solve(1, new InputStreamReader(inputStream), out);\n" +
          "        out.close();\n" +
          "    }\n\n" +
          "    public void solve(int test, Reader in, PrintWriter out)\n" +
          "    {\n"  +
          "    }\n\n" +
          "}\n";

  public JavaCodeGenerator(List<ProblemStatement> problems) {
    super(problems);
  }

  @Override
  public String getTestsSource(ProblemStatement ps) {
    
    boolean hasLargeInput = false;
    boolean hasLargeOutput = false;
    for(ProblemStatement.TestCase tc : ps.getTestCases()) {
      if(tc.input.length() > 2000)
        hasLargeInput = true;
      if(tc.output.length() > 2000)
        hasLargeOutput = true;
      
    }
    boolean hasAnyLarge = hasLargeInput | hasLargeOutput;
    
    StringBuilder result = new StringBuilder();
    result.append("import org.junit.Assert;\n");
    result.append("import org.junit.Before;\n");
    if(hasAnyLarge)
      result.append("import org.junit.Rule;\n");
    result.append("import org.junit.Test;\n\n");
    if(hasAnyLarge)
      result.append("import net.fmoraes.eclipseforces.java.FileInputOutputWatcher;\n");
    if(hasLargeInput)
      result.append("import net.fmoraes.eclipseforces.java.TestInput;\n");
    if(hasLargeOutput)
      result.append("import net.fmoraes.eclipseforces.java.TestOutput;\n");

    result.append("import net.fmoraes.eclipseforces.util.SolutionChecker;\n");
    result.append("import java.io.PrintWriter;\n");
    result.append("import java.io.StringReader;\n");
    result.append("import java.io.StringWriter;\n\n");
    result.append("public class " + ps.getClassName() + "Test {\n\n");
    result.append("    protected SolutionChecker checker;\n");
    result.append("    protected "
        + ps.getClassName() + " solution;\n\n");
    if(hasAnyLarge) {
      result.append("    @Rule\n");
      result.append("    public FileInputOutputWatcher watcher = new FileInputOutputWatcher();\n\n");
    }

    result.append("    @Before\n");
    result.append("    public void setUp() {\n");
    result.append("        solution = new " + ps.getClassName()
        + "();\n");
    result.append("        checker = new SolutionChecker();\n");
    if(ps.getError() != 0.0)
      result.append("        checker.setRelativeError(" + ps.getError() + ");\n");
    result.append("    }\n\n");

    int i = -1;
    int timeLimit = Integer.parseInt(ps.getTimeLimit());
    for (ProblemStatement.TestCase testCase : ps.getTestCases()) {
      i++;
      getTestCaseSource(result, ps.getClassName(), timeLimit, i, testCase.input, testCase.output);
    }

    result.append("}\n");
    return result.toString();
  }
  
  @Override
  public void getTestCaseSource(StringBuilder result, String className, int timeLimit, int number, String input,
      String output)
  {
    result.append("    @Test");
    if (EclipseForcesPlugin.getDefault().isGenerateJUnitTimeout()) {
      result.append(String.format("(timeout = %d)", 1000*timeLimit));
    }
    result.append("\n");
    if(input.length() > 2000)
      result.append("    @TestInput(fileName = \"testcases/" + className + "_testCase" + number + ".input\")\n");
    if(output.length() > 2000)
      result.append("    @TestOutput(fileName = \"testcases/" + className + "_testCase" + number + ".output\")\n");
    result.append("    public void testCase" + number + "() {\n");
    if(input.length() <= 2000)
      result.append("        String input = \"" + StringEscapeUtils.escapeJava(input) + "\";\n");
    else
      createFile("testcases/" + className + "_testcase" + number + ".input", input);
    if(output.length() <= 2000)
      result.append("        String expected = \"" + StringEscapeUtils.escapeJava(output) + "\";\n");
    else
      createFile("testcases/" + className + "_testcase" + number + ".output", output);
    result.append("        StringWriter sw = new StringWriter();\n");
    result.append("        PrintWriter output = new PrintWriter(sw);\n");
    if(input.length() <= 2000)
      result.append(String.format("        solution.solve(%d, new StringReader(input), output);\n", number));
    else
      result.append(String.format("        solution.solve(%d, watcher.getInputReader(), output);\n", number));
    if(output.length() <= 2000)
      result.append("        String result = checker.verify(expected, sw.toString());\n");
    else
      result.append("        String result = checker.verify(watcher.getOutputReader(), sw.toString());\n");
    result.append("        Assert.assertTrue(result, result == null);\n");
    result.append("    }\n\n");
  }

  @Override
  public String getModuloString(ProblemStatement problemStatement) {
    String modulo = problemStatement.getModulo();
    if(modulo != null && modulo.length() > 0) {
      return "public static final int MOD = " + modulo + ";";
    }
    return super.getModuloString(problemStatement);
  }

}
