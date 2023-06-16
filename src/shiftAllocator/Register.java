package shiftAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    
    // private static final DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    
    private int amountOfTeams = 10;
    
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
    public void createAllocatedShiftsExcel() {
        String month = this.startDate.getMonth().toString();
        String fileName = this.directory + "/" + month + "_SHIFTS.xlsx";
        boolean skipWeekends = true;
        
        try ( OutputStream fileOut = new FileOutputStream(fileName) ) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(month + "_SHIFTS");
                
                
                // Creates the fist two rows containing agent info headers, dates and weekdays
                Row rowHead = sheet.createRow(0);
                rowHead.createCell(0).setCellValue("Team");
                rowHead.createCell(1).setCellValue("ID");
                rowHead.createCell(2).setCellValue("Name");
                
                Row weekOfDayRow = sheet.createRow(1);
                this.iteratorDate = this.startDate;
                int dateCellIndex = 3;
                
                while ( this.iteratorDate.compareTo(this.endDate) <= 0 ) {
                    rowHead.createCell(dateCellIndex).setCellValue(this.iteratorDate.toString());
                    weekOfDayRow.createCell(dateCellIndex).setCellValue(this.iteratorDate.getDayOfWeek().toString());
                    dateCellIndex++;
                    this.iteratorDate = this.iteratorDate.plusDays(1);
                }
                
                this.iteratorDate = this.startDate; // Just in case
                
                int rowNumber = 2;
                for ( int teamNumber = 1 ; teamNumber <= this.amountOfTeams ; teamNumber++ ) {
                    ArrayList<Agent> membersOfTeam = getAllMembersOfTeam(teamNumber);
                    
                    for (int i = 0; i < membersOfTeam.size(); i++) {
                        Row currentRow = sheet.createRow(rowNumber);
                        Agent agent = membersOfTeam.get(i);
                        currentRow.createCell(0).setCellValue(agent.getTeamNumber());
                        currentRow.createCell(1).setCellValue(agent.getIDNumber());
                        currentRow.createCell(2).setCellValue(agent.getFullName());
                        rowNumber++;
                    }
                }
                
                dateCellIndex = 3;
                int agentIDCell = 1;
                
                while (rowHead.getCell(dateCellIndex) != null) {
                    
                    if ( skipWeekends ) {
                        String weekOfDay = weekOfDayRow.getCell(dateCellIndex).toString();
                        if ( weekOfDay.equals("SATURDAY") || weekOfDay.equals("SUNDAY")  ) {
                            dateCellIndex++;
                            continue;
                        }
                    }
                    
                    
                    int currentRowIndex = 2;
                    Row currentRow = sheet.getRow(currentRowIndex);
                    
                    while ( currentRow != null ) {
                        int agentID = (int) currentRow.getCell(agentIDCell).getNumericCellValue();
                        Agent agent = this.agents.findAgent(agentID);
                        String dateString = rowHead.getCell(dateCellIndex).getStringCellValue();
                        
                        // Checks if an agent has a default text and adds it to the cell
                        if ( !agent.get(4).equals("") ) {
                            currentRow.createCell(dateCellIndex).setCellValue(agent.get(4));
                        }
                        
                        // Checks if agent has any absences and adds them to the cell if he does
                        List<Absence> agentsAbsences = new ArrayList<Absence>();
                        agentsAbsences = this.getAbsences(agent);
                        
                        for (Absence absent : agentsAbsences) {
                            if (absent.getDate().isEqual(LocalDate.parse(dateString))) {
                                String text;
                                if (!absent.getWholeDayFlag()) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(absent.getExplanation())
                                      .append(" ")
                                      .append(absent.getStartTime())
                                      .append("-")
                                      .append(absent.getStopTime());
                                    text = sb.toString();
                                } else {
                                    text = absent.getExplanation();
                                }
                                currentRow.createCell(dateCellIndex).setCellValue(text);
                            }
                        }
                        
                        // Checks for actual shifts and adds them to the excel if the agent has any
                        ArrayList<Workshift> daysShifts = (ArrayList<Workshift>) this.workshifts.find(LocalDate.parse(dateString));
                        
                        for ( Workshift shift: daysShifts ) {
                            if ( shift.getAgentsID() == agentID ) {
                                currentRow.createCell(dateCellIndex).setCellValue(shift.getName());
                            }
                        }
                        
                        currentRowIndex++;
                        currentRow = sheet.getRow(currentRowIndex);
                    }
                    
                    // Creates and shows still vacant workshifts for current day at the end of the column

                    String date = rowHead.getCell(dateCellIndex).toString();
                    LocalDate day = LocalDate.parse(date);
                    ArrayList<Workshift> shiftsOfToday = (ArrayList<Workshift>) workshifts.find(day);
                    ArrayList<Workshift> vacantShifts = new ArrayList<Workshift>();
                    for (Workshift shift: shiftsOfToday) {
                        if ( shift.getAgentsID() == 0) {
                            vacantShifts.add(shift);
                        } 
                    }
                    int vacantCellIndex = currentRowIndex+10;
                    for ( Workshift shift: vacantShifts ) {
                        currentRow = sheet.getRow(vacantCellIndex);
                        if ( currentRow == null ) {
                            currentRow = sheet.createRow(vacantCellIndex);
                        }
                        currentRow.createCell(dateCellIndex).setCellValue(shift.getName());
                        vacantCellIndex++;
                    }
                    
                    
                    dateCellIndex++;
                    
                }
                
                // Creates in the final column a cell for each agent that shows their total shift count
                int currentRowIndex = 2;
                Row currentRow = sheet.getRow(currentRowIndex);
                int lastCell = dateCellIndex;
                
                while (currentRow != null) {
                    int agentID = (int) currentRow.getCell(agentIDCell).getNumericCellValue();
                    Agent agent = this.agents.findAgent(agentID);
                    currentRow.createCell(lastCell).setCellValue(agent.getShiftAmountAll());
                    currentRowIndex++;
                    currentRow = sheet.getRow(currentRowIndex);
                }
                
                workbook.write(fileOut);
                
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
    }
    
    
    /**
     * Downloads absence and reservation data from an Excel file and processes it to create absence records and shift reservations.
     * TODO: Better exception and error handling
     *
     * @return a message indicating the result of the download process
     */
    public String downloadAbsenceExcel() {
        
        String month = this.startDate.getMonth().toString();
        String fileName = this.directory + "/" + month + "_ABSENCES.xlsx";
        String currentLocation = "";
        
        File file = new File(fileName);
        if (!file.exists()) {
            return "Error: Excel file does not exist in the specified directory or is named wrong.";
        }
        
        try ( FileInputStream fis = new FileInputStream(new File(fileName)) ) {
            try (Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                // Creates a list and read all the reservable shifts to it for future comparison
                Row reservationRow = sheet.getRow(1);
                int reservationCellIndex = 3;
                List<String> reservableShifts = new ArrayList<>();
                while ( reservationRow.getCell(reservationCellIndex) != null ) {
                    Cell cell = reservationRow.getCell(reservationCellIndex);
                    String reservableShift = cell.getStringCellValue().trim();
                    reservableShifts.add(reservableShift);
                    reservationCellIndex++;
                }
                
                Row headerRow = sheet.getRow(2);
                int dateCellIndex = 3;
                int agentIDCellIndex = 1;
                
                while ( headerRow.getCell(dateCellIndex) != null ) {
                    int currentRowIndex = 4; // start of the data that is to be accessed
                    Row currentRow = sheet.getRow(currentRowIndex);
                    
                    while (currentRow != null) {
                        Cell cell = currentRow.getCell(dateCellIndex);
                        
                        
                        if (cell != null) {
                            currentLocation = "Row: " + (currentRowIndex + 1) + ", Column: " + (dateCellIndex + 1);
                            
                            String cellValue = cell.getStringCellValue().trim();
                            String dateString = headerRow.getCell(dateCellIndex).getStringCellValue();
                            LocalDate date = LocalDate.parse(dateString);
                            int agentID = (int) currentRow.getCell(agentIDCellIndex).getNumericCellValue();

                            

                            if (reservableShifts.contains(cellValue)) {
                                // Checks for reported reservations during the date and in this cell
                                ReservedShift reservation = new ReservedShift(cellValue, date, agentID);
                                this.addShiftReservation(reservation);

                                } else if (cellValue.contains(",")) { // Checks for reported partial absences during the date
                                    String trimmedString = cellValue.trim();
                                    String[] parts = trimmedString.split(",", 2); // Split into maximum 2 parts

                                    String explanation = parts[0].trim(); // Absence explanation before comma
                                    String modifiedString = parts[1].trim(); // String of absence time after comma

                                    StringBuilder sb = new StringBuilder(modifiedString);

                                    String[] timeParts = sb.toString().split("-");
                                    LocalTime startTime = LocalTime.parse(timeParts[0]);
                                    LocalTime endTime = LocalTime.parse(timeParts[1]);

                                    Absence absence = new Absence(date, startTime, endTime, false, explanation, agentID);
                                    absence.register();
                                    this.add(absence);
    
                                } else {
                                    // If absence or reservation haven't been created so far, creates an absence for the whole day
                                    LocalTime startTime = LocalTime.parse("00:00");
                                    LocalTime endTime = LocalTime.parse("00:00");
    
                                    Absence absence = new Absence(date, startTime, endTime, true, cellValue, agentID);
                                    absence.register();
                                    this.add(absence);
                                }
                            }
                            currentRowIndex++;
                            currentRow = sheet.getRow(currentRowIndex);
                    }
                    dateCellIndex++;
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                return "Error at: " + currentLocation;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return "Error at: " + currentLocation;
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                return "Error at: " + currentLocation;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Can't read file " + fileName;
        }
        return "Absences downloaded successfully";
    }
    
    
    
    
    /**
     * Creates an Excel file for recording absences.
     *
     * @return a message indicating the success of creating the Excel file
     * @throws FileNotFoundException if the file path is not found
     * @throws IOException           if an I/O error occurs while creating the Excel file
     */
    
    public String createAbsenceExcel() throws FileNotFoundException, IOException {
        String month = this.startDate.getMonth().toString();
        String fileName = this.directory + "/" + month + "_ABSENCES.xlsx";
        
        try (OutputStream fileOut = new FileOutputStream(fileName)){
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Absences_" + month);
                // --------------- FIRST TWO INFO ROWS -----------------------------------
                // Create cell styles
                CellStyle boldCellStyle = workbook.createCellStyle();
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                boldCellStyle.setFont(boldFont);
                
                // Create rows for details and information about the excel and for the list of absences that can be reserved
                Row absenceRow = sheet.createRow(0);
                Row reservationRow = sheet.createRow(1);
                
                Cell absenceInfo = absenceRow.createCell(2);
                absenceInfo.setCellValue("Poissaolojen ilmoittaminen:");
                absenceInfo.setCellStyle(boldCellStyle);
                Cell absenceExample1 = absenceRow.createCell(3);
                absenceExample1.setCellValue("poissa");
                Cell absenceExample2 = absenceRow.createCell(4);
                absenceExample2.setCellValue("loma");
                Cell absenceExample3 = absenceRow.createCell(5);
                absenceExample3.setCellValue("poissa, XX:XX-XX:XX");
                
                
                int cellIndex = 2;
                Cell reservationInfo = reservationRow.createCell(cellIndex);
                reservationInfo.setCellValue("Varattavat vuorot: ");
                reservationInfo.setCellStyle(boldCellStyle);
                
                // create and write every possible shift name to the first row from default shifts
                try {
                    ArrayList<String> uniqueShifts = readUniqueShiftsFromFiles();
                    
                    for (String uniqueShift : uniqueShifts) {
                        Cell cell = reservationRow.createCell(cellIndex+1);
                        cell.setCellValue(uniqueShift);
                        cellIndex++;
                    }
                    
                } catch (SailoException e) {
                    e.printStackTrace();
                }
                
                // ------------------- Printing agents, agent info, dates etc. ------------------------
                
                Row rowHead = sheet.createRow(2); // Row for header and dates
                Row rowWeekday = sheet.createRow(3); // Row for weekdays
                
                rowHead.createCell(0).setCellValue("Team");
                rowHead.createCell(1).setCellValue("ID");
                rowHead.createCell(2).setCellValue("Name");
                
                this.iteratorDate = this.startDate;
                cellIndex = 3;
                while ( this.iteratorDate.compareTo(this.endDate) <= 0 ) {
                    rowHead.createCell(cellIndex).setCellValue(this.iteratorDate.toString());
                    rowWeekday.createCell(cellIndex).setCellValue(this.iteratorDate.getDayOfWeek().toString());
                    cellIndex++;
                    this.iteratorDate = this.iteratorDate.plusDays(1);
                } // creates cells for dates and weekdays below it
                
                this.iteratorDate = this.startDate; // just in case it's needed again
                
                int rowNumber = 4;
                
                for (int teamNumber = 1; teamNumber <= this.amountOfTeams; teamNumber++) {
                    ArrayList<Agent> allAgentsofTeam = getAllMembersOfTeam(teamNumber);
                    
                    for (int i = 0; i < allAgentsofTeam.size(); i++) {
                        Row rivi = sheet.createRow(rowNumber);
                        Agent agent = allAgentsofTeam.get(i);
                        rivi.createCell(0).setCellValue(agent.getTeamNumber());
                        rivi.createCell(1).setCellValue(agent.getIDNumber());
                        rivi.createCell(2).setCellValue(agent.getFullName());
                        rowNumber++;
                    }
                }
                
                workbook.write(fileOut);
            }
        }

        return "Excel created succesfully!";
    }
    

    private ArrayList<String> readUniqueShiftsFromFiles() throws SailoException{
        Set<String> uniqueShiftsSet = new LinkedHashSet<>();  // Uses LinkedHashSet to preserve insertion order
        
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        
        // Reads all the unique shifts from defaults shifts and adds them to uniqueShiftSets
        for (String day : daysOfWeek) {
            String fileName = this.directory + "/DefaultShifts/default" + day + ".dat";
            try (Scanner fi = new Scanner(new FileInputStream(fileName))) {
                while (fi.hasNext()) {
                    String line = fi.nextLine();
                    if (line == null || "".equals(line) || line.charAt(0) == ';') continue;
                    
                    String shiftName = line.split("\\|")[0];
                    uniqueShiftsSet.add(shiftName);
                }
            } catch (FileNotFoundException e) {
                throw new SailoException("Can't read file " + fileName);
            }
        }
        
        ArrayList<String> uniqueShiftsList = new ArrayList<>(uniqueShiftsSet);
        
        return uniqueShiftsList;
    }
    
    
    
    
    
    /**
     * Distributes vacant shifts to agents based on priority and reservations.
     *
     * @return the number of vacant shifts remaining after distribution
     */
    public int distributeVacantShifts() {
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
        
        ArrayList<Workshift> vacantShifts = this.findVacantWorkshifts();
        return vacantShifts.size(); // vacant shifts after distributing
        
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
    
    
    
    /**
     * @param teamNumber of the team
     * @return ArrayList with all the agents in the team in alphabetical order
     */
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
            throw new SailoException("Can't read file " + responData);
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
        this.iteratorDate = this.startDate;
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
