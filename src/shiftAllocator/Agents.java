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
 * @author Santeri Tammisto
 * @version 11.6.2023
 *
 */
public class Agents {
    
    private static int MAX_AGENTS = 5;
    
    private int count = 0;
    private Agent[] items;
    
    
    /**
     * Creates initial array
     */
    public Agents() {
        items = new Agent[MAX_AGENTS];
    }
    
    
    /**
     * Adds the given agent to the next available element.
     * @param agent The agent to be added to the elements.
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
     * Returns a reference to the i-th agent.
     * @param i the index of the agent reference desired
     * @return a reference to the agent with the index i
     * @throws IndexOutOfBoundsException if i is out of the allowed range
     */
    public Agent getAgent(int i) throws IndexOutOfBoundsException {
        if (i < 0 || this.count <= i)
            throw new IndexOutOfBoundsException("Illegal index: " + i);
        return items[i];
    }
    
    
    /**
     * Replaces an agent in the data structure. Takes ownership of the agent.
     * Searches for a agent with the same identification number. If not found, adds as a new agent.
     * @param agent reference to the agent to be added.
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
     * Searches for an agent in the data structure based on the identification number.
     * @param idNumber the number used to search for the agent
     * @return null if no agent with the specified identification number is found, otherwise a reference to the found agent
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
     * @return the amount of agents in the register
     */
    public int getCount() {
        return this.count;
    }
    
    
    /**
     * Reads agents from a file
     * @param directory directory of the file
     * @throws SailoException if reading fails
     */
    public void readFromFile(String directory) throws SailoException {
        String name = directory + "/agents.dat";
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
     * Saves agents to a file
     * @param directory directory of the file
     * @throws SailoException if saving fails
     */
    public void save(String directory) throws SailoException {
        File ftied = new File(directory + "/agents.dat");
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
     * @param clause what is being searched
     * @param k the index of the field to search by
     * @return the found items
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
     * @param teamNumber of the team
     * @return ArrayList with all the agents in the team in alphabetical order
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
     * Searches and returns an ArrayList of agents who have the fewest shifts in the shiftAmountAll attribute / smallest value.
     * If the loop doesn't find any agents after 30 iterations, it returns null.
     * @return ArrayList<Integer> containing the identification numbers of the agents
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
     * Finds an agents index based on its ID-number
     * @param id of the agent that's being searched
     * @return index of the agent if found, -1 if not
     */
    public int findIndexWithID(int id) {
        for (int i = 0; i < this.count; i++) {
            if (id == this.items[i].getIDNumber()) return i;
        }
        return -1;
    }
    
    
    /**
     * Removes given agent from the array and makes it smaller
     * @param id of the agent
     * @return 1 if success, 0 if not found
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
