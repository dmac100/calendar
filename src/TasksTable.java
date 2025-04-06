import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TasksTable {
	private static final String allTasks = "All Tasks";
	private static final String uncompletedTasks = "Uncompleted Tasks";
	
	private final Composite container;
	private final Table table;
	private final Combo filterCombo;
	
	private Listener tasksChangeListener;
	private List<CalendarTask> tasks = new ArrayList<>();
	
	public TasksTable(Composite parent) {
		this.container = new Composite(parent, SWT.NONE);
		GridLayout containerLayout = new GridLayout(1, false);
		containerLayout.marginWidth = 0;
		containerLayout.marginHeight = 0;
		containerLayout.verticalSpacing = 1;
		container.setLayout(containerLayout);
		container.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		
		Composite topRow = new Composite(container, SWT.NONE);
		GridLayout topRowLayout = new GridLayout(2, false);
		topRowLayout.marginWidth = 0;
		topRowLayout.marginHeight = 0;
		topRowLayout.verticalSpacing = 1;
		topRow.setLayout(topRowLayout);
		
		filterCombo = new Combo(topRow, SWT.DROP_DOWN | SWT.READ_ONLY);
		filterCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		filterCombo.add(allTasks);
		filterCombo.add(uncompletedTasks);
		filterCombo.select(0);

		filterCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTasks();
			}
		});
		
		Button createTaskButton = new Button(topRow, SWT.NONE);
		createTaskButton.setText("New Task");
		createTaskButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				newTask();
			}
		});
		
		table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumn completedColumn = new TableColumn(table, SWT.NONE);
		completedColumn.setText("Completed");
		completedColumn.setWidth(100);

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
					confirmBox.setMessage("Are you sure you want to delete the selected task" + ((selectionIndices.length == 1) ? "?" : "s?"));
					if(confirmBox.open() == SWT.YES) {
						Arrays.sort(selectionIndices);
	
						for(int selectionIndex = selectionIndices.length - 1; selectionIndex >= 0; selectionIndex--) {
							tasks.remove(selectionIndices[selectionIndex]);
						}
	
						updateTasks();
						notifyListeners();
					}
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				int selectedIndex = table.getSelectionIndex();
				if(selectedIndex >= 0) {
					TaskDetailsDialog dialog = new TaskDetailsDialog(table.getShell(), tasks.get(selectedIndex));
					dialog.open();
					
					updateTasks();
				}
			}
		});

		TableSorter.addSortHandlers(table, () -> {
			int sortIndex = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
			TableSorter.sortBy(tasks, (table.getSortDirection() == SWT.UP), row -> getField(row, sortIndex));

			updateTasks();
			notifyListeners();
		});
	}
	
	private String getField(CalendarTask task, int index) {
		return switch(index) {
			case 1 -> task.isCompleted() ? "Yes" : "No";
			case 2 -> task.getTitle();
			case 3 -> task.getDescription();
			default -> "";
		};
	}

	public void setTasksChangeListener(Listener listener) {
		this.tasksChangeListener = listener;
	}
	
	private void newTask() {
		CalendarTask task = new CalendarTask();
		task.setTitle("New Task");
		task.setDescription("");
		
		TaskDetailsDialog dialog = new TaskDetailsDialog(table.getShell(), task);
		dialog.open();
		
		if(dialog.wasSaved()) {
			tasks.add(task);
			
			updateTasks();
			notifyListeners();
		}
	}

	public void setTasks(List<CalendarTask> tasks) {
		this.tasks = tasks;
		updateTasks();
	}

	public void updateTasks() {
		if(tasks == null) return;

		table.removeAll();
		for(CalendarTask task:tasks) {
			if(showTask(task)) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(new String[] {
					task.isCompleted() ? "Yes" : "No",
					task.getTitle(),
					task.getDescription()
				});
			}
		}
	}

	private boolean showTask(CalendarTask task) {
		if(filterCombo.getText().equals(uncompletedTasks)) {
			if(task.isCompleted()) {
				return false;
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

		if(tasksChangeListener != null) {
			tasksChangeListener.handleEvent(event);
		}
	}
	
	public Control getControl() {
		return container;
	}
}