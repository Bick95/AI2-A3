package clusterer;

import java.util.*;

public class KMeans extends ClusteringAlgorithm
{
	// Number of clusters
	private int k;

	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;
	
	// Array of k clusters, class cluster is used for easy bookkeeping
	private Cluster[] clusters;
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and memberlists with the ID's (which are Integer objects) of the datapoints that are member of that cluster.
	// You also want to remember the previous members so you can check if the clusters are stable.
	static class Cluster
	{
		float[] prototype;

		Set<Integer> currentMembers;
		Set<Integer> previousMembers;
		  
		public Cluster()
		{
			currentMembers = new HashSet<Integer>();
			previousMembers = new HashSet<Integer>();
		}
	}
	// These vectors contains the feature vectors you need; the feature vectors are float arrays.
	// Remember that you have to cast them first, since vectors return objects.
	private Vector<float[]> trainData;
	private Vector<float[]> testData;

	// Results of test()
	private double hitrate;
	private double accuracy;
	
	public KMeans(int k, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.k = k;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;
		prefetchThreshold = 0.5;
		
		// Here k new cluster are initialized
		clusters = new Cluster[k];
		for (int ic = 0; ic < k; ic++)
			clusters[ic] = new Cluster();
	}


        private boolean differenceDetected(){
            /// TODO: NEEDS IMPLEMENTATION
            System.err.println("differenceDetected() still needs implementation!");
            return false;
        }
        
        private void generateNewPartitioning(){
            for (Cluster cl : clusters){ /// Update previous members and prepare for new set of current members
                cl.previousMembers = cl.currentMembers;
                cl.currentMembers = new HashSet<Integer>();
            }
            
            for (float[] member : trainData){
                /// Prepare array of distances
                float[] distances = new float[k];
                Arrays.fill(distances, 0f);
                
                /// Compute distance to each cluster-centroid
                for (int cl = 0; cl < k; cl++){ /// For each cluster
                    for (int feature = 0; feature < trainData.get(0).length; feature++){ /// For each feature in training data
                        distances[cl] += (float) Math.pow((member[feature] - clusters[cl].prototype[feature]), 2);
                    }
                    distances[cl] = (float) Math.sqrt(distances[cl]);
                }
                
                /// Find smallest distance
                System.err.println("TODO::: generateNewPartitioning() still needs further implementation!");
                
                /// Add member to cluster with smallest distance between member and cluster's centroid
                System.err.println("TODO::: generateNewPartitioning() still needs further implementation!");
                
            }
        }
        
        
        private void computePrototype(Cluster cl){
            System.err.println("Computing prototype...");
            float[] average = null;
            if (cl.currentMembers.size() != 0){
                average = new float[trainData.get(0).length];
                Arrays.fill(average, 0f);
                System.err.println("Arrayinitialized to have " + trainData.get(0).length + " spaces.");
            }
            
            //int i = 0; i < cl.currentMembers.size(); i++
            for (Integer idx : cl.currentMembers){ /// Iterate through all clients of the cluster
                for (int feature = 0; feature < trainData.get(0).length; feature++){ /// Iterate through all features per client
                    average[feature]  += ((float) trainData.get(idx.intValue())[feature] ); /// "/ (float) cl.currentMembers.size()" lead to rounding errors
                }
            }
            for (int i = 0; i < average.length; i++){
                average[i] = average[i] / (float) cl.currentMembers.size();
            }
            cl.prototype = average;
        }
        
        private void computePrototypes(){
            /// For each cluster, compute prototype
            for (int i = 0; i < clusters.length; i++)
                computePrototype(clusters[i]);
        }
        
        private void initialPartitioning(){
            Random rn = new Random();
            int idx = 0;
            System.err.println("k: " + k);
            
            /// Random partitionng
            for (int i = 0; i < trainData.size(); i++){ /// Assign every client to a cluster
                idx = rn.nextInt(k); /// Get random index for a cluster
                clusters[idx].currentMembers.add(new Integer(i)); /// Add index of current client to randomly picked cluster
            }
            
            /// Print
            System.err.println("Cluster sizes:");
            for (int i = 0; i < clusters.length; i++)
                System.out.println(i + ": " + clusters[i].currentMembers.size());
            
            /// Compute initial prototypes
            computePrototypes();
            
            /// Print
            System.err.println("Prototypes:");
            for (int i = 0; i < clusters.length; i++){
                System.out.println("Cluster: " + i + " prototype: ");
                for (int ii = 0; ii < clusters[i].prototype.length; ii++)
                    System.out.print(clusters[i].prototype[ii] + "  ");
                System.out.println("\n");
            }
        }
        
	public boolean train()
	{
            /// Get overview over data
            for (int i = 0; i < trainData.size(); i++){
                System.out.println(i + ": " + (float[]) trainData.get(i) + "Size: " + ((float[]) trainData.get(i)).length);
                float[] featureVector = (float[]) trainData.get(i);
                for (int ii = 0; ii < featureVector.length; ii++){
                    System.out.print((float) featureVector[ii] + " ");
                }
                System.out.println("\n");
            }
            
            //implement k-means algorithm here:
            // Step 1: Select an initial random partioning with k clusters
            initialPartitioning();
            
            while (differenceDetected()){
                // Step 2: Generate a new partition by assigning each datapoint to its closest cluster center
                generateNewPartitioning();
                
		// Step 3: recalculate cluster centers
                computePrototypes();
                
		// Step 4: repeat until clustermembership stabilizes
            }
		
		return false;
	}

	public boolean test()
	{
		// iterate along all clients. Assumption: the same clients are in the same order as in the testData
		// for each client find the cluster of which it is a member
		// get the actual testData (the vector) of this client
		// iterate along all dimensions
		// and count prefetched htmls
		// count number of hits
		// count number of requests
		// set the global variables hitrate and accuracy to their appropriate value
		return true;
	}


	// The following members are called by RunClustering, in order to present information to the user
	public void showTest()
	{
		System.out.println("Prefetch threshold=" + this.prefetchThreshold);
		System.out.println("Hitrate: " + this.hitrate);
		System.out.println("Accuracy: " + this.accuracy);
		System.out.println("Hitrate+Accuracy=" + (this.hitrate + this.accuracy));
	}
	
	public void showMembers()
	{
		for (int i = 0; i < k; i++)
			System.out.println("\nMembers cluster["+i+"] :" + clusters[i].currentMembers);
	}
	
	public void showPrototypes()
	{
		for (int ic = 0; ic < k; ic++) {
			System.out.print("\nPrototype cluster["+ic+"] :");
			
			for (int ip = 0; ip < dim; ip++)
				System.out.print(clusters[ic].prototype[ip] + " ");
			
			System.out.println();
		 }
	}

	// With this function you can set the prefetch threshold.
	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}
