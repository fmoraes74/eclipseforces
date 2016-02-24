package net.fmoraes.eclipseforces;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class EclipseForcesPlugin extends AbstractUIPlugin {
  
  static private EclipseForcesPlugin instance;
  
  public static final String PLUGIN_ID = "net.fmoraes.eclipseforces";
  
  public static final String CODE_TEMPLATE_PREFERENCE = "codeTemplatePreference";

  public static final String GENERATE_JUNIT_TIMEOUT_PREFERENCE = "generateJUnitTimeoutPreference";
  
  public static final String PREFERENCE_LANGUAGE = "languagePreference";

  public EclipseForcesPlugin()
  {
    instance = this;
  }
  
  public static EclipseForcesPlugin getDefault()
  {
    return instance;
  }

  public String getCodeTemplate() {
    return getPreferenceStore().getString(CODE_TEMPLATE_PREFERENCE);
  }

  public boolean isGenerateJUnitTimeout() {
    return getPreferenceStore().getBoolean(GENERATE_JUNIT_TIMEOUT_PREFERENCE);
  }

  public static IEclipsePreferences getProjectPrefs(IProject project) {
    IScopeContext context = new ProjectScope(project);
    IEclipsePreferences prefs = context.getNode(EclipseForcesPlugin.PLUGIN_ID);
    return prefs;
  }

}
