package net.fmoraes.eclipseforces.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.StringTokenizer;


public class SolutionChecker {
  
  private double error = 0.0;
  private boolean relativeError = false;
  
  public SolutionChecker()
  {
    
  }
  
  public void setRelativeError(double e)
  {
    error = e;
    relativeError = true;
  }
  
  public String verify(FileReader expected, String output)
  {
    try {
      StringWriter sw = new StringWriter();
      
      char[] buffer = new char[4096];
      
      int n = expected.read(buffer);
      while(n != -1) {
        sw.write(buffer, 0, n);
        n = expected.read(buffer);
      }
      return verify(sw.toString(), output);
    }
    catch(IOException e) {
      return e.getMessage();
    }
  }
  
  public String verify(String expected, String output)
  {
    StringTokenizer exp = new StringTokenizer(expected, "\t\r\n ");
    StringTokenizer out = new StringTokenizer(output, "\t\r\n ");
    
    int i = 0;
    while(exp.hasMoreTokens() && out.hasMoreTokens()) {
      String e = exp.nextToken();
      String o = out.nextToken();
      if(!e.equals(o)) {
        boolean good = false;
        if(relativeError) {
          try {
            double d = Double.parseDouble(o);
            double ex = Double.parseDouble(e);
            if(ex - error <= d && d <= ex + error)
              good = true;
          }
          catch(Exception ex) {
            
          }
        }
        if(!good)
          return "Difference in token " + i + ". Expected: " + e + ", received: " + o;
      }
      i++;
    }
    if(exp.hasMoreTokens() != out.hasMoreTokens()) {
      return "Different number of tokens received.\nExpected:\n" + expected + "Received:\n" + output;
    }
    return null;
  }

}
