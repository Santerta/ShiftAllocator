package shiftAllocator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

import fi.jyu.mit.ohj2.Mjonot;
import verifiers.TimeFormatVerifier;

/**
 * @author Santeri Tammisto
 * @version 10.6.2023
 *
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
     * Default constructor. Initializes the workshifts data with empty values.
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
     * @param name name of the shift
     * @param givenDate shifts date
     * @param startTime shift start time
     * @param endTime shifts end time
     * @param isWholeDay if the shift lasts for the whole day
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
     * @return start time of the shift in LocalTime
     */
    public LocalTime getStartTimeLT() {
        return this.startTime;
    }
    
    /**
     * @return end time of the shift in LocalTime
     */
    public LocalTime getEndTimeLT() {
        return this.endTime;
    }
    
    
    /**
     * Sets the agent to the shift based on given agents ID
     * @param givenAgentID ID of the agent
     */
    public void setAgent(int givenAgentID) {
        this.agentID = givenAgentID;
    }
    
    
    /**
     * Sets the ID for the shift and makes sure that nextNumber is larger than the biggest one yet
     * @param nr ID to be set
     */
    private void setID(int nr) {
        this.id = nr;
        if ( this.id >= nextNumber ) nextNumber = this.id + 1;
    }
    
    
    @Override
    public Workshift clone() throws CloneNotSupportedException {
        Workshift newShift;
        newShift = (Workshift) super.clone();
        return newShift;
    }
    
    
    /**
     * Sets the value of the k-th field to the provided string value.
     * @param k Which field's value is being set. 1 = name, 2 = start time, 3 = end time, 4 = whole day flag, 5 = responsibility
     * @param newString The string value to be set as the field's value.
     * @return null if the setting is successful, otherwise the corresponding error message.
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
     * Sets the value of givenDate to this workshifts date
     * @param givenDate LocalDate that's given
     */
    public void setDate(LocalDate givenDate) {
        this.date = givenDate;
    }
    
    
    /**
     * Gives a shift the next ID
     * @return registered shifts ID
     */
    public int register() {
        this.id = nextNumber;
        nextNumber++;
        return this.id;
    }
    
    /**
     * Returns shifts ID
     * @return ID of the shift
     */
    public int getID() {
        return this.id;
    }
    
    
    /**
     * @return shifts name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @return ID of the agent that has the shift
     */
    public int getAgentsID() {
        return this.agentID;
    }
    
    
    /**
     * @return date of the shift in LocalDate
     */
    public LocalDate getDate() {
        return this.date;
    }
    
    
    /**
     * @return weekday of the shift as a string
     */
    public String getWeekdayOfShift() {
        return this.date.getDayOfWeek().toString();
    }
    
    
    /**
     * @return start time of the shift as a string
     */
    public String getStartTime() {
        return this.startTime.toString();
    }
    
    
    /**
     * @return end time of the shift as a string
     */
    public String getEndTime() {
        return this.endTime.toString();
    }
    
    
    /**
     * @return true or false depending on if the shift lasts for the whole day
     */
    public boolean getWholeDayFlag() {
        return this.wholeDayFlag;
    }
    
    /**
     * @return "Yes" if the shift lasts for whole day and "No" if it doesn't
     */
    public String getWholeDayString() {
        if (this.wholeDayFlag == true) return "Yes";
        return "No";
    }
    
    
    /**
     * @return responsibility number as an int
     */
    public int getResponsibility() {
        return this.responsibility;
    }
    
    
    /**
     * Returns the shift information as a string that can be saved to a file.
     * @return The shift information as a pipe-delimited string.
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
     * Extracts the shift information from a pipe-delimited string.
     * Ensures that the nextNumber is greater than the incoming task number.
     * @param row The string from which the shift information is extracted.
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
     * Extracts the default shift information from a pipe-delimited string.
     * @param row The String from which the shift information is extracted.
     * @param givenDate date of the shift
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
     * Class that can compare two shifts based on their name
     */
    public static class Comparer implements Comparator<Workshift>{
        @Override
        public int compare(Workshift workShiftOne, Workshift workShiftTwo) {
            return workShiftOne.getName().compareTo(workShiftTwo.getName());
        }
    }
    
    /**
     * Class that can compare two shifts based on their date
     */
    public static class ComparerDate implements Comparator<Workshift>{
        @Override
        public int compare(Workshift workShiftOne, Workshift workShiftTwo) {
            return workShiftOne.getDate().compareTo(workShiftTwo.getDate());
        }
    }
}
