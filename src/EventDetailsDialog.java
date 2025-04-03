import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EventDetailsDialog extends Dialog {
	private CalendarEvent event;
	private Shell dialogShell;
	private Text titleText;
	private Text descriptionText;

	public EventDetailsDialog(Shell parent, CalendarEvent event) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.event = event;
	}

	public void open() {
		Shell parent = getParent();
		dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialogShell.setText("Event Details");
		dialogShell.setLayout(new GridLayout(2, false));

		// Date
		new Label(dialogShell, SWT.NONE).setText("Date:");
		Label dateLabel = new Label(dialogShell, SWT.NONE);
		dateLabel.setText(event.getDate().toString());
		dateLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Title
		new Label(dialogShell, SWT.NONE).setText("Title:");
		titleText = new Text(dialogShell, SWT.BORDER);
		titleText.setText(event.getTitle());
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Description
		new Label(dialogShell, SWT.NONE).setText("Description:");
		descriptionText = new Text(dialogShell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionText.setText(event.getDescription());
		GridData descriptionData = new GridData(SWT.FILL, SWT.FILL, true, true);
		descriptionData.heightHint = 100;
		descriptionText.setLayoutData(descriptionData);

		// Buttons
		Composite buttonComposite = new Composite(dialogShell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		buttonComposite.setLayout(new GridLayout(2, false));

		Button saveButton = new Button(buttonComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addListener(SWT.Selection, e -> {
			saveChanges();
			dialogShell.dispose();
		});

		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addListener(SWT.Selection, e -> dialogShell.dispose());

		dialogShell.pack();
		dialogShell.setSize(400, 300);
		dialogShell.open();

		Display display = parent.getDisplay();
		while (!dialogShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void saveChanges() {
		String newTitle = titleText.getText();
		String newDescription = descriptionText.getText();

		event.setTitle(newTitle);
		event.setDescription(newDescription);
	}
}