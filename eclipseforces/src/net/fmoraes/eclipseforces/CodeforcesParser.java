package net.fmoraes.eclipseforces;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import net.fmoraes.eclipseforces.util.FileUtilities;
import net.fmoraes.eclipseforces.util.Utilities;

public class CodeforcesParser {
  public enum ContestPageType {
    ACTIVE,
    PAGE, 
    FULL;
  };

  public IStatus getContests(IProgressMonitor monitor, ContestListView.ListReference receiver,  ContestPageType complete) {
    if(complete == ContestPageType.FULL)
      return getContestsFromAPI(monitor, receiver);

    String contestsPage = FileUtilities.getWebPageContent(complete == ContestPageType.PAGE ? 
        "http://codeforces.com/contests?complete=true" : "http://codeforces.com/contests");
    if (contestsPage == null)
      return Status.CANCEL_STATUS;
    List<Contest> contests = new ArrayList<>();
    int index = contestsPage.indexOf("<div class=\"contests-table\"");
    if(index == -1)
      index = contestsPage.length();
    Matcher m = Pattern.compile("data-contestId=\"(\\d+)\"\\s*>\\s*<td>\\s*(.+?)</td>.+?<span class=\"format-time\".*?>(\\S+)\\s(\\d\\d:\\d\\d)</span>", Pattern.DOTALL).matcher(contestsPage.substring(0, index));
    SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy");

    int end = 0;
    try {
      while(m.find()) {
        end = m.end() + 1;
        String id = m.group(1).trim();
        String name = m.group(2).trim();
        int idx = name.indexOf("<br/");
        if(idx != -1)
          name = name.substring(0, idx).trim();
        String date = m.group(3).trim();

        Contest contest = new Contest(id, name, sdf.parse(date));
        contests.add(contest);
      }
    }
    catch(Exception e) {
      Utilities.showException(e);
      monitor.setCanceled(true);;
    }

    if(complete == ContestPageType.PAGE) {
      m = Pattern.compile("data-contestId=\"(\\d+)\"\\s*>\\s*<td>\\s*((\\S| )+?)\\s*?<br/>\\s*?.*?<span class=\"format-date\".+?>(\\S+) (\\S+).*?</td>\\s*?<td>\\s*?(\\S+)\\s*?</td>", Pattern.DOTALL).matcher(contestsPage.substring(end));

      try {
        while(m.find()) {
          String id = m.group(1).trim();
          String name = m.group(2).trim();
          String date = m.group(4).trim();

          Contest contest = new Contest(id, name, sdf.parse(date));
          contests.add(contest);
        }
      }
      catch(Exception e) {
        Utilities.showException(e);
        monitor.setCanceled(true);;
      }
    }
    if (!monitor.isCanceled())
      receiver.contests = contests;
    else
      return Status.CANCEL_STATUS;
    return Status.OK_STATUS;
  }

  public IStatus getContestsFromAPI(IProgressMonitor monitor, ContestListView.ListReference receiver)
  {
    monitor.subTask("Downloading contest list");
    String problems = FileUtilities.getWebPageContent("http://codeforces.com/api/contest.list"); //$NON-NLS-1$

    if(problems == null || problems.length() == 0)
      return Status.CANCEL_STATUS;

    try {
      monitor.subTask("Parsing list");
      JsonObject obj = Json.parse(problems).asObject();

      monitor.worked(10);
      JsonArray arr = obj.get("result").asArray();

      monitor.worked(20);
      List<Contest> contests = new ArrayList<>();
      for(JsonValue val : arr) {
        JsonObject ctst = val.asObject();
        String id = Integer.toBinaryString(ctst.get("id").asInt());
        String name = ctst.get("name").asString().replace("<br>", " ").replace("<BR>"," ");
        long time = ctst.get("startTimeSeconds").asLong();

        Date d = new Date(time*1000);

        Contest contest = new Contest(id, name, d);
        contests.add(contest);
      }
      if(!monitor.isCanceled())
        receiver.contests = contests;

    }
    catch(Exception e) {
      Utilities.showException(e);
      monitor.setCanceled(true);;
    }    

    return Status.OK_STATUS;
  }

  public IStatus parseContest(IProgressMonitor monitor, Contest contest, List<ProblemStatement> receiver) {
    String mainPage = FileUtilities.getWebPageContent("http://codeforces.com/contest/" + contest.id);
    if (mainPage == null)
      return Status.CANCEL_STATUS;
    List<ProblemStatement> ids = new ArrayList<ProblemStatement>();
    Matcher m = Pattern.compile("class=\"id\">.*?<a href=\".*?\">\\s*(.+?)\\s*</a>.*?<!--\\s*?-->(.+?)<!--", Pattern.DOTALL).matcher(mainPage);
    while(m.find()) {
      String problemID = m.group(1).trim();
      String name = m.group(2).trim();
      ProblemStatement ps = new ProblemStatement();
      ps.setContest(contest.id);
      ps.setContestName(contest.name);
      ps.setID(problemID);
      ps.setName(name);
      ids.add(ps);
    }
    if (!monitor.isCanceled())
      receiver.addAll(ids);
    else
      return Status.CANCEL_STATUS;
    return Status.OK_STATUS;
  }

  public void parseProblem(ProblemStatement ps) {
    String id = ps.getID();
    String contest = ps.getContest();
    ps.setClassName("Problem_" + contest + "_" + id);
    String text = FileUtilities.getWebPageContent("http://codeforces.com/contest/" + contest + "/problem/" + id);
    if (text == null)
      return;

    Matcher timeLimit = Pattern.compile("class=\"time-limit\">.*?</div>(\\d+) .+?</div>").matcher(text);
    if(timeLimit.find()) {
      ps.setTimeLimit(timeLimit.group(1));
    }

    Matcher m = Pattern.compile("mod(ulo)? (\\d[\\d,\\.]*\\d)").matcher(text);
    if(m.find()) {
      try {
        String modulo = "" + 
            java.text.NumberFormat.
            getNumberInstance(java.util.Locale.US).parse(m.group(2));
        ps.setModulo(modulo);
      } catch (Exception e) {
      }
    }

    m = Pattern.compile("modulo.+?(\\d+).*?(\\d+).*?\\+.*?(\\d+)").matcher(text);
    if(m.find()) {
      try {
        String modulo = "" + ((long)Math.pow(Long.parseLong(m.group(1)), Long.parseLong(m.group(2))) + Long.parseLong(m.group(3)));
        ps.setModulo(modulo);
      } catch (Exception e) {
      }
    }

    m = Pattern.compile("(absolute|relative)\\serror.*?<span class=\"tex-span\">1<i>e</i>\\p{javaWhitespace}*?-\\p{javaWhitespace}*?(\\d+)</span>").matcher(text);
    if(m.find()) {
      try {
        int exp = Integer.parseInt(m.group(2));
        double error = Math.pow(10, -exp);
        ps.setError(error);
      }
      catch(Exception e) {
      }
    }

    m = Pattern.compile("(absolute|relative)\\serror.*?<span class=\"tex-span\">10<sup class=\"upper-index\">\\p{javaWhitespace}*?-\\p{javaWhitespace}*?(\\d+)</sup></span>").matcher(text);
    if(m.find()) {
      try {
        int exp = Integer.parseInt(m.group(2));
        double error = Math.pow(10, -exp);
        ps.setError(error);
      }
      catch(Exception e) {
      }
    }

    Matcher testFinder = Pattern.compile("class=\"input\".*?<pre>(.*?)</pre>.*?class=\"output\".*?<pre>(.+?)</pre>").matcher(text);
    List<ProblemStatement.TestCase> tests = new ArrayList<>();
    while (testFinder.find()) {
      String in = StringEscapeUtils.unescapeHtml4(testFinder.group(1)).replaceAll("<br />", "\n");
      String out = StringEscapeUtils.unescapeHtml4(testFinder.group(2)).replaceAll("<br />", "\n");
      tests.add(new ProblemStatement.TestCase(in, out));
    }
    ps.setTestCases(tests);;
  }
}

