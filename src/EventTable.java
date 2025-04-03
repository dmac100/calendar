import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.swt.SWT;
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

    public EventTable(Composite parent) {
        table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Add columns to the table
        TableColumn dateColumn = new TableColumn(table, SWT.NONE);
        dateColumn.setText("Date");
        dateColumn.setWidth(100);

        TableColumn titleColumn = new TableColumn(table, SWT.NONE);
        titleColumn.setText("Event");
        titleColumn.setWidth(200);

        TableColumn descriptionColumn = new TableColumn(table, SWT.NONE);
        descriptionColumn.setText("Description");
        descriptionColumn.setWidth(300);
    }

    public void setEvents(List<CalendarEvent> events) {
        this.events = events;
        updateEvents();
    }

    public void updateEvents() {
        if (events == null) return;
        
        table.removeAll();
        for (CalendarEvent event : events) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { 
                event.getDate().format(DATE_FORMATTER),
                event.getTitle(),
                event.getDescription()
            });
        }
    }

    public Table getTable() {
        return table;
    }
} 