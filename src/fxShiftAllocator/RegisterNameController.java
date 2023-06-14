package fxShiftAllocator;

import fi.jyu.mit.fxgui.ModalController;
import fi.jyu.mit.fxgui.ModalControllerInterface;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Asks registers name and creates a dialog for it
 * 
 * @author Santeri Tammisto
 * @version 14.6.2023
 */
public class RegisterNameController implements ModalControllerInterface<String> {
    
    @FXML private TextField textAnswer;
    private String answer = null;

    
    @FXML private void handleOK() {
        answer = textAnswer.getText();
        ModalController.closeStage(textAnswer);
    }

    
    @FXML private void handleCancel() {
        ModalController.closeStage(textAnswer);
    }


    @Override
    public String getResult() {
        return answer;
    }

    
    @Override
    public void setDefault(String oletus) {
        textAnswer.setText(oletus);
    }

    
    @Override
    public void handleShown() {
        textAnswer.requestFocus();
    }
    
    
    /**
     * Creates a name input dialog and returns the name entered in it or null.
     * @param modalityStage the stage to which the dialog is modal, null = application level
     * @param defaultName the name to be displayed as a default
     * @return null if Cancel button is pressed, otherwise the entered name
     */
    public static String askName(Stage modalityStage, String defaultName) {
        return ModalController.showModal(
                RegisterNameController.class.getResource("RegisterNameView.fxml"),
                "Register",
                modalityStage, defaultName);
    }
}
