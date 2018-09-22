package algorithms;

import algorithms.Clusterer;
import dataprocessors.DataSet;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.awt.*;
import java.io.IOException;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;


import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class RandomClusterer extends Clusterer {
    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean     tocontinue;
    private ApplicationTemplate applicationTemplate;
    private boolean             continues;

    public RandomClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, ApplicationTemplate applicationTemplate, boolean continues) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.applicationTemplate = applicationTemplate;
        this.tocontinue = new AtomicBoolean(false);
        this.continues = continues;
    }


    @Override
    public void run() {
        ((AppUI)applicationTemplate.getUIComponent()).getRun().setDisable(true);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations && continues) {
            assignLabel();
            Platform.runLater(this :: clearingChart);
            Platform.runLater(this::toChart);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        while (iteration++ < maxIterations && !continues) {
            assignLabel();
            Platform.runLater(this::clearingChart);
            Platform.runLater(this::toChart);
            try {
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).getRun().setDisable(false);
                Thread.sleep(Integer.MAX_VALUE);
            } catch (Exception ex) {

            }
        }
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
        ((AppUI)applicationTemplate.getUIComponent()).setAlgoRunning(false);
       // ((AppUI)applicationTemplate.getUIComponent()).setStartThread(false);
        ((AppUI)applicationTemplate.getUIComponent()).getRun().setDisable(false);
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            try {
                while (chosen.contains(instanceNames.get(i)))
                    ++i;
                chosen.add(instanceNames.get(i));
            }
            catch (Exception e) {
                break;
            }
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    public void assignLabel(){
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            try {
                while (chosen.contains(instanceNames.get(i)))
                    ++i;
                chosen.add(instanceNames.get(i));
            }
            catch (Exception e) {
                break;
            }
        }
        dataset.getLocations().forEach((instanceName, location) -> {
            int i= r.nextInt(numberOfClusters);
            dataset.updateLabel(instanceName, Integer.toString(i));
        });

    }

    private void toChart(){
        Set<String> labels = new HashSet<>(dataset.getLabels().values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataset.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataset.getLocations().get(entry.getKey());
                XYChart.Data<Number, Number> points = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(points);
            });
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(series);
            series.getNode().setStyle("-fx-stroke: transparent");
        }
    }

    private void clearingChart(){
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
    }


    public int getNumberOfClusters() { return numberOfClusters; }

    @Override
    public int getMaxIterations() {
        return 0;
    }

    @Override
    public int getUpdateInterval() {
        return 0;
    }

    @Override
    public boolean tocontinue() {
        return false;
    }
}
