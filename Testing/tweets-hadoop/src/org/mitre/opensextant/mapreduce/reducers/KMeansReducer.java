package org.mitre.opensextant.mapreduce.reducers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.mitre.geodesy.GeodeticCoord;
import org.mitre.opensextant.data.GeoTweetWritable;
import org.mitre.opensextant.data.GeoTweetWritable.Type;
import org.mitre.opensextant.data.UserClustersWritable;
import org.mitre.opensextant.distance.GeoDistance;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class KMeansReducer extends Reducer<Text, GeoTweetWritable, Text, Text> {

	private static final double CLUSTER_SIZE_TARGET = 5.0;

	public void reduce(Text key, Iterable<GeoTweetWritable> values, Context context) throws IOException, InterruptedException {
		List<GeoTweetWritable> userLocations = new ArrayList<GeoTweetWritable>();
		List<GeoTweetWritable> extractedLocations = new ArrayList<GeoTweetWritable>();
		Set<String> matchTexts = new HashSet<String>();
		for (GeoTweetWritable val : values) {
			GeoTweetWritable cloned = val.clone();
			if (val.getType() == Type.USER) {
				userLocations.add(cloned);
			} else {
				extractedLocations.add(cloned);
				matchTexts.add(cloned.getMatchText());
			}
			
		}
		
		Instances locations = insertIntoWeka(extractedLocations, "extractedLocations");
		
		System.out.println("extracted #: " + extractedLocations.size());

		try {
			List<GeodeticCoord> geoCentroids = new ArrayList<GeodeticCoord>();
			List<List<GeodeticCoord>> clusters = new ArrayList<List<GeodeticCoord>>();

			if (locations.size() > 0) {
				SimpleKMeans kmeans = new SimpleKMeans();

				kmeans.setSeed(10);

				// This is the important parameter to set
				kmeans.setPreserveInstancesOrder(true);
				kmeans.setNumClusters((int)Math.ceil(locations.size()/CLUSTER_SIZE_TARGET));
				kmeans.setMaxIterations(100);
				kmeans.setDistanceFunction(new GeoDistance());
				kmeans.buildClusterer(locations);
				
				ClusterEvaluation eval = new ClusterEvaluation();
				eval.setClusterer(kmeans);
				 
				eval.evaluateClusterer(locations);
				System.out.println("# of clusters: " + eval.getNumClusters());

				// This array returns the cluster number (starting with 0) for each instance
				// The array has as many elements as the number of instances
				int[] assignments = kmeans.getAssignments();
				Instances centroidInstances = kmeans.getClusterCentroids();
				
				
				for (Instance centroid : centroidInstances) {
					geoCentroids.add(GeodeticCoord.createFromDegrees(centroid.value(1), centroid.value(0), 0));
				}

				for (int i = 0; i < kmeans.getNumClusters(); i++) {
					clusters.add(new ArrayList<GeodeticCoord>());
				}
				int i = 0;
				for(int clusterNum : assignments) {
					Instance instance = locations.get(i);
					clusters.get(clusterNum).add(GeodeticCoord.createFromDegrees(instance.value(1), instance.value(0), 0));
					i++;
				}
				
				UserClustersWritable userClustersWritable = new UserClustersWritable(null, geoCentroids, clusters, locations.size(), matchTexts);
				context.write(key, userClustersWritable.toText());

			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Instances insertIntoWeka(List <GeoTweetWritable> points, final String name)	{       
	    // Create numeric attributes "x" and "y" and "z"
	    Attribute x = new Attribute("x");
	    Attribute y = new Attribute("y");
	    Attribute z = new Attribute("z");
	     
	        // Create vector of the above attributes
	    ArrayList<Attribute> attributes = new ArrayList<Attribute>(3);
	    attributes.add(x);
	    attributes.add(y);
	    attributes.add(z);
	 
	    // Create the empty datasets "wekaPoints" with above attributes
	    Instances wekaPoints = new Instances(name, attributes, 0);
	         
	    for (GeoTweetWritable p : points) 
	    {
	        // Create empty instance with three attribute values

	    	Instance inst = new DenseInstance(3); 
	 
	        // Set instance's values for the attributes "x", "y", and "z"
	        inst.setValue(x, p.getLongitude());
	        inst.setValue(y, p.getLatitude());
	        inst.setValue(z, 0);
	 
	        // Set instance's dataset to be the dataset "wekaPoints"
	        inst.setDataset(wekaPoints);
	             
	        // Add the Instance to Instances
	        wekaPoints.add(inst);
	    }
	         
	    return wekaPoints;
	}

}
