package shiftAllocator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

import fi.jyu.mit.ohj2.Mjonot;
import verifiers.TimeFormatVerifier;

/**
 * Represents a workshift with various properties such as name, date, start time, end time, etc.
 * Implements the Cloneable interface to support cloning of workshift objects.
 *
 * @author Santeri Tammisto
 * @version 10.6.2023
 */
public class Workshift implements Cloneable {
    
    private int         id;
    
    private String      name;
    private LocalDate   date;
    private LocalTime   startTime;
    private LocalTime   endTime;
    private boolean     wholeDayFlag;
    private int         responsibility;
    
    private int         agentID;

    private static int  nextNumber = 1;
    
    /**
     * Default constructor. Initializes the workshift's data with empty values.
     */
    public Workshift() {
        this.id = 0;
        this.name = "";
        this.date = LocalDate.now();
        this.startTime = LocalTime.of(0, 0);
        this.endTime = LocalTime.of(0, 0);
        this.wholeDayFlag = false;
        this.responsibility = 0;
        this.agentID = 0;
    }
    

    /**
     * Constructor
     *
     * @param name           name of the shift
     * @param givenDate      shift's date
     * @param startTime      shift's start time
     * @param endTime        shift's end time
     * @param isWholeDay     indicates if the shift lasts for the whole day
     * @param responsibility group of responsibility
     */
    public Workshift(String name, LocalDate givenDate, String startTime,
            String endTime, boolean isWholeDay, int responsibility) {
        this.name = name;
        this.date = givenDate;
        this.startTime = LocalTime.parse(startTime);
        this.endTime = LocalTime.parse(endTime);
        this.wholeDayFlag = isWholeDay;
        this.responsibility = responsibility;
    }
    
    
    /**
     * Returns the start time of the shift.
     *
     * @return the start time of the shift as a LocalTime object
     */
    public LocalTime getStartTimeLT() {
        return this.startTime;
    }
    
    
    /**
     * Returns the end time of the shift.
     *
     * @return the end time of the shift as a LocalTime object
     */
    public LocalTime getEndTimeLT() {
        return this.endTime;
    }
    
    
    /**
     * Sets the agent for the shift based on the given agent ID.
     *
     * @param givenAgentID the ID of the agent
     */
    public void setAgent(int givenAgentID) {
        this.agentID = givenAgentID;
    }
    
    
    /**
     * Sets the ID for the shift and ensures that nextNumber is larger than the largest ID used so far.
     *
     * @param nr the ID to be set
     */
    private void setID(int nr) {
        this.id = nr;
        if ( this.id >= nextNumber ) nextNumber = this.id + 1;
    }
    
    
    /**
     * Clones the workshift object.
     *
     * @return a clone of the workshift
     * @throws CloneNotSupportedException if cloning is not supported
     */
    @Override
    public Workshift clone() throws CloneNotSupportedException {
        Workshift newShift;
        newShift = (Workshift) super.clone();
        return newShift;
    }
    
    
    /**
     * Sets the value of the specified field.
     * 1 = name, 2 = start time, 3 = end time, 4 = whole day flag, 5 = responsibility
     *
     * @param k         the index of the field to be set
     * @param newString the new value to be set
     * @return null if the setting is successful, otherwise the corresponding error message
     */
    public String set(int k, String newString) {
        String tString = newString.trim();
        String error = null;
        switch ( k ) {
        case 1:
            this.name = tString;
            return null;
        case 2:
            TimeFormatVerifier time = new TimeFormatVerifier();
            error = time.verify(newString);
            if (error != null) return error;
            
            StringBuilder sb = new StringBuilder(newString);
            String hours = sb.substring(0, 2);
            String minutes = sb.substring(3);
            int hour = Integer.parseInt(hours);
            int minute = Integer.parseInt(minutes);
            this.startTime = LocalTime.of(hour, minute);
            return null;
        case 3:
            TimeFormatVerifier time2 = new TimeFormatVerifier();
            error = time2.verify(newString);
            if (error != null) return error;
            
            StringBuilder sb2 = new StringBuilder(newString);
            String hours2 = sb2.substring(0, 2);
            String minutes2 = sb2.substring(3);
            int hour2 = Integer.parseInt(hours2);
            int minute2 = Integer.parseInt(minutes2);
            this.endTime = LocalTime.of(hour2, minute2);
            return null;
        case 4:
            this.wholeDayFlag = Boolean.parseBoolean(tString);
            return null;
        case 5:
            this.responsibility = Integer.parseInt(tString);
            return null;
        default:
            System.out.println("IDIOT");
        }
        return "";
    }
    
    
    /**
     * Sets the date of the workshift.
     *
     * @param givenDate the date to be set
     */
    public void setDate(LocalDate givenDate) {
        this.date = givenDate;
    }
    
    
    /**
     * Registers the workshift and assigns it the next available ID.
     *
     * @return the registered ID of the workshift
     */
    public int register() {
        this.id = nextNumber;
        nextNumber++;
        return this.id;
    }
    
    
    /**
     * Returns the ID of the workshift.
     *
     * @return the ID of the workshift
     */
    public int getID() {
        return this.id;
    }
    
    
    /**
     * Returns the name of the workshift.
     *
     * @return the name of the workshift
     */
    public String getName() {
        return this.name;
    }
    
    
    /**
     * Returns the ID of the agent assigned to the workshift.
     *
     * @return the ID of the agent assigned to the workshift
     */
    public int getAgentsID() {
        return this.agentID;
    }
    
    
    /**
     * Returns the date of the workshift.
     *
     * @return the date of the workshift as a LocalDate object
     */
    public LocalDate getDate() {
        return this.date;
    }
    
    
    /**
     * Returns the weekday of the workshift as a string.
     *
     * @return the weekday of the workshift as a string
     */
    public String getWeekdayOfShift() {
        return this.date.getDayOfWeek().toString();
    }
    
    
    /**
     * Returns the start time of the workshift as a string.
     *
     * @return the start time of the workshift as a string
     */
    public String getStartTime() {
        return this.startTime.toString();
    }
    
    
    /**
     * Returns the end time of the workshift as a string.
     *
     * @return the end time of the workshift as a string
     */
    public String getEndTime() {
        return this.endTime.toString();
    }
    
    

    /**
     * Returns a flag indicating if the workshift lasts for the whole day.
     *
     * @return true if the workshift lasts for the whole day, false otherwise
     */
    public boolean getWholeDayFlag() {
        return this.wholeDayFlag;
    }
    
    
    /**
     * Returns a string representation of the whole day flag.
     *
     * @return "Yes" if the workshift lasts for the whole day, "No" otherwise
     */
    public String getWholeDayString() {
        if (this.wholeDayFlag == true) return "Yes";
        return "No";
    }
    
    
    /**
     * Returns the responsibility number.
     *
     * @return the responsibility number as an int
     */
    public int getResponsibility() {
        return this.responsibility;
    }
    
    
    /**
     * Returns the workshift information as a string that can be saved to a file.
     *
     * @return the workshift information as a pipe-delimited string
     */
    @Override
    public String toString() {
        return "" +
                this.getID() + "|" +
                this.name + "|" +
                this.date + "|" +
                this.startTime + "|" +
                this.endTime + "|" +
                this.wholeDayFlag + "|" +
                this.responsibility + "|" +
                this.agentID;
    }
    
    
    /**
     * Extracts the workshift information from a pipe-delimited string.
     *
     * @param row the string from which the workshift information is extracted
     */
    public void parse(String row) {
        StringBuffer sb = new StringBuffer(row);
        this.setID(Mjonot.erota(sb, '|', this.getID()));
        this.name = Mjonot.erota(sb, '|', this.name);
        this.date = LocalDate.parse(Mjonot.erota(sb, '|', this.date.toString()));
        this.startTime = LocalTime.parse(Mjonot.erota(sb, '|', this.startTime.toString()));
        this.endTime = LocalTime.parse(Mjonot.erota(sb, '|', this.endTime.toString()));
        this.wholeDayFlag = Boolean.parseBoolean(Mjonot.erota(sb, '|', this.wholeDayFlag));
        this.responsibility = Mjonot.erota(sb, '|', this.responsibility);
        this.agentID = Mjonot.erota(sb, '|', this.agentID);
    }
    
    
    /**
     * Extracts the default workshift information from a pipe-delimited string.
     *
     * @param row        the string from which the workshift information is extracted
     * @param givenDate  the date of the workshift
     */
    public void parseDefaultShift(String row, LocalDate givenDate) {
        StringBuffer sb = new StringBuffer(row);
        this.name = Mjonot.erota(sb, '|', this.name);
        this.date = givenDate;
        this.startTime = LocalTime.parse(Mjonot.erota(sb, '|', this.startTime.toString()));
        this.endTime = LocalTime.parse(Mjonot.erota(sb, '|', this.endTime.toString()));
        this.wholeDayFlag = Boolean.parseBoolean(Mjonot.erota(sb, '|', this.wholeDayFlag));
        this.responsibility = Mjonot.erota(sb, '|', this.responsibility) - 1;
        this.agentID = 0;
    }

    
    /**
     * Comparator for comparing two workshifts based on their names.
     */
    public static class Comparer implements Comparator<Workshift>{
        @Override
        public int compare(Workshift workShiftOne, Workshift workShiftTwo) {
            return workShiftOne.getName().compareTo(workShiftTwo.getName());
        }
    }
    
    /**
     * Comparator for comparing two workshifts based on their dates.
     */
    public static class ComparerDate implements Comparator<Workshift>{
        @Override
        public int compare(Workshift workShiftOne, Workshift workShiftTwo) {
            return workShiftOne.getDate().compareTo(workShiftTwo.getDate());
        }
    }
}
