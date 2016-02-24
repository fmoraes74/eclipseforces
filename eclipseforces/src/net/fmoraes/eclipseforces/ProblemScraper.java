package net.fmoraes.eclipseforces;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import net.fmoraes.eclipseforces.util.Utilities;

/**
 * Scraping problem archive on www.topcoder.com with regular expressions.
 */
class ProblemScraper {

  /** Utility class for pair of strings */
  public static class StringPair {
    public String first;

    public String second;

    public StringPair(String first, String second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public String toString() {
      return "StringPair[first=" + first + ", second=" + second + "]\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  /**
   * Download the list of problem and serialize it it to disk.
   * 
   * @param listReference
   *            a reference to a list which will be filled in upon success
   */
  public static IStatus downloadProblemStats(IProgressMonitor monitor, ProblemListView.ListReference listReference) {
    ObjectOutputStream out = null;
    try {
      ProblemScraper connection = new ProblemScraper();
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;
      monitor.worked(20);

      monitor.subTask("Opening output stream");
      out = new ObjectOutputStream(new FileOutputStream(getProblemListFile()));
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;
      monitor.worked(5);

      // getProblemStatsList() calls monitor.worked() itself
      List<ProblemStats> list = connection.getProblemStatsList(monitor);
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;

      monitor.subTask("Saving to disk");
      out.writeObject(list);
      listReference.problemStats = list;
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;
      monitor.worked(10);

      return Status.OK_STATUS;
    } catch (Exception e) {
      return new Status(IStatus.ERROR, EclipseForcesPlugin.PLUGIN_ID, IStatus.OK,
          "Failed to download problems: " + e.getMessage(), e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          Utilities.showException(e);
        }
      }
    }
  }

  /**
   * Download the list of problem and serialize it it to disk.
   * 
   * @param listReference
   *            a reference to a list which will be filled in upon success
   */
  public static IStatus downloadContests(IProgressMonitor monitor, ContestListView.ListReference listReference, CodeforcesParser.ContestPageType listType) {
    ObjectOutputStream out = null;
    try {
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;
      monitor.worked(20);

      monitor.subTask("Getting contests");

      CodeforcesParser parser = new CodeforcesParser();
      
      IStatus status = parser.getContests(monitor, listReference, listType);
      
      monitor.worked(50);

      return status;
    } catch (Exception e) {
      return new Status(IStatus.ERROR, EclipseForcesPlugin.PLUGIN_ID, IStatus.OK,
          "Failed to download problems: " + e.getMessage(), e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          Utilities.showException(e);
        }
      }
    }
  }

  /** Utility method for encoding key-value parameters */
  //	private static String getEncoded(Map<String, String> parameters) throws UnsupportedEncodingException {
  //		StringBuilder builder = new StringBuilder();
  //		for (Map.Entry<String, String> mapEntry : parameters.entrySet()) {
  //			if (builder.length() != 0)
  //				builder.append('&');
  //			builder.append(URLEncoder.encode(mapEntry.getKey(), "UTF-8") + "=" //$NON-NLS-1$ //$NON-NLS-2$
  //					+ URLEncoder.encode(mapEntry.getValue(), "UTF-8")); //$NON-NLS-1$
  //		}
  //		return builder.toString();
  //	}

  /**
   * Return the file used to store the problem list in a serialized format.
   * Note that this file may not exist (problem list not yet downloaded) or be
   * incorrupt (aborted write or change of java version).
   */
  private static File getProblemListFile() {
    return EclipseForcesPlugin.getDefault().getStateLocation().append("problemStats.ser").toFile(); //$NON-NLS-1$
  }

  /**
   * Load the problem stats list from local storage. Return null if there was
   * a problem loading this file (file corrupted or not existing).
   */
  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public static List<ProblemStats> loadProblemStats() {
    List<ProblemStats> result = null;
    File storageFile = getProblemListFile();
    try {
      if (!storageFile.exists())
        return null;
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(storageFile));
      result = (List<ProblemStats>) in.readObject();
      in.close();
    } catch (EOFException e) {
      // invalid serialization file - need to be recreated
      storageFile.delete();
      return null;
    } catch (Exception e) {
      Utilities.showException(e);
      return null;
    }
    return result;
  }

  /** Read all text from an InputStream */
  private static String readAll(InputStream in) throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
      builder.append('\n');
    }
    return builder.toString();
  }

  ProblemScraper() throws Exception {
  }

  /**
   * First element is expected result, second the parameters.
   * 
   * @param problem
   *            The problem whose examples should be retrieved.
   * @return A list of pairs [expected, parameters]
   * @throws Exception
   */
  public List<StringPair> getExamples(ProblemStats problem) throws Exception {
    List<StringPair> result = new ArrayList<StringPair>();
    return result;
  }

  /**
   * Get the HTML problem statement for the problem.
   */
  public String getHtmlProblemStatement(ProblemStats problem) throws Exception {
    return "";
  }

  private String getPage(String url) throws Exception {
    return readAll(openStream(url));
  }

  @SuppressWarnings("static-method")
  private InputStream openStream(String fullURL) throws Exception {
    URL url = new URL(fullURL);
    URLConnection connection = url.openConnection();
    //		connection.setRequestProperty("Cookie", httpCookies); //$NON-NLS-1$
    return connection.getInputStream();
  }

  public List<ProblemStats> getProblemStatsList(IProgressMonitor monitor) throws Exception {
    monitor.subTask("Downloading page");
    String page = getPage("http://codeforces.com/api/contest.list"); //$NON-NLS-1$
    monitor.worked(30);

    monitor.subTask("Parsing page");
    
    JsonObject obj = Json.parse(page).asObject();
    List<ProblemStats> result = new ArrayList<ProblemStats>();

    JsonArray arr = obj.get("result").asArray();
    
    HashMap<Integer, JsonObject> rounds = new HashMap<Integer, JsonObject>();
    for(JsonValue val : arr) {
      JsonObject round = val.asObject();
      int roundId = round.get("id").asInt();
      
      rounds.put(roundId,  round);
    }
    
    monitor.subTask("Downloading problems");
    String problems = getPage("http://codeforces.com/api/problemset.problems"); //$NON-NLS-1$
    
    monitor.worked(40);
    
    monitor.subTask("Parsing problems");
    obj = Json.parse(problems).asObject();
    
    obj = obj.get("result").asObject();
    
    arr = obj.get("problems").asArray();
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    for(JsonValue val : arr) {
      JsonObject prb = val.asObject();
      String problemId = prb.get("index").asString();
      String problemName = prb.get("name").asString();
      int contestId = prb.get("contestId").asInt();
      
      String categories = "";
      
      JsonArray c = prb.get("tags").asArray();
      for(int j = 0; j < c.size(); j++) {
        if(j > 0)
          categories += "," + c.get(j).asString();
        else
          categories = c.get(j).asString();
      }
      
      JsonObject contest = rounds.get(contestId);
      
      String contestName = contest.get("name").asString();
      
      Date d = new Date(contest.get("startTimeSeconds").asLong()*1000);
      
      String contestDate = sdf.format(d);

      ProblemStats problem = new ProblemStats(problemName, problemId, contestId, contestName, contestDate,
          categories);
      result.add(problem);
    }

    monitor.worked(20);
    return result;
  }

}
