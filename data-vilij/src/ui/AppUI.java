package ui;

import actions.AppActions;
import algorithms.Classifier;
import algorithms.Clusterer;
import algorithms.KMeansClusterer;
import dataprocessors.AppData;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;

import dataprocessors.DataSet;
import dataprocessors.TSDProcessor;
import javafx.application.Platform;
import javafx.geometry.Pos;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import algorithms.RandomClassifier;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;


/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */

    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private final static String SEPARATOR = "/";

    public Button getScrnshotButton() {
        return scrnshotButton;
    }

    public ToggleButton getTogButton() {
        return togButton;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public boolean isAlgoRunning() {
        return algoRunning;
    }

    private Thread newThread;
    private Label metaLabel = new Label();
    private HashSet<String> k;
    private String[] classificationList = {"1", "1", "false"};
    private String[] clusteringList = {"1", "1", "false", "2"};
    private ToggleButton togButton = new ToggleButton("Done");
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private String                       scrnshotPath;
    private String                       settingssPath;
    private String                       runPath;
    private boolean                      algoRunning = false;
    private Button                       run = new Button();
    private VBox vbox = new VBox();
    private RadioButton                  radioA = new RadioButton("Clustering");
    private RadioButton                  radioB = new RadioButton("Classification");
    private Button setting;
    private VBox                         vboxF = new VBox();
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private TextArea                     textArea = new TextArea();       // text area for new data input
    private boolean                      hasNewText = true;     // whether or not the text area has any new data since last display
    private boolean                      threadStarted = false;
    private Classifier                   fire;
    private Clusterer                    fury;
    private int                          name;
    private ArrayList<RadioButton> algoButtonList = new ArrayList<>(10);

    public boolean isThread2Started() {
        return thread2Started;
    }

    public void setThread2Started(boolean thread2Started) {
        this.thread2Started = thread2Started;
    }

    private boolean                      thread2Started = true;
    private TextField                    textIterate;
    private CheckBox                     cRun;
    private TextField                    textInterval;
    private TextField                    textCluster;
    private DataSet                      dataSet = new DataSet();
    private Thread                       abc;

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public LineChart<Number, Number> getChart() { return chart; }

    public void setTextArea(String text) {
        this.textArea.setText(text);
    }

    public String getText() {
       return  textArea.getText();
    }
    public void deleteText() {
        textArea.deleteText(0,textArea.getLength());
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshotPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        super.setToolBar(applicationTemplate);
        newButton.setDisable(false);
        scrnshotButton = setToolbarButton(scrnshotPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar.getItems().add(scrnshotButton);
        toolBar.setStyle("-fx-base: DDB0A0;");
    }
    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> {
            if(!vbox.getChildren().contains(vboxF)) {
                displayVbox();
            }
            if(textArea.getText()!="") {
                applicationTemplate.getActionComponent().handleNewRequest();
            }
            else {
                applicationTemplate.getActionComponent().handleNewRequest();
            }
        });
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e1) {

            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        togButton.setSelected(false);
        radioA.setSelected(false);
        radioB.setSelected(false);
        togButton.setDisable(false);
        chart.getData().clear();
        scrnshotButton.setDisable(true);
        metaLabel.setText(null);
        vboxF.getChildren().clear();
        for(int i=0; i<2; i++) {
            clusteringList[i] = "1";
            classificationList[i] = "1";
        }
        // TODO for homework 1
    }

    public Button getRun() {
        return run;
    }

    public void metaData(String bigString, String filePath){
        String[] lines = bigString.split("\n");
        k = new HashSet();
        int i=0;
        for (i = 0; i < lines.length; i++) {
            String[] elements = lines[i].split("\t");
            k.add(elements[1]);
        }
        String labels = "";
        for(String l : k) {
             labels += l+", ";
        }
        labels.substring(0, labels.length()-1);
        metaLabel.setText("The total number of instances are "+lines.length+"\n the total number of labels are "+k.size()+"\n the labels are" +
                ": "+labels+"\n the source path is "+filePath);
    }

    public void displayVbox() {
        vbox.setSpacing(15);
        vbox.getChildren().clear();
        VBox vbox1 = new VBox();
        VBox vbox2 = new VBox();
        HBox hbox2 = new HBox();
        togButton.setStyle("-fx-base: DDB0A0;");
        hbox2.getChildren().addAll(togButton);
        hbox2.setSpacing(30.0);
        Label label1 = new Label("Data File");
        label1.setFont(Font.font("Arial", 15));
        vbox2.getChildren().add(label1);
        vbox2.setMaxWidth(400.0);
        vbox2.setAlignment(Pos.CENTER);
        textArea.setMinHeight(200.0);
        textArea.setMaxWidth(400.0);
        vbox1.getChildren().add(textArea);
        vbox.getChildren().addAll(vbox2, vbox1, hbox2, metaLabel, vboxF);
        String t = ((AppData)applicationTemplate.getDataComponent()).getBigString();
    }
    private void layout() {
        vbox.setMinSize(400,400);
        HBox hbox1 = new HBox();
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.forceZeroInRangeProperty().setValue(false);
        yAxis.forceZeroInRangeProperty().setValue(false);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setMinWidth(600.0);
        chart.setVerticalZeroLineVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setStyle("-fx-base: ffffff;");
        hbox1.getChildren().addAll(vbox, chart);
        appPane.getChildren().add(hbox1);
        String css = this.getClass().getResource("/gui.css").toExternalForm();
        primaryScene.getStylesheets().add(css);
        }

    public void setThreadStarted(boolean threadStarted) {
        this.threadStarted = threadStarted;
    }

    public void configWindow(int p) {
        String[] tempList = new String[4];
        Label confiLabel = new Label("Algorithm Run Configuration");
        confiLabel.setFont(new Font(20));
        Pane secondaryLayout = new VBox(20);
        HBox hBox1 = new HBox();
        Label mIterations = new Label("Max Iteration: ");
        textIterate = new TextField();
        HBox hBox2 = new HBox();
        Label updateInterval = new Label("Update Interval:  ");
        textInterval = new TextField();
        textCluster = new TextField();
        cRun = new CheckBox("Continuous Run?");
        cRun.setStyle("-fx-base: ffffff;");

        if(p==1){
            if(classificationList[0]!=null){
                textIterate.setText(classificationList[0]);
            }
            if(classificationList[1]!=null){
                textInterval.setText(classificationList[1]);
            }
            if(classificationList[2]!=null){
                if(classificationList[2].equals("false")) {
                    cRun.setSelected(false);
                }
                else
                    cRun.setSelected(true);
            }
        }
        else{
            if(clusteringList[0]!=null){
                textIterate.setText(clusteringList[0]);
            }
            if(clusteringList[1]!=null){
                textInterval.setText(clusteringList[1]);
            }
            if(clusteringList[2]!=null){
                if(clusteringList[2].equals("false")) {
                    cRun.setSelected(false);
                }
                else
                    cRun.setSelected(true);
            }
            if(clusteringList[3]!=null) {
                textCluster.setText(clusteringList[3]);
            }
        }
        hBox1.getChildren().addAll(mIterations, textIterate);
        hBox2.getChildren().addAll(updateInterval, textInterval);
        hBox1.setSpacing(10.0);
        hBox2.setSpacing(10.0);
        secondaryLayout.getChildren().addAll(confiLabel, hBox1,hBox2, cRun);
        if(p==2) {
            HBox hBox3 = new HBox();
            Label cluster = new Label("Cluster:  ");
            hBox3.getChildren().addAll(cluster, textCluster);
            secondaryLayout.getChildren().add(hBox3);
        }
        secondaryLayout.setStyle("-fx-base: DDB0A0;");
        Scene secondScene = new Scene(secondaryLayout, 350, 300);
        Stage newWindow = new Stage();
        newWindow.setMaxHeight(400);
        newWindow.setMaxWidth(500);
        newWindow.setTitle("How would you like to run?");
        newWindow.setScene(secondScene);
        newWindow.showAndWait();
        try{
            int it = Integer.parseInt(textIterate.getText());
            tempList[0]= it+"";
        }catch(Exception e2){
            tempList[0]="1";
        }
        try{
            int it = Integer.parseInt(textInterval.getText());
            tempList[1]=it+"";
        }catch(Exception e2){
            tempList[1]="1";
        }
        if(cRun.isSelected()){
            tempList[2]="true";
        }
        else{
            tempList[2]= "false";
        }
        try{
            int cluster = Integer.parseInt(textCluster.getText());
            tempList[3]= cluster+"";
        }
        catch (Exception e3) {
            tempList[3] = "1";
        }
        if(p==1){
            classificationList=tempList;
        }
        else{
            clusteringList=tempList;
        }
      /*  run.setOnAction(i-> {
            if(p==1) {

            }
            else if(p==2) {
            }
        }); */
    }
    public void whichAlgoSelected(int num) {
        String trip = "";
        for (RadioButton b : algoButtonList) {
            if(b.isSelected()) {
                trip = b.getText();
                break;
            }
        }
        if(num==2) {
            try {
                abc = new Thread((Runnable) Class.forName("algorithms." + trip).getDeclaredConstructor(DataSet.class, int.class, int.class, int.class, ApplicationTemplate.class, boolean.class).newInstance(dataSet, Integer.parseInt(textIterate.getText()), Integer.parseInt(textInterval.getText()), Integer.parseInt(textCluster.getText()), applicationTemplate, cRun.isSelected()));
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            } catch (ClassNotFoundException e) {
                ErrorDialog.getDialog().show("The Class is not present.", "The class thy has chosen is not present in the package. Please try choosing any valid algorithm.");
            }
        }
        else if(num==1) {
            try {
                newThread = new Thread((Runnable) Class.forName("algorithms." + trip).getDeclaredConstructor(DataSet.class, int.class, int.class, boolean.class, ApplicationTemplate.class).newInstance(dataSet, Integer.parseInt(textIterate.getText()), Integer.parseInt(textInterval.getText()), cRun.isSelected(), applicationTemplate));
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            } catch (ClassNotFoundException e) {
                ErrorDialog.getDialog().show("The Class is not present.", "The class thy has chosen is not present in the package. Please try choosing any valid algorithm.");
            }
        }

    }
    private void setWorkspaceActions(){
        PropertyManager manager = applicationTemplate.manager;

        radioA.setOnAction(e-> {
            vboxF.getChildren().clear();
            String runiconspath = SEPARATOR + String.join(SEPARATOR,
                    manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                    manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
            runPath = String.join(SEPARATOR, runiconspath, manager.getPropertyValue(RUN_ICON.name()));
            Image runImage = new Image(runPath);
            ImageView runView = new ImageView(runImage);
            runView.setFitWidth(20.0);
            runView.setFitHeight(20.0);
            run = new Button(null, runView);
            run.minHeight(10.0);

            Label labelClus = new Label("Clustering");
            labelClus.setFont(Font.font("Arial", 15));
            vboxF.getChildren().add(labelClus);

            StringBuilder stringBuffer = new StringBuilder();
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("algorithms.txt"));
                String text;
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text+"\n");
                }
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (FileNotFoundException e1) {
            } catch (IOException e1) {
            }
            String[] algos = stringBuffer.toString().split("\n");
            for (RadioButton g : algoButtonList) {
                g = new RadioButton();
            }
            for(int ij =0; ij<algos.length; ij++) {
                String iconsPath = SEPARATOR + String.join(SEPARATOR,
                        manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                        manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
                settingssPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SETTINGSS_ICON.name()));
                Image setImage = new Image(settingssPath);
                ImageView settView = new ImageView(setImage);
                settView.setFitHeight(20.0);
                settView.setFitWidth(20.0);
                setting = new Button(null, settView);
                setting.setMinHeight(10.0);

                algoButtonList.add(ij, new RadioButton(algos[ij]));
                algoButtonList.get(ij).setStyle("-fx-base: DDB0A0;");
                algoButtonList.get(ij).setFont(Font.font("Arial", 18));
                HBox algoHboxClus = new HBox();
                setting.setStyle("-fx-base: DDB0A0;");
                name = ij;

                setting.setOnAction(event1 -> {
                    try {
                        configWindow(2);
                      /*  Class c = Class.forName("algorithms." + algos[name]);
                        Constructor constructor = c.getDeclaredConstructor(DataSet.class, int.class, int.class,int.class, ApplicationTemplate.class, boolean.class);
                        fury = (Clusterer) constructor.newInstance(dataSet, Integer.parseInt(textIterate.getText()), Integer.parseInt(textInterval.getText()), Integer.parseInt(textCluster.getText()), applicationTemplate, cRun.isSelected()); */

                    } catch (Exception e2) {
                        //System.out.print("asdasd");
                    }
                });
                algoHboxClus.getChildren().addAll(algoButtonList.get(ij), setting);
                vboxF.getChildren().add(algoHboxClus);

            }

          /*  setting.setOnAction(r-> {
                if (algoAclus.isSelected()) {
                    configWindow(2);
                }
            }); */
            Button back = new Button("Back <-");
            back.setStyle("-fx-base: DDB0A0;");
            back.setOnAction(d-> {
                makeVBoxF();
                radioA.setSelected(false);
            });
            vboxF.getChildren().addAll(back, run);
            run.setOnAction(i-> {
                if(thread2Started) {
                    algoRunning = true;
                    whichAlgoSelected(2);
                    abc.start();
                }
                else {

                }
            });
        });
        radioB.setOnAction(e-> {
            if(k.size()==2) {
                vboxF.getChildren().clear();
                String runiconspath = SEPARATOR + String.join(SEPARATOR,
                        manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                        manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
                runPath = String.join(SEPARATOR, runiconspath, manager.getPropertyValue(RUN_ICON.name()));
                Image runImage = new Image(runPath);
                ImageView runView = new ImageView(runImage);
                runView.setFitWidth(20.0);
                runView.setFitHeight(20.0);
                run = new Button(null, runView);
                run.minHeight(10.0);

                Label labelClus = new Label("Classification");
                labelClus.setFont(Font.font("Arial", 15));
                vboxF.getChildren().add(labelClus);

                StringBuilder stringBuffer = new StringBuilder();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader("algorithms.txt"));
                    String text;
                    while ((text = bufferedReader.readLine()) != null) {
                        stringBuffer.append(text+"\n");
                    }
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (FileNotFoundException e1) {
                } catch (IOException e1) {
                }
                String[] algos = stringBuffer.toString().split("\n");

                for(int ij =0; ij<algos.length; ij++) {

                    String iconsPath = SEPARATOR + String.join(SEPARATOR,
                            manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                            manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
                    settingssPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SETTINGSS_ICON.name()));
                    Image setImage = new Image(settingssPath);
                    ImageView settView = new ImageView(setImage);
                    settView.setFitHeight(20.0);
                    settView.setFitWidth(20.0);
                    setting = new Button(null, settView);
                    setting.setMinHeight(10.0);

                    algoButtonList.add(new RadioButton(algos[ij]));
                    algoButtonList.get(ij).setStyle("-fx-base: DDB0A0;");
                    algoButtonList.get(ij).setFont(Font.font("Arial", 18));
                    HBox algoHboxClus = new HBox();
                    setting.setStyle("-fx-base: DDB0A0;");
                    name = ij;

                    setting.setOnAction(event1 -> {
                        try {
                            configWindow(1);
                          /*  Class c = Class.forName("algorithms." + algos[name].substring(1));
                            Constructor constructor = c.getDeclaredConstructor(DataSet.class, int.class, int.class, boolean.class, ApplicationTemplate.class);
                            fire = (RandomClassifier) constructor.newInstance(dataSet, Integer.parseInt(textIterate.getText()), Integer.parseInt(textInterval.getText()), cRun.isSelected(), applicationTemplate); */

                        } catch (Exception e2) {
                            //System.out.print("asdasd");
                        }
                    });
                    algoHboxClus.getChildren().addAll(algoButtonList.get(ij), setting);
                    vboxF.getChildren().add(algoHboxClus);
                }

          /*  setting.setOnAction(r-> {
                if (algoAclus.isSelected()) {
                    configWindow(2);
                }
            }); */
                Button back = new Button("Back <-");
                back.setStyle("-fx-base: DDB0A0;");
                back.setOnAction(d-> {
                    makeVBoxF();
                    radioA.setSelected(false);
                });
                vboxF.getChildren().addAll(back, run);
            }
            else {
                ErrorDialog.getDialog().show("Not two non-null labels", "There should be exactly two non-null labels to select Classification.");
                radioB.setSelected(false);
            }

            run.setOnAction(i-> {
                if (!cRun.isSelected()) {
                        ErrorDialog.getDialog().show("You choose not continue", "Press run button again to move ahead or finish the algorithm.");
                    }
                    if (!threadStarted) {
                        RandomClassifier.clearRandom();
                        getChart().getData().remove(RandomClassifier.getRandomSeries());
                        // ((RandomClassifier)fire).clearLabel();
                        try {
                            algoRunning = true;
                            run.setDisable(true);
                            whichAlgoSelected(1);
                            newThread.start();
                        } catch (Exception ex) {
                        }
                        threadStarted = true;
                    } else {
                        synchronized (newThread) {
                            try {
                                newThread.interrupt();
                            } catch (Exception r) {
                            }
                        }
                }
            });
        });


        textArea.setOnKeyReleased(e-> {
            if(textArea.getText().equals("")) {
                newButton.setDisable(true);
                saveButton.setDisable(true);
                return;
            }
            newButton.setDisable(false);
            saveButton.setDisable(false);
        });

        textArea.textProperty().addListener(e -> {
            String[] bLine = textArea.getText().split("\n");
            if(bLine.length<10 && ((AppData)applicationTemplate.getDataComponent()).getListLineA().size()>0) {
                textArea.appendText(((AppData)applicationTemplate.getDataComponent()).getListLineA().remove(0) + "\n");
            }
        });
        togButton.setOnAction(e-> {
            if(togButton.isSelected()) {
                AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                dataComponent.clear();
                chart.getData().clear();
                String temp = textArea.getText();
                if(temp.equals("")) {
                    ErrorDialog.getDialog().show(manager.getPropertyValue(NO_TEXT_TO_DISPLAY_TITLE.name()), manager.getPropertyValue(NO_TEXT_TO_DISPLAY.name()));
                    newButton.setDisable(true);
                    saveButton.setDisable(true);
                }
                else {
                    ((AppData) applicationTemplate.getDataComponent()).loadData(temp);
                }
                textArea.setDisable(true);
                if(!getText().equals("")) {
                    makeVBoxF();
                }
            }
            else {
                textArea.setDisable(false);
            }
        });
        }
        public void makeVBoxF() {
            Label label2 = new Label("Algorithm Type");
            label2.setFont(Font.font("Arial", 15));
            vboxF.getChildren().clear();
            radioA.setStyle("-fx-base: DDB0A0;");
            radioB.setStyle("-fx-base: DDB0A0;");
            vboxF.getChildren().addAll(label2, radioA, radioB);
        }

    public void setAlgoRunning(boolean algoRunning) {
        this.algoRunning = algoRunning;
    }

    public static void testingCase3(String c, String c1, String c2) {

    }
}