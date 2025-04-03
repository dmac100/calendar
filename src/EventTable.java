import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class EventTable {
	private Table table;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private List<CalendarEvent> events;
	private Listener eventChangeListener;

	public EventTable(Composite parent) {
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Add columns to the table
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
				if (e.keyCode == SWT.DEL) {
					int[] selectionIndices = table.getSelectionIndices();
					Arrays.sort(selectionIndices);

					for (int selectionIndex = selectionIndices.length - 1; selectionIndex >= 0; selectionIndex--) {
						events.remove(selectionIndices[selectionIndex]);
					}

					updateEvents();
					notifyListeners();
				}
			}
		});
		
		TableSorter.addSortHandlers(table, () -> {
			int sortIndex = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
			TableSorter.sortBy(events, (table.getSortDirection() == SWT.UP), row -> getField(row, sortIndex));
			
			updateEvents();
		});
	}

	private String getField(CalendarEvent event, int index) {
		return switch(index) {
			case 0 -> event.getDate().format(DATE_FORMATTER);
			case 1 -> event.getStartTime() == null ? "" : String.valueOf(event.getStartTime());
			case 2 -> event.getEndTime() == null ? "" : String.valueOf(event.getEndTime());
			case 3 -> event.getTitle();
			case 4 -> event.getDescription();
			default -> "";
		};
	}

	public void setEventChangeListener(Listener listener) {
		this.eventChangeListener = listener;
	}

	public void setEvents(List<CalendarEvent> events) {
		this.events = events;
		updateEvents();
	}

	public void updateEvents() {
		if (events == null)
			return;

		table.removeAll();
		for (CalendarEvent event : events) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] {
				event.getDate().format(DATE_FORMATTER),
				(event.getStartTime() != null ? event.getStartTime().toString() : ""),
				(event.getEndTime() != null ? event.getEndTime().toString() : ""),
				event.getTitle(),
				event.getDescription()
			});
		}
	}

	public Table getTable() {
		return table;
	}

	private void notifyListeners() {
		Event event = new Event();
		event.widget = table;
		table.notifyListeners(SWT.Modify, event);

		// Notify the canvas about the event change
		if (eventChangeListener != null) {
			eventChangeListener.handleEvent(event);
		}
	}
}