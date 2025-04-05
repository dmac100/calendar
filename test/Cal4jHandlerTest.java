import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class Cal4jHandlerTest {
	@Test
	public void saveFile() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		List<CalendarEvent> events = new ArrayList<>();
		var event = new CalendarEvent();
		event.setTitle("Title");
		event.setDescription("Description");
		event.setDate(LocalDate.of(2000, 1, 1));
		event.setStartTime(LocalTime.of(14, 0));
		events.add(event);
		
		Cal4jHandler.saveFile(byteArrayOutputStream, events);
		
		System.out.println(byteArrayOutputStream);
	}
	
	@Test
	public void openFile() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		List<CalendarEvent> events = new ArrayList<>();
		var event = new CalendarEvent();
		event.setTitle("Title");
		event.setDescription("Description");
		event.setDate(LocalDate.of(2000, 1, 1));
		event.setStartTime(LocalTime.of(14, 0));
		event.setEndTime(LocalTime.of(15, 0));
		events.add(event);
		
		Cal4jHandler.saveFile(byteArrayOutputStream, events);
		
		events.clear();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		Cal4jHandler.openFile(inputStream, events);
		
		assertEquals("Title", events.get(0).getTitle());
		assertEquals("Description", events.get(0).getDescription());
		assertEquals(LocalTime.of(14, 0), events.get(0).getStartTime());
		assertEquals(LocalTime.of(15, 0), events.get(0).getEndTime());
	}
	
	@Test
	public void openFile_allDayEvent() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		List<CalendarEvent> events = new ArrayList<>();
		var event = new CalendarEvent();
		event.setTitle("Title");
		event.setDescription("Description");
		event.setDate(LocalDate.of(2000, 1, 1));
		events.add(event);
		
		Cal4jHandler.saveFile(byteArrayOutputStream, events);
		
		events.clear();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		Cal4jHandler.openFile(inputStream, events);
		
		assertEquals(LocalDate.of(2000, 1, 1), events.get(0).getDate());
		assertNull(events.get(0).getStartTime());
	}
}