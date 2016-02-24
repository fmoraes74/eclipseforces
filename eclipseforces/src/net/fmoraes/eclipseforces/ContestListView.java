package net.fmoraes.eclipseforces;


import java.text.DateFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.xml.sax.SAXException;

import net.fmoraes.eclipseforces.languages.LanguageSupportFactory;

/**
 * A view which allows the user to browse and select problems for download and
 * project generation from the TopCoder problem archive.
 */
public class ContestListView extends ViewPart {

  static ContestListView instance;
  
  static DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

  private final class UpdateListJob extends Job {
    CodeforcesParser.ContestPageType listType = CodeforcesParser.ContestPageType.ACTIVE;
    {
      setUser(true);
    }

    private UpdateListJob(String name, CodeforcesParser.ContestPageType type)
    {
      super(name);
      
      listType = type;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

      monitor.beginTask("Updating contest list", 100);
      final ListReference listReference = new ListReference();
      IStatus status = ProblemScraper.downloadContests(monitor, listReference, listType);
      if (!status.isOK())
        return status;
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;

      monitor.subTask("Updating table");

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          viewer.setInput(listReference.contests);
          for (int i = 0; i < contestListTable.getColumnCount(); i++) {
            contestListTable.getColumn(i).pack();
          }
          setContentDescription(""); //$NON-NLS-1$
        }
      });
      return status;
    }
  }

  public static class ListReference {
    List<Contest> contests;
  }

  private Job updateListJob = new UpdateListJob("Updating contest list", CodeforcesParser.ContestPageType.PAGE);

  private Job updateActiveListJob = new UpdateListJob("Updating active contest list", CodeforcesParser.ContestPageType.ACTIVE);
  
  private Job updateFullListJob = new UpdateListJob("Updating full contest list", CodeforcesParser.ContestPageType.FULL);

  Table contestListTable;

  TableViewer viewer;

  @Override
  public void createPartControl(final Composite parent) {
    ContestListView.instance = this;

    fillLocalPullDown(getViewSite().getActionBars().getMenuManager());

    createTable(parent);
  }

  void createTable(Composite parent) {
    contestListTable = new Table(parent, SWT.FULL_SELECTION);
    contestListTable.setHeaderVisible(true);
    contestListTable.setLinesVisible(true);

    viewer = new TableViewer(contestListTable);
    viewer.setLabelProvider(new DescriptionLabelProvider());
    viewer.setContentProvider(new ArrayContentProvider());

    Menu rightClickMenu = new Menu(viewer.getTable());

    viewer.getTable().setMenu(rightClickMenu);

    for (String columnTitle : new String[] { "Name" , "Date"}) {
      TableColumn column = new TableColumn(contestListTable, SWT.LEFT);
      column.setText(columnTitle);
    }

//    viewer.setSorter(new DescriptionComparator(0, false));
    //    contestListTable.setSortColumn(contestListTable.getColumn(0));
    //    contestListTable.setSortDirection(SWT.DOWN);

    for (int i = 0; i < contestListTable.getColumnCount(); i++) {
//      final int index = i;
//      contestListTable.getColumn(i).addSelectionListener(new SelectionAdapter() {
//        @Override
//        public void widgetSelected(SelectionEvent e) {
//          DescriptionComparator previousSorter = (DescriptionComparator) viewer.getSorter();
//          boolean reversed = false;
//          if (previousSorter.column == index) {
//            reversed = !previousSorter.reversed;
//          }
//          contestListTable.setSortColumn(contestListTable.getColumn(index));
//          contestListTable.setSortDirection(reversed ? SWT.UP : SWT.DOWN);
//          viewer.setSorter(new DescriptionComparator(index, reversed));
//        }
//      });
      contestListTable.getColumn(i).pack();
    }

    contestListTable.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseDoubleClick(MouseEvent mouseEvent) {
        Contest stats = (Contest) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
        if (stats == null) {
          return;
        }

        new ContestFetcherJob(stats).schedule();
      }
    });

    List<Contest> contestList = null;

    if (contestList == null) {
      setContentDescription("Need to download contests");
    }
  }

  private void fillLocalPullDown(IMenuManager menuManager) {
    ImageDescriptor updateListImage = PlatformUI.getWorkbench().getSharedImages()
        .getImageDescriptor(ISharedImages.IMG_TOOL_COPY);

    IAction theAction = new DisabledWhileRunningJobAction("Update List (active)", updateListImage,
        updateActiveListJob);
    menuManager.add(theAction);

    theAction = new DisabledWhileRunningJobAction("Update List (page)", updateListImage,
        updateListJob);
    menuManager.add(theAction);

    theAction = new DisabledWhileRunningJobAction("Update List (full)", updateListImage,
        updateFullListJob);
    menuManager.add(theAction);

    String preferedLanguage = EclipseForcesPlugin.getDefault().getPreferenceStore()
        .getString(EclipseForcesPlugin.PREFERENCE_LANGUAGE);
    final String LANG_GROUP = "lang_group"; //$NON-NLS-1$

    MenuManager langMenuManager = new MenuManager("Use &language", null);
    menuManager.add(new Separator());
    menuManager.add(langMenuManager);

    langMenuManager.add(new GroupMarker(LANG_GROUP));
    for (String lang : LanguageSupportFactory.supportedLanguages()) {
      Action langAction = new Action(lang, IAction.AS_RADIO_BUTTON) {
        @Override
        public void run() {
          if (isChecked()) {
            EclipseForcesPlugin.getDefault().getPreferenceStore()
            .setValue(EclipseForcesPlugin.PREFERENCE_LANGUAGE, getText());
          }
        }
      };
      langMenuManager.appendToGroup(LANG_GROUP, langAction);
      if (lang.equals(preferedLanguage)) {
        langAction.setChecked(true);
      }
    }

  }

  @Override
  public void setFocus() {
    // if (downloadButton != null) {
    // downloadButton.setFocus();
    // } else if (problemListTable != null) {
    contestListTable.setFocus();
    // }
  }
  @SuppressWarnings("serial")//$NON-NLS-1$
  class CanceledException extends SAXException {
    // just a tag class
  }

//  class DescriptionComparator extends ViewerSorter {
//    public int column;
//
//    public boolean reversed;
//
//    public DescriptionComparator(int column, boolean reversed) {
//      this.column = column;
//      this.reversed = reversed;
//    }
//
//    @Override
//    public int compare(Viewer viewer, Object e1, Object e2) {
//      Description s1 = (Description) e1;
//      Description s2 = (Description) e2;
//      int ret = s1.description.compareTo(s2.description);
//      return (reversed ? -1 : 1) * ret;
//    }
//
//  }
//
  class DescriptionLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      Contest stats = (Contest) element;
      if(columnIndex == 0)
        return stats.name;
      return df.format(stats.date);
    }

  }
}

