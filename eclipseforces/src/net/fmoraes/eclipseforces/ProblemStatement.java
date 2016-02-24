package net.fmoraes.eclipseforces;

import java.util.ArrayList;
import java.util.List;

/**
 * A problem statement.
 */
public class ProblemStatement {
  public static class TestCase {
    public String input;

    public String output;

    public TestCase() {
      // no-arg constructor needed by serialization
    }

    public TestCase(String in, String out) {
      input = in;
      output = out;
    }

    public String getInput()
    {
      return input;
    }

    public String getOutput()
    {
      return output;
    }
  }

  private String contest;
  
  public String getContest()
  {
    return contest;
  }

  public void setContest(String contest)
  {
    this.contest = contest;
  }

  private String id;
  
  private String name;
  
  private String htmlDescription;
  
  private String timeLimit;
  
  private String className;
  
  private String contestName;
  
  private double error;

  public double getError()
  {
    return error;
  }

  public void setError(double error)
  {
    this.error = error;
  }

  public String getContestName()
  {
    return contestName;
  }

  public void setContestName(String contestName)
  {
    this.contestName = contestName;
  }

  public String getClassName()
  {
    return className;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public String getTimeLimit()
  {
    return timeLimit;
  }

  public void setTimeLimit(String timeLimit)
  {
    this.timeLimit = timeLimit;
  }

  private List<TestCase> testCases = new ArrayList<TestCase>();

  private String modulo = "";

  public String getHtmlDescription() {
    return htmlDescription;
  }

  public List<TestCase> getTestCases() {
    return testCases;
  }

  public void setHtmlDescription(String htmlDescription) {
    this.htmlDescription = htmlDescription;
  }

  public void setTestCases(List<TestCase> testCases) {
    this.testCases = testCases;
  }
  
  public void setModulo(String mod)
  {
    modulo = mod;
  }

  public String getModulo() {
    return modulo;
  }

  public String getID()
  {
    return id;
  }

  public void setID(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }


}
