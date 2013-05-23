package org.mitre.opensextant.distance;

import java.util.Enumeration;

import org.mitre.geodesy.GeodeticCoord;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.neighboursearch.PerformanceStats;

public class GeoDistance extends EuclideanDistance {

	@Override
	public int closestPoint(Instance arg0, Instances arg1, int[] arg2) throws Exception {
		return super.closestPoint(arg0, arg1, arg2);
	}

	@Override
	public double distance(Instance first, Instance second) {
		return Math.random() * 100;
	}

	@Override
	public double distance(Instance first, Instance second, PerformanceStats stats) {
		return Math.random() * 100;
	}

	@Override
	public double getMiddle(double[] ranges) {
		return super.getMiddle(ranges);
	}

	@Override
	public String getRevision() {
		return super.getRevision();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		return super.getTechnicalInformation();
	}

	@Override
	public String globalInfo() {
		return super.globalInfo();
	}

	@Override
	public void postProcessDistances(double[] arg0) {
		super.postProcessDistances(arg0);
	}

	@Override
	public double sqDifference(int index, double val1, double val2) {
		return super.sqDifference(index, val1, val2);
	}

	@Override
	protected double updateDistance(double currDist, double diff) {
		return super.updateDistance(currDist, diff);
	}

	@Override
	public boolean valueIsSmallerEqual(Instance instance, int dim, double value) {
		return super.valueIsSmallerEqual(instance, dim, value);
	}

	@Override
	public String attributeIndicesTipText() {
		return super.attributeIndicesTipText();
	}

	@Override
	protected double difference(int arg0, double arg1, double arg2) {
		return super.difference(arg0, arg1, arg2);
	}

	@Override
	public double distance(Instance first, Instance second, double arg2, PerformanceStats arg3) {
		GeodeticCoord coord = GeodeticCoord.createFromDegrees(first.value(1), first.value(0), 0);
		double dist = coord.distanceInMetersTo(second.value(1), second.value(0)) / 1000.0;
		return dist;
	}

	@Override
	public double distance(Instance first, Instance second, double cutOffValue) {
		GeodeticCoord coord = GeodeticCoord.createFromDegrees(first.value(1), first.value(0), 0);
		GeodeticCoord coord2 = GeodeticCoord.createFromDegrees(second.value(1), second.value(0), 0);
		double dist = coord.distanceInMetersTo(coord2);
		return dist;
	}

	@Override
	public String dontNormalizeTipText() {
		return super.dontNormalizeTipText();
	}

	@Override
	public String getAttributeIndices() {
		return super.getAttributeIndices();
	}

	@Override
	public boolean getDontNormalize() {
		return super.getDontNormalize();
	}

	@Override
	public Instances getInstances() {
		return super.getInstances();
	}

	@Override
	public boolean getInvertSelection() {
		return super.getInvertSelection();
	}

	@Override
	public String[] getOptions() {
		return super.getOptions();
	}

	@Override
	public double[][] getRanges() throws Exception {
		return super.getRanges();
	}

	@Override
	public boolean inRanges(Instance arg0, double[][] arg1) {
		return super.inRanges(arg0, arg1);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	protected void initializeAttributeIndices() {
		super.initializeAttributeIndices();
	}

	@Override
	public double[][] initializeRanges() {
		return super.initializeRanges();
	}

	@Override
	public double[][] initializeRanges(int[] arg0, int arg1, int arg2) throws Exception {
		return super.initializeRanges(arg0, arg1, arg2);
	}

	@Override
	public double[][] initializeRanges(int[] arg0) throws Exception {
		return super.initializeRanges(arg0);
	}

	@Override
	public void initializeRangesEmpty(int arg0, double[][] arg1) {
		super.initializeRangesEmpty(arg0, arg1);
	}

	@Override
	protected void invalidate() {
		super.invalidate();
	}

	@Override
	public String invertSelectionTipText() {
		return super.invertSelectionTipText();
	}

	@Override
	public Enumeration listOptions() {
		return super.listOptions();
	}

	@Override
	protected double norm(double x, int i) {
		return super.norm(x, i);
	}

	@Override
	public boolean rangesSet() {
		return super.rangesSet();
	}

	@Override
	public void setAttributeIndices(String value) {
		super.setAttributeIndices(value);
	}

	@Override
	public void setDontNormalize(boolean dontNormalize) {
		super.setDontNormalize(dontNormalize);
	}

	@Override
	public void setInstances(Instances insts) {
		super.setInstances(insts);
	}

	@Override
	public void setInvertSelection(boolean value) {
		super.setInvertSelection(value);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		super.setOptions(options);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void update(Instance ins) {
		super.update(ins);
	}

	@Override
	public double[][] updateRanges(Instance arg0, double[][] arg1) {
		return super.updateRanges(arg0, arg1);
	}

	@Override
	public void updateRanges(Instance arg0, int arg1, double[][] arg2) {
		super.updateRanges(arg0, arg1, arg2);
	}

	@Override
	public void updateRanges(Instance instance) {
		super.updateRanges(instance);
	}

	@Override
	public void updateRangesFirst(Instance arg0, int arg1, double[][] arg2) {
		super.updateRangesFirst(arg0, arg1, arg2);
	}

	@Override
	protected void validate() {
		super.validate();
	}

}
