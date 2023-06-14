package shiftAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * @author Santeri Tammisto
 * @version 10.6.2023
 *
 */
public class Absences {
    
    private ArrayList<Absence> items = new ArrayList<Absence>();
    
    
    /**
     * Initialize
     */
    public Absences() {
        // 
    }
    
    
    /**
     * Replaces the absence in the data structure. Takes ownership of the absence.
     * Searches for an absence with the same ID. If not found, adds it as a new absence.
     * @param absence Reference to the absence to be added.
     */
    public void replaceOrAdd(Absence absence) {
        int id = absence.getID();
        for (int i = 0; i < this.items.size(); i++ ) {
            if ( this.items.get(i).getID() == id ) {
                this.items.set(i, absence);
                return;
            }
        }
        this.add(absence);
    }
    
    
    /**
     * @param absence to be added
     */
    public void add(Absence absence) {
        items.add(absence);
    }
    
    
    /**
     * Reads absences from directory
     * @param directory Files directory
     * @throws SailoException if reading gives an error
     */
    public void readFromFile(String directory) throws SailoException {
        String name = directory + "/absences.dat";
        File ftied = new File(name);
        try (Scanner fi = new Scanner(new FileInputStream(ftied))) {
            while ( fi.hasNext() ) {
                String s = fi.nextLine().trim();
                if ( "".equals(s) || s.charAt(0) == ';' ) continue;
                Absence absent = new Absence();
                absent.parse(s);
                this.add(absent);
            }
        } catch ( FileNotFoundException e) {
            throw new SailoException("Can't read the file  " + name);
        }
    }
    
    
    /**
     * Saves absences to a file
     * @param directory of the saveable file
     * @throws SailoException if saving fails
     */
    public void save(String directory) throws SailoException {
        File ftied = new File(directory + "/absences.dat");
        try (PrintStream fo = new PrintStream(new FileOutputStream(ftied, false))) {
            for (Absence absent: items) {
                fo.println(absent.toString());
            }
        } catch (FileNotFoundException ex) {
            throw new SailoException("File " + ftied.getAbsolutePath());
        }
    }
    
    
    /**
     * Retrieves all absences of a single employee.
     * @param agentsID The ID of the agent whose absences are being retrieved.
     * @return List containing references to the found absences
     */
    public List<Absence> getAbsences(int agentsID) {
        List<Absence> found = new ArrayList<Absence>();
        for (Absence absence : items) {
            if (absence.getAgentsID() == agentsID) found.add(absence);
        }
        return found;
    }

    
    /**
     * Removes the provided absence from the list.
     * @param absence The absence to be removed.
     * @return true if successful, false otherwise.
     */
    public boolean remove(Absence absence) {
        boolean ret = this.items.remove(absence);
        return ret;
    }
    
    
    /**
     * Removes all absences that have the given agents ID
     * @param agentsID The ID of the agent that absences are to be removed
     * @return The number of absences removed
     */
    public int removeAgentsAbsences(int agentsID) {
        int n = 0;
        for (Iterator<Absence> it = this.items.iterator(); it.hasNext();) {
            Absence poissa = it.next();
            if ( poissa.getAgentsID() == agentsID ) {
                it.remove();
                n++;
            }
        }
        return n;
    }
    

}
