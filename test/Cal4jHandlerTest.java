import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
		
		Cal4jHandler.saveFile(byteArrayOutputStream, events, new ArrayList<>());
		
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
		event.setEndDate(LocalDate.of(2000, 2, 1));
		event.setStartTime(LocalTime.of(14, 0));
		event.setEndTime(LocalTime.of(15, 0));
		events.add(event);
		
		Cal4jHandler.saveFile(byteArrayOutputStream, events, new ArrayList<>());
		
		events.clear();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		Cal4jHandler.openFile(inputStream, events, new ArrayList<>());
		
		assertEquals("Title", events.get(0).getTitle());
		assertEquals("Description", events.get(0).getDescription());
		assertEquals(LocalDate.of(2000, 1, 1), events.get(0).getDate());
		assertEquals(LocalDate.of(2000, 2, 1), events.get(0).getEndDate());
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
		
		Cal4jHandler.saveFile(byteArrayOutputStream, events, new ArrayList<>());
		
		events.clear();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		Cal4jHandler.openFile(inputStream, events, new ArrayList<>());
		
		assertEquals(LocalDate.of(2000, 1, 1), events.get(0).getDate());
		assertNull(events.get(0).getStartTime());
	}
	
	@Test
	public void openFile_tasks() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		List<CalendarTask> tasks = new ArrayList<>();
		var task = new CalendarTask();
		task.setTitle("Title");
		task.setDescription("Description");
		task.setCompleted(true);
		tasks.add(task);
		
		Cal4jHandler.saveFile(byteArrayOutputStream, new ArrayList<>(), tasks);
		
		tasks.clear();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		Cal4jHandler.openFile(inputStream, new ArrayList<>(), tasks);
		
		assertEquals("Title", tasks.get(0).getTitle());
		assertEquals("Description", tasks.get(0).getDescription());
		assertTrue(tasks.get(0).isCompleted());
	}
}