package tests;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.*;
import shiftAllocator.*;

@SuppressWarnings({ "all" })
public class AbsenceTest {
    

    @Test
    public void testSetValueFor_DateField_ValidValue() {
        Absence absence = new Absence();
        String newDate = "2023-06-10";
        
        assertNull(absence.setValueFor(1, newDate));
        assertEquals(LocalDate.parse(newDate), absence.getDate());
    }
    
    @Test
    public void testSetValueFor_DateField_InvalidValue() {
        Absence absence = new Absence();
        String invalidDate = "2023/06/10";
        
        assertEquals("Provide the date in format YYYY-MM-DD", absence.setValueFor(1, invalidDate));
    }
    
    @Test
    public void testSetValueFor_StartTimeField_ValidValue() {
        Absence absence = new Absence();
        String newStartTime = "09:00";
        
        assertNull(absence.setValueFor(2, newStartTime));
        assertEquals(LocalTime.of(9, 0), absence.getStarTimeLT());
    }
    
    @Test
    public void testSetValueFor_StartTimeField_InvalidValue() {
        Absence absence = new Absence();
        String invalidStartTime = "09-00";
        
        assertEquals("Provide the time in the format of XX:XX", absence.setValueFor(2, invalidStartTime));
        assertEquals(LocalTime.of(0, 0), absence.getStarTimeLT());
    }
    
    @Test
    public void testSetValueFor_StopTimeField_ValidValue() {
        Absence absence = new Absence();
        String newStopTime = "17:30";
        
        assertNull(absence.setValueFor(3, newStopTime));
        assertEquals(LocalTime.of(17, 30), absence.getStopTimeLT());
    }
    
    @Test
    public void testSetValueFor_StopTimeField_InvalidValue() {
        Absence absence = new Absence();
        String invalidStopTime = "17-30";
        
        assertEquals("Provide the time in the format of XX:XX", absence.setValueFor(3, invalidStopTime));
        assertEquals(LocalTime.of(0, 0), absence.getStopTimeLT());
    }
    
    @Test
    public void testSetValueFor_WholeDayFlagField_ValidValue() {
        Absence absence = new Absence();
        String newWholeDayFlag = "true";
        
        assertNull(absence.setValueFor(4, newWholeDayFlag));
        assertTrue(absence.getWholeDayFlag());
    }
    
    @Test
    public void testSetValueFor_ExplanationField_ValidValue() {
        Absence absence = new Absence();
        String newExplanation = "Sick leave";
        
        assertNull(absence.setValueFor(5, newExplanation));
        assertEquals(newExplanation, absence.getExplanation());
    }
}