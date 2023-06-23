package shiftAllocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Register class represents a register that holds information about workshifts, agents, and absences.
 * It provides various methods to add, replace, and retrieve information from the register.
 * The register can be saved to a file and loaded back from the file.
 * 
 * @author Santeri Tammisto
 * @version 11.6.2023
 */
public class Register {
    
    @SuppressWarnings("javadoc")
    protected Workshifts workshifts = new Workshifts();
    @SuppressWarnings("javadoc")
    protected Agents agents = new Agents();
    @SuppressWarnings("javadoc")
    protected Absences absences = new Absences();

    @SuppressWarnings("javadoc")
    protected String directory = "";
    
    /**
     * Default constructor for the Register class.
     */
    public Register() {

    }
    
    
    /**
     * Replaces an agent in the data structure or adds a new agent if not found.
     * Takes ownership of the agent object.
     * 
     * @param agent the agent to be added or replaced
     */
    public void replaceOrAdd(Agent agent) {
        agents.replaceOrAdd(agent);
    }
    
    
    /**
     * Replaces a workshift in the data structure or adds a new workshift if not found.
     * Takes ownership of the workshift object.
     * 
     * @param workshift the workshift to be added or replaced
     */
    public void replaceOrAdd(Workshift workshift) {
        workshifts.replaceOrAdd(workshift);
    }
    

    /**
     * Replaces an absence in the data structure or adds a new absence if not found.
     * Takes ownership of the absence object.
     * 
     * @param absence the absence to be added or replaced
     */
    public void replaceOrAdd(Absence absence) {
        absences.replaceOrAdd(absence);
    }
    
    
    /**
     * Searches for an agent in the data structure based on the identification number.
     * 
     * @param idNumber the identification number of the agent
     * @return the agent object if found, or null if not found
     */
    public Agent findAgent(int idNumber) {
        return agents.findAgent(idNumber);
    }
    
    
    /**
     * Adds a new workshift to the register.
     * 
     * @param workshift the workshift to be added
     */
    public void add(Workshift workshift) {
        this.workshifts.add(workshift);
    }
    
    
    /**
     * Adds a new agent to the register.
     * 
     * @param agent the agent to be added
     */
    public void add(Agent agent) {
        this.agents.addAgent(agent);
    }
    
    
    /**
     * Adds a new absence to the register.
     * 
     * @param absence the absence to be added
     */
    public void add(Absence absence) {
        this.absences.add(absence);
    }
    
    
    /**
     * Retrieves all absences for a specific agent.
     * 
     * @param agent the agent for whom absences are being retrieved
     * @return a list of absences for the agent
     */
    public List<Absence> getAbsences(Agent agent) {
        return absences.getAbsences(agent.getIDNumber());
    }

    
    /**
     * Returns the number of workshifts in the register.
     * 
     * @return the count of workshifts
     */
    public int getWorkshiftsSize() {
        return this.workshifts.getSize();
    }
    
    
    /**
     * Returns the count of agents in the register.
     * 
     * @return the count of agents
     */
    public int getAgentsCount() {
        return this.agents.getCount();
    }
    
    
    /**
     * Retrieves the workshift at the specified index in the register.
     * 
     * @param index the position of the workshift
     * @return the workshift at the specified index
     */
    public Workshift getWorkshift(int index) {
        return this.workshifts.getShift(index);
    }
    
    
    /**
     * Retrieves the agent at the specified index in the register.
     * 
     * @param index the position of the agent (starting from 0)
     * @return the agent at the specified index
     */
    public Agent getAgent(int index) {
        return this.agents.getAgent(index);
    }
    
    
    /**
     * Finds agents that match the specified search criteria.
     * 
     * @param clause the search criteria
     * @param k the index of the field to search
     * @return a collection of agents that match the search criteria
     */
    public Collection<Agent> findAgents(String clause, int k) {
        return agents.findAgents(clause, k);
    }
    
    
    /**
     * Finds workshifts that match the specified search date.
     * 
     * @param searchDate the date being searched
     * @return a collection of workshifts that match the search date
     */
    public Collection<Workshift> findWorkshifts(LocalDate searchDate) {
        return workshifts.find(searchDate);
    }
    
    
    /**
     * Finds all available workshifts in the register.
     * 
     * @return a list of workshifts that have no assigned agent yet
     */
    public ArrayList<Workshift> findVacantWorkshifts(){
        return workshifts.findVacantWorkshifts();
    }
        
    
    /**
     * Removes the specified absence from the register.
     * 
     * @param absence the absence to be removed
     */
    public void removeAbsence(Absence absence) {
        absences.remove(absence);
    }
    

    /**
     * Removes the given agent and their absences from the register.
     * 
     * @param agent the agent to be removed
     * @return 1 if the agent was successfully removed, 0 if the agent was not found
     */
    public int removeAgent(Agent agent) {
        if ( agent == null ) return 0;
        int ret = agents.removeAgent(agent.getIDNumber());
        absences.removeAgentsAbsences(agent.getIDNumber());
        return ret;
    }
    

    /**
     * Removes the specified workshift from the register.
     * 
     * @param workshift the workshift to be removed
     */
    public void removeWorkshift(Workshift workshift) {
        workshifts.remove(workshift);
    }
    
    
    /**
     * Saves the register's information to a file.
     * 
     * @throws SailoException if there is a problem encountered in saving
     */
    public void save() throws SailoException {
        String error = "";
        try {
            agents.save(directory);
            // workshifts.save(directory);
            // absences.save(directory);
        } catch ( SailoException ex ) {
            error = ex.getMessage();
        }
        if ( !"".equals(error) ) throw new SailoException(error);
    }

}
