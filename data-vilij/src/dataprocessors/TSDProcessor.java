package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Rectangle;

import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    private double minX = 0.0;
    private double maxX = 0.0;
    private double tot = 0.0;
    private double counter = 0.0;
    private double avg = 0.0;
    private HashMap<Point2D, String> bands;
    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = " All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private static Map<String, String> dataLabels;
    private static Map<String, Point2D> dataPoints;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        bands = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        AtomicInteger myInt = new AtomicInteger(0);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      if(!hadAnError.get()) {
                          myInt.getAndIncrement();
                          String name = checkedname(list.get(0));
                          String label = list.get(1);
                          String[] pair = list.get(2).split(",");
                          Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                          dataLabels.put(name, label);
                          dataPoints.put(name, point);
                          bands.put(point, name);
                      }
                  } catch (Exception e){
                      String t = e.getMessage().substring(0, e.getMessage().length()-1);
                          errorMessage.setLength(0);
                          errorMessage.append("There is an error in line " + myInt + ". ").append(t);
                          hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
                if(minX ==0 || minX > point.getX()) {
                    minX = point.getX();
                }
                if(maxX < point.getX()) {
                    maxX = point.getX();
                }
                if(maxX < minX) {
                    double temp = minX;
                    minX = maxX;
                    maxX = temp;
                }
                counter++;
                tot += point.getY();
            });
            chart.getData().add(series);
            series.getNode().setStyle("-fx-stroke: transparent");
            for(XYChart.Series<Number, Number> w : chart.getData()) {
                for(XYChart.Data<Number, Number> x : series.getData()) {
                    Point2D pointk = new Point2D((double) x.getXValue(), (double) x.getYValue());
                    String name  = bands.get(pointk);
                    Tooltip tultip = new Tooltip();
                    tultip.setText(name);
                    Tooltip.install(x.getNode(), tultip);
                    x.getNode().setCursor(Cursor.CROSSHAIR);
                }
            }
            }
        avg = (tot/counter);
      /*  XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        series2.setName("Average Line");
        XYChart.Data data1 = new XYChart.Data<>(minX, avg);
        XYChart.Data data2 = new XYChart.Data<>(maxX, avg);
        Rectangle rect = new Rectangle(0, 0);
        rect.setVisible(false);
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setVisible(false);
        data1.setNode(rect);
        data2.setNode(rectangle);
        series2.getData().addAll(data1, data2);
        chart.getData().add(series2); */
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
        minX=0.0;
        maxX=0.0;
        avg=0.0;
        tot=0.0;
        counter=0.0;
    }

    public String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }
}
