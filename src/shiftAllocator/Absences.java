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
 * Represents a collection of absences. Allows adding, replacing, removing, and retrieving absences.
 * Provides methods to read absences from a file, save absences to a file, and manipulate absences.
 * 
 * @author Santeri Tammisto
 * @version 10.6.2023
 */
public class Absences {
    
    private ArrayList<Absence> items = new ArrayList<Absence>();
    
    
    /**
     * Initializes an empty Absences object.
     */
    public Absences() {
        // 
    }
    
    
    /**
     * Replaces the absence in the data structure. Takes ownership of the absence.
     * Searches for an absence with the same ID. If not found, adds it as a new absence.
     * 
     * @param absence the reference to the absence to be added or replaced
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
     * Adds an absence to the collection.
     * 
     * @param absence the absence to be added
     */
    public void add(Absence absence) {
        items.add(absence);
    }
    
    
    /**
     * Reads absences from a file in the specified directory.
     * 
     * @param directory the directory where the file is located
     * @throws SailoException if reading the file gives an error
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
     * Saves absences to a file in the specified directory.
     * 
     * @param directory the directory where the file is saved
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
     * Retrieves all absences of a single employee with the specified agent ID.
     * 
     * @param agentsID the ID of the agent whose absences are being retrieved
     * @return a list containing references to the found absences
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
     * 
     * @param absence the absence to be removed
     * @return {@code true} if removal is successful, {@code false} otherwise
     */
    public boolean remove(Absence absence) {
        boolean ret = this.items.remove(absence);
        return ret;
    }
    
    
    /**
     * Removes all absences that have the given agent ID.
     * 
     * @param agentsID the ID of the agent whose absences are to be removed
     * @return the number of absences removed
     */
    public int removeAgentsAbsences(int agentsID) {
        int n = 0;
        for (Iterator<Absence> it = this.items.iterator(); it.hasNext();) {
            Absence absence = it.next();
            if ( absence.getAgentsID() == agentsID ) {
                it.remove();
                n++;
            }
        }
        return n;
    }
    

}
