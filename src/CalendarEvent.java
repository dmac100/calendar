import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarEvent {
	private LocalDate date;
	private String title;
	private String description;
	private boolean selected;
	private LocalTime startTime;
	private LocalTime endTime;

	public CalendarEvent() {
	}
	
	public CalendarEvent(LocalDate date, String title, String description) {
		this.date = date;
		this.title = title;
		this.description = description;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}
	
	public boolean isAllDay() {
		return (startTime == null);
	}
}