/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.util.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.jfree.fx.FXGraphics2D;
import com.google.common.collect.Range;
import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Feature;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tooltip;

/**
 * Simple lightweight component for plotting peak shape
 */
public class CombinedXICComponent extends Canvas {

  // public static final Border componentBorder = BorderFactory.createLineBorder(Color.lightGray);

  // plot colors for plotted files, circulated by numberOfDataSets
  public static final Color[] plotColors = {new Color(0, 0, 192), // blue
      new Color(192, 0, 0), // red
      new Color(0, 192, 0), // green
      Color.magenta, Color.cyan, Color.orange};

  private Feature[] peaks;

  private Range<Double> rtRange;
  private double maxIntensity;

  /**
   * @param ChromatographicPeak [] Picked peaks to plot
   */
  public CombinedXICComponent(Feature[] peaks, int id) {

    // We use the tool tip text as a id for customTooltipProvider
    if (id >= 0) {
      Tooltip tooltip = new Tooltip(ComponentToolTipManager.CUSTOM + id);
      Tooltip.install(this, tooltip);
    }

    double maxIntensity = 0;
    this.peaks = peaks;

    // find data boundaries
    for (Feature peak : peaks) {
      if (peak == null)
        continue;

      maxIntensity = Math.max(maxIntensity, peak.getRawDataPointsIntensityRange().upperEndpoint());
      if (rtRange == null)
        rtRange = peak.getDataFile().getDataRTRange();
      else
        rtRange = rtRange.span(peak.getDataFile().getDataRTRange());
    }

    this.maxIntensity = maxIntensity;

    paint();

    widthProperty().addListener(e -> paint());
    heightProperty().addListener(e -> paint());

  }

  @Override
  public boolean isResizable() {
    return true;
  }

  private void paint() {

    // use Graphics2D for antialiasing
    Graphics2D g2 = new FXGraphics2D(this.getGraphicsContext2D());

    // turn on antialiasing
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int colorIndex = 0;

    for (Feature peak : peaks) {

      // set color for current XIC
      g2.setColor(plotColors[colorIndex]);
      colorIndex = (colorIndex + 1) % plotColors.length;

      // if we have no data, just return
      if ((peak == null) || (peak.getScanNumbers().length == 0))
        continue;

      // get scan numbers, one data point per each scan
      int scanNumbers[] = peak.getScanNumbers();

      // for each datapoint, find [X:Y] coordinates of its point in
      // painted image
      int xValues[] = new int[scanNumbers.length + 2];
      int yValues[] = new int[scanNumbers.length + 2];

      // find one datapoint with maximum intensity in each scan
      for (int i = 0; i < scanNumbers.length; i++) {

        double dataPointIntensity = 0;
        DataPoint dataPoint = peak.getDataPoint(scanNumbers[i]);

        if (dataPoint != null)
          dataPointIntensity = dataPoint.getIntensity();

        // get retention time (X value)
        double retentionTime = peak.getDataFile().getScan(scanNumbers[i]).getRetentionTime();

        // calculate [X:Y] coordinates
        xValues[i + 1] = (int) Math.floor((retentionTime - rtRange.lowerEndpoint())
            / (rtRange.upperEndpoint() - rtRange.lowerEndpoint()) * ((int) getWidth() - 1));
        yValues[i + 1] = (int) getHeight()
            - (int) Math.floor(dataPointIntensity / maxIntensity * ((int) getHeight() - 1));
      }

      // add first point
      xValues[0] = xValues[1];
      yValues[0] = (int) getHeight() - 1;

      // add terminal point
      xValues[xValues.length - 1] = xValues[xValues.length - 2];
      yValues[yValues.length - 1] = (int) getHeight() - 1;

      // draw the peak shape
      g2.drawPolyline(xValues, yValues, xValues.length);

    }

  }

}
