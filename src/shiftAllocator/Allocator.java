package shiftAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * @version 23.6.2023
 *
 */
public class Allocator extends Register {
    
    private LocalDate viewDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate iteratorDate;
    
    private List<ReservedShift> reservedShifts;
    private List<PriorityQueue<Agent>> priorityQueues;
    private Random random;
    
    
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
    
    private int amountOfTeams = 10;
    
    
    /**
     * Default constructor
     */
    public Allocator() {
        
        
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
    
    
    /**
     * @param givenDirectory as
     * @throws SailoException asd
     * @throws IOException  asd
     */
    public void readFromFile(String givenDirectory) throws SailoException, IOException {
        this.directory = "./" + givenDirectory;
        
        Path directoryPath = Paths.get(".", givenDirectory);
        Path dataDirectoryPath = directoryPath.resolve("data");
        Path defaultShiftsDirectoryPath = dataDirectoryPath.resolve("DefaultShifts");
        Path responsibilitiesFilePath = dataDirectoryPath.resolve("responsibilities.dat");
        Path settingsFilePath = dataDirectoryPath.resolve("settings.dat");

        createDirectory(dataDirectoryPath);
        
        if (!Files.exists(defaultShiftsDirectoryPath)) {
            createDirectory(defaultShiftsDirectoryPath);
            createDefaultShiftFiles(defaultShiftsDirectoryPath);
        }
        
        createSettingsFile(settingsFilePath);
        getAbsenceWorthFromFile(settingsFilePath);
        
        createResponsibilitiesFile(responsibilitiesFilePath, amountOfResponsibilities);

        try {
            readResponsibilitiesFromFile(responsibilitiesFilePath);
        } catch (IOException e) {
            throw new SailoException("Can't read file " + responsibilitiesFilePath);
        }

        //this.agents = new Agents();
        //this.absences = new Absences();
        //this.workshifts = new Workshifts();

        this.agents.readFromFile(givenDirectory);
    }
    
    
    private void createSettingsFile(Path settingsFilePath) throws SailoException {
        if (Files.exists(settingsFilePath)) {
            return; // File already exists
        }
        
        try (PrintStream fileOutput = new PrintStream(new FileOutputStream(settingsFilePath.toFile()))) {
            fileOutput.println("; Absence modifier");
            fileOutput.println(this.absenceWorth);
        } catch (IOException e) {
            throw new SailoException("Failed to create responsibilities file: " + settingsFilePath);
        }
    }
    
    
    private void getAbsenceWorthFromFile(Path settingsFilePath) throws IOException {
        
        try (Scanner fileInput = new Scanner(settingsFilePath)) {
            while (fileInput.hasNextLine()) {
                String line = fileInput.nextLine();
                if (line == null || line.isEmpty() || line.charAt(0) == ';') continue;

                try {
                    double newAbsenceWorth = Double.parseDouble(line);
                    this.absenceWorth = newAbsenceWorth;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    private void createDirectory(Path directoryPath) throws SailoException {
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            throw new SailoException("Failed to create directory: " + directoryPath);
        }
    }
    
    
    private void createDefaultShiftFiles(Path defaultShiftsDirectoryPath) throws IOException {
        
        Files.createDirectories(defaultShiftsDirectoryPath);
        
        String[] defaults = {"1_monday.dat", "2_tuesday.dat", "3_wednesday.dat", 
                "4_thursday.dat", "5_friday.dat", "6_saturday.dat",
                "7_sunday.dat"};
        
        for (String defaultFile : defaults) {
            Path newFilePath = defaultShiftsDirectoryPath.resolve(defaultFile);
            Files.createFile(newFilePath);
        }
    }
    
    
    private void createResponsibilitiesFile(Path responsibilitiesFilePath, int responsibilitiesAmount) throws SailoException {
        
        if (Files.exists(responsibilitiesFilePath)) {
            return; // File already exists
        }

        try (PrintStream fileOutput = new PrintStream(new FileOutputStream(responsibilitiesFilePath.toFile()))) {
            for (int i = 0; i < responsibilitiesAmount; i++) {
                fileOutput.println("R" + (i + 1));
            }
        } catch (IOException e) {
            throw new SailoException("Failed to create responsibilities file: " + responsibilitiesFilePath);
        }
        
    }
    
    
    private void readResponsibilitiesFromFile(Path responsibilitiesFilePath) throws IOException {
        try (Scanner fileInput = new Scanner(responsibilitiesFilePath)) {
            while (fileInput.hasNextLine()) {
                String line = fileInput.nextLine();
                if (line == null || line.isEmpty() || line.charAt(0) == ';')
                    continue;
                Allocator.responsibilities.add(line);
            }
        }
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
     * @param number of the responsibility
     * @return name of the responsibility 
     */
    public String getResponsibilityName(int number) {
        return responsibilities.get(number);
    }
    
    
    /**
     * Increases shiftAmountAll by how much an absence of whole day is worth
     * @param agent that's absent
     */
    public void increaseShiftsByAbsence(Agent agent) {
        agent.increaseShiftsByAbsenceModifier(absenceWorth);
    }
    
    
    /**
     * Creates an Excel file containing the allocated shifts based on the current state of the Allocator.
     * The Excel file includes information about agents, dates, weekdays, absences, and shifts.
     * The file is saved in the specified directory with the naming format "{month}_SHIFTS.xlsx".
     * If skipWeekends is set to true, weekends (Saturday and Sunday) will be skipped while creating the shifts.
     * The Excel file includes a summary column at the end showing the total shift count for each agent.
     *
     * @throws FileNotFoundException if the specified file or directory for saving the Excel file is not found.
     */
    public void createAllocatedShiftsExcel() throws FileNotFoundException {
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
        
        String[] daysOfWeek = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        
        // Reads all the unique shifts from defaults shifts and adds them to uniqueShiftSets
        int dayNumber = 1;
        for (String day : daysOfWeek) {
            String fileName = this.directory + "/data/DefaultShifts/" + dayNumber + "_" + day + ".dat";
            dayNumber++;
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
     * Checks if a worker is absent during a given workshift.
     * TODO: Create a better solution to checking and adding absence-points to agents
     * 
     * @param agent The agent for whom to check the absence.
     * @param shift The workshift for which to check the worker's absence.
     * @return {@code true} if the worker is absent during the workshift, {@code false} otherwise.
     */
    private boolean workerIsAbsent(Agent agent, Workshift shift) {
        List<Absence> foundAbsences = this.absences.getAbsences(agent.getIDNumber());
        
        if (!foundAbsences.isEmpty()) {
            for (Absence absence : foundAbsences) {
                if (absence.getDate().isEqual(shift.getDate()) && absence.getWholeDayFlag()) {
                    if (!agent.hasAllocatedDay(shift.getDate())) {
                        agent.addAllocatedDay(shift.getDate());
                        agent.increaseShiftsByAbsenceModifier(this.absenceWorth);
                    }
                    return true; // Whole day absence on the same date
                }
                if ( absence.getDate().isEqual(shift.getDate()) ) {
                    LocalTime absenceStart = absence.getStarTimeLT();
                    LocalTime absenceEnd = absence.getStopTimeLT();
                    LocalTime shiftStart = shift.getStartTimeLT();
                    LocalTime shiftEnd = shift.getEndTimeLT();
                    if (shiftStart.isBefore(absenceEnd) && shiftEnd.isAfter(absenceStart)) {
                        return true; // Overlapping absence and shift
                    }
                    // Check for equal times
                    if (shiftStart.equals(absenceEnd) || shiftEnd.equals(absenceStart)) {
                        return false; // Times are equal, worker is not absent
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
    
    
}
