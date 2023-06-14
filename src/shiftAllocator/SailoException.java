package shiftAllocator;

/**
 * Exception class for exceptions caused by data structure. 
 * The class extends the standard Exception class.
 * It allows providing a message for the exception.
 * @author Vesa Lappalainen
 * @version 1.0, 22.02.2003
 */
public class SailoException extends Exception {
    private static final long serialVersionUID = 1L;


    /**
     * Constructor for the exception that takes a message for the exception.
     * @param message The message for the exception.
     */
    public SailoException(String message) {
        super(message);
    }
}
