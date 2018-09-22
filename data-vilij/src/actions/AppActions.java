package actions;

import dataprocessors.AppData;
import dataprocessors.DataSet;
import dataprocessors.TSDProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Chart;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import static settings.AppPropertyTypes.*;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;
    private File selectedFile;
    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    @Override
    public void handleNewRequest() {
        if(!(((AppUI)(applicationTemplate.getUIComponent())).getText().equals(""))) {
            try {
                if(promptToSave() == true) {
                    ((AppUI)(applicationTemplate.getUIComponent())).deleteText();
                    applicationTemplate.getDataComponent().clear();
                    applicationTemplate.getUIComponent().clear();
                    dataFilePath = null;
                    ((UITemplate)applicationTemplate.getUIComponent()).getNewButton().setDisable(true);
                    ((UITemplate)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                    ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // TODO for homework 1
    }

    @Override
    public void handleSaveRequest() {
        PropertyManager manager = applicationTemplate.manager;
        TSDProcessor tdp = new TSDProcessor();
            try {
                tdp.processString(((AppUI) applicationTemplate.getUIComponent()).getText());
                if(dupliLine().equals("")) {
                        promptToSave();
                }
                else {
                    ErrorDialog.getDialog().show(manager.getPropertyValue(INVALID_DATA_TITLE.name()), "There are multiple: " + dupliLine());
                }
            }
            catch (java.lang.Exception e) {
                ErrorDialog.getDialog().show(manager.getPropertyValue(INVALID_DATA_TITLE.name()), String.format(e.toString()));
                return;
            }
        }
            // TODO: NOT A PART OF HW 1

    @Override
    public void handleLoadRequest() {
        PropertyManager manager = applicationTemplate.manager;
        FileChooser newFile = new FileChooser();
        newFile.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(DATA_FILE_EXT.name())));
        selectedFile = newFile.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (selectedFile != null) {
            applicationTemplate.getDataComponent().clear();
            applicationTemplate.getUIComponent().clear();
            dataFilePath = Paths.get(selectedFile.getAbsolutePath());
            ((AppUI)applicationTemplate.getUIComponent()).displayVbox();
            (applicationTemplate.getDataComponent()).loadData(dataFilePath);
            try {
                DataSet.fromTSDFile(dataFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        if(!((UITemplate)applicationTemplate.getUIComponent()).getSaveButton().isDisabled()) {
            try {
                if(promptToSave()==true) {
                    applicationTemplate.getUIComponent().getPrimaryWindow().close();
                }
            } catch (IOException e) {

            }
        }
        if(((AppUI)applicationTemplate.getUIComponent()).isAlgoRunning()) {
            if(exitAlgorithm()==true) {
                applicationTemplate.getUIComponent().getPrimaryWindow().close();
            }
        }
        else {
            applicationTemplate.getUIComponent().getPrimaryWindow().close();
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        Chart chart = ((AppUI)applicationTemplate.getUIComponent()).getChart();
        try {
            WritableImage myImage = chart.snapshot(new SnapshotParameters(), null);
            File myFile = new File("awesomeImage.png");
            ImageIO.write(SwingFXUtils.fromFXImage(myImage, null), "png", myFile);
        }
        catch (IOException r) {
            
        }
        // TODO: NOT A PART OF HW 1
    }
    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
    }

    public String dupliLine() {
        String a = ((AppData)applicationTemplate.getDataComponent()).getBigString();
        String[] aLine = a.split("\n");
        if(aLine.length==1) {
            return "";
        }
        String[] aName = new String[3];
        ArrayList<String> allNames = new ArrayList<>();
        for(String s : aLine) {
            aName = s.split("\t");
            allNames.add(aName[0]);
        }
        for(int i=0; i < allNames.size()-1; i++) {
            if(allNames.get(i).equals(allNames.get(i+1))) {
                return allNames.get(i);
            }
        }
        return "";
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    public boolean exitAlgorithm() {
        ConfirmationDialog.getDialog().show("Algorithm running at the moment", "Would you like to kill the algorithm and exit anyway?");
        if(ConfirmationDialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            return true;
        }
        return false;
    }
    private boolean promptToSave() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
            ConfirmationDialog.getDialog().show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
            if(ConfirmationDialog.getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
                return true;
            }
            else if(ConfirmationDialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                if(dataFilePath == null) {
                    FileChooser newFile = new FileChooser();
                    newFile.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(DATA_FILE_EXT.name())));
                    File selectedFile = newFile.showSaveDialog(ConfirmationDialog.getDialog());
                    if (selectedFile != null) {
                        try {
                            FileWriter write = new FileWriter(selectedFile);
                            write.write(((AppUI) (applicationTemplate.getUIComponent())).getText());
                            write.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }

                    try {
                        dataFilePath = selectedFile.toPath();
                    }
                    catch (Exception t) {
                    }
                    ((UITemplate) (applicationTemplate.getUIComponent())).getSaveButton().setDisable(true);
                    return true;
                }
                else {
                    save();
                    return true;
                }
        }
        return false;
    }
}
