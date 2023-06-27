package shiftAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

/**
 * The Workshifts class represents a collection of workshifts.
 * It provides methods for managing and manipulating the workshifts.
 * 
 * @author Santeri Tammisto
 * @version 10.6.2023
 */
public class Workshifts {
    
    private ArrayList<Workshift> items = new ArrayList<Workshift>();
    
    /**
     * Initializes a new instance of the Workshifts class.
     */
    public Workshifts() {
        //
    }
    
    
    /**
     * Replaces the existing workshift in the data structure or adds it as a new workshift.
     * If a workshift with the same ID already exists, it is replaced with the provided workshift.
     * If a workshift with the same ID does not exist, the provided workshift is added to the collection.
     * 
     * @param shift The workshift to be added or replaced
     */
    public void replaceOrAdd(Workshift shift) {
        int id = shift.getID();
        for (int i = 0; i < this.items.size(); i++ ) {
            if ( this.items.get(i).getID() == id ) {
                this.items.set(i, shift);
                return;
            }
        }
        this.add(shift);
    }
    
    
    /**
     * Adds a workshift to the collection.
     * 
     * @param shift The workshift to be added
     */
    public void add(Workshift shift) {
        this.items.add(shift);
    }
    
    
    /**
     * Returns the workshift at the specified index in the collection.
     * 
     * @param i The index of the workshift
     * @return The workshift at the specified index
     */
    public Workshift getShift(int i){
        return this.items.get(i);
    }
    
    
    /**
     * Returns the size of the workshift collection.
     * 
     * @return The size of the workshift collection
     */
    public int getSize() {
        return this.items.size();
    }
    
    
    /**
     * Reads workshifts from a file.
     * 
     * @param directory The directory of the file
     * @throws SailoException if reading fails
     */
    public void readFromFile(String directory) throws SailoException {
        String name = directory + "/workshifts.dat";
        File ftied = new File(name);
        try (Scanner fi = new Scanner(new FileInputStream(ftied))) {
            while ( fi.hasNext() ) {
                String s = fi.nextLine().trim();
                if ( "".equals(s) || s.charAt(0) == ';' ) continue;
                Workshift shift = new Workshift();
                shift.parse(s);
                this.add(shift);
            }
        } catch ( FileNotFoundException e) {
            throw new SailoException("Can't read file  " + name);
        }
    }
    

    /**
     * Saves workshifts to a file.
     * 
     * @param directory The directory of the file to be saved
     * @throws SailoException if saving fails
     */
    public void save(String directory) throws SailoException {
        File ftied = new File(directory + "/workshifts.dat");
        try (PrintStream fo = new PrintStream(new FileOutputStream(ftied, false))) {
            for (Workshift shift: items) {
                fo.println(shift.toString());
            }
        } catch (FileNotFoundException ex) {
            throw new SailoException("File " + ftied.getAbsolutePath() + " won't open");
        }
    }
    
    
    /**
     * Finds workshifts for the specified date.
     * 
     * @param givenDate The date to search for workshifts
     * @return A collection of workshifts found for the specified date
     */
    public Collection<Workshift> find(LocalDate givenDate){
        ArrayList<Workshift> found = new ArrayList<Workshift>();
        for (int i = 0; i < this.getSize(); i++) {
            Workshift shift = this.getShift(i);
            if ( shift.getDate().compareTo(givenDate) == 0) {
                found.add(shift);
            }
        }
        
        Collections.sort(found, new Workshift.Comparer());
        return found;
    }
    
    
    /**
     * Removes the specified workshift from the collection.
     * 
     * @param shift The workshift to be removed
     * @return true if the workshift was successfully removed, false otherwise
     */
    public boolean remove(Workshift shift) {
        boolean ret = this.items.remove(shift);
        return ret;
    }


    /**
     * Creates default workshifts for the given date based on the day of the week.
     * 
     * @param givenDate The date for which the workshifts are created
     * @param directory The location of the directory
     * @throws SailoException if the file with default shifts is not found
     */
    public void createDefaultWorkshifts(LocalDate givenDate, String directory) throws SailoException {
        String weekDay = givenDate.getDayOfWeek().name();
        
        switch ( weekDay ) {
        case "MONDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/1_monday.dat", givenDate);
            break;
        case "TUESDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/2_tuesday.dat", givenDate);
            break;
        case "WEDNESDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/3_wednesday.dat", givenDate);
            break;
        case "THURSDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/4_thursday.dat", givenDate);
            break;
        case "FRIDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/5_friday.dat", givenDate);
            break;
        case "SATURDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/6_saturday.dat", givenDate);
            break;
        case "SUNDAY":
            createDefaultWorkshiftsFromFile(directory + "/data/DefaultShifts/7_sunday.dat", givenDate);
            break;
        default:
            break;
        }
    }
    
    
    /**
     * Creates default workshifts for the given date based on the day of the week.
     * 
     * @param givenDate The date for which the workshifts are created
     * @param directory The location of the directory
     * @throws SailoException if the file with default shifts is not found
     */
    private void createDefaultWorkshiftsFromFile(String path, LocalDate givenDate) throws SailoException {
        File ftied = new File(path);
        try (Scanner fi = new Scanner(new FileInputStream(ftied))) {
            while ( fi.hasNext() ) {
                String s = fi.nextLine().trim();
                if ( "".equals(s) || s.charAt(0) == ';') continue;
                Workshift shift = new Workshift();
                shift.parseDefaultShift(s, givenDate);
                shift.register();
                this.add(shift);
            }
        } catch (FileNotFoundException e) {
            throw new SailoException("Can't read file " + path);
        }
        
    }

    
    /**
     * Finds and returns a list of vacant workshifts, i.e., workshifts without an assigned agent.
     * 
     * @return A list of vacant workshifts
     */
    public ArrayList<Workshift> findVacantWorkshifts(){
        ArrayList<Workshift> found = new ArrayList<Workshift>();
        for (int i = 0; i < this.getSize(); i++) {
            Workshift shift = this.getShift(i);
            if ( shift.getAgentsID() == 0 ) {
                found.add(shift);
            }
        }
        
        Collections.sort(found, new Workshift.ComparerDate());
        return found;
    }
}
