package net.fmoraes.eclipseforces.java;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddTestCaseDialog extends Dialog {

  private Text txtInput;
  private Text txtOutput;

  private String input = "";
  private String output = "";

  public AddTestCaseDialog(Shell parent)
  {
    super(parent);
  }

  @Override
  protected Control createDialogArea(Composite parent)
  {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, false);
    layout.marginRight = 5;
    layout.marginLeft = 10;
    container.setLayout(layout);

    Label lblInput = new Label(container, SWT.NONE);
    lblInput.setText("Input:");
    GridDataFactory.fillDefaults().grab(false, false).span(1, 2).align(SWT.FILL, SWT.CENTER).applyTo(lblInput);

    txtInput = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    GridDataFactory.fillDefaults().grab(true, false).span(1, 2).hint(400, 200).align(SWT.FILL, SWT.CENTER).applyTo(txtInput);
    Listener scrollBarListener = new Listener (){
      @Override
      public void handleEvent(Event event) {
        Text t = (Text)event.widget;
        Rectangle r1 = t.getClientArea();
        // use r1.x as wHint instead of SWT.DEFAULT
        Rectangle r2 = t.computeTrim(r1.x, r1.y, r1.width, r1.height); 
        Point p = t.computeSize(SWT.DEFAULT,  SWT.DEFAULT,  true); 
        t.getVerticalBar().setVisible(r2.height <= p.y);
        t.getHorizontalBar().setVisible(r2.width <= p.x);
        if (event.type == SWT.Modify){
          t.getParent().layout(true);
          t.showSelection();
        }
      }};
      txtInput.addListener(SWT.Resize, scrollBarListener);
      txtInput.addListener(SWT.Modify, scrollBarListener);
      txtInput.setText("");

      Label lblOutput = new Label(container, SWT.NONE);
      lblOutput.setText("Output:");
      GridDataFactory.fillDefaults().grab(false, false).span(1, 2).align(SWT.FILL, SWT.CENTER).applyTo(lblOutput);

      txtOutput = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
      GridDataFactory.fillDefaults().grab(true, false).span(1, 2).hint(400, 200).align(SWT.FILL, SWT.CENTER).applyTo(txtOutput);
      txtOutput.setText("");
      txtOutput.addListener(SWT.Resize, scrollBarListener);
      txtOutput.addListener(SWT.Modify, scrollBarListener);
    
      return container;
  }

  @Override
  protected void configureShell(Shell newShell)
  {
    super.configureShell(newShell);
    newShell.setText("Add Testcase");
  }

  @Override
  protected void okPressed()
  {
    input = txtInput.getText().trim() + "\n";
    output = txtOutput.getText().trim() + "\n";

    super.okPressed();
  }

  public String getInput()
  {
    return input;
  }

  public String getOutput()
  {
    return output;
  }
}
