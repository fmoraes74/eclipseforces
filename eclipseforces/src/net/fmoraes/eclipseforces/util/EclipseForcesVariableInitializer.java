package net.fmoraes.eclipseforces.util;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;

import net.fmoraes.eclipseforces.EclipseForcesPlugin;

public class EclipseForcesVariableInitializer extends
ClasspathVariableInitializer {

  public static final String ECLIPSEFORCES_VARIABLE = "ECLIPSEFORCES";
  @Override
  public void initialize(String arg0)
  {
    try {
      URL url = new URL("platform:/plugin/" + EclipseForcesPlugin.PLUGIN_ID);
      String folderPath = FileLocator.toFileURL(url).getFile();
      
      File f = new File(folderPath, "net");
      if(!f.exists()) {
        f = new File(folderPath, "bin");
        if(f.exists())
          folderPath = f.getAbsolutePath();
      }
      Path path = new Path(folderPath);

      JavaCore.setClasspathVariable(ECLIPSEFORCES_VARIABLE, path, null);
    }
    catch(Exception e) {
      Utilities.showException(e);
    }

  }

}
