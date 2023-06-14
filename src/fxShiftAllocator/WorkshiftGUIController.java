package fxShiftAllocator;

import java.net.URL;
import java.util.ResourceBundle;

import fi.jyu.mit.fxgui.ComboBoxChooser;
import fi.jyu.mit.fxgui.Dialogs;
import fi.jyu.mit.fxgui.ModalController;
import fi.jyu.mit.fxgui.ModalControllerInterface;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import shiftAllocator.Register;
import shiftAllocator.Workshift;

/**
 * @author Santeri
 * @version 5.6.2022
 *
 */
public class WorkshiftGUIController implements ModalControllerInterface<Workshift>,Initializable {
    
    @FXML TextField editShiftName;
    @FXML TextField editShiftStart;
    @FXML TextField editShiftEnd;
    @FXML CheckBox editShiftWholeDay;
    
    @FXML ComboBoxChooser<String> cbFields;
    
    @FXML Label labelError;
    
    @FXML private static Button cancelButton;

    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeView();
    }

    
    @Override
    public Workshift getResult() {
        return chosenWorkshift;
    }

    
    @Override
    public void handleShown() {
        //
    }

    
    @Override
    public void setDefault(Workshift defaultShift) {
        chosenWorkshift = defaultShift;
        showWorkshift(chosenWorkshift);
        
    }
    
    
    @FXML void handleOK() {
        
        if (chosenWorkshift != null && chosenWorkshift.getName().trim().equals("")) {
            showError("Name can not be empty");
            return;
        }
        
        // TODO: FIX
        // Changes every value even though there hasn't been any changes to them
        //kasitteleMuutosTyovuoroon(1);
        handleChangestoWorkShift(2);
        handleChangestoWorkShift(3);
        handleChangestoWorkShift(4);
        handleChangestoWorkShift(5);

        if (labelError.getText().equals("")) ModalController.closeStage(labelError);
        return;
        
    }

    
    @FXML void handleCancel() {
        ModalController.closeStage(labelError);
    }
    
    
    // =====================================================================
    
    
    private static Workshift chosenWorkshift;
    
    private void initializeView() {
        cbFields.clear();
        for (int i = 0; i < Register.responsibilities.size(); i++) {
            cbFields.add(Register.responsibilities.get(i));
        }
        
        // TODO: Create handlers for other events and fields
        editShiftName.setOnKeyReleased( e -> handleChangestoWorkShift(1));
    }
    
    
    private void handleChangestoWorkShift(int field) {
        if ( chosenWorkshift == null) return;
        String s = "";
        String error = null;
        switch (field) {
        case 1:
            s = editShiftName.getText();
            error = chosenWorkshift.set(field, s);
            if (error == null) {
                Dialogs.setToolTipText(editShiftName, "");
                showError(error);
            } else {
                Dialogs.setToolTipText(editShiftName, "");
                editShiftName.getStyleClass().add("error");
                showError(error);
            }
            break;
        case 2:
            s = editShiftStart.getText();
            error = chosenWorkshift.set(field, s);
            if (error == null) {
                Dialogs.setToolTipText(editShiftStart, "");
                editShiftStart.getStyleClass().removeAll(error);
                showError(error);
            } else {
                Dialogs.setToolTipText(editShiftStart, "");
                editShiftStart.getStyleClass().add("error");
                showError(error);
            }
            break;
        case 3:
            s = editShiftEnd.getText();
            error = chosenWorkshift.set(field, s);
            if (error == null) {
                Dialogs.setToolTipText(editShiftEnd, "");
                editShiftEnd.getStyleClass().removeAll(error);
                showError(error);
            } else {
                Dialogs.setToolTipText(editShiftEnd, "");
                editShiftEnd.getStyleClass().add("error");
                showError(error);
            }
            break;
        case 4:
            s = String.valueOf(editShiftWholeDay.isSelected());
            chosenWorkshift.set(field, s);
            break;
        case 5:
            s = "" + cbFields.getSelectedIndex();
            chosenWorkshift.set(field, s);
            break;
        default:
            System.out.println("IDIOT");
        }
    }
    
    
    private void showWorkshift(Workshift shift) {
        if (shift == null) return;

        editShiftName.setText(shift.getName());
        editShiftStart.setText(shift.getStartTime());
        editShiftEnd.setText(shift.getEndTime());
        editShiftWholeDay.setSelected(shift.getWholeDayFlag());
        
        cbFields.setSelectedIndex(shift.getResponsibility());
    }
    
    
    private void showError(String error) {
        if ( error == null || error.isEmpty() ) {
            labelError.setText("");
            labelError.getStyleClass().removeAll("virhe");
            return;
        }
        labelError.setText(error);
        labelError.getStyleClass().add("virhe");
    }
    

    /**
     * Creates a workshift input dialog and returns the modified shift object or null.
     * @param modalityStage the stage to which the dialog is modal, null = application level
     * @param defaultShift the data to be displayed as a default
     * @return null if Cancel button is pressed, otherwise the filled shift object
     */
    public static Workshift askWorkshift(Stage modalityStage, Workshift defaultShift) {
        return ModalController.showModal(AllocatorGUIController.class.getResource("WorkshiftGUIView.fxml"), "Workshift", modalityStage, defaultShift);
    }
    
}