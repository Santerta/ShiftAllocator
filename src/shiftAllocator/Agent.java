package shiftAllocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

import fi.jyu.mit.ohj2.Mjonot;

/**
 * @author Santeri Tammisto
 * @version 11.6.2023
 *
 */
public class Agent implements Cloneable {
    private int         id;
    private String      firstName = "";
    private String      lastName = "";
    private int         teamNumber = 1;
    private String      defaultState = "";
    private double      shiftIncreaseModifier = 1.0;
    
    private boolean[] responsibilitiesArray = new boolean[Register.responsibilities.size()];
    
    private double[] amountOfShiftsArray = new double[Register.responsibilities.size()];
    private double shiftAmountAll = 0.0;
    
    private ArrayList<LocalDate> allocatedDays = new ArrayList<>();
    
    private static int  nextNumber = 1;
    
    
    
    /**
     * Adds a date to the set of allocated days for the agent.
     * @param date The date to be added as an allocated day.
     */
    public void addAllocatedDay(LocalDate date) {
        this.allocatedDays.add(date);
    }
    
    /**
     * Checks if the agent has already been allocated points for a specific date.
     * @param date The date to check for allocated points.
     * @return {@code true} if the agent has already been allocated points for the date, {@code false} otherwise.
     */
    public boolean hasAllocatedDay(LocalDate date) {
        return allocatedDays.contains(date);
    }
    
    
    /**
     * Default constructor. Initializes the employee's information as empty.
     */
    public Agent() {
        //
    }
    
    
    /**
     * Returns the responsibility value of the employee corresponding to the specified index as a boolean value.
     * @param index The index of the responsibility being searched in the responsibility array
     * @return true if the responsibility exists, false otherwise
     */
    public boolean getResponsibilityByIndex(int index) {
        return this.responsibilitiesArray[index];
    }
    
    
    /**
     * Sets the k-th responsibility value to either true or false.
     * @param k the index of the boolean value to be changed
     * @param newValue the boolean value
     * @return null if the setting is successful, otherwise the corresponding error message
     */
    public String setResponsibility(int k, boolean newValue) {
        this.responsibilitiesArray[k] = newValue;
        return null;
    }
    
    
    /**
     * Sets the value of the k-th field to the value of the provided string parameter.
     * 1 = first name
     * 2 = last name
     * 3 = team number
     * 4 = default state
     * 5 = shift increase modifier
     * @param k the index of the field for which the value is being set
     * @param newValue the string value to be set as the field's value
     * @return null if the setting is successful, otherwise the corresponding error message
     */
    public String set(int k, String newValue) {
        String tString = newValue.trim();
        switch ( k ) {
        case 1:
            this.firstName = tString;
            return null;
        case 2:
            this.lastName = tString;
            return null;
        case 3:
            this.teamNumber = Integer.parseInt(tString);
            return null;
        case 4:
            this.defaultState = tString;
            return null;
        case 5:
            this.shiftIncreaseModifier = Double.parseDouble(tString);
            return null;
        default:
            return "Ääliö!";
        }
    }

    
    /**
     * Returns the content of the k-th field as a string.
     * 0 = id
     * 1 = first name
     * 2 = last name
     * 3 = team number
     * 4 = default state
     * 5 = shift increase modifier
     * @param k the index of the field whose content is returned
     * @return the content of the field as a string
     */
    public String get(int k) {
        switch ( k ) {
        case 0: return "" + this.id;
        case 1: return "" + this.firstName;
        case 2: return "" + this.lastName;
        case 3: return "" + this.teamNumber;
        case 4: return "" + this.defaultState;
        case 5: return "" + this.shiftIncreaseModifier;
        default: return "IDIOT";
        }
    }
    
    
    /**
     * Returns the question corresponding to the k-th field.
     * @param k the index of the field for which the question is returned
     * @return the question corresponding to the k-th field
     */
    public String getQuestion(int k) {
        switch ( k ) {
        case 0: return "Agent ID:";
        case 1: return "First name:";
        case 2: return "Last name:";
        case 3: return "Team number:";
        case 4: return "Default state:";
        case 5: return "Shift increase modifier:";
        default: return "IDIOT";
        }
    }
    
    
    /**
     * @return how many shifts this agent has all together
     */
    public double getShiftAmountAll() {
        return this.shiftAmountAll;
    }
    
    
    /**
     * @return the length of responsibilites array
     */
    public int getResponsibilitiesArrayLength() {
        return this.responsibilitiesArray.length;
    }

    
    /**
     * @param index the shift that's value is asked
     * @return the amount of shifts this agent has in the index-th responsibility
     */
    public double getShiftAmount(int index) {
        return this.amountOfShiftsArray[index];
    }
    
    
    /**
     * Increases shiftAmountAll by (one * modifier) and particular shift by 1.0.
     * If agent has shiftIncreaseModifier
     * for example at 2.0, he will get twice less shifts assigned to him.
     * @param shiftIndex the shift thats amount is to be increased
     */
    public void increaseShiftsByOne(int shiftIndex) {
        this.amountOfShiftsArray[shiftIndex] += 1.0;
        this.shiftAmountAll += (1.0 * this.shiftIncreaseModifier);
    }
    
    /**
     * Increases shiftAmountAll by how much an absence of whole day is worth
     * @param absenceWorth how much shiftAmountAll is increased
     */
    public void increaseShiftsByAbsenceModifier(double absenceWorth) {
        this.shiftAmountAll += absenceWorth;
    }
    
    
    /**
     * Returns agents field amount for searches
     * @return the amount of fields
     */
    public int getFields() {
        return 6;
    }
    
    
    /**
     * First field that's reasonable to ask
     * @return index of that field
     */
    public int firstField() {
        return 1;
    }
    
    
    @Override
    public Agent clone() throws CloneNotSupportedException {
        Agent newAgent;
        newAgent = (Agent) super.clone();
        return newAgent;
    }

    
    /**
     * Sets the ID and ensures that the next number (nextNumber) is always greater than 
     * the current maximum.
     * @param nr The ID to be set.
     */
    private void setIDNumber(int nr) {
        this.id = nr;
        if (this.id >= nextNumber) nextNumber = this.id + 1;
    }
    
    
    /**
     * Provides the next ID number for an employee.
     * @return the ID of the registered employee
     */
    public int register() {
        this.id = nextNumber;
        nextNumber++;
        return this.id;
    }
    
    /**
     * @return ID of the agent
     */
    public int getIDNumber() {
        return this.id;
    }
    
    
    /**
     * @return Workers full name as "firstName lastName"
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    
    /**
     * @return agents team number as int
     */
    public int getTeamNumber() {
        return this.teamNumber;
    }

    
    /**
     * @param index of the responsibility
     * @return "Yes" or "No" as a string depending on if agent has responsibility
     */
    public String getResponsibilityYesOrNo(int index) {
        if ( this.getResponsibilityByIndex(index) == true ) return "Yes";
        return "No";
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id + "|" + this.firstName + "|" + this.lastName + "|" 
                + this.teamNumber + "|" + this.defaultState + "|" + this.shiftIncreaseModifier);
        
        for (int i = 0 ; i < this.getResponsibilitiesArrayLength() ; i++) {
            sb.append("|" + this.responsibilitiesArray[i]);
        }
        
        String result = sb.toString();
        return result;
    }

    
    /**
     * Retrieves the employee's information from a string separated by "|".
     * Ensures that the next number (nextNumber) is greater than the upcoming ID.
     * @param row The string from which the employee's information is extracted.
     */
    public void parse(String row) {
        StringBuilder sb = new StringBuilder(row);
        this.setIDNumber(Mjonot.erota(sb, '|', this.getIDNumber()));
        this.firstName = Mjonot.erota(sb, '|', this.firstName);
        this.lastName = Mjonot.erota(sb, '|', this.lastName);
        this.teamNumber = Mjonot.erota(sb, '|', this.teamNumber);
        this.defaultState = Mjonot.erota(sb, '|', this.defaultState);
        this.shiftIncreaseModifier = Mjonot.erota(sb, '|', this.shiftIncreaseModifier);
        for (int i = 0 ; i < this.getResponsibilitiesArrayLength() ; i++) {
            this.responsibilitiesArray[i] = Boolean.parseBoolean(Mjonot.erota(sb, '|', this.responsibilitiesArray[i]));
        }
    }
    

    /**
     * Class that can compare to agents
     */
    public static class Comparer implements Comparator<Agent>{
        @Override
        public int compare(Agent agent1, Agent agent2) {
            return agent1.getFullName().compareTo(agent2.getFullName());
        }
    }
}
