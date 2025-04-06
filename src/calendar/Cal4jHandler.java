package calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;

public class Cal4jHandler {
	private static final String PROD_ID = "-//Calendar App//iCal4j 4.1.1//EN";

	public static void openFile(InputStream inputStream, List<CalendarEvent> events, List<CalendarTask> tasks) throws IOException {
		try {
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(inputStream);

			events.clear();
			tasks.clear();
			for(Component component:calendar.getComponents()) {
				if(component instanceof VEvent event) {
					CalendarEvent calendarEvent = new CalendarEvent();

					calendarEvent.setTitle(event.getSummary().getValue());

					event.getProperty(Property.DTSTART).ifPresent(startTime -> {
						if(startTime instanceof DtStart dtStart) {
							calendarEvent.setDate(LocalDate.from(dtStart.getDate()));
							if(dtStart.getDate().isSupported(ChronoField.HOUR_OF_DAY)) {
								calendarEvent.setStartTime(LocalTime.from(dtStart.getDate()));
							}
						}
					});

					event.getProperty(Property.DTEND).ifPresent(endTime -> {
						if(endTime instanceof DtEnd dtEnd) {
							calendarEvent.setEndDate(LocalDate.from(dtEnd.getDate()));
							if(dtEnd.getDate().isSupported(ChronoField.HOUR_OF_DAY)) {
								calendarEvent.setEndTime(LocalTime.from(dtEnd.getDate()));
							}
						}
					});

					event.getProperty(Property.DESCRIPTION).ifPresent(description -> {
						calendarEvent.setDescription(description.getValue());
					});

					events.add(calendarEvent);
				} else if(component instanceof VToDo todo) {
					CalendarTask calendarTask = new CalendarTask();
					
					calendarTask.setTitle(todo.getSummary().getValue());
					
					todo.getProperty(Property.DESCRIPTION).ifPresent(description -> {
						calendarTask.setDescription(description.getValue());
					});
					
					todo.getProperty(Property.COMPLETED).ifPresent(completed -> {
						calendarTask.setCompleted(true);
					});
					
					tasks.add(calendarTask);
				}
			}
		} catch(ParserException e) {
			throw new IOException(e);
		}
	}

	public static void saveFile(OutputStream outputStream, List<CalendarEvent> events, List<CalendarTask> tasks) throws IOException {
		Calendar calendar = new Calendar();
		calendar.add(new ProdId(PROD_ID));
		calendar.add(ImmutableVersion.VERSION_2_0);

		UidGenerator uidGenerator = new RandomUidGenerator();

		for(CalendarEvent event:events) {
			VEvent vEvent;
			
			Temporal start = getTemporal(event.getDate(), event.getStartTime());
			Temporal end = getTemporal(event.getEndDate(), event.getEndTime());
			
			if(start != null && end != null) {
				vEvent = new VEvent(start, end, event.getTitle());
			} else if(start != null) {
				vEvent = new VEvent(start, event.getTitle());
			} else {
				System.err.println("Skipping event with no date: " + event);
				continue;
			}

			if(event.getDescription() != null) {
				vEvent.add(new Description(event.getDescription()));
			}

			vEvent.add(uidGenerator.generateUid());

			calendar.add(vEvent);
		}
		
		for(CalendarTask task:tasks) {
			VToDo vTodo = new VToDo(Instant.now(), task.getTitle());
			
			if(task.getDescription() != null) {
				vTodo.add(new Description(task.getDescription()));
			}
			
			if(task.isCompleted()) {
				vTodo.add(new Completed(Instant.now()));
			}

			vTodo.add(uidGenerator.generateUid());

			calendar.add(vTodo);
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(calendar, outputStream);
	}

	private static Temporal getTemporal(LocalDate date, LocalTime time) {
		if(time == null) {
			return date;
		} else if(date == null) {
			return null;
		} else {
			return LocalDateTime.of(date, time);
		}
	}
}