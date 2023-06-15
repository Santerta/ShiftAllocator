package fxShiftAllocator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import fi.jyu.mit.fxgui.ComboBoxChooser;
import fi.jyu.mit.fxgui.Dialogs;
import fi.jyu.mit.fxgui.ListChooser;
import fi.jyu.mit.fxgui.StringGrid;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import shiftAllocator.Absence;
import shiftAllocator.Register;
import shiftAllocator.SailoException;
import shiftAllocator.Agent;
import shiftAllocator.Workshift;

/**
 * @author Santeri Tammisto
 * @version 14.6.2023
 *
 */
public class AllocatorGUIController implements Initializable {
    
    @FXML private ListChooser<Agent> chooserAgents;
    @FXML private ScrollPane panelAgent;
    @FXML TextField showFirstname;
    @FXML TextField showLastname;
    @FXML TextField showTeamNumber;
    @FXML TextField showDefaultState;
    @FXML TextField showModifier;

    @FXML ComboBoxChooser<String> cbFields;
    @FXML TextField searchCriteria;
    
    @FXML private ListChooser<Workshift> chooserWorkshifts;
    @FXML private ScrollPane panelWorkshift;
    @FXML TextField showShiftName;
    @FXML TextField showShiftStart;
    @FXML TextField showShiftEnd;
    @FXML TextField showShiftWholeDay;
    @FXML TextField showShiftResponsibility;
    @FXML TextField showShiftAgent;
    
    @FXML StringGrid<Absence> tableAbsences;
    
    @FXML private DatePicker showViewDate;
    @FXML private DatePicker showStartDate;
    @FXML private DatePicker showEndDate;
    
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeRegister();
    }
    
    
    @FXML private void handleSearchCriteria() {
        getAgent(0);
    }
    
    
    @FXML private void handleSaveMenu() {
        save();
    }

    
    @FXML private void handleOpenHelp() {
        // TODO: create this
        Dialogs.showMessageDialog(":(");
    }

    
    @FXML private void handleExit() {
        save();
        Platform.exit();
    }

    
    @FXML private void handleCreateDistributedShiftsExcel() {
        createAllocatedShiftsExcel();
    }
    
    
    @FXML private void handleCreateAbsenceExcel() throws FileNotFoundException, IOException {
        createAbsenceExcel();
    }
    
    
    @FXML private void handleDownloadAbsenceExcel() {
        downloadAbsenceExcel();
    }

    
    
    @FXML private void handleCreateDefaultShifts() throws SailoException {
        createDefaultShifts();
    }
    
    
    @FXML private void handleDistributeShifts() {
        distributeVacantShifts();
    }

    
    @FXML private void handleCreateNewAgent() {
        newAgent();
    }
    
    
    @FXML private void handleCreateNewAbsence() {
        newAbsence();
    }

    
    @FXML private void handleCreateNewShift() {
        newShift();
    }
    
    
    @FXML private void handleEditAgent() {
        editAgent();
    }
    
    
    @FXML private void handleEditAbsence() {
        editAbsence();
    }
    
    
    @FXML private void handleEditShift() {
        editShift();
    }

    
    @FXML private void handleRemoveAgent() {
        removeAgent();
    }
    
    
    @FXML private void handleRemoveAbsence() {
        removeAbsence();
    }

    
    @FXML private void handleRemoveShift() {
        removeWorkshift();
    }
    
    
    // ===================================================================
    
    
    private String registername = "";
    private Agent chosenAgent;
    private Workshift chosenShift;
    private Register register;
    private static Agent tempAgent = new Agent();
    
    private void initializeRegister() {
        
        // initializes agent+absence tab
        panelAgent.setFitToHeight(true);
        chooserAgents.clear();
        chooserAgents.addSelectionListener(e -> showAgent());
        
        cbFields.clear();
        for (int k = tempAgent.firstField(); k < tempAgent.getFields(); k++) {
            cbFields.add(tempAgent.getQuestion(k), null);
        }
        cbFields.setSelectedIndex(0);
        
        
        showFirstname.setEditable(false);
        showFirstname.setOnMouseClicked( e -> { if ( e.getClickCount() > 1 ) editAgent(); });
        showLastname.setEditable(false);
        showLastname.setOnMouseClicked( e -> { if ( e.getClickCount() > 1 ) editAgent(); });
        //showTeamNumber.setEditable(false);
        //showTeamNumber.setOnMouseClicked( e -> { if ( e.getClickCount() > 1 ) editAgent(); });
        //showDefaultState.setEditable(false);
        //showDefaultState.setOnMouseClicked( e -> { if ( e.getClickCount() > 1 ) editAgent(); });
        //showModifier.setEditable(false);
        //showModifier.setOnMouseClicked( e -> { if ( e.getClickCount() > 1 ) editAgent(); });
        
        
        
        tableAbsences.setEditable(false);
        tableAbsences.setOnMouseClicked( e -> { if ( e.getClickCount() > 1 ) editAbsence(); });
        
        // initializes workshift-ab
        panelWorkshift.setFitToHeight(true);
        chooserWorkshifts.clear();
        chooserWorkshifts.addSelectionListener(e -> showWorkshift());
        
        showShiftName.setEditable(false);
        showShiftName.setOnMouseClicked(e -> { if ( e.getClickCount() > 1 ) editShift(); });
        showShiftStart.setEditable(false);
        showShiftStart.setOnMouseClicked(e -> { if ( e.getClickCount() > 1 ) editShift(); });
        showShiftEnd.setEditable(false);
        showShiftEnd.setOnMouseClicked(e -> { if ( e.getClickCount() > 1 ) editShift(); });
        
        
        showViewDate.valueProperty().addListener( e -> handleDateChange(1));
        showStartDate.valueProperty().addListener( e -> handleDateChange(2));
        showEndDate.valueProperty().addListener( e -> handleDateChange(3));
    }
    
    
    private void distributeVacantShifts() {
        int result = register.distributeVacantShifts();
        Dialogs.showMessageDialog("" + result + " still vacant workshifts");
        getWorkshift(0);
    }
    
    
    private void createAllocatedShiftsExcel() {
        register.createAllocatedShiftsExcel();
    }
    

    private void createAbsenceExcel() throws FileNotFoundException, IOException {
        String message = register.createAbsenceExcel();
        Dialogs.showMessageDialog(message);
    }
    
    
    private void downloadAbsenceExcel() {
        String message = register.downloadAbsenceExcel();
        Dialogs.showMessageDialog(message);
    }
    
    
    private void createDefaultShifts() throws SailoException {
        register.createDefaultShifts();
        getWorkshift(0);
    }
    
    
    private void removeAbsence() {
        int row = tableAbsences.getRowNr();
        if ( row < 0 ) return;
        Absence absence = tableAbsences.getObject();
        if ( absence == null ) return;
        register.removeAbsence(absence);
        showAbsences(chosenAgent);
        int absencesSize = tableAbsences.getItems().size();
        if ( row >= absencesSize ) row = absencesSize-1;
        tableAbsences.getFocusModel().focus(row);
        tableAbsences.getSelectionModel().select(row);
    }
    
    
    private void removeAgent() {
        Agent agent = chosenAgent;
        if ( agent == null ) return;
        if ( !Dialogs.showQuestionDialog("Agent removal", "Remove agent: " + agent.getFullName(), "Remove", "Cancel") ) 
                return;
        register.removeAgent(agent);
        int index = chooserAgents.getSelectedIndex();
        getAgent(0);
        chooserAgents.setSelectedIndex(index);
    }
    
    
    private void removeWorkshift() {
        Workshift shift = chosenShift;
        if ( shift == null ) return;
        register.removeWorkshift(shift);
        int index = chooserWorkshifts.getSelectedIndex();
        getWorkshift(0);
        chooserWorkshifts.setSelectedIndex(index);
    }
    
    
    /**
     * Initializes register by reading it from given filename
     * @param name file from where the register information will be read
     * @return null if success, error text if not
     */
    protected String readFile(String name) {
        registername = name;
        try {
            register.readFromFile(name);
            getAgent(0);
            getWorkshift(0);
            return null;
        } catch ( SailoException e ) {
            getAgent(0);
            getWorkshift(0);
            String error = e.getMessage();
            if ( error != null ) Dialogs.showMessageDialog(error);
            return error;
        }
    }
    
    
    /**
     * Asks filename and reads it
     * @return true if success, false if not
     */
    public boolean open() {
        String newName = RegisterNameController.askName(null, registername);
        if ( newName == null) return false;
        readFile(newName);
        return true;
    }
    
    
    /**
     * Saving of files
     * @return null if success, error text if not
     */
    private String save() {
        try {
            register.save();
            return null;
        } catch ( SailoException ex ) {
            Dialogs.showMessageDialog("There was a problem in saving: " + ex.getMessage());
            return ex.getMessage();
        }
    }
    
    
    /**
     * Checks that information is saved
     * @return true if you can exit application, false if not
     */
    public boolean canClose() {
        save();
        return true;
    }
    
    
    private void showWorkshift() {
        chosenShift = chooserWorkshifts.getSelectedObject();
        
        if (chosenShift == null) return;
        showShiftName.setText(chosenShift.getName());
        showShiftStart.setText(chosenShift.getStartTime());
        showShiftEnd.setText(chosenShift.getEndTime());
        showShiftWholeDay.setText(chosenShift.getWholeDayString());
        showShiftResponsibility.setText(register.getResponsibilityName(chosenShift.getResponsibility()));
        
        int agentID = chosenShift.getAgentsID();
        if ( chosenShift.getAgentsID() != 0) {
            showShiftAgent.setText(getAgentName(agentID));
        } else {
            showShiftAgent.setText("");
        }
        
    }
    
    
    private String getAgentName(int idNumber) {
        Agent found = register.findAgent(idNumber);
        String fullName = found.getFullName();
        return fullName;
    }
    
    
    private void editShift() {
        if ( chosenShift == null) return;
        try {
            Workshift shift;
            shift = WorkshiftGUIController.askWorkshift(null, chosenShift.clone());
            if (shift == null ) return;
            register.replaceOrAdd(shift);
            getWorkshift(shift.getID());
        } catch (CloneNotSupportedException e) {
            //
        }
    }
    
    
    private void showAgent() {
        chosenAgent = chooserAgents.getSelectedObject();
        
        // System.out.println(chosenAgent.toString());
        
        if (chosenAgent == null) return;
        showFirstname.setText(chosenAgent.get(1));
        showLastname.setText(chosenAgent.get(2));
        showTeamNumber.setText(chosenAgent.get(3));
        showDefaultState.setText(chosenAgent.get(4));
        showModifier.setText(chosenAgent.get(5));
        
        showAbsences(chosenAgent);
    }
    
    
    private void editAgent() {
        if ( chosenAgent == null ) return;
        try {
            Agent agent;
            agent = AgentGUIController.askForAgent(null, chosenAgent.clone());
            if (agent == null) return;
            register.replaceOrAdd(agent);
            getAgent(agent.getIDNumber());
        } catch (CloneNotSupportedException e) {
            //
        }
    }
    
    
    private void showAbsences(Agent agent) {
        tableAbsences.clear();
        if (agent == null) return;
        
        List<Absence> absences = register.getAbsences(agent);
        if (absences.size() == 0) return;
        for (Absence absence : absences) {
            showAbsence(absence);
        }
    }
    
    
    private void showAbsence(Absence absence) {
        String[] row = absence.toString().split("\\|");
        
        if (absence.getExplanation().equals("")) {
            tableAbsences.add(absence, row[1], row[3], row[4], row[5]);
            return;
        }
        tableAbsences.add(absence, row[1], row[3], row[4], row[5], row[6]);
    }
    
    
    private void editAbsence() {
        int r = tableAbsences.getRowNr();
        if ( r < 0 ) return; // clicked title
        Absence absence = tableAbsences.getObject();
        if ( absence == null ) return;
        
        try {
            absence = AbsenceGUIController.askForAbsence(null, absence.clone());
            if ( absence == null ) return;
            register.replaceOrAdd(absence);
            showAbsences(chosenAgent);
            tableAbsences.selectRow(r);
        } catch (CloneNotSupportedException e) {
            //
        }
    }
    
    
    /**
     * Sets the register that's to be used
     * @param register that's used
     */
    public void setRegister(Register register) {
        this.register = register;
        
        showViewDate.setValue(this.register.getRegisterDate("view"));
        showStartDate.setValue(this.register.getRegisterDate("start"));
        showEndDate.setValue(this.register.getRegisterDate("end"));
    }
    
    
    private void handleDateChange(int field) {
        switch ( field ) {
        case 1:
            register.setRegisterDate(field, showViewDate.getValue());
            getWorkshift(0);
            break;
        case 2:
            register.setRegisterDate(field, showStartDate.getValue());
            break;
        case 3:
            register.setRegisterDate(field, showEndDate.getValue());
            break;
        default:
            System.out.println("IDIOT");
        }
    }
    
    
    private void getWorkshift(final int jnr) {
        int jnro = jnr;
        if (jnro == 0) {
            chosenShift = chooserWorkshifts.getSelectedObject();
            if (chosenShift != null) jnro = chosenShift.getID();
        }
        
        chooserWorkshifts.clear();
        Collection<Workshift> workshifts = register.findWorkshifts(showViewDate.getValue());
        
        /*
        System.out.println(this.register.getWorkshiftsSize());
        if (this.register.getWorkshiftsSize() > 0) {
            for (int i = 0 ; i < this.register.getWorkshiftsSize() ; i++ ) {
                System.out.println(this.register.getWorkshift(i).toString());
            }
        }
        */
        
        for (Workshift shift: workshifts) {
            chooserWorkshifts.add(shift.getName(), shift);
        }
        chooserWorkshifts.setSelectedIndex(jnro);
        
    }
    
    
    /**
     * Gets agents information to a list
     * @param jnr employee number to activate after the search
     *          if 0, the current employee is activated
     */
    private void getAgent(final int jnr) {
        int jnro = jnr;
        if (jnro == 0) {
            chosenAgent = chooserAgents.getSelectedObject();
            if (chosenAgent != null) jnro = chosenAgent.getIDNumber();
        }
        int k = cbFields.getSelectionModel().getSelectedIndex() + tempAgent.firstField();
        chooserAgents.clear();
        String criteria = searchCriteria.getText();
        if (criteria.indexOf('*') < 0) criteria = "*" + criteria + "*";
        Collection<Agent> agents = register.findAgents(criteria, k);
        int index = 0;
        int ci = 0;
        for (Agent agent: agents) {
            if (agent.getIDNumber() == jnro) index = ci;
            chooserAgents.add(agent.getFullName(), agent);
            ci++;
        }
        chooserAgents.setSelectedIndex(index);
    }
    
    
    /**
     * Adds a new workshift to the register
     */
    private void newShift() {
        Workshift newShift = new Workshift();
        newShift = WorkshiftGUIController.askWorkshift(null, newShift);
        if ( newShift == null ) return;
        newShift.setDate(showViewDate.getValue());
        newShift.register();
        register.add(newShift);
        getWorkshift(newShift.getID());
    }
    
    
    /**
     * Adds a new agent to the register
     */
    protected void newAgent() {
        Agent newAgent = new Agent();
        newAgent = AgentGUIController.askForAgent(null, newAgent);
        if ( newAgent == null ) return;
        newAgent.register();
        register.add(newAgent);
        getAgent(newAgent.getIDNumber());
    }
    
    /**
     * Adds a new absence to the register for the chosen agent
     */
    private void newAbsence() {
        if ( chosenAgent == null ) return;
        
        Absence newAbsence = new Absence(chosenAgent.getIDNumber());
        newAbsence = AbsenceGUIController.askForAbsence(null, newAbsence);
        if ( newAbsence == null ) return;
        newAbsence.register();
        register.add(newAbsence);
        showAbsences(chosenAgent);
        tableAbsences.selectRow(1000);
        
    }

}
