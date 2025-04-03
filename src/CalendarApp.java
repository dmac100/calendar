import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class CalendarApp {
	private static final Color COLOR_WEEKEND_BACKGROUND = new Color(245, 245, 245);
	private static final Color COLOR_GRID = new Color(200, 200, 200);
	private static final Color COLOR_LIGHT_TEXT = new Color(100, 100, 100);
	private static final Color COLOR_WHITE = new Color(255, 255, 255);
	private static final Color COLOR_WIDGET_BACKGROUND = new Color(240, 240, 240);
	private static final Color COLOR_BLACK = new Color(0, 0, 0);
	private static final Color COLOR_BLUE_HIGHLIGHT = new Color(46, 78, 145);
	private static final Color COLOR_ORANGE_HIGHLIGHT = new Color(255, 231, 156);
	private static final Color COLOR_ORANGE_HIGHLIGHT_HEADER = new Color(247, 224, 147);
	
	private static final Color COLOR_EVENT_BACKGROUND = new Color(195, 213, 234);
	private static final Color COLOR_EVENT_BORDER = new Color(169, 194, 225);
	private static final Color COLOR_EVENT_BACKGROUND_SELECTED = new Color(255, 229, 147);
	private static final Color COLOR_EVENT_BORDER_SELECTED = new Color(255, 219, 103);
	private static final Color COLOR_EVENT_TEXT = new Color(50, 50, 50);
	
	private static final int CALENDAR_HEADER_HEIGHT = 20;
	
	private Font normalFont;
	private Font boldFont;
	
	private Display display;
	private Shell shell;
	private Label monthLabel;
	private LocalDate currentDate;
	private Canvas calendarCanvas;
	private Table eventTable;
	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
	private static final int HEADER_HEIGHT = 25;
	
	private LocalDate selectedDate = LocalDate.of(2025, 04, 04);

	public CalendarApp() {
		display = new Display();
		shell = new Shell(display);
		currentDate = LocalDate.now();
		
		loadFonts();
	}
	
	private void loadFonts() {
		this.normalFont = Display.getCurrent().getSystemFont();
		
		FontData fontData = normalFont.getFontData()[0];
		fontData.setStyle(SWT.BOLD);
		
		this.boldFont = new Font(display, fontData);
	}

	public void open() {
		shell.setText("SWT Calendar");
		shell.setSize(900, 600);
		GridLayout shellLayout = new GridLayout(1, false);
		shell.setLayout(shellLayout);
		shell.setBackground(COLOR_WHITE);
		
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
		topComposite.setBackground(COLOR_WHITE);

		// Create event table
		eventTable = new Table(topComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		eventTable.setHeaderVisible(true);
		eventTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		eventTable.setBackground(COLOR_WHITE);

		// Add columns to the table
		TableColumn dateColumn = new TableColumn(eventTable, SWT.NONE);
		dateColumn.setText("Date");
		dateColumn.setWidth(100);

		TableColumn titleColumn = new TableColumn(eventTable, SWT.NONE);
		titleColumn.setText("Event");
		titleColumn.setWidth(200);

		// Create composite for the bottom section (calendar)
		Composite bottomComposite = new Composite(sashForm, SWT.NONE);
		GridLayout bottomLayout = new GridLayout(1, false);
		bottomLayout.marginWidth = 0;
		bottomLayout.marginHeight = 0;
		bottomLayout.verticalSpacing = 0;
		bottomComposite.setLayout(bottomLayout);
		bottomComposite.setBackground(COLOR_WHITE);

		// Create navigation composite
		Composite navComposite = new Composite(bottomComposite, SWT.NONE);
		GridLayout navLayout = new GridLayout(7, false);
		navLayout.marginWidth = 0;
		navLayout.horizontalSpacing = 5;
		navLayout.marginHeight = 5;
		navComposite.setLayout(navLayout);
		navComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		navComposite.setBackground(COLOR_WHITE);

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
		monthLabel.setBackground(COLOR_WHITE);
		updateMonthLabel();

		// Calendar canvas
		calendarCanvas = new Canvas(bottomComposite, SWT.DOUBLE_BUFFERED);
		GridData calendarCanvasData = new GridData(SWT.FILL, SWT.FILL, true, true);
		calendarCanvas.setLayoutData(calendarCanvasData);

		calendarCanvas.addPaintListener(e -> drawCalendar(e.gc));
		calendarCanvas.addListener(SWT.Resize, e -> calendarCanvas.redraw());
		
		 // Add click handler for date selection
        calendarCanvas.addListener(SWT.MouseDown, e -> handleClick(e));
        
		// Set initial sash weights (20/80 split)
		sashForm.setWeights(new int[] { 20, 80 });

		// Add some sample events
		addSampleEvents();

		updateCalendar();
		shell.open();

		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
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
		TableItem item1 = new TableItem(eventTable, SWT.NONE);
		item1.setText(new String[] { "2025-04-01", "Event" });
	}
	
	private void handleClick(Event e) {
		Rectangle clientArea = calendarCanvas.getClientArea();
        int cellWidth = clientArea.width / 7;
        int cellHeight = (clientArea.height - CALENDAR_HEADER_HEIGHT) / 6;
        
        int column = e.x / cellWidth;
        int row = (e.y - CALENDAR_HEADER_HEIGHT) / cellHeight;
        
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDay = yearMonth.atDay(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        
        int dayOfMonth = (row * 7 + column) - firstDayOfWeek + 1;
        
        if (dayOfMonth >= 1 && dayOfMonth <= yearMonth.lengthOfMonth()) {
            selectedDate = firstDay.plusDays(dayOfMonth - 1);
            calendarCanvas.redraw();
        }
	}

	private void drawCalendar(GC gc) {
		int eventHeight = 18;
		int dayHeaderHeight = 20;
		
		Rectangle clientArea = calendarCanvas.getClientArea();
		gc.setBackground(COLOR_WHITE);
		gc.fillRectangle(clientArea);

		// Calculate cell dimensions based on available space
		double cellWidth = (clientArea.width - 1) / 7.0;
		double cellHeight = (clientArea.height - CALENDAR_HEADER_HEIGHT - 1) / 6.0;

		YearMonth yearMonth = YearMonth.from(currentDate);
		LocalDate firstDay = yearMonth.atDay(1);
		int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
		int daysInMonth = yearMonth.lengthOfMonth();

		// Draw header
		String[] dayNames = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
		for(int i = 0; i < 7; i++) {
			int width = gc.textExtent(dayNames[i]).x;
			gc.setBackground(COLOR_WIDGET_BACKGROUND);
			gc.fillRectangle((int) (i * cellWidth), CALENDAR_HEADER_HEIGHT / 2, (int) cellWidth, CALENDAR_HEADER_HEIGHT / 2);
			gc.drawText(dayNames[i], (int) ((i + 0.5) * cellWidth) - width / 2, 2, true);
		}
		
		// Draw weekend backgrounds.
		gc.setBackground(COLOR_WEEKEND_BACKGROUND);
		gc.fillRectangle(0, (int) CALENDAR_HEADER_HEIGHT, (int) cellWidth, (int) (cellHeight * 6));
		gc.fillRectangle((int) (6 * cellWidth), (int) CALENDAR_HEADER_HEIGHT, (int) cellWidth, (int) (cellHeight * 6));
		
		// Draw day cells
		int day = 1;
		for(int i = 0; i < 6 && day <= daysInMonth; i++) {
			for(int j = 0; j < 7; j++) {
				int x = (int) (j * cellWidth);
				int y = (int) (i * cellHeight) + CALENDAR_HEADER_HEIGHT;

				if(i == 0 && j < firstDayOfWeek) continue;
				if(day > daysInMonth) break;
				
				if(firstDay.plusDays(day - 1).equals(selectedDate)) {
					gc.setBackground(COLOR_ORANGE_HIGHLIGHT);
					gc.fillRectangle(x, y, (int) cellWidth + 1, (int) cellHeight + 1);
					
					gc.setBackground(COLOR_ORANGE_HIGHLIGHT_HEADER);
					gc.fillRectangle(x, y, (int) cellWidth + 1, (int) dayHeaderHeight);
					
					gc.setFont(boldFont);
				} else {
					gc.setFont(normalFont);
				}
				
				// Draw day number
				gc.setForeground(COLOR_LIGHT_TEXT);
				int width = gc.textExtent(String.valueOf(day)).x;
				gc.drawString(String.valueOf(day), x + (int) cellWidth - width - 3, y + 3, true);
				gc.setFont(normalFont);
				
				// Draw events
				if(j == 1) {
					gc.setClipping(x, y, (int) cellWidth, (int) cellHeight);
					
					for(int event = 0; event < 2; event++) {
						gc.setBackground(COLOR_EVENT_BACKGROUND);
						gc.setForeground(COLOR_EVENT_BORDER);
						if(false) {
							gc.setBackground(COLOR_EVENT_BACKGROUND_SELECTED);
							gc.setForeground(COLOR_EVENT_BORDER_SELECTED);
						}
						gc.fillRectangle(x + 3, y + dayHeaderHeight + event * (eventHeight + 2), (int) cellWidth - 4, eventHeight);
						gc.drawRectangle(x + 3, y + dayHeaderHeight + event * (eventHeight + 2), (int) cellWidth - 4, eventHeight);
						gc.setForeground(COLOR_EVENT_TEXT);
						gc.drawText("Event 123456789 123456789", x + 5, y + dayHeaderHeight + event * (eventHeight + 2) + 1);
					}
						
					gc.setClipping(clientArea);
				}
				
				day++;
			}
		}
		
		// Draw grid lines
		gc.setBackground(COLOR_WHITE);
		gc.setForeground(COLOR_GRID);

		// Draw vertical lines
		for(int i = 0; i <= 7; i++) {
			int x = (int) (i * cellWidth);
			gc.drawLine(x, 0, x, clientArea.height);
		}

		// Draw horizontal lines
		gc.drawLine(0, 0, clientArea.width, 0);
		for(int i = 0; i <= 6; i++) {
			int y = (int) (i * cellHeight);
			gc.drawLine(0, CALENDAR_HEADER_HEIGHT + y, clientArea.width, CALENDAR_HEADER_HEIGHT + y);
		}
	}

	private void updateMonthLabel() {
		monthLabel.setText(currentDate.format(MONTH_FORMATTER));
	}

	private void changeMonth(int delta) {
		currentDate = currentDate.plusMonths(delta);
		updateMonthLabel();
		updateCalendar();
	}

	private void updateCalendar() {
		calendarCanvas.redraw();
	}

	public static void main(String[] args) {
		new CalendarApp().open();
	}
}