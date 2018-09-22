package algorithms;
import dataprocessors.AppData;
import dataprocessors.DataSet;
import dataprocessors.TSDProcessor;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Rectangle;
import ui.AppUI;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {
    private static final Random RAND = new Random();

    public DataSet getDataset() {
        return dataset;
    }

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    public static XYChart.Series<Number, Number> getRandomSeries() {
        return randomSeries;
    }

    private static XYChart.Series<Number, Number> randomSeries = new XYChart.Series<>();
    private ApplicationTemplate applicationTemplate;
    private final int maxIterations;
    private final int updateInterval;
    private double minY;
    private double maxY;
    private XYChart.Data data1;
    private XYChart.Data data2;
  //  private Object lock = new Object();

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue,
                            ApplicationTemplate applicationTemplate) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = applicationTemplate;
    }
    public static void clearRandom() {
        randomSeries.getData().clear();
    }
    @Override
    public void run() {
        Platform.runLater(this::makeLine);
        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
              //  System.out.printf("Iteration number %d: ", i);
                Platform.runLater(this::updateXY);
              //  flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
              //  System.out.printf("Iteration number %d: ", i);
                Platform.runLater(this::updateXY);
               // flush();
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).setAlgoRunning(false);
                ((AppUI)applicationTemplate.getUIComponent()).setThreadStarted(false);
                break;
            }
            try {
                Thread.sleep(500);
            } catch (Exception ex) {

            }
        }
        if (!tocontinue()) {
            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
            for (int i = 1; i <= maxIterations; i++) {
                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int constant = new Double(RAND.nextDouble() * 100).intValue();

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);
                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
                if (i % updateInterval == 0) {
                 //   System.out.printf("Iteration number %d: ", i);
                    Platform.runLater(this::updateXY);
                  //  flush();

                    synchronized (this) {
                        try {
                            Thread.sleep(500);
                            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getRun().setDisable(false);
                            this.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                }
                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                 //   System.out.printf("Iteration number %d: ", i);
                    Platform.runLater(this::updateXY);
                   // flush();
                    break;
                }

            }
        }
        ((AppUI)applicationTemplate.getUIComponent()).setThreadStarted(false);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
        ((AppUI)applicationTemplate.getUIComponent()).getRun().setDisable(false);
        Platform.runLater(this::finished);
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /**
     * A placeholder main method to just make sure this code runs smoothly
     */
   /* public static void main(String... args) throws IOException {
        DataSet dataset = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        classifier.run(); // no multithreading yet
    } */
   public void finished() {
       ErrorDialog.getDialog().show("DONE", "Algorithm has finished running. WARNING: If you press run button again, it will start again.");
   }
   public void makeLine() {
       TSDProcessor tsd = ((AppData)applicationTemplate.getDataComponent()).getProcessor();
       randomSeries = new XYChart.Series<>();
       randomSeries.setName("Random Line");
       data1 = new XYChart.Data<>(tsd.getMinX(), minY);
       data2 = new XYChart.Data<>(tsd.getMaxX(), maxY);
       Rectangle rect = new Rectangle(0, 0);
       rect.setVisible(false);
       Rectangle rectangle = new Rectangle(0, 0);
       rectangle.setVisible(false);
       data1.setNode(rect);
       data2.setNode(rectangle);
       randomSeries.getData().clear();
       randomSeries.getData().addAll(data1, data2);
       ((AppUI)applicationTemplate.getUIComponent()).getChart().getData().add(randomSeries);
   }

    public void updateXY() {
        TSDProcessor tsd = ((AppData)applicationTemplate.getDataComponent()).getProcessor();
        minY = -(output.get(2) + (output.get(0)*tsd.getMinX()))/output.get(1);
        maxY = -(output.get(2) + (output.get(0)*tsd.getMaxX()))/output.get(1);
        data1.setXValue(tsd.getMinX());
        data2.setXValue(tsd.getMaxX());
        data1.setYValue(minY);
        data2.setYValue(maxY);
    }
}
