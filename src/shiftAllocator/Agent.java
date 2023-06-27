package shiftAllocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

import fi.jyu.mit.ohj2.Mjonot;

/**
 * Represents an agent with various attributes such as ID, name, team number, responsibilities, and shift information.
 * Provides methods to set and get agent information, manipulate shift amounts, and perform comparisons.
 * Supports cloning and parsing from a string representation.
 * 
 * The class also includes an inner `Comparer` class that implements the `Comparator` interface for comparing agents based on their full names.
 * 
 * @author Santeri Tammisto
 * @version 11.6.2023
 */
public class Agent implements Cloneable {
    
    private int         id;
    private String      firstName = "";
    private String      lastName = "";
    private int         teamNumber = 1;
    private String      defaultState = "";
    private double      shiftIncreaseModifier = 1.0;
    
    private boolean[] responsibilitiesArray = new boolean[Allocator.responsibilities.size()];
    
    private double[] amountOfShiftsArray = new double[Allocator.responsibilities.size()];
    private double shiftAmountAll = 0.0;
    
    private ArrayList<LocalDate> allocatedDays = new ArrayList<>();
    
    private static int  nextNumber = 1;
    
    
    /**
     * Default constructor. Initializes the employee's information as empty.
     */
    public Agent() {
        //
    }
    
    
    /**
     * Adds a date to the set of allocated days for the agent.
     * 
     * @param date the date to be added as an allocated day
     */
    public void addAllocatedDay(LocalDate date) {
        this.allocatedDays.add(date);
    }
    
    /**
     * Checks if the agent has already been allocated points for a specific date.
     * 
     * @param date the date to check for allocated points
     * @return {@code true} if the agent has already been allocated points for the date, {@code false} otherwise
     */
    public boolean hasAllocatedDay(LocalDate date) {
        return allocatedDays.contains(date);
    }
    
    
    /**
     * Returns the responsibility value of the agent corresponding to the specified index as a boolean value.
     * 
     * @param index the index of the responsibility being searched in the responsibility array
     * @return {@code true} if the responsibility exists, {@code false} otherwise
     */
    public boolean getResponsibilityByIndex(int index) {
        return this.responsibilitiesArray[index];
    }
    
    
    /**
     * Sets the k-th responsibility value to either true or false.
     * 
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
     * 
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
     * 
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
     * 
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
     * Returns the total number of shifts for the agent.
     * 
     * @return the total number of shifts for the agent
     */
    public double getShiftAmountAll() {
        return this.shiftAmountAll;
    }
    
    
    /**
     * Returns the length of the responsibilities array.
     * 
     * @return the length of the responsibilities array
     */
    public int getResponsibilitiesArrayLength() {
        return this.responsibilitiesArray.length;
    }

    
    /**
     * Returns the amount of shifts the agent has in the specified responsibility index.
     * 
     * @param index the index of the responsibility
     * @return the amount of shifts the agent has in the specified responsibility index
     */
    public double getShiftAmount(int index) {
        return this.amountOfShiftsArray[index];
    }
    
    
    /**
     * Increases the number of shifts for the agent by one in the specified responsibility index.
     * 
     * @param shiftIndex the shift thats amount is to be increased
     */
    public void increaseShiftsByOne(int shiftIndex) {
        this.amountOfShiftsArray[shiftIndex] += 1.0;
        this.shiftAmountAll += (1.0 * this.shiftIncreaseModifier);
    }
    
    /**
     * Increases the total number of shifts for the agent by the specified absence worth.
     * 
     * @param absenceWorth the value by which the total number of shifts is increased
     */
    public void increaseShiftsByAbsenceModifier(double absenceWorth) {
        this.shiftAmountAll += absenceWorth;
    }
    
    
    /**
     * Returns the number of fields in the agent.
     * 
     * @return the number of fields in the agent
     */
    public int getFields() {
        return 6;
    }
    
    
    /**
     * Returns the index of the first reasonable field to ask.
     * 
     * @return the index of the first reasonable field to ask
     */
    public int firstField() {
        return 1;
    }
    
    
    /**
     * Clones the agent object.
     * 
     * @return the cloned agent object
     */
    @Override
    public Agent clone() throws CloneNotSupportedException {
        Agent newAgent;
        newAgent = (Agent) super.clone();
        return newAgent;
    }

    
    /**
     * Sets the ID and ensures that the next number (nextNumber) is always greater than the current maximum.
     * 
     * @param nr the ID to be set
     */
    private void setIDNumber(int nr) {
        this.id = nr;
        if (this.id >= nextNumber) nextNumber = this.id + 1;
    }
    
    
    /**
     * Provides the next ID number for an agent.
     * 
     * @return the ID of the registered agent
     */
    public int register() {
        this.id = nextNumber;
        nextNumber++;
        return this.id;
    }
    
    /**
     * Returns the ID of the agent.
     * 
     * @return the ID of the agent
     */
    public int getIDNumber() {
        return this.id;
    }
    
    
    /**
     * Returns the full name of the agent.
     * 
     * @return the full name of the agent as "firstName lastName"
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    
    /**
     * Returns the team number of the agent.
     * 
     * @return the team number of the agent as an integer
     */
    public int getTeamNumber() {
        return this.teamNumber;
    }

    
    /**
     * Returns "Yes" or "No" as a string depending on whether the agent has the specified responsibility.
     * 
     * @param index the index of the responsibility
     * @return "Yes" if the agent has the responsibility, "No" otherwise
     */
    public String getResponsibilityYesOrNo(int index) {
        if ( this.getResponsibilityByIndex(index) == true ) return "Yes";
        return "No";
    }
    
    
    /**
     * Returns the string representation of the agent object.
     * 
     * @return the string representation of the agent
     */
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
     * Parses the agent's information from a string representation.
     * 
     * @param row the string from which the agent's information is extracted
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
     * A comparator class that compares agents based on their full names.
     */
    public static class Comparer implements Comparator<Agent>{
        
        /**
         * Compares two agents based on their full names.
         * 
         * @param agent1 the first agent to compare
         * @param agent2 the second agent to compare
         * @return a negative integer if agent1 is less than agent2, zero if they are equal, a positive integer otherwise
         */
        @Override
        public int compare(Agent agent1, Agent agent2) {
            return agent1.getFullName().compareTo(agent2.getFullName());
        }
    }
}
