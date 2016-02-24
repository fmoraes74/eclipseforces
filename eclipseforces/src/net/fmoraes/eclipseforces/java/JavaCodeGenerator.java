package net.fmoraes.eclipseforces.java;

import java.util.List;

import net.fmoraes.eclipseforces.EclipseForcesPlugin;
import net.fmoraes.eclipseforces.ProblemStatement;
import net.fmoraes.eclipseforces.languages.CodeGenerator;

import org.apache.commons.lang3.StringEscapeUtils;

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
    StringBuilder result = new StringBuilder();
    result.append("import org.junit.Assert;\n");
    result.append("import org.junit.Before;\n");
    result.append("import org.junit.Test;\n\n");
    result.append("import net.fmoraes.eclipseforces.util.SolutionChecker;\n");
    result.append("import java.io.PrintWriter;\n");
    result.append("import java.io.StringReader;\n");
    result.append("import java.io.StringWriter;\n\n");
    result.append("public class " + ps.getClassName() + "Test {\n\n");
    result.append("    protected SolutionChecker checker;\n");
    result.append("    protected "
        + ps.getClassName() + " solution;\n\n");
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
      getTestCaseSource(result, timeLimit, i, testCase.input, testCase.output);
    }

    result.append("}\n");
    return result.toString();
  }
  
  @Override
  public void getTestCaseSource(StringBuilder result, int timeLimit, int number, String input,
      String output)
  {
    result.append("    @Test");
    if (EclipseForcesPlugin.getDefault().isGenerateJUnitTimeout()) {
      result.append(String.format("(timeout = %d)", 1000*timeLimit));
    }
    result.append("\n");
    result.append("    public void testCase" + number + "() {\n");
    result.append("        String input = \"" + StringEscapeUtils.escapeJava(input) + "\";\n");
    result.append("        String expected = \"" + StringEscapeUtils.escapeJava(output) + "\";\n");
    result.append("        StringWriter sw = new StringWriter();\n");
    result.append("        PrintWriter output = new PrintWriter(sw);\n");
    result.append(String.format("        solution.solve(%d, new StringReader(input), output);\n", number));
    result.append("        String result = checker.verify(expected, sw.toString());\n");
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
