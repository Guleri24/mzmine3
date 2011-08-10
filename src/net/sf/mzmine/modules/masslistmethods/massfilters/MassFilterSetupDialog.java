/*
 * Copyright 2006-2011 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.masslistmethods.massfilters;

import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;

import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.MassList;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.modules.visualization.spectra.PlotMode;
import net.sf.mzmine.modules.visualization.spectra.SpectraPlot;
import net.sf.mzmine.modules.visualization.spectra.SpectraVisualizerWindow;
import net.sf.mzmine.modules.visualization.spectra.datasets.DataPointsDataSet;
import net.sf.mzmine.modules.visualization.spectra.datasets.ScanDataSet;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialogWithScanPreview;

/**
 * This class extends ParameterSetupDialog class, including a spectraPlot. This
 * is used to preview how the selected mass detector and his parameters works
 * over the raw data file.
 */
public class MassFilterSetupDialog extends ParameterSetupDialogWithScanPreview {

	public static final Color removedPeaksColor = Color.orange;

	private MassFilter massFilter;
	private ParameterSet parameters;
	private String massListName;

	/**
	 * @param parameters
	 * @param massFilterTypeNumber
	 */
	public MassFilterSetupDialog(Class massFilterClass, ParameterSet parameters) {

		super(parameters, null);

		this.parameters = parameters;
		
		for (MassFilter filter : MassFilteringParameters.massFilters) {
			if (filter.getClass().equals(massFilterClass)) {
				this.massFilter = filter;
			}
		}
		this.massListName = parameters.getParameter(
				MassFilteringParameters.massList).getValue();
	}

	/**
	 * This function set all the information into the plot chart
	 * 
	 * @param scanNumber
	 */
	protected void loadPreview(SpectraPlot spectrumPlot, Scan previewScan) {

		MassList massList = previewScan.getMassList(massListName);
		if (massList == null)  
			return;

		ScanDataSet scanDataSet = new ScanDataSet(previewScan);

		DataPoint mzValues[] = massList.getDataPoints();
		DataPoint remainingMzValues[] = massFilter.filterMassValues(mzValues, parameters);

		Vector<DataPoint> removedPeaks = new Vector<DataPoint>();
		removedPeaks.addAll(Arrays.asList(mzValues));
		removedPeaks.removeAll(Arrays.asList(remainingMzValues));
		DataPoint removedMzValues[] = removedPeaks.toArray(new DataPoint[0]);

		DataPointsDataSet removedPeaksDataSet = new DataPointsDataSet(
				"Removed peaks", removedMzValues);
		DataPointsDataSet remainingPeaksDataSet = new DataPointsDataSet(
				"Remaining peaks", remainingMzValues);

		spectrumPlot.removeAllDataSets();
		spectrumPlot.addDataSet(scanDataSet, SpectraVisualizerWindow.scanColor,
				false);
		spectrumPlot.addDataSet(removedPeaksDataSet, removedPeaksColor, false);
		spectrumPlot.addDataSet(remainingPeaksDataSet,
				SpectraVisualizerWindow.peaksColor, false);

		// if the scan is centroided, switch to centroid mode
		if (previewScan.isCentroided()) {
			spectrumPlot.setPlotMode(PlotMode.CENTROID);
		} else {
			spectrumPlot.setPlotMode(PlotMode.CONTINUOUS);
		}

	}

}