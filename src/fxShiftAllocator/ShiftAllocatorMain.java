package fxShiftAllocator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import shiftAllocator.Allocator;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;


/**
 * @author Santeri Tammisto
 * @version 14.6.2023
 *
 */
public class ShiftAllocatorMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader ldr = new FXMLLoader(getClass().getResource("AllocatorGUIView.fxml"));
            final Pane root = ldr.load();
            final AllocatorGUIController shiftAllocatorCtrl = (AllocatorGUIController)ldr.getController();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Fenix");
            
            primaryStage.setOnCloseRequest((event) -> {
                if ( !shiftAllocatorCtrl.canClose() ) event.consume();
            });
            
            Allocator allocator = new Allocator();
            shiftAllocatorCtrl.setAllocator(allocator);
            primaryStage.show();
            
            Application.Parameters params = getParameters();
            if ( params.getRaw().size() > 0) {
                shiftAllocatorCtrl.readFile(params.getRaw().get(0));
            } else {
                if ( !shiftAllocatorCtrl.open() ) Platform.exit();
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args not in use
     */
    public static void main(String[] args) {
        launch(args);
    }
}