package org.lightsolutions.notifier.gui;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.lightsolutions.notifier.NetworkNotifier;
import org.lightsolutions.notifier.utils.SystemTrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class NotifierFrame extends JFrame {

    private final XYChart chart;

    public NotifierFrame(){
        this.setSize(600,400);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SystemTrayUtils.sendNotification(TrayIcon.MessageType.INFO,"Program is still monitoring network status in the background!");
            }
        });

        var mainPanel = new JPanel();
        mainPanel.setSize(600,400);

        // Initialize all content in there.
        this.chart = new XYChartBuilder()
                .height(mainPanel.getHeight())
                .width(mainPanel.getWidth())
                .theme(Styler.ChartTheme.GGPlot2)
                .title("Ping Graph").xAxisTitle("Time").yAxisTitle("Ping").build();
        this.chart.addSeries("ping",new double[]{0});
        this.chart.addSeries("unreachable",new double[]{0});
        this.chart.getStyler().setZoomEnabled(true);
        this.chart.getStyler().setZoomResetByDoubleClick(false);
        this.chart.getStyler().setZoomResetByButton(true);
        this.chart.getStyler().setZoomSelectionColor(new Color(0,0,192,128));
        this.chart.getStyler().setCursorEnabled(true);
        this.chart.getStyler().setYAxisMax(100.0);
        this.chart.getStyler().setMarkerSize(0);
        var chartPanel = new XChartPanel<>(chart);
        mainPanel.add(chartPanel);

        this.setContentPane(mainPanel);
        this.pack();
        this.setLocationRelativeTo(null);

    }

    public void updateGraph(){
        var results = NetworkNotifier.getInstance().getPingResults();

        var index = 0;
        var maxValue = 0L;
        var xData = new double[results.size()];
        var yData = new double[results.size()];
        var downData = new double[results.size()];
        for(Map.Entry<Long,Long> entry : results.entrySet()){
            xData[index] = (entry.getKey() - System.currentTimeMillis()) / 1000d;

            var value = entry.getValue();
            yData[index] = value;
            if(value == -1){
                downData[index] = 100;
            }else{
                downData[index] = 0;
            }
            if(value > maxValue) maxValue = value;
            index++;
        }

        this.chart.getStyler().setYAxisMax((double) Math.max(maxValue,100));
        this.chart.updateXYSeries("ping",xData,yData,null);
        this.chart.updateXYSeries("unreachable",xData,downData,null);
        this.repaint();
    }

}
