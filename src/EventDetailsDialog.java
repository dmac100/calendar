import java.time.LocalDate;
import java.time.LocalTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
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
	private DateTime startDate;
	private DateTime endDate;
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

		Label titleLabel = new Label(shell, SWT.NONE);
		titleLabel.setText("Title:");
		titleLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		titleText = new Text(shell, SWT.BORDER);
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label startDateLabel = new Label(shell, SWT.NONE);
		startDateLabel.setText("Start date:");
		startDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		GridLayout startDateLayout = new GridLayout(4, false);
		startDateLayout.marginWidth = 0;
		startDateLayout.marginHeight = 0;
		
		Composite startDateComposite = new Composite(shell, SWT.NONE);
		startDateComposite.setLayout(startDateLayout);
		startDateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		startDate = new DateTime(startDateComposite, SWT.DROP_DOWN);
		
		startHourCombo = new Combo(startDateComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		startMinuteCombo = new Combo(startDateComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		
		allDayCheckbox = new Button(startDateComposite, SWT.CHECK);
		allDayCheckbox.setText("All day event");
		allDayCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refreshEnabled();
			}
		});
		
		Label endDateLabel = new Label(shell, SWT.NONE);
		endDateLabel.setText("End date:");
		endDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		GridLayout endDateLayout = new GridLayout(4, false);
		endDateLayout.marginWidth = 0;
		endDateLayout.marginHeight = 0;
		
		Composite endDateComposite = new Composite(shell, SWT.NONE);
		endDateComposite.setLayout(endDateLayout);
		endDateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		endDate = new DateTime(endDateComposite, SWT.DROP_DOWN);
		
		endHourCombo = new Combo(endDateComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		endMinuteCombo = new Combo(endDateComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		
		hasEndCheckbox = new Button(endDateComposite, SWT.CHECK);
		hasEndCheckbox.setText("Has end");
		hasEndCheckbox.setSelection(!event.isAllDay() && event.getEndTime() != null);
		hasEndCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refreshEnabled();
			}
		});

		// Populate hour and minute combos
		for(int i = 0; i < 24; i++) {
			String hour = String.format("%02d", i);
			startHourCombo.add(hour);
			endHourCombo.add(hour);
		}
		for(int i = 0; i < 60; i += 5) {
			String minute = String.format("%02d", i);
			startMinuteCombo.add(minute);
			endMinuteCombo.add(minute);
		}

		Label descriptionLabel = new Label(shell, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		descriptionText = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

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
		
		setEvent(event);

		refreshEnabled();

		shell.pack();
		shell.setSize(550, 500);
		shell.open();

		Display display = parent.getDisplay();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void setEvent(CalendarEvent event) {
		titleText.setText(event.getTitle());
		
		descriptionText.setText(event.getDescription());
		
		if(event.getDate() != null) {
			startDate.setDate(event.getDate().getYear(), event.getDate().getMonthValue() - 1, event.getDate().getDayOfMonth());
		}
		
		if(event.getEndDate() == null) {
			if(event.getDate() != null) {
				endDate.setDate(event.getDate().getYear(), event.getDate().getMonthValue() - 1, event.getDate().getDayOfMonth());
			}
		} else {
			endDate.setDate(event.getEndDate().getYear(), event.getEndDate().getMonthValue() - 1, event.getEndDate().getDayOfMonth());
		}
		
		if(event.getStartTime() != null) {
			startHourCombo.setText(String.format("%02d", event.getStartTime().getHour()));
			startMinuteCombo.setText(String.format("%02d", event.getStartTime().getMinute()));
		}
		
		if(event.getEndTime() != null) {
			endHourCombo.setText(String.format("%02d", event.getEndTime().getHour()));
			endMinuteCombo.setText(String.format("%02d", event.getEndTime().getMinute()));
		}
		
		allDayCheckbox.setSelection(event.isAllDay());
		hasEndCheckbox.setSelection(event.hasEnd());
	}

	private void refreshEnabled() {
		boolean allDay = allDayCheckbox.getSelection();
		boolean hasEnd = hasEndCheckbox.getSelection();
		startHourCombo.setEnabled(!allDay);
		startMinuteCombo.setEnabled(!allDay);
		endHourCombo.setEnabled(!allDay && hasEnd);
		endMinuteCombo.setEnabled(!allDay && hasEnd);
		endDate.setEnabled(hasEnd);
		
		if(startHourCombo.getText().isEmpty() && startHourCombo.isEnabled()) {
			startHourCombo.setText("00");
		}
		if(endHourCombo.getText().isEmpty() && endHourCombo.isEnabled()) {
			endHourCombo.setText(startHourCombo.getText());
		}
		if(startMinuteCombo.getText().isEmpty() && startMinuteCombo.isEnabled()) {
			startMinuteCombo.setText("00");
		}
		if(endMinuteCombo.getText().isEmpty() && endMinuteCombo.isEnabled()) {
			endMinuteCombo.setText(startMinuteCombo.getText());
		}
	}

	private void saveChanges() {
		event.setTitle(titleText.getText());
		event.setDescription(descriptionText.getText());

		if(allDayCheckbox.getSelection()) {
			event.setStartTime(null);
			event.setEndTime(null);
		} else {
			if(startHourCombo.getText().length() > 0 && startMinuteCombo.getText().length() > 0) {
				int startHour = Integer.parseInt(startHourCombo.getText());
				int startMinute = Integer.parseInt(startMinuteCombo.getText());
				event.setStartTime(LocalTime.of(startHour, startMinute));
			} else {
				event.setStartTime(LocalTime.of(0, 0));
			}

			if(endHourCombo.getText().length() > 0 && endMinuteCombo.getText().length() > 0) {
				if(hasEndCheckbox.getSelection()) {
					int endHour = Integer.parseInt(endHourCombo.getText());
					int endMinute = Integer.parseInt(endMinuteCombo.getText());
					event.setEndTime(LocalTime.of(endHour, endMinute));
				} else {
					event.setEndTime(null);
				}
			} else {
				if(hasEndCheckbox.getSelection()) {
					event.setEndTime(LocalTime.of(23, 55));
				}
			}
		}
		
		event.setDate(LocalDate.of(startDate.getYear(), startDate.getMonth() + 1, startDate.getDay()));
		
		if(hasEndCheckbox.getSelection()) {
			event.setEndDate(LocalDate.of(endDate.getYear(), endDate.getMonth() + 1, endDate.getDay()));
			if(event.getEndDate().isBefore(event.getDate())) {
				event.setEndDate(event.getDate());
			}
		} else {
			event.setEndDate(null);
		}

		changesSaved = true;
	}
}