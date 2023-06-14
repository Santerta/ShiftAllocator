package fxShiftAllocator;

import java.net.URL;
import java.util.ResourceBundle;

import fi.jyu.mit.fxgui.Dialogs;
import fi.jyu.mit.fxgui.ModalController;
import fi.jyu.mit.fxgui.ModalControllerInterface;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import shiftAllocator.Absence;

/**
 * @author Santeri Tammisto
 * @version 14.6.2023
 *
 */
public class AbsenceGUIController implements ModalControllerInterface<Absence>, Initializable {
    
    @FXML private DatePicker chosenDate;
    @FXML private TextField editStartTime;
    @FXML private TextField editEndTime;
    @FXML private CheckBox editWholeDay;
    @FXML private TextField editExplanation;

    @FXML private Button cancelButton;
    @FXML private Label labelError;
    
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeView();
    }
    
    
    @Override
    public Absence getResult() {
        return chosenAbsence;
    }
    
    
    @Override
    public void handleShown() {
        //
    }
    
    
    @Override
    public void setDefault(Absence defaultAbsence) {
        this.chosenAbsence = defaultAbsence;
        showAbsence(chosenAbsence);
        
    }
    
    
    @FXML void handleOK() {
        if (chosenAbsence != null && chosenDate.getValue() == null) {
            showError("Date field can not be empty");
            return;
        }
        
        handleChangeToAbsence(1);
        handleChangeToAbsence(2);
        handleChangeToAbsence(3);
        handleChangeToAbsence(4);
        handleChangeToAbsence(5);
        
        if (labelError.getText().equals("")) ModalController.closeStage(labelError);
        return;
    }

    
    @FXML void handleCancel() {
        chosenAbsence = null;
        ModalController.closeStage(labelError);
    }

    // =====================================================================================================
    
    private Absence chosenAbsence;
    
    private void initializeView() {
        // TODO: listeners
    }
    
    private void showAbsence(Absence absence) {
        if ( absence == null ) return;
        
        chosenDate.setValue(absence.getDate());
        editStartTime.setText(absence.getStartTime());
        editEndTime.setText(absence.getStopTime());
        editWholeDay.setSelected(absence.getWholeDayFlag());
        editExplanation.setText(absence.getExplanation());
    }
    
    
    private void handleChangeToAbsence(int field) {
        if ( chosenAbsence == null ) return;
        String s = "";
        String error = null;
        switch ( field ) {
        case 1:
            s = chosenDate.getValue().toString();
            error = chosenAbsence.setValueFor(field, s);
            if (error == null) {
                Dialogs.setToolTipText(chosenDate, "");
                chosenDate.getStyleClass().removeAll(error);
                showError(error);
            } else {
                Dialogs.setToolTipText(chosenDate, "");
                chosenDate.getStyleClass().add("error");
                showError(error);
            }
            break;
        case 2:
            s = editStartTime.getText();
            error = chosenAbsence.setValueFor(field, s);
            if (error == null) {
                Dialogs.setToolTipText(editStartTime, "");
                editStartTime.getStyleClass().removeAll(error);
                showError(error);
            } else {
                Dialogs.setToolTipText(editStartTime, "");
                editStartTime.getStyleClass().add("virhe");
                showError(error);
            }
            break;
        case 3:
            s = editEndTime.getText();
            error = chosenAbsence.setValueFor(field, s);
            if (error == null) {
                Dialogs.setToolTipText(editEndTime, "");
                editEndTime.getStyleClass().removeAll(error);
                showError(error);
            } else {
                Dialogs.setToolTipText(editEndTime, "");
                editEndTime.getStyleClass().add("virhe");
                showError(error);
            }
            break;
        case 4:
            s = String.valueOf(editWholeDay.isSelected());
            chosenAbsence.setValueFor(field, s);
            break;
        case 5:
            s = editExplanation.getText();
            chosenAbsence.setValueFor(field, s);
            break;
        default:
            System.out.println("IDIOT");
        }
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
     * Luodaan poissaolon kysymysdialogi ja palautetaan sama tietue muutettuna tai null
     * @param modalityStage mille ollaan modaalisia, null = sovellukselle
     * @param oletus mitä dataa näytetään oletuksena
     * @return null jos painetaan Cancel, muuten täytetty tietue
     */
    public static Absence askForAbsence(Stage modalityStage, Absence oletus) {
        return ModalController.showModal(AllocatorGUIController.class.getResource("AbsenceGUIView.fxml"), "Poissaolo", modalityStage, oletus);
    }
    
    
}