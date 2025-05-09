package calendar.ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;

import com.google.common.eventbus.EventBus;

import calendar.CalendarEvent;
import calendar.event.ChangeEvent;

public class CalendarCanvas extends Canvas {
	private static final Color COLOR_WEEKEND_BACKGROUND = new Color(245, 245, 245);
	private static final Color COLOR_NODAY_BACKGROUND = new Color(230, 230, 230);
	private static final Color COLOR_GRID = new Color(200, 200, 200);
	private static final Color COLOR_LIGHT_TEXT = new Color(100, 100, 100);
	private static final Color COLOR_WHITE = new Color(255, 255, 255);
	private static final Color COLOR_WIDGET_BACKGROUND = new Color(240, 240, 240);
	private static final Color COLOR_HEADER_BACKGROUND = new Color(250, 250, 250);
	private static final Color COLOR_WEEKEND_HEADER_BACKGROUND = new Color(240, 240, 240);
	private static final Color COLOR_HEADER_TEXT = new Color(20, 20, 20);
	private static final Color COLOR_ORANGE_HIGHLIGHT = new Color(255, 231, 156);
	private static final Color COLOR_ORANGE_HIGHLIGHT_HEADER = new Color(247, 224, 147);
	private static final Color COLOR_EVENT_BACKGROUND = new Color(195, 213, 234);
	private static final Color COLOR_EVENT_BORDER = new Color(169, 194, 225);
	private static final Color COLOR_EVENT_BACKGROUND_SELECTED = new Color(255, 229, 147);
	private static final Color COLOR_EVENT_BORDER_SELECTED = new Color(255, 219, 103);
	private static final Color COLOR_EVENT_TEXT = new Color(50, 50, 50);
	private static final int CALENDAR_HEADER_HEIGHT = 20;

	private final EventBus eventBus;
	
	private Font normalFont;
	private Font boldFont;
	private LocalDate selectedDate;
	private List<CalendarEvent> events;
	private int eventHeight = 18;
	private int dayHeaderHeight = 20;

	public CalendarCanvas(Composite parent, LocalDate selectedDate, List<CalendarEvent> events, EventBus eventBus) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.selectedDate = selectedDate;
		this.events = events;
		this.eventBus = eventBus;

		loadFonts();
		setupListeners();
	}

	private void loadFonts() {
		this.normalFont = getDisplay().getSystemFont();

		FontData fontData = normalFont.getFontData()[0];
		fontData.setStyle(SWT.BOLD);

		this.boldFont = new Font(getDisplay(), fontData);
	}

	private void setupListeners() {
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				drawCalendar(e.gc);
			}
		});

		addListener(SWT.Resize, e -> redraw());

		addListener(SWT.MouseDown, e -> handleClick(e));

		addListener(SWT.KeyDown, e -> {
			if(e.keyCode == SWT.DEL) {
				MessageBox confirmBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				confirmBox.setText("Confirm Deletion");
				confirmBox.setMessage("Are you sure you want to delete the selected event?");
				if(confirmBox.open() == SWT.YES) {
					CalendarEvent selectedEvent = getSelectedEvent();
					if(selectedEvent != null) {
						events.remove(selectedEvent);
						notifyListeners();
						redraw();
					}
				}
			}
		});

		addListener(SWT.MouseDoubleClick, e -> handleDoubleClick(e));
	}

	private void handleClick(Event e) {
		Rectangle clientArea = getClientArea();
		int cellWidth = clientArea.width / 7;
		int cellHeight = (clientArea.height - CALENDAR_HEADER_HEIGHT) / 6;

		int column = e.x / cellWidth;
		int row = (e.y - CALENDAR_HEADER_HEIGHT) / cellHeight;

		YearMonth yearMonth = YearMonth.from(selectedDate);
		LocalDate firstDay = yearMonth.atDay(1);
		int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

		int dayOfMonth = (row * 7 + column) - firstDayOfWeek + 1;

		if(dayOfMonth >= 1 && dayOfMonth <= yearMonth.lengthOfMonth()) {
			LocalDate clickedDate = firstDay.plusDays(dayOfMonth - 1);
			selectedDate = clickedDate;

			deselectAllEvents();

			// Check if click was on an event
			List<CalendarEvent> dayEvents = getEventsForDate(clickedDate);
			if(!dayEvents.isEmpty()) {
				// Calculate the y position within the cell
				int yInCell = e.y - (row * cellHeight + CALENDAR_HEADER_HEIGHT);

				// Check if click was in the event area
				if(yInCell >= dayHeaderHeight) {
					int eventIndex = (yInCell - dayHeaderHeight) / (eventHeight + 2);
					if(eventIndex < dayEvents.size()) {
						// Deselect all events first
						for(CalendarEvent event:events) {
							event.setSelected(false);
						}
						// Select the clicked event
						dayEvents.get(eventIndex).setSelected(true);
						notifyListeners();
					}
				}
			}
			redraw();
		}
	}

	private void handleDoubleClick(Event e) {
		Rectangle clientArea = getClientArea();
		int cellWidth = clientArea.width / 7;
		int cellHeight = (clientArea.height - CALENDAR_HEADER_HEIGHT) / 6;

		int column = e.x / cellWidth;
		int row = (e.y - CALENDAR_HEADER_HEIGHT) / cellHeight;

		YearMonth yearMonth = YearMonth.from(selectedDate);
		LocalDate firstDay = yearMonth.atDay(1);
		int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

		int dayOfMonth = (row * 7 + column) - firstDayOfWeek + 1;

		if(dayOfMonth >= 1 && dayOfMonth <= yearMonth.lengthOfMonth()) {
			LocalDate clickedDate = firstDay.plusDays(dayOfMonth - 1);

			// Check if click was on an event
			List<CalendarEvent> dayEvents = getEventsForDate(clickedDate);
			if(!dayEvents.isEmpty()) {
				// Calculate the y position within the cell
				int yInCell = e.y - (row * cellHeight + CALENDAR_HEADER_HEIGHT);

				// Check if click was in the event area
				if(yInCell >= dayHeaderHeight) {
					int eventIndex = (yInCell - dayHeaderHeight) / (eventHeight + 2);
					if(eventIndex < dayEvents.size()) {
						// Show event details dialog
						CalendarEvent clickedEvent = dayEvents.get(eventIndex);
						EventDetailsDialog dialog = new EventDetailsDialog(getShell(), clickedEvent);
						dialog.open();

						notifyListeners();
						redraw();

						return;
					}
				}
			}

			CalendarEvent newEvent = new CalendarEvent();
			newEvent.setDate(clickedDate);
			newEvent.setDescription("");
			newEvent.setSelected(true);
			newEvent.setTitle("New Event");

			EventDetailsDialog dialog = new EventDetailsDialog(getShell(), newEvent);
			dialog.open();

			if(dialog.wasSaved()) {
				events.add(newEvent);

				notifyListeners();
				redraw();
			}
		}
	}

	private void deselectAllEvents() {
		for(CalendarEvent event:events) {
			if(event.isSelected()) {
				event.setSelected(false);
			}
		}
	}

	private void drawCalendar(GC gc) {
		Rectangle clientArea = getClientArea();
		gc.setBackground(COLOR_WHITE);
		gc.fillRectangle(clientArea);

		// Calculate cell dimensions based on available space
		double cellWidth = (clientArea.width - 1) / 7.0;
		double cellHeight = (clientArea.height - CALENDAR_HEADER_HEIGHT - 1) / 6.0;

		YearMonth yearMonth = YearMonth.from(selectedDate);
		LocalDate firstDay = yearMonth.atDay(1);
		int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
		int daysInMonth = yearMonth.lengthOfMonth();

		// Draw header
		String[] dayNames = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
		for(int i = 0; i < 7; i++) {
			int width = gc.textExtent(dayNames[i]).x;
			gc.setBackground(COLOR_WIDGET_BACKGROUND);
			gc.setForeground(COLOR_HEADER_TEXT);
			gc.fillRectangle((int) (i * cellWidth), CALENDAR_HEADER_HEIGHT / 2, (int) cellWidth,
					CALENDAR_HEADER_HEIGHT / 2);
			gc.drawText(dayNames[i], (int) ((i + 0.5) * cellWidth) - width / 2, 2, true);
		}

		// Draw weekend backgrounds
		gc.setBackground(COLOR_WEEKEND_BACKGROUND);
		gc.fillRectangle(0, (int) CALENDAR_HEADER_HEIGHT, (int) cellWidth, (int) (cellHeight * 6));
		gc.fillRectangle((int) (6 * cellWidth), (int) CALENDAR_HEADER_HEIGHT, (int) cellWidth, (int) (cellHeight * 6));

		// Draw day cells
		int day = 1;
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 7; j++) {
				int x = (int) (j * cellWidth);
				int y = (int) (i * cellHeight) + CALENDAR_HEADER_HEIGHT;

				if(i == 0 && j < firstDayOfWeek || day > daysInMonth) {
					gc.setBackground(COLOR_NODAY_BACKGROUND);
					gc.fillRectangle(x, y, (int) cellWidth, (int) cellHeight);
					continue;
				}

				LocalDate currentDay = firstDay.plusDays(day - 1);
				
				// Draw day header
				gc.setBackground((j == 0 || j == 6) ? COLOR_WEEKEND_HEADER_BACKGROUND : COLOR_HEADER_BACKGROUND);
				gc.fillRectangle(x, y, (int) cellWidth + 1, (int) dayHeaderHeight);
				
				// Draw selected day
				if(currentDay.equals(selectedDate)) {
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

				// Draw events for this day
				List<CalendarEvent> dayEvents = getEventsForDate(currentDay);
				if(!dayEvents.isEmpty()) {
					gc.setClipping(x, y, (int) cellWidth, (int) cellHeight);

					for(int eventIndex = 0; eventIndex < dayEvents.size(); eventIndex++) {
						CalendarEvent event = dayEvents.get(eventIndex);
						String text = event.getTitle();
						if(event.getStartTime() != null && event.getDate().equals(currentDay)) {
							text = event.getStartTime().toString() + " " + text;
						}

						gc.setBackground(COLOR_EVENT_BACKGROUND);
						gc.setForeground(COLOR_EVENT_BORDER);
						if(event.isSelected()) {
							gc.setBackground(COLOR_EVENT_BACKGROUND_SELECTED);
							gc.setForeground(COLOR_EVENT_BORDER_SELECTED);
						}
						gc.fillRectangle(x + 3, y + dayHeaderHeight + eventIndex * (eventHeight + 2), (int) cellWidth - 4, eventHeight);
						gc.drawRectangle(x + 3, y + dayHeaderHeight + eventIndex * (eventHeight + 2), (int) cellWidth - 4, eventHeight);
						gc.setForeground(COLOR_EVENT_TEXT);
						gc.drawText(text, x + 5, y + dayHeaderHeight + eventIndex * (eventHeight + 2) + 1);
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

	private List<CalendarEvent> getEventsForDate(LocalDate date) {
		List<CalendarEvent> dayEvents = new ArrayList<>();
		for(CalendarEvent event:events) {
			if(event.hasEnd()) {
				if(!event.getDate().isAfter(date) && !event.getEndDate().isBefore(date)) {
					dayEvents.add(event);
				}
			} else {
				if(event.getDate().equals(date)) {
					dayEvents.add(event);
				}
			}
		}
		Collections.sort(dayEvents,
			Comparator.comparing((CalendarEvent event) -> !event.isAllDay()).
				thenComparing((CalendarEvent event) -> (event.getStartTime() == null) ? LocalTime.of(0, 0) : event.getStartTime())
		);
		return dayEvents;
	}

	public void setCurrentDate(LocalDate date) {
		this.selectedDate = date;
		redraw();
	}

	public LocalDate getSelectedDate() {
		return selectedDate;
	}

	public void setEvents(List<CalendarEvent> events) {
		this.events = events;
		redraw();
	}

	private CalendarEvent getSelectedEvent() {
		for(CalendarEvent event:events) {
			if(event.isSelected()) {
				return event;
			}
		}
		return null;
	}
	
	private void notifyListeners() {
		notifyListeners(SWT.Modify, new Event());
		eventBus.post(new ChangeEvent());
	}

	@Override
	public void dispose() {
		if(boldFont != null) {
			boldFont.dispose();
		}
		super.dispose();
	}
}