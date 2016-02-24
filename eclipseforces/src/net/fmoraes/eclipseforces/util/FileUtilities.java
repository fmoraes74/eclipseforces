package net.fmoraes.eclipseforces.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Egor Kulikov (kulikov@devexperts.com)
 */
public class FileUtilities {
  public static String getWebPageContent(String address) {
    return getWebPageContent(address, "UTF-8");
  }

  public static String getWebPageContent(String address, String charset) {
    for (int i = 0; i < 10; i++) {
      try {
        URL url = new URL(address);
        InputStream input = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName(charset)));
        StringBuilder builder = new StringBuilder();
        String s;
        while ((s = reader.readLine()) != null) {
          builder.append(s).append('\n');
        }
        return new String(builder.toString().getBytes("UTF-8"), "UTF-8");
      } catch (IOException ignored) {
      }
    }
    return null;
  }
}
