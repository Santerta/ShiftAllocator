package shiftAllocator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import fi.jyu.mit.ohj2.Mjonot;
import verifiers.TimeFormatVerifier;
import verifiers.DateFormatVerifier;

/**
 * @author Santeri Tammisto
 * @version 10.6.2023
 *
 */
public class Absence implements Cloneable {
    
    private int         id;
    private LocalDate   date;
    private LocalTime   startTime;
    private LocalTime   stopTime;
    private boolean     wholeDayFlag;
    private String      explanation;
    private int         agentID;
    
    private static int  nextNumber = 1;
    
    /**
     * Sets the ID and ensures that it is always greater than the current maximum nextNumber.
     * @param nr The ID to be set.
     */
    private void setID(int number) {
        this.id = number;
        if ( this.id >= nextNumber ) nextNumber = this.id + 1;
    }
    
    
    /**
     * Default constructor, Initializes absence information as empty.
     */
    public Absence() {
        LocalDate dateToSet = LocalDate.now();
        this.date = dateToSet.with(TemporalAdjusters.firstDayOfNextMonth());
        
        this.startTime = LocalTime.of(0, 0);
        this.stopTime = LocalTime.of(0, 0);
        this.wholeDayFlag = false;
        this.explanation = "";
    }
    
    /**
     * Initializes absence for specific agent
     * @param agentsID ID of the agent
     */
    public Absence(int agentsID) {
        LocalDate dateToSet = LocalDate.now();
        this.date = dateToSet.with(TemporalAdjusters.firstDayOfNextMonth());
        
        this.startTime = LocalTime.of(0, 0);
        this.stopTime = LocalTime.of(0, 0);
        this.wholeDayFlag = false;
        this.explanation = "";
        
        this.agentID = agentsID;
    }
    
    
    /**
     * Creates an absence with given information
     * @param date The provided date
     * @param startTime The provided start time
     * @param endTime The provided end time
     * @param wholeDay Indicates if it's a full-day absence
     * @param explanation The explanation of the absence
     * @param agentID The ID of the agent to whom the absence is created
     */
    public Absence(LocalDate date, LocalTime startTime, LocalTime endTime, boolean wholeDay, String explanation, int agentID) {
        this.date = date;
        this.startTime = startTime;
        this.stopTime = endTime;
        this.wholeDayFlag = wholeDay;
        this.explanation = explanation;
        this.agentID = agentID;
    }
    
    
    @Override
    public Absence clone() throws CloneNotSupportedException {
        Absence newTemp;
        newTemp = (Absence) super.clone();
        return newTemp;
    }
    
    
    /**
     * Set the value of the k-th field to the provided string value
     * @param k Which field's value is being set
     * @param newString The string value to be set as the field's value
     * @return null if the setting is successfull, otherwise the corresponding error message.
     */
    public String setValueFor(int k, String newString) {
        String tString = newString.trim();
        String error = null;
        
        switch ( k ) {
        case 1:
            DateFormatVerifier dateVerifier = new DateFormatVerifier();
            error = dateVerifier.verify(newString);
            if (error != null) return error;
            this.date = LocalDate.parse(newString);
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
            this.stopTime = LocalTime.of(hour2, minute2);
            return null;
        case 4:
            this.wholeDayFlag = Boolean.parseBoolean(tString);
            return null;
        case 5:
            this.explanation = tString;
            return null;
        default:
            System.out.println("Idiot");
        }
        return "";
    }
    
    
    /**
     * @return Start-time in string-format
     */
    public String getStartTime() {
        return this.startTime.toString();
    }
    
    
    /**
     * @return Stop-time in string-format
     */
    public String getStopTime() {
        return this.stopTime.toString();
    }
    
    
    /**
     * @return Start-time in LocalTime
     */
    public LocalTime getStarTimeLT() {
        return this.startTime;
    }
    
    
    /**
     * @return Stop-time in LocalTime
     */
    public LocalTime getStopTimeLT() {
        return this.stopTime;
    }
    
    
    /**
     * @return boolean-value if wholeDayFlag is true or false
     */
    public boolean getWholeDayFlag() {
        return this.wholeDayFlag;
    }
    
    
    /**
     * @return explanation for the absence in String-format
     */
    public String getExplanation() {
        return this.explanation;
    }
    
    
    /**
     * @return date of the absence
     */
    public LocalDate getDate() {
        return this.date;
    }
    
    
    /**
     * Antaa poissaololle seuraavan id-numeron.
     * @return rekisteröidyn poissaolon id
     */
    public int register() {
        this.id = nextNumber;
        nextNumber++;
        return this.id;
    }
    
    
    /**
     * @return ID of the absence
     */
    public int getID() {
        return this.id;
    }
    
    
    /**
     * @return ID of the agent that has the absence
     */
    public int getAgentsID() {
        return this.agentID;
    }
    
    
    /**
     * Returns the absence information as a string that can be saved to a file.
     * @return Absence information as a string separated by vertical bars
     */
    @Override
    public String toString() {
        return "" +
                this.getID() + "|" +
                this.date + "|" +
                this.agentID + "|" +
                this.startTime + "|" +
                this.stopTime + "|" +
                this.wholeDayFlag + "|" +
                this.explanation;
    }
    
    
    /**
     * Retrieves absence information from a string separated by vertical bars.
     * Ensures that the nextNumber is greater than the upcoming identifier.
     * @param row The row from which the absence information is taken.
     */
    public void parse(String row) {
        StringBuffer sb = new StringBuffer(row);
        this.setID(Mjonot.erota(sb, '|', this.getID()));
        this.date = LocalDate.parse(Mjonot.erota(sb, '|', this.date.toString()));
        this.agentID = Mjonot.erota(sb, '|', this.agentID);
        this.startTime = LocalTime.parse(Mjonot.erota(sb, '|', this.startTime.toString()));
        this.stopTime = LocalTime.parse(Mjonot.erota(sb, '|', this.stopTime.toString()));
        this.wholeDayFlag = Boolean.parseBoolean(Mjonot.erota(sb, '|', this.wholeDayFlag));
        this.explanation = Mjonot.erota(sb, '|', this.explanation);
    }
    
    
    /**
     * @return Weekday of the absence as a string
     */
    public String getWeekdayOfAbsence() {
        return this.date.getDayOfWeek().toString();
    }

}
