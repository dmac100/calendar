import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;

public class Cal4jHandler {
	private static final String PROD_ID = "-//Calendar App//iCal4j 4.1.1//EN";
	
	public static void openFile(InputStream inputStream, List<CalendarEvent> events) throws IOException {
		try {
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(inputStream);

			events.clear();
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
							calendarEvent.setDate(LocalDate.from(dtEnd.getDate()));
							calendarEvent.setEndTime(LocalTime.from(dtEnd.getDate()));
						}
					});
					
					event.getProperty(Property.DESCRIPTION).ifPresent(description -> {
						calendarEvent.setDescription(description.getValue());
					});

					events.add(calendarEvent);
				}
			}
		} catch(ParserException e) {
			throw new IOException(e);
		}
	}

	public static void saveFile(OutputStream outputStream, List<CalendarEvent> events) throws IOException {
		Calendar calendar = new Calendar();
		calendar.add(new ProdId(PROD_ID));
		calendar.add(ImmutableVersion.VERSION_2_0);
		
		UidGenerator uidGenerator = new RandomUidGenerator();
		
		for(CalendarEvent event:events) {
			VEvent vEvent;
			if(event.getStartTime() != null && event.getEndTime() != null) {
				LocalDateTime start = LocalDateTime.of(event.getDate(), event.getStartTime());
				LocalDateTime end = LocalDateTime.of(event.getDate(), event.getEndTime());
				vEvent = new VEvent(start, end, event.getTitle());
			} else if(event.getStartTime() != null) {
				LocalDateTime start = LocalDateTime.of(event.getDate(), event.getStartTime());
				vEvent = new VEvent(start, event.getTitle());
			} else if(event.getDate() != null) {
				vEvent = new VEvent(event.getDate(), event.getTitle());
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

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(calendar, outputStream);
	}
}