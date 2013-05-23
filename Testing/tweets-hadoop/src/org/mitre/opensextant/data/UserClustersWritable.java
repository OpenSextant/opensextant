package org.mitre.opensextant.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.mitre.geodesy.GeodeticCoord;

public class UserClustersWritable implements Writable {
	
	private List<GeodeticCoord> centroids = new ArrayList<GeodeticCoord>();
	private int clusterCount = 0;
	private List<List<GeodeticCoord>> clusters = new ArrayList<List<GeodeticCoord>>();
	private List<Integer> clusterSizes = new ArrayList<Integer>();
	private List<Double> clusterMeans = new ArrayList<Double>();
	private List<Double> clusterMedians = new ArrayList<Double>();
	private double totalMeanClusterSpread;
	private double totalMedianClusterSpread;
	private int pointCount;

	private GeodeticCoord userLocation;
	private double meanClusterDistanceFromUser;
	private double medianClusterDistanceFromUser;
	private Set<String> matchTexts;

	public UserClustersWritable() {
	}

	public UserClustersWritable(GeodeticCoord userLocation, List<GeodeticCoord> centroids, List<List<GeodeticCoord>> clusters, int pointCount, Set<String> matchTexts) {
		this.userLocation = userLocation;
		this.centroids = centroids;
		this.clusters = clusters;
		this.pointCount = pointCount;
		this.clusterCount = clusters.size();
		this.matchTexts = matchTexts;
		
		double[] distances = new double[pointCount]; 
		int i = 0;
		for (int index=0; index < clusterCount; index++) {
			List<GeodeticCoord> cluster = clusters.get(index);
			clusterSizes.add(cluster.size());
			GeodeticCoord centroid = centroids.get(index);
			double[] memberDistances = new double[cluster.size()];
			for (int clusterIndex=0; clusterIndex < cluster.size(); clusterIndex++) {
				double distance = centroid.distanceInMetersTo(cluster.get(clusterIndex))/1000.0;
				memberDistances[clusterIndex] = distance;
				distances[i] = distance;
				i++;
			}
			Mean meanDistance = new Mean();
			meanDistance.setData(memberDistances);
			clusterMeans.add(meanDistance.evaluate());
			Median medianDistance = new Median();
			medianDistance.setData(memberDistances);
			clusterMedians.add(medianDistance.evaluate());
		}
		Mean meanDistance = new Mean();
		meanDistance.setData(distances);
		totalMeanClusterSpread = meanDistance.evaluate();
		Median medianDistance = new Median();
		medianDistance.setData(distances);
		totalMedianClusterSpread = medianDistance.evaluate();
	}


	public Text toText() {
		return new Text(this.toString());
	}
	
	@Override
	public String toString() {
//		return "[clusterCount=" + clusterCount + ", meanSpread=" + totalMeanClusterSpread + ", medianSpread=" + totalMedianClusterSpread + ", points=" + pointCount + ", centroids=" + centroids + ", clusterMeans=" + clusterMeans + ", clusterMedians=" + clusterMedians + ", clusters=" + clusters +  ", matches=" + matchTexts + "]";
		return clusterCount + "," + totalMeanClusterSpread + "," + totalMedianClusterSpread + "," + pointCount + ",\"" + centroids + "\",\"" + clusterMeans + "\",\"" + clusterMedians + "\",\"" + clusters +  "\",\"" + matchTexts + "\"";
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		
	}

	@Override
	public void write(DataOutput output) throws IOException {
		
	}

}
