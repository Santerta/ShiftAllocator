package tests;
import static org.junit.Assert.*;
import org.junit.*;
import verifiers.*;

@SuppressWarnings({ "all" })
public class DateFormatVerifierTest {
    
    @Test
    public void testVerify_ValidDateFormat_ReturnsNull() {
        DateFormatVerifier verifier = new DateFormatVerifier();
        String date = "2023-06-10";
        assertNull(verifier.verify(date));
    }

    @Test
    public void testVerify_InvalidDateFormat_ReturnsErrorMessage() {
        DateFormatVerifier verifier = new DateFormatVerifier();
        String date = "2023/06/10";
        assertEquals("Provide the date in format YYYY-MM-DD", verifier.verify(date));
    }

    @Test
    public void testVerify_InvalidMonth_ReturnsErrorMessage() {
        DateFormatVerifier verifier = new DateFormatVerifier();
        String date = "2023-13-10";
        assertEquals("Provide the date in format YYYY-MM-DD", verifier.verify(date));
    }
}