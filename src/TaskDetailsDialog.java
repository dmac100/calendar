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

public class TaskDetailsDialog extends Dialog {
	private CalendarTask task;
	private boolean changesSaved = false;
	private Text titleText;
	private Text descriptionText;
	private Button completedCheck;

	public TaskDetailsDialog(Shell parent, CalendarTask task) {
		super(parent);
		this.task = task;
	}

	public boolean wasSaved() {
		return changesSaved;
	}

	public void open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Event Details");
		shell.setLayout(new GridLayout(2, false));

		Label titleLabel = new Label(shell, SWT.NONE);
		titleLabel.setText("Title:");
		titleLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		titleText = new Text(shell, SWT.BORDER);
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label descriptionLabel = new Label(shell, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		descriptionText = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label completedLabel = new Label(shell, SWT.NONE);
		completedLabel.setText("Completed:");
		completedLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		completedCheck = new Button(shell, SWT.CHECK);
		completedCheck.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, true));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Button saveButton = new Button(buttonComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		saveButton.addListener(SWT.Selection, e -> {
			saveChanges();
			shell.close();
		});

		cancelButton.addListener(SWT.Selection, e -> shell.close());
		
		setTask(task);

		shell.pack();
		shell.setSize(550, 300);
		shell.open();

		Display display = parent.getDisplay();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void setTask(CalendarTask event) {
		titleText.setText(event.getTitle());
		descriptionText.setText(event.getDescription());
		completedCheck.setSelection(event.isCompleted());
	}

	private void saveChanges() {
		task.setTitle(titleText.getText());
		task.setDescription(descriptionText.getText());
		task.setCompleted(completedCheck.getSelection());

		changesSaved = true;
	}
}