package net.fmoraes.eclipseforces.java;

import net.fmoraes.eclipseforces.EclipseForcesPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

  /**
   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = EclipseForcesPlugin.getDefault().getPreferenceStore();

    store.setDefault(EclipseForcesPlugin.GENERATE_JUNIT_TIMEOUT_PREFERENCE, true);

    store.setDefault(EclipseForcesPlugin.CODE_TEMPLATE_PREFERENCE, JavaCodeGenerator.DEFAULT_CODE_TEMPLATE);
  }

}
