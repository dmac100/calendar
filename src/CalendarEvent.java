import java.time.LocalDate;

public class CalendarEvent {
    private LocalDate date;
    private String title;
    private String description;
    private boolean selected;

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

	@Override
    public String toString() {
        return title;
    }
} 