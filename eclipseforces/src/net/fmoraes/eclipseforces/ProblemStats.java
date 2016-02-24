package net.fmoraes.eclipseforces;


import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

public class ProblemStats implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final int COLUMN_CLASS_NAME = 0;
  public static final int COLUMN_CONTEST_NAME = 1;
  public static final int COLUMN_DATE = 2;
  public static final int COLUMN_LEVEL = 3;
  public static final int COLUMN_CATEGORIES = 4;

  static final String[] COLUMN_NAMES = { "Problem Name", "Contest",
    "Date", "Level",
    "Categories" };

  String categories;
  String className;
  String contestDate;
  String contestName;
  String problemId;
  int roundId;

  public ProblemStats() {
    // to allow instantiation during deserialization
  }

  public ProblemStats(String className, String problemId, int roundId, String contestName, String contestDate,
      String categories) {
    this.className = className;
    this.roundId = roundId;
    this.problemId = problemId;
    this.contestName = contestName;
    this.contestDate = contestDate;
    this.categories = categories;
  }

  public int compareTo(ProblemStats other, int column) {
    switch (column) {
    case COLUMN_CLASS_NAME:
    case COLUMN_CONTEST_NAME:
    case COLUMN_DATE:
    case COLUMN_CATEGORIES:
      return getFieldString(column).compareTo(other.getFieldString(column));
    case COLUMN_LEVEL:
      return getLevelString().compareTo(other.getLevelString());
    default:
      Assert.isTrue(false, "Invalid column: " + column);
      return 0;
    }
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof ProblemStats && ((ProblemStats) other).problemId == problemId);
  }

  public String getFieldString(int which) {
    switch (which) {
    case COLUMN_CLASS_NAME:
      return className;
    case COLUMN_CONTEST_NAME:
      return contestName;
    case COLUMN_DATE:
      return contestDate;
    case COLUMN_LEVEL:
      return getLevelString();
    case COLUMN_CATEGORIES:
      return categories;
    default:
      return "DEFAULT"; //$NON-NLS-1$
    }
  }

  private String getLevelString() {
    return problemId;
  }

  @Override
  public int hashCode() {
    return className.hashCode();
  }

  @Override
  public String toString() {
    return "Problem[className=" + className + ",problemId=" + problemId //$NON-NLS-1$ //$NON-NLS-2$
        + "]"; //$NON-NLS-1$
  }

}
