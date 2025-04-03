import java.time.LocalTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EventDetailsDialog extends Dialog {
	private CalendarEvent event;
	private boolean changesSaved = false;
	private Text titleText;
	private Text descriptionText;
	private Combo startHourCombo;
	private Combo startMinuteCombo;
	private Combo endHourCombo;
	private Combo endMinuteCombo;
	private Button allDayCheckbox;
	private Button hasEndCheckbox;

	public EventDetailsDialog(Shell parent, CalendarEvent event) {
		super(parent);
		this.event = event;
	}

	public boolean wasSaved() {
		return changesSaved;
	}

	public void open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Event Details");
		shell.setLayout(new GridLayout(2, false));

		// Title
		Label titleLabel = new Label(shell, SWT.NONE);
		titleLabel.setText("Title:");
		titleLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		titleText = new Text(shell, SWT.BORDER);
		titleText.setText(event.getTitle());
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// All day event
		Label allDayLabel = new Label(shell, SWT.NONE);
		allDayLabel.setText("All day event:");
		allDayLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		allDayCheckbox = new Button(shell, SWT.CHECK);
		allDayCheckbox.setSelection(event.isAllDay());
		allDayCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refreshEnabled();
			}
		});

		// Time Selection
		Label startTimeLabel = new Label(shell, SWT.NONE);
		startTimeLabel.setText("Start Time:");
		startTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Composite startTimeComposite = new Composite(shell, SWT.NONE);
		startTimeComposite.setLayout(new GridLayout(2, false));
		startTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label hasEndLabel = new Label(shell, SWT.NONE);
		hasEndLabel.setText("Has End Time:");
		hasEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		hasEndCheckbox = new Button(shell, SWT.CHECK);
		hasEndCheckbox.setSelection(!event.isAllDay() && event.getEndTime() != null);
		hasEndCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refreshEnabled();
			}
		});

		Label endTimeLabel = new Label(shell, SWT.NONE);
		endTimeLabel.setText("End Time:");
		endTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Composite endTimeComposite = new Composite(shell, SWT.NONE);
		endTimeComposite.setLayout(new GridLayout(2, false));
		endTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Start Time
		startHourCombo = new Combo(startTimeComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		startMinuteCombo = new Combo(startTimeComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		endHourCombo = new Combo(endTimeComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		endMinuteCombo = new Combo(endTimeComposite, SWT.DROP_DOWN | SWT.READ_ONLY);

		// Populate hour and minute combos
		for (int i = 0; i < 24; i++) {
			String hour = String.format("%02d", i);
			startHourCombo.add(hour);
			endHourCombo.add(hour);
		}
		for (int i = 0; i < 60; i += 5) {
			String minute = String.format("%02d", i);
			startMinuteCombo.add(minute);
			endMinuteCombo.add(minute);
		}

		// Set initial values
		if (event.getStartTime() != null) {
			startHourCombo.setText(String.format("%02d", event.getStartTime().getHour()));
			startMinuteCombo.setText(String.format("%02d", event.getStartTime().getMinute()));
		}
		if (event.getEndTime() != null) {
			endHourCombo.setText(String.format("%02d", event.getEndTime().getHour()));
			endMinuteCombo.setText(String.format("%02d", event.getEndTime().getMinute()));
		}

		// Description
		Label descriptionLabel = new Label(shell, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		descriptionText = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionText.setText(event.getDescription());
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

		// Buttons
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, true));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Button saveButton = new Button(buttonComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Save button handler
		saveButton.addListener(SWT.Selection, e -> {
			saveChanges();
			shell.close();
		});

		// Cancel button handler
		cancelButton.addListener(SWT.Selection, e -> shell.close());

		refreshEnabled();

		shell.pack();
		shell.setSize(500, 450);
		shell.open();

		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void refreshEnabled() {
		boolean allDay = allDayCheckbox.getSelection();
		boolean hasEnd = hasEndCheckbox.getSelection();
		startHourCombo.setEnabled(!allDay);
		startMinuteCombo.setEnabled(!allDay);
		endHourCombo.setEnabled(!allDay && hasEnd);
		endMinuteCombo.setEnabled(!allDay && hasEnd);
	}

	private void saveChanges() {
		event.setTitle(titleText.getText());
		event.setDescription(descriptionText.getText());

		if (allDayCheckbox.getSelection()) {
			event.setStartTime(null);
			event.setEndTime(null);
		} else {
			if (startHourCombo.getText().length() > 0 && startMinuteCombo.getText().length() > 0) {
				int startHour = Integer.parseInt(startHourCombo.getText());
				int startMinute = Integer.parseInt(startMinuteCombo.getText());
				event.setStartTime(LocalTime.of(startHour, startMinute));
			}

			if (endHourCombo.getText().length() > 0 && endMinuteCombo.getText().length() > 0) {
				if(hasEndCheckbox.getSelection()) {
					int endHour = Integer.parseInt(endHourCombo.getText());
					int endMinute = Integer.parseInt(endMinuteCombo.getText());
					event.setEndTime(LocalTime.of(endHour, endMinute));
				} else {
					event.setEndTime(null);
				}
			}
		}

		changesSaved = true;
	}
}