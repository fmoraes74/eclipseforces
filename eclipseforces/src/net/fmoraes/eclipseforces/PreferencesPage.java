package net.fmoraes.eclipseforces;

import java.util.List;

import net.fmoraes.eclipseforces.languages.LanguageSupportFactory;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class PreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public PreferencesPage() {
    super(GRID);
    setTitle("EclipseCoder");
    setPreferenceStore(EclipseForcesPlugin.getDefault().getPreferenceStore());
  }

  @Override
  protected void createFieldEditors() {
    List<String> languages = LanguageSupportFactory.supportedLanguages();
    String[][] labelAndValues = new String[languages.size()][2];
    for (int i = 0; i < labelAndValues.length; i++) {
      labelAndValues[i][0] = labelAndValues[i][1] = languages.get(i);
    }
    addField(new RadioGroupFieldEditor(EclipseForcesPlugin.PREFERENCE_LANGUAGE, "Preferred programming language", 1,
        labelAndValues, getFieldEditorParent(), true));
  }

  @Override
  public boolean performOk() {
    return super.performOk();
  }

  public void init(IWorkbench workbench) {
    // from IWorkbenchPreferencePage interface
  }

}