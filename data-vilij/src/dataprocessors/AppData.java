package dataprocessors;

import actions.AppActions;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static settings.AppPropertyTypes.INVALID_DATA_TITLE;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private String              bigString = "";
    private ArrayList<String> listLineA = new ArrayList<>();
    public ArrayList<String> getListLineA() {
        return listLineA;
    }
    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }
    public String getBigString() {
        return bigString;
    }
    public int getNumInstances() {
        return bigString.split("\n").length;
    }
    @Override
    public void loadData(Path dataFilePath) {
        PropertyManager manager = applicationTemplate.manager;
        try {
            StringBuilder stringBuffer = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(((AppActions)applicationTemplate.getActionComponent()).getSelectedFile()));
            try {
                String text;
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text+"\n");
                }
            }
            catch (FileNotFoundException ex) {

            }
            finally {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            String[] aLine = stringBuffer.toString().split("\n");
            listLineA = new ArrayList(Arrays.asList(aLine));
            if(listLineA.size()>10) {
                ErrorDialog.getDialog().show("Large File Loaded", "Loaded data consists of " + listLineA.size() + " lines. Showing only the first 10 in the text-area.");
                String d = "";
                for(int i=0; i<10; i++) {
                    d += listLineA.remove(0) + "\n";
                }
                ((AppUI) applicationTemplate.getUIComponent()).setTextArea(d);
            }
            else {
                ((AppUI) applicationTemplate.getUIComponent()).setTextArea(stringBuffer.toString());
                listLineA.clear();
            }
            bigString = stringBuffer.toString();
            loadData(stringBuffer.toString());
            ((AppUI)applicationTemplate.getUIComponent()).makeVBoxF();
            ((AppUI)applicationTemplate.getUIComponent()).getTogButton().setDisable(true);
            ((AppUI)applicationTemplate.getUIComponent()).metaData(bigString, dataFilePath.toString());
            ((AppUI)applicationTemplate.getUIComponent()).setDataSet(DataSet.fromTSDFile(dataFilePath));

        } catch (IOException e1) {

        } catch (Exception e) {
        }
        // TODO: NOT A PART OF HW 1
    }

    public void loadData(String dataString) {
        PropertyManager manager = applicationTemplate.manager;
            try {
                if(getBigString().equals("")) {
                    bigString = dataString;
                }
            processor.processString(dataString);
            ((UITemplate)applicationTemplate.getUIComponent()).getNewButton().setDisable(false);
            ((UITemplate)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
            if(((AppActions)applicationTemplate.getActionComponent()).dupliLine().equals("")) {
                displayData();
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
                ((AppUI)applicationTemplate.getUIComponent()).metaData(dataString, "");
            }
            else {
                ErrorDialog.getDialog().show(manager.getPropertyValue(INVALID_DATA_TITLE.name()), "There are multiple: " + ((AppActions)applicationTemplate.getActionComponent()).dupliLine());
            }
        } catch (Exception e1) {
            ErrorDialog.getDialog().show(manager.getPropertyValue(INVALID_DATA_TITLE.name()), String.format(e1.toString()));
            ((UITemplate)applicationTemplate.getUIComponent()).getNewButton().setDisable(true);
            ((UITemplate)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
        }
        // TODO for homework 1
    }

    @Override
    public void saveData(Path dataFilePath) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getText());
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void clear() {
        processor.clear();
        bigString = "";
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }

    public TSDProcessor getProcessor() {
        return processor;
    }
}
