package net.fmoraes.eclipseforces.util;

import java.net.URL;

import net.fmoraes.eclipseforces.EclipseForcesPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;

public class EclipseForcesVariableInitializer extends
ClasspathVariableInitializer {

  public static final String ECLIPSEFORCES_VARIABLE = "ECLIPSEFORCES";
  @Override
  public void initialize(String arg0)
  {
    try {
      URL url = new URL("platform:/plugin/" + EclipseForcesPlugin.PLUGIN_ID);
      IPath path = new Path(FileLocator.toFileURL(url).getFile());

      JavaCore.setClasspathVariable(ECLIPSEFORCES_VARIABLE, path, null);
    }
    catch(Exception e) {
      Utilities.showException(e);
    }

  }

}
