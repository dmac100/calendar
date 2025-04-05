import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;

class TableSorter {
	public static void addSortHandlers(Table table, Runnable refreshCallback) {
		table.setSortColumn(table.getColumn(0));
		table.setSortDirection(SWT.UP);

		for(var column:table.getColumns()) {
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					if(table.getSortColumn() != column) {
						table.setSortColumn(column);
						table.setSortDirection(SWT.UP);
					} else {
						table.setSortDirection((table.getSortDirection() == SWT.UP) ? SWT.DOWN : SWT.UP);
					}
					refreshCallback.run();
				}
			});
		}
	}

	public static <A> void sortBy(List<A> elements, boolean sortUp, Function<A, String> keyExtractor) {
		Collections.sort(elements, (a, b) -> {
			String aText = keyExtractor.apply(a);
			String bText = keyExtractor.apply(b);

			Double aDouble = getDouble(aText);
			Double bDouble = getDouble(bText);

			if(aDouble != null && bDouble != null) {
				return Double.compare(aDouble, bDouble) * (sortUp ? 1 : -1);
			} else {
				return aText.compareToIgnoreCase(bText) * (sortUp ? 1 : -1);
			}
		});
	}

	private static Double getDouble(String text) {
		try {
			return Double.parseDouble(text);
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
