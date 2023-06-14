package shiftAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import fi.jyu.mit.ohj2.Mjonot;

/**
 * @author Santeri Tammisto
 * @version 11.6.2023
 *
 */
public class Register {
    
    private Workshifts workshifts = new Workshifts();
    private Agents agents = new Agents();
    private Absences absences = new Absences();

    private String directory = "";
    
    private LocalDate viewDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate iteratorDate;
    
    private List<ReservedShift> reservedShifts;
    private List<PriorityQueue<Agent>> priorityQueues;
    private Random random;
    
    private static final DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    
    
    /**
     * TODO: Create a non-static implementation
     * Responsibilities for the shift allocator
     */
    public static ArrayList<String> responsibilities = new ArrayList<>();
    // TODO: Make the amount of responsibilities modifiable
    private int amountOfResponsibilities = 12;
    /**
     * TODO: Create a way to calculate this based on number of agents and shifts instead of hardcoding it
     * The amount of shifts increased to agents when they have an absence of a whole day
     */
    private double absenceWorth = 0.25;
    
    
    /**
     * 
     */
    public void distributeVacantShifts() {
        createPriorityQueues();
        addAgentsToPriorityQueues();
        
        ArrayList<Workshift> shiftsToDistribute = this.findVacantWorkshifts();
        
        for (Workshift shift : shiftsToDistribute) {
            
            Agent agent = null;
            
            // Checks if reservedShifts has already a reservation for any agent, and adds this workshift to that agent if it does
            int reservedLength = this.getReservedShiftsLength();
            
            if ( reservedLength > 0 ) {
                for (int i = 0 ; i < reservedLength ; i++) {
                    ReservedShift reservation = this.reservedShifts.get(i);
                    if ( reservation.getDateOfReservedShift().isEqual(shift.getDate()) && reservation.getShiftName().equals(shift.getName()) ) {
                        agent = this.findAgent(reservation.getAgentID());
                        shift.setAgent(agent.getIDNumber());
                        agent.increaseShiftsByOne(shift.getResponsibility());
                        this.reservedShifts.remove(i);
                        break;
                    }
                }
                if (shift.getAgentsID() != 0) continue;
            }
            
            // If there was no reservations for the shift, add an agent to the shift based on priorityqueues
            if (agent == null) { // Only retrieve agent if it was not already assigned from reservations
                agent = getAgentForShift(shift);
                if (agent != null) {
                    shift.setAgent(agent.getIDNumber());
                    agent.increaseShiftsByOne(shift.getResponsibility());
                }
            }
        
        }
        
    }
    
    
    private Agent getAgentForShift(Workshift shift) {
        PriorityQueue<Agent> queue = this.priorityQueues.get(shift.getResponsibility());
        List<Agent> eligibleAgents = new ArrayList<>();
        
        // Find all eligible agents that have the responsibility needed and are not absent
        for (Agent agent : queue) {
            if ( agentHasNoShifts(agent, shift.getDate()) && !workerIsAbsent(agent, shift)) {
                eligibleAgents.add(agent);
            }
        }
        
        if (eligibleAgents.isEmpty()) {
            return null; // No eligible agents found for the shift
        }
        
        // Sort eligible agents based on their shift counts
        eligibleAgents.sort(Comparator.comparingDouble(Agent::getShiftAmountAll)
                .thenComparingDouble(w -> w.getShiftAmount(shift.getResponsibility())));
        
        // Find agents with the minimum shift count
        double minShiftCountAll = eligibleAgents.get(0).getShiftAmountAll();
        double minShiftCount = eligibleAgents.get(0).getShiftAmount(shift.getResponsibility());
        
        List<Agent> agentsWithLeastShifts = new ArrayList<>();
        for (Agent agent : eligibleAgents) {
            if (agent.getShiftAmountAll() == minShiftCountAll && agent.getShiftAmount(shift.getResponsibility()) == minShiftCount) {
                agentsWithLeastShifts.add(agent);
            }
        }
        
        if (agentsWithLeastShifts.size() == 1) return agentsWithLeastShifts.get(0);
        
        // Randomly select an agent from the agents with the least shifts if there are more than one
        int randomIndex = this.random.nextInt(agentsWithLeastShifts.size());
        return agentsWithLeastShifts.get(randomIndex);
        
    }
    
    
    
    
    private void createPriorityQueues() {
        // Create a priority queue for each responsibility
        for (int i = 0 ; i < responsibilities.size() ; i++) {
            final int index = i;
            Comparator<Agent> comparator = Comparator.comparingDouble(Agent::getShiftAmountAll)
                    .thenComparingDouble(w -> w.getShiftAmount(index));
            
            PriorityQueue<Agent> queue = new PriorityQueue<>(comparator);
            this.priorityQueues.add(queue);
        }
    }
    
    

    
    private void addAgentsToPriorityQueues() {
        
        for (int i = 0 ; i < this.agents.getCount() ; i++) {
            
            Agent agent = this.agents.getAgent(i);
            for (int j = 0 ; j < agent.getResponsibilitiesArrayLength() ; j++) {
                if ( agent.getResponsibilityByIndex(j) ) {
                    PriorityQueue<Agent> queue = this.priorityQueues.get(j);
                    queue.add(agent);
                }
            }
        }
        
    }
    
    
    /**
     * Default constructor
     */
    public Register() {
        LocalDate firstday = LocalDate.now();
        this.viewDate = firstday.with(TemporalAdjusters.firstDayOfNextMonth());
        this.startDate = firstday.with(TemporalAdjusters.firstDayOfNextMonth());
        this.endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        this.iteratorDate = startDate;
        
        this.reservedShifts = new ArrayList<ReservedShift>();
        this.priorityQueues = new ArrayList<>();
        this.random = new Random();
    }
    
    
    
    
    /**
     * Increases shiftAmountAll by how much an absence of whole day is worth
     * @param agent that's absent
     */
    public void increaseShiftsByAbsence(Agent agent) {
        agent.increaseShiftsByAbsenceModifier(absenceWorth);
    }
    
    
    
    
    /**
     * @param number of the responsibility
     * @return name of the responsibility 
     */
    public String getResponsibilityName(int number) {
        return responsibilities.get(number);
    }
    
    

    
    /**
     * Replaces an agent in the data structure. Takes ownership of the agent.
     * Searches for a agent with the same identification number. If not found, adds as a new agent.
     * @param agent reference to the agent to be added. Note that the data structure becomes the owner.
     */
    public void replaceOrAdd(Agent agent) {
        agents.replaceOrAdd(agent);
    }
    
    
    
    
    /**
     * Replaces a workshift in the data structure. Takes ownership of the workshift.
     * Searches for a workshift with the same identification number. If not found, adds as a new workshift.
     * @param workshift reference to the workshift to be added. Note that the data structure becomes the owner.
     */
    public void replaceOrAdd(Workshift workshift) {
        workshifts.replaceOrAdd(workshift);
    }
    
    
    
    
    /**
     * Replaces a absence in the data structure. Takes ownership of the absence.
     * Searches for a absence with the same identification number. If not found, adds as a new absence.
     * @param absence reference to the absence to be added. Note that the data structure becomes the owner.
     */
    public void replaceOrAdd(Absence absence) {
        absences.replaceOrAdd(absence);
    }
    
    
    
    
    /**
     * Searches for an agent in the data structure based on the identification number.
     * @param idNumber the number used to search for the agent
     * @return null if no agent with the specified identification number is found, otherwise a reference to the found agent
     */
    public Agent findAgent(int idNumber) {
        return agents.findAgent(idNumber);
    }
    
    
    
    
    /**
     * Adds a new workshift to workshifts
     * @param workshift to be added
     */
    public void add(Workshift workshift) {
        this.workshifts.add(workshift);
    }
    
    
    
    
    /**
     * Adds a new agent to agents
     * @param agent to be added
     */
    public void add(Agent agent) {
        this.agents.addAgent(agent);
    }
    
    
    
    
    /**
     * Adds a new absence to absences
     * @param absence to be added
     */
    public void add(Absence absence) {
        this.absences.add(absence);
    }
    

    
    
    /**
     * Retrieves all absences for an agent.
     * @param agent The agent for whom absences are being retrieved.
     * @return A data structure containing references to the found absences.
     */
    public List<Absence> getAbsences(Agent agent) {
        return absences.getAbsences(agent.getIDNumber());
    }

    
    
    
    /**
     * @return count of workshifts
     */
    public int getWorkshiftsSize() {
        return this.workshifts.getSize();
    }
    
    
    
    
    /**
     * @return count of agents
     * 
     */
    public int getAgentsCount() {
        return this.agents.getCount();
    }
    
    
    
    
    /**
     * Provides the i-th workshift in the registry.
     * @param index the position of the workshift
     * @return the workshift at position i
     */
    public Workshift getWorkshift(int index) {
        return this.workshifts.getShift(index);
    }
    
    
    
    
    /**
     * Provides the i-th agent in the registry
     * @param index the position of the agent (starts from 0)
     * @return agent at position index
     */
    public Agent getAgent(int index) {
        return this.agents.getAgent(index);
    }
    
    
    
    
    /**
     * Returns a list of agents that match the search criteria.
     * @param clause the search criteria
     * @param k the index of the field to search
     * @return the found agents
     */
    public Collection<Agent> findAgents(String clause, int k) {
        return agents.findAgents(clause, k);
    }
    
    
    
    
    /**
     * Returns a list of workshifts that match the search criteria / whose date is the specified searchDate.
     * @param searchDate the date being searched
     * @return the found workshifts
     */
    public Collection<Workshift> findWorkshifts(LocalDate searchDate) {
        return workshifts.find(searchDate);
    }
    
    
    /**
     * Finds all available workshifts from the list of workshifts.
     * @return a collection of all workshifts whose agent ID is 0 / have no assigned agent yet
     */
    public ArrayList<Workshift> findVacantWorkshifts(){
        return workshifts.findVacantWorkshifts();
    }
    
    
    /**
     * Checks if the agent is available on the given date by checking if the agent already has a shift assigned.
     * @param agent The agent for whom the availability is being checked.
     * @param givenDate The date for which the availability is being checked.
     * @return {@code true} if the agent is available, {@code false} if the agent already has a shift assigned.
     */
    private boolean agentHasNoShifts(Agent agent, LocalDate givenDate) {
        Collection<Workshift> shiftsCollection = this.findWorkshifts(givenDate);
        
        for (Workshift shift : shiftsCollection) {
            if (shift.getAgentsID() == agent.getIDNumber()) {
                return false; // Agent already has a shift assigned
            }
        }
        
        return true; // Agent is available
    }
    
    
    
    /**
     * Checks if a worker is absent during a given workshift.
     * @param agent The agent for whom to check the absence.
     * @param shift The workshift for which to check the worker's absence.
     * @return {@code true} if the worker is absent during the workshift, {@code false} otherwise.
     */
    private boolean workerIsAbsent(Agent agent, Workshift shift) {
        List<Absence> foundAbsences = this.absences.getAbsences(agent.getIDNumber());
        
        if (!foundAbsences.isEmpty()) {
            for (Absence absence : foundAbsences) {
                if (absence.getDate().isEqual(shift.getDate()) && absence.getWholeDayFlag()) {
                    return true; // Whole day absence on the same date
                }
                if ( absence.getDate().isEqual(shift.getDate()) ) {
                    LocalTime absenceStart = absence.getStarTimeLT();
                    LocalTime absenceEnd = absence.getStopTimeLT();
                    LocalTime shiftStart = shift.getStartTimeLT();
                    LocalTime shiftEnd = shift.getEndTimeLT();
                    if (shiftStart.isBefore(absenceEnd) && shiftEnd.isBefore(absenceStart)) {
                        return true; // Overlapping absence and shift
                    }
                }
            }
        }
        return false; // No absences or no overlapping absences found
    }
    
    
    

    private ArrayList<Agent> getAllMembersOfTeam(int teamNumber){
        return this.agents.getAllMembersOfTeam(teamNumber);
    }
    
    
    
    
    /**
     * Reads registers information from file and/or creates files
     * @param name that's used in reading
     * @throws SailoException if reading fails
     */
    public void readFromFile(String name) throws SailoException {
        File dir = new File("./" + name);
        dir.mkdirs();
        
        this.agents = new Agents();
        this.absences = new Absences();
        this.workshifts = new Workshifts();
        
        this.directory = dir.getAbsolutePath();
        
        // creates the files for defaults workshifts if it doesn't exist
        File dirDefaults = new File(dir, "DefaultShifts");
        if (!dirDefaults.exists()) {
            dirDefaults.mkdirs();
            
            String[] defaults = {"defaultMonday.dat", "defaultTuesday.dat", "defaultWednesday.dat", 
                    "defaultThursday.dat", "defaultFriday.dat", "defaultSaturday.dat",
                    "defaultSunday.dat"};
            
            for (int i = 0; i < defaults.length; i++) {
                File newFile = new File(dirDefaults, defaults[i]);
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // creates the file for creating responsibilities if it doesn't exist
        File responData = new File(name + "/responsibilities.dat");
        if (!responData.exists()) {
            try {
                responData.createNewFile();
                try (PrintStream fo = new PrintStream(new FileOutputStream(responData, false))) {
                    for (int i = 0 ; i < this.amountOfResponsibilities ; i++) {
                        fo.println("R" + (i+1));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // reads responsibilities.dat file and adds the lines to this registers responsibilities array
        try (Scanner fi = new Scanner(new FileInputStream(responData))) {
            while ( fi.hasNext() ) {
                String s = fi.nextLine();
                if ( s == null || "".equals(s) || s.charAt(0) == ';' ) continue;
                Register.responsibilities.add(s);
            }
        } catch (FileNotFoundException e) {
            throw new SailoException("Ei saa luettua tiedostoa " + responData);
        }
        
        this.agents.readFromFile(name);
        //this.poissaolot.readFromFile(name);
        //this.tyotehtavat.readFromFile(name);
    }
    
    
    
    
    /**
     * Removes the provided absence from the list.
     * @param absence The absence to be removed.
     */
    public void removeAbsence(Absence absence) {
        absences.remove(absence);
    }
    
    
    
    
    /**
     * Removes the given agent and their absences.
     * @param agent the employee to be removed
     * @return 1 if successful, 0 if not found
     */
    public int removeAgent(Agent agent) {
        if ( agent == null ) return 0;
        int ret = agents.removeAgent(agent.getIDNumber());
        absences.removeAgentsAbsences(agent.getIDNumber());
        return ret;
    }
    
    
    
    
    /**
     * Removes the provided workshift from workshifts
     * @param workshift given workshift
     */
    public void removeWorkshift(Workshift workshift) {
        workshifts.remove(workshift);
    }
    
    
    
    
    /**
     * Saves registers information to a file
     * @throws SailoException if there's a problem encountered in saving
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

    
    
    
    /**
     * @param typeOfDate the date that is asked as string.
     * 
     * view = viewDate
     * start = startDate
     * end = endDate
     * iterator = iteratorDate
     * @return LocalDate-object based on the specified typeOfDate-parameter.
     */
    public LocalDate getRegisterDate(String typeOfDate) {
        switch (typeOfDate) {
            case "view":
                return this.viewDate;
            case "start":
                return this.startDate;
            case "end":
                return this.endDate;
            case "iterator":
                return this.iteratorDate;
            default:
                return null;
        }
    }
    
    
    
    /**
     * @param typeOfDate the date that is to be changed.
     * 1 = viewDate
     * 2 = starDate
     * 3 = endDate
     * @param givenDate new date
     */
    public void setRegisterDate(int typeOfDate, LocalDate givenDate) {
        switch (typeOfDate) {
            case 1:
                this.viewDate = givenDate;
                break;
            case 2:
                this.startDate = givenDate;
                break;
            case 3:
                this.endDate = givenDate;
                break;
            default:
                break;
        }
    }
    

    
    
    /**
     * Creates default workshifts based on the entered start date, end date, and weekdays within that range.
     * @throws SailoException if reading default shifts from file fails
     */
    public void createDefaultShifts() throws SailoException {
        while ( this.iteratorDate.compareTo(this.endDate) <= 0) {
            this.workshifts.createDefaultWorkshifts(this.iteratorDate, this.directory);
            this.iteratorDate = this.iteratorDate.plusDays(1);
        }
    }

    
    // Class for reserved shifts, getters and setters for reservedShifts-list.
    
    
    /**
     * Own class for reserved shifts
     * @author Santeri Tammisto
     * @version 14.6.2023
     *
     */
    public class ReservedShift {
        
        private String shiftName;
        private LocalDate shiftDate;
        private int agentID;
        
        
        /**
         * Default constructor
         */
        public ReservedShift() {
            //
        }
        
        
        /**
         * Constructor with parameters
         * @param shiftname name of the shift that is being reserved
         * @param shiftdate date of the shift
         * @param agentsID ID of the agent that's reserving the shift
         */
        public ReservedShift(String shiftname, LocalDate shiftdate, int agentsID) {
            this.shiftName = shiftname;
            this.shiftDate = shiftdate;
            this.agentID = agentsID;
        }
        
        
        /**
         * @return name of the reserved shift
         */
        public String getShiftName() {
            return this.shiftName;
        }
        
        
        /**
         * @return date of the reserved shift
         */
        public LocalDate getDateOfReservedShift() {
            return this.shiftDate;
        }
        
        
        /**
         * @return agents ID that has reserved the shift
         */
        public int getAgentID() {
            return this.agentID;
        }
        
    }
    
    
    /**
     * Add given shift reservation to reservedShift-list
     * @param shift that's being reserved
     */
    private void addShiftReservation(ReservedShift shift) {
        this.reservedShifts.add(shift);
    }
    
    
    /**
     * @return the length of reserved shifts -list
     */
    private int getReservedShiftsLength() {
        return this.reservedShifts.size();
    }

}
