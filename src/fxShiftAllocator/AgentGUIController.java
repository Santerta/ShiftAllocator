package fxShiftAllocator;

import java.net.URL;
import java.util.ResourceBundle;

import fi.jyu.mit.fxgui.Dialogs;
import fi.jyu.mit.fxgui.ModalController;
import fi.jyu.mit.fxgui.ModalControllerInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import shiftAllocator.Agent;
import shiftAllocator.Allocator;
import shiftAllocator.Register;

/**
 * @author Santeri Tammisto
 * @version 14.6.2023
 *
 */
public class AgentGUIController implements ModalControllerInterface<Agent>,Initializable {

    @FXML TextField editFirstname;
    @FXML TextField editLastname;
    @FXML ChoiceBox<Integer> editTeamNumber;
    @FXML TextField editDefaultState;
    @FXML TextField editModifier;
    
    @FXML CheckBox editR1;
    @FXML CheckBox editR2;
    @FXML CheckBox editR3;
    @FXML CheckBox editR4;
    @FXML CheckBox editR5;
    @FXML CheckBox editR6;
    @FXML CheckBox editR7;
    @FXML CheckBox editR8;
    @FXML CheckBox editR9;
    @FXML CheckBox editR10;
    @FXML CheckBox editR11;
    @FXML CheckBox editR12;
    
    @FXML Label labelError;
    
    @FXML private Button cancelButton;
    
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeView();
    }

    
    @Override
    public Agent getResult() {
        return chosenAgent;
    }

    
    @Override
    public void handleShown() {
        //
        
    }

    
    @Override
    public void setDefault(Agent defaultAgent) {
        this.chosenAgent = defaultAgent;
        showAgent(chosenAgent);
    }
    
    
    @FXML private void handleOK() {
        if ( chosenAgent != null && chosenAgent.get(1).trim().equals("") || chosenAgent != null && chosenAgent.get(2).trim().equals("")) {
            showError("Names can not be empty");
            return;
        }

        // After "OK" changes every value for chosenAgent even though there hasn't been any changes
        // TODO: Create a better solution
        for (int i = chosenAgent.firstField() ; i < chosenAgent.getFields() ; i++) {
            handleChangeToAgentInfo(i);
        }
        
        // Same here for responsibilities
        for (int i = 0; i < chosenAgent.getResponsibilitiesArrayLength(); i++) {
            handleChangeToAgentResponsibility(i);
        }
        
        ModalController.closeStage(labelError);
    }

    
    @FXML private void handleCancel() {
        chosenAgent = null;
        ModalController.closeStage(labelError);
    }
    
    
    // =======================================================
    
    
    private Agent chosenAgent;
    
    
    private void initializeView() {
        // TODO: More listeners
        editFirstname.setOnKeyReleased( e -> handleChangeToAgentInfo(1));
        editLastname.setOnKeyReleased(e -> handleChangeToAgentInfo(2));
        
        // Sets the text for responsibilities instead of using R1-R12
        // TODO: Non-static implementation
        editR1.setText(Allocator.responsibilities.get(0));
        editR2.setText(Allocator.responsibilities.get(1));
        editR3.setText(Allocator.responsibilities.get(2));
        editR4.setText(Allocator.responsibilities.get(3));
        editR5.setText(Allocator.responsibilities.get(4));
        editR6.setText(Allocator.responsibilities.get(5));
        editR7.setText(Allocator.responsibilities.get(6));
        editR8.setText(Allocator.responsibilities.get(7));
        editR9.setText(Allocator.responsibilities.get(8));
        editR10.setText(Allocator.responsibilities.get(9));
        editR11.setText(Allocator.responsibilities.get(10));
        editR12.setText(Allocator.responsibilities.get(11));
    }
    
    
    private void handleChangeToAgentInfo(int field) {
        if (chosenAgent == null) return;
        String newValue = "";
        String error = null;
        switch (field) {
        case 1:
            newValue = editFirstname.getText();
            error = chosenAgent.set(field, newValue);
            if (error == null) {
                Dialogs.setToolTipText(editFirstname, "");
                editFirstname.getStyleClass().removeAll("error");
                showError(error);
            } else {
                Dialogs.setToolTipText(editFirstname, error);
                editFirstname.getStyleClass().add("error");
                showError(error);
            }
            break;
        case 2:
            newValue = editLastname.getText();
            error = chosenAgent.set(field, newValue);
            if (error == null) {
                Dialogs.setToolTipText(editLastname, "");
                editLastname.getStyleClass().removeAll("error");
                showError(error);
            } else {
                Dialogs.setToolTipText(editLastname, error);
                editLastname.getStyleClass().add("error");
                showError(error);
            }
            break;
        case 3:
            newValue = "" + editTeamNumber.getValue();
            error = chosenAgent.set(field, newValue);
            break;
        case 4:
            newValue = editDefaultState.getText();
            error = chosenAgent.set(field, newValue);
            break;
        case 5:
            newValue = editModifier.getText();
            error = chosenAgent.set(field, newValue);
            break;
        default:
            System.out.println("IDIOT");
        }
        
    }
    
    private void handleChangeToAgentResponsibility(int index) {
        switch ( index ){
            case 0:
                chosenAgent.setResponsibility(index, editR1.isSelected());
                break;
            case 1:
                chosenAgent.setResponsibility(index, editR2.isSelected());
                break;
            case 2:
                chosenAgent.setResponsibility(index, editR3.isSelected());
                break;
            case 3:
                chosenAgent.setResponsibility(index, editR4.isSelected());
                break;
            case 4:
                chosenAgent.setResponsibility(index, editR5.isSelected());
                break;
            case 5:
                chosenAgent.setResponsibility(index, editR6.isSelected());
                break;
            case 6:
                chosenAgent.setResponsibility(index, editR7.isSelected());
                break;
            case 7:
                chosenAgent.setResponsibility(index, editR8.isSelected());
                break;
            case 8:
                chosenAgent.setResponsibility(index, editR9.isSelected());
                break;
            case 9:
                chosenAgent.setResponsibility(index, editR10.isSelected());
                break;
            case 10:
                chosenAgent.setResponsibility(index, editR11.isSelected());
                break;
            case 11:
                chosenAgent.setResponsibility(index, editR12.isSelected());
                break;
            default:
                System.out.println("IDIOT");
                    
        }
    }
    
    
    private void showAgent(Agent agent) {
        if(agent == null) return;
        
        // Basic info
        editFirstname.setText(agent.get(1));
        editLastname.setText(agent.get(2));
        
        
        // TODO: Get the amount of teams from the register
        ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        editTeamNumber.setItems(options);
        editTeamNumber.setValue(options.get(agent.getTeamNumber()-1));
        
        editDefaultState.setText(agent.get(4));
        editModifier.setText(agent.get(5).toString());
        
        // Responsibilities
        editR1.setSelected(agent.getResponsibilityByIndex(0));
        editR2.setSelected(agent.getResponsibilityByIndex(1));
        editR3.setSelected(agent.getResponsibilityByIndex(2));
        editR4.setSelected(agent.getResponsibilityByIndex(3));
        editR5.setSelected(agent.getResponsibilityByIndex(4));
        editR6.setSelected(agent.getResponsibilityByIndex(5));
        editR7.setSelected(agent.getResponsibilityByIndex(6));
        editR8.setSelected(agent.getResponsibilityByIndex(7));
        editR9.setSelected(agent.getResponsibilityByIndex(8));
        editR10.setSelected(agent.getResponsibilityByIndex(9));
        editR11.setSelected(agent.getResponsibilityByIndex(10));
        editR12.setSelected(agent.getResponsibilityByIndex(11));
        
    }
    
    
    private void showError(String error) {
        if ( error == null || error.isEmpty() ) {
            labelError.setText("");
            labelError.getStyleClass().removeAll("error");
            return;
        }
        labelError.setText(error);
        labelError.getStyleClass().add("error");
    }
    
    
    /**
     * Creates an agent input dialog and returns the modified record or null.
     * @param modalityStage the stage to which the dialog is modal, null = application level
     * @param defaultChoice the data to be displayed as a default
     * @return null if Cancel button is pressed, otherwise the filled record
     */
    public static Agent askForAgent(Stage modalityStage, Agent defaultChoice) {
        return ModalController.showModal(AllocatorGUIController.class.getResource("AgentGUIView.fxml"), "Agent", modalityStage, defaultChoice);   
    }
    
}