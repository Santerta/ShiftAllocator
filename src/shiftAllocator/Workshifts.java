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
 * @author Santeri Tammisto
 * @version 10.6.2023
 *
 */
public class Workshifts {
    
    private ArrayList<Workshift> items = new ArrayList<Workshift>();
    
    /**
     * Initialization
     */
    public Workshifts() {
        //
    }
    
    
    /**
     * Replaces the shift in the data structure. Takes ownership of the shift.
     * Searches for a shift with the same ID. If not found, adds it as a new shift.
     * @param shift Reference to the shift to be added.
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
     * Adds given workshift to workshifts
     * @param shift to be added
     */
    public void add(Workshift shift) {
        this.items.add(shift);
    }
    
    
    /**
     * Returns a reference to i-th shift in workshifts
     * @param i index of the shift
     * @return shift that's found
     */
    public Workshift getShift(int i){
        return this.items.get(i);
    }
    
    
    /**
     * @return size of workshifts
     */
    public int getSize() {
        return this.items.size();
    }
    
    
    /**
     * Reads workshifts from a file
     * @param directory of the file
     * @throws SailoException if it fails
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
     * Saves workshifts to a file
     * @param directory of the file to be saved
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
     * @param givenDate to be searched
     * @return workshifts that are found for that date
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
     * Removes the given workshift from workshifts
     * @param shift to be removed
     * @return true if success, fail if not
     */
    public boolean remove(Workshift shift) {
        boolean ret = this.items.remove(shift);
        return ret;
    }


    /**
     * Creates default workshifts to a given date based on the day of the week
     * @param givenDate date that the shifts are created
     * @param directory location of directory
     * @throws SailoException if file with default shifts is not found
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
     * @return Arraylist of all those workshifts that doesn't yet have an agent assigned to them
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
