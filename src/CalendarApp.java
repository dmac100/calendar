import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class CalendarApp {
	private static final Color COLOR_BLUE_HIGHLIGHT = new Color(46, 78, 145);
	private static final Color COLOR_WHITE = new Color(255, 255, 255);

	private Display display;
	private Shell shell;
	private Label monthLabel;
	private LocalDate currentDate;
	private CalendarCanvas calendarCanvas;
	private EventTable eventTable;
	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

	private List<CalendarEvent> events = new ArrayList<>();

	public CalendarApp() {
		display = new Display();
		shell = new Shell(display);
		currentDate = LocalDate.now();
	}

	public void open() {
		shell.setText("SWT Calendar");
		shell.setSize(900, 600);
		GridLayout shellLayout = new GridLayout(1, false);
		shell.setLayout(shellLayout);

		createMenuBar();

		// Create SashForm for draggable divider
		SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
		GridData sashData = new GridData(SWT.FILL, SWT.FILL, true, true);
		sashForm.setLayoutData(sashData);

		// Create composite for the top section (events)
		Composite topComposite = new Composite(sashForm, SWT.NONE);
		GridLayout topLayout = new GridLayout(1, false);
		topLayout.marginWidth = 0;
		topLayout.marginHeight = 0;
		topComposite.setLayout(topLayout);

		// Create event table
		eventTable = new EventTable(topComposite);
		eventTable.setEvents(events);
		eventTable.setEventChangeListener(e -> calendarCanvas.redraw());

		// Create composite for the bottom section (calendar)
		Composite bottomComposite = new Composite(sashForm, SWT.NONE);
		GridLayout bottomLayout = new GridLayout(1, false);
		bottomLayout.marginWidth = 0;
		bottomLayout.marginHeight = 0;
		bottomLayout.verticalSpacing = 0;
		bottomComposite.setLayout(bottomLayout);

		// Create navigation composite
		Composite navComposite = new Composite(bottomComposite, SWT.NONE);
		GridLayout navLayout = new GridLayout(7, false);
		navLayout.marginWidth = 5;
		navLayout.horizontalSpacing = 5;
		navLayout.marginHeight = 5;
		navComposite.setLayout(navLayout);
		navComposite.setBackground(COLOR_WHITE);
		navComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));

		// Previous month button
		Button prevButton = new Button(navComposite, SWT.PUSH);
		prevButton.setText("←");
		prevButton.addListener(SWT.Selection, e -> changeMonth(-1));

		// Next month button
		Button nextButton = new Button(navComposite, SWT.PUSH);
		nextButton.setText("→");
		nextButton.addListener(SWT.Selection, e -> changeMonth(1));

		// Month label
		monthLabel = new Label(navComposite, SWT.CENTER);
		monthLabel.setForeground(COLOR_BLUE_HIGHLIGHT);
		monthLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		updateMonthLabel();

		// Create calendar canvas
		calendarCanvas = new CalendarCanvas(bottomComposite, currentDate, events);
		calendarCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Add listener for event changes
		calendarCanvas.addListener(SWT.Modify, e -> eventTable.updateEvents());
		
		calendarCanvas.addMouseWheelListener(e -> changeMonth((e.count > 0) ? -1 : 1));

		// Set initial sash weights (20/80 split)
		sashForm.setWeights(new int[] { 20, 80 });

		// Add some sample events
		addSampleEvents();

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private void createMenuBar() {
		Menu menuBar = new Menu(shell, SWT.BAR);

		MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuItem.setText("File");

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuItem.setMenu(fileMenu);

		MenuItem exitItem = new MenuItem(fileMenu, SWT.NONE);
		exitItem.setText("Exit");

		exitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});

		shell.setMenuBar(menuBar);
	}

	private void addSampleEvents() {
		// Add some sample events
		events.add(new CalendarEvent(LocalDate.of(2025, 4, 1), "Team Meeting", "Weekly team sync"));
		events.add(new CalendarEvent(LocalDate.of(2025, 4, 1), "Lunch with Client", "Discuss project requirements"));
		events.add(new CalendarEvent(LocalDate.of(2025, 4, 4), "Doctor Appointment", "Annual checkup"));

		events.get(0).setStartTime(LocalTime.of(14, 0));

		eventTable.updateEvents();
	}

	private void updateMonthLabel() {
		monthLabel.setText(currentDate.format(MONTH_FORMATTER));
	}

	private void changeMonth(int delta) {
		currentDate = currentDate.plusMonths(delta);
		updateMonthLabel();
		calendarCanvas.setCurrentDate(currentDate);
	}

	public static void main(String[] args) {
		new CalendarApp().open();
	}
}