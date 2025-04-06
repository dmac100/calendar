package calendar.ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import calendar.CalendarEvent;

public class EventsTable {
	private static final String allEvents = "All Events";
	private static final String eventsNextWeek = "Events in the next 7 days";
	private static final String futureEvents = "Events in the future";
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	
	private final Composite container;
	private final Table table;
	private final Combo filterCombo;
	
	private Listener eventsChangeListener;
	private List<CalendarEvent> events = new ArrayList<>();
	
	public EventsTable(Composite parent) {
		this.container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 1;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		filterCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		filterCombo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
		filterCombo.add(allEvents);
		filterCombo.add(eventsNextWeek);
		filterCombo.add(futureEvents);
		filterCombo.select(0);

		filterCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEvents();
			}
		});
		
		table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumn dateColumn = new TableColumn(table, SWT.NONE);
		dateColumn.setText("Date");
		dateColumn.setWidth(100);

		TableColumn startTimeColumn = new TableColumn(table, SWT.NONE);
		startTimeColumn.setText("Start");
		startTimeColumn.setWidth(200);

		TableColumn endTimeColumn = new TableColumn(table, SWT.NONE);
		endTimeColumn.setText("End");
		endTimeColumn.setWidth(200);

		TableColumn titleColumn = new TableColumn(table, SWT.NONE);
		titleColumn.setText("Title");
		titleColumn.setWidth(200);

		TableColumn descriptionColumn = new TableColumn(table, SWT.NONE);
		descriptionColumn.setText("Description");
		descriptionColumn.setWidth(300);

		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.DEL) {
					int[] selectionIndices = table.getSelectionIndices();
					MessageBox confirmBox = new MessageBox(table.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					confirmBox.setText("Confirm Deletion");
					confirmBox.setMessage("Are you sure you want to delete the selected event" + ((selectionIndices.length == 1) ? "?" : "s?"));
					if(confirmBox.open() == SWT.YES) {
						Arrays.sort(selectionIndices);
	
						for(int selectionIndex = selectionIndices.length - 1; selectionIndex >= 0; selectionIndex--) {
							events.remove(selectionIndices[selectionIndex]);
						}
	
						updateEvents();
						notifyListeners();
					}
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				int selectedIndex = table.getSelectionIndex();
				if(selectedIndex >= 0) {
					EventDetailsDialog dialog = new EventDetailsDialog(table.getShell(), events.get(selectedIndex));
					dialog.open();
					
					updateEvents();
				}
			}
		});

		TableSorter.addSortHandlers(table, () -> {
			int sortIndex = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
			TableSorter.sortBy(events, (table.getSortDirection() == SWT.UP), row -> getField(row, sortIndex));

			updateEvents();
			notifyListeners();
		});
	}

	private String getField(CalendarEvent event, int index) {
		return switch(index) {
			case 0 -> event.getDate().format(DATE_FORMATTER);
			case 1 -> formatDate(event.getDate(), event.getStartTime());
			case 2 -> formatDate(event.getEndDate(), event.getEndTime());
			case 3 -> event.getTitle();
			case 4 -> event.getDescription();
			default -> "";
		};
	}

	public void setEventsChangeListener(Listener listener) {
		this.eventsChangeListener = listener;
	}

	public void setEvents(List<CalendarEvent> events) {
		this.events = events;
		updateEvents();
	}

	public void updateEvents() {
		if(events == null) return;

		table.removeAll();
		for(CalendarEvent event:events) {
			if(showEvent(event)) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(new String[] {
					event.getDate().format(DATE_FORMATTER),
					formatDate(event.getDate(), event.getStartTime()),
					formatDate(event.getEndDate(), event.getEndTime()),
					event.getTitle(),
					event.getDescription()
				});
			}
		}
	}

	private static String formatDate(LocalDate date, LocalTime time) {
		if(date == null) {
			return "";
		} else if(time == null) {
			return String.valueOf(date);
		} else {
			return String.valueOf(date) + " " + String.valueOf(time);
		}
	}

	private boolean showEvent(CalendarEvent event) {
		if(Set.of(futureEvents, eventsNextWeek).contains(filterCombo.getText())) {
			 if(event.getDate().isBefore(LocalDate.now())) {
				 return false;
			 } else if(event.getDate().isEqual(LocalDate.now())) {
				 if(event.getStartTime() != null && event.getStartTime().isBefore(LocalTime.now())) {
					 return false;
				 }
			 }
			 
			 if(filterCombo.getText().equals(eventsNextWeek)) {
				 if(event.getDate().isAfter(LocalDate.now().plusDays(7))) {
					 return false;
				 }
			 }
		}
		return true;
	}

	public Table getTable() {
		return table;
	}

	private void notifyListeners() {
		Event event = new Event();
		event.widget = table;
		table.notifyListeners(SWT.Modify, event);

		if(eventsChangeListener != null) {
			eventsChangeListener.handleEvent(event);
		}
	}
	
	public Control getControl() {
		return container;
	}
}