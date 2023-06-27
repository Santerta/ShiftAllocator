package shiftAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

import fi.jyu.mit.ohj2.WildChars;

/**
 * The Agents class represents a collection of agents and provides methods to manage the agents.
 * Agents can be added, retrieved, replaced or added, found by identification number or specific fields,
 * read from a file, saved to a file, and searched using various criteria.
 * The class also includes methods to get the count of agents, get all members of a team,
 * find agents with the fewest shifts, find an agent's index based on their ID number,
 * and remove agents from the collection.
 *
 * @author Santeri Tammisto
 * @version 11.6.2023
 */
public class Agents {
    
    private static int MAX_AGENTS = 5;
    
    private int count = 0;
    private Agent[] items;
    
    
    /**
     * Creates a new instance of the Agents class.
     * Initializes the agent array with a default capacity.
     */
    public Agents() {
        items = new Agent[MAX_AGENTS];
    }
    
    
    /**
     * Adds the given agent to the collection.
     * If the collection is full, the capacity is increased.
     *
     * @param agent The agent to be added to the collection.
     */
    public void addAgent(Agent agent) {
        if (count >= items.length) {
            MAX_AGENTS = MAX_AGENTS + 5;
            Agent[] newItems = new Agent[MAX_AGENTS];
            for (int i = 0; i < items.length; i++) {
                newItems[i] = items[i];
            }
            items = newItems;
        }
        items[count] = agent;
        count++;
    }
    
    
    /**
     * Returns a reference to the agent at the specified index.
     *
     * @param i The index of the agent reference to be retrieved.
     * @return The agent at the specified index.
     * @throws IndexOutOfBoundsException If the index is out of the allowed range.
     */
    public Agent getAgent(int i) throws IndexOutOfBoundsException {
        if (i < 0 || this.count <= i)
            throw new IndexOutOfBoundsException("Illegal index: " + i);
        return items[i];
    }
    
    
    /**
     * Replaces an agent in the collection or adds it as a new agent.
     * The replacement is based on the identification number of the agent.
     *
     * @param agent The agent to be replaced or added.
     */
    public void replaceOrAdd(Agent agent) {
        int id = agent.getIDNumber();
        for (int i = 0; i < count; i++ ) {
            if (items[i].getIDNumber() == id ) {
                items[i] = agent;
                return;
            }
        }
        this.addAgent(agent);
    }
    
    
    /**
     * Searches for an agent in the collection based on the identification number.
     *
     * @param idNumber The identification number used to search for the agent.
     * @return The agent with the specified identification number, or null if not found.
     */
    public Agent findAgent(int idNumber) {
        for (int i = 0; i < count; i++) {
            if (items[i].getIDNumber() == idNumber ) {
                Agent found = items[i];
                return found;
            }
        }
        return null;
    }
    
    
    /**
     * Returns the number of agents in the collection.
     *
     * @return The count of agents in the collection.
     */
    public int getCount() {
        return this.count;
    }
    
    
    /**
     * Reads agents from a file and adds them to the collection.
     *
     * @param directory The directory of the file.
     * @throws SailoException If reading from the file fails.
     */
    public void readFromFile(String directory) throws SailoException {
        String name = directory + "/data/agents.dat";
        File ftied = new File(name);
        
        try (Scanner fi = new Scanner(new FileInputStream(ftied))) {
            while ( fi.hasNext() ) {
                String s = fi.nextLine();
                if ( s == null || "".equals(s) || s.charAt(0) == ';' ) continue;
                Agent agent = new Agent();
                agent.parse(s);
                this.addAgent(agent);
            }
        } catch ( FileNotFoundException e) {
            throw new SailoException("Can't read file " + name);
        }
    }
    
    
    /**
     * Saves agents to a file.
     *
     * @param directory The directory of the file.
     * @throws SailoException If saving to the file fails.
     */
    public void save(String directory) throws SailoException {
        File ftied = new File(directory + "/data/agents.dat");
        try (PrintStream fo = new PrintStream(new FileOutputStream(ftied, false))) {
            for (int i = 0; i < this.getCount(); i++) {
                Agent agent = this.getAgent(i);
                fo.println(agent.toString());
            }
        } catch (FileNotFoundException ex) {
            throw new SailoException("File " + ftied.getAbsolutePath() + " can't be opened");
        }
    }
    
    
    /**
     * Searches for agents in the collection based on a specified clause and field index.
     *
     * @param clause The search criteria.
     * @param k      The index of the field to search by.
     * @return A collection of agents that match the search criteria.
     */
    public Collection<Agent> findAgents(String clause, int k) {
        ArrayList<Agent> found = new ArrayList<Agent>();
        int hk = k;
        if (hk < 0) hk = 1;
        for (int i = 0; i < this.getCount(); i++ ) {
            Agent agent = this.getAgent(i);
            String content = agent.get(hk);
            if ( WildChars.onkoSamat(content, clause) ) {
                found.add(agent);
            }
        }
        Collections.sort(found, new Agent.Comparer());
        return found;
    }
    
    
    /**
     * Returns an ArrayList of all the agents in a specified team, sorted in alphabetical order.
     *
     * @param teamNumber The team number.
     * @return ArrayList containing all the agents in the team in alphabetical order.
     */
    public ArrayList<Agent> getAllMembersOfTeam(int teamNumber){
        ArrayList<Agent> found = new ArrayList<Agent>();
        for ( int i = 0; i < this.getCount(); i++ ) {
            Agent agent = this.getAgent(i);
            if ( agent.getTeamNumber() == teamNumber ) {
                found.add(agent);
            }
        }
        
        Collections.sort(found, new Agent.Comparer());
        return found;
    }

    
    /**
     * Returns an ArrayList of agents who have the fewest shifts.
     * The search is based on the shiftAmountAll attribute, where the smallest value indicates the fewest shifts.
     * If no agents are found after 30 iterations, null is returned.
     *
     * @return An ArrayList of agents with the fewest shifts, or null if not found.
     */
    public ArrayList<Agent> findAgentsWithLeastShifts(){
        ArrayList<Agent> found = new ArrayList<Agent>();
        int minimumShifts = 0;
        for (int i = 0 ; i < this.getCount() ; i++) {
            Agent agent = this.getAgent(i);
            if ( agent.getShiftAmountAll() <= minimumShifts ) { 
                found.add(agent); // if this agent has the same amount or less shifts than current minimum, agent is added to found, which has every agent with the least shifts
            }
            if ( i == this.getCount()-1 && found.size() == 0) {
                // if there's no more agents and algorithm hasn't found any agents with less or equal shifts than current minimum, increase minimum by one and start the agent-looping from 0
                minimumShifts++;
                i = 0;
                if ( minimumShifts > 30) return null;
            }
        }
        return found;
    }
    
    
    /**
     * Finds the index of an agent in the collection based on its ID number.
     *
     * @param id The ID number of the agent to be searched.
     * @return The index of the agent if found, or -1 if not found.
     */
    public int findIndexWithID(int id) {
        for (int i = 0; i < this.count; i++) {
            if (id == this.items[i].getIDNumber()) return i;
        }
        return -1;
    }
    
    
    /**
     * Removes an agent from the collection.
     *
     * @param id The ID of the agent to be removed.
     * @return 1 if the agent is successfully removed, 0 if the agent is not found.
     */
    public int removeAgent(int id) {
        int ind = findIndexWithID(id);
        if (ind < 0) return 0;
        count--;
        for (int i = ind; i < count; i++) {
            this.items[i] = items[i+1];
        }
        items[count] = null;
        return 1;
    }
}
