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

	//store membership for data
    private int[] clusterMembership;

    //stopping criterion (in case the partitioning did not change
    private Boolean stopCriterion;
	
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
		this.prefetchThreshold = 0.5;

		stopCriterion = false;
		clusterMembership = new int[trainData.size()]; /// Contains cluster-indx if which a client was member i previous round
                
		// Here k new cluster are initialized
		clusters = new Cluster[k];
		for (int ic = 0; ic < k; ic++)
			clusters[ic] = new Cluster();
	}
        
        private void generateNewPartitioning(){
            for (Cluster cl : clusters){ /// Update previous members and prepare for new set of current members
                cl.previousMembers = cl.currentMembers;
                cl.currentMembers = new HashSet<Integer>();
            }
            
            int i = 0;
            stopCriterion = true; /// Keep only going on if there is some change(s)
            float sumDistances = 0;
            System.out.println("traindata length is " + trainData.size());
            
            for (float[] member : trainData){
                
                /// Compute distance to each cluster-centroid and find smallest
                float distance = 0;
                int bestCentroid = 0;
                float bestDistance = 9999999;
                
                for (int cl = 0; cl < k; cl++){ /// For each cluster
                    /// Calculate Euclidean distance from client to centroid
                    for (int feature = 0; feature < dim; feature++){ /// For each feature in training data

                        distance += (float) Math.pow((member[feature] - clusters[cl].prototype[feature]), 2);
                    }
                    distance = (float) Math.sqrt(distance);
                    
                    /// Find smallest distance from member to a centroid
                    if (distance < bestDistance){
                        bestDistance = distance;
                        bestCentroid = cl;
                    }
                }
                /// Add member to cluster with smallest distance between member and cluster's centroid
                sumDistances += bestDistance;
                if(clusterMembership[i] != bestCentroid){
                    stopCriterion = false;
                }
                clusterMembership[i] = bestCentroid;
                clusters[bestCentroid].currentMembers.add(i++);
            }
            System.out.println("sum distance is " + sumDistances + " and i: " + i);
        }
        
        
        private void computePrototype(Cluster cl){
            System.out.println("Computing prototype...");
            
            float[] average = new float[dim];
            Arrays.fill(average, 0f);
            
            System.out.println("Array initialized to have " + dim + " spaces.");

            for (Integer idx : cl.currentMembers){ /// Iterate through all clients of the cluster
                
                for (int feature = 0; feature < dim; feature++){ /// Iterate through all features per client
                    average[feature]  += ( (float) trainData.get(idx.intValue())[feature] ); /// "/ (float) cl.currentMembers.size()" lead to rounding errors
                }
                
            }
            
            if (cl.currentMembers.size() != 0)
                for (int i = 0; i < dim; i++){
                    average[i] = average[i] / (float) cl.currentMembers.size();
                }
            
            cl.prototype = average;
        }
        
        private void computePrototypes(){
            /// For each cluster, compute prototype
            for (int i = 0; i < clusters.length; i++){
                computePrototype(clusters[i]);
            }
        }
        
        private void initialPartitioning(){
            Random rn = new Random();
            int idx = 0;
            
            /// Random partitionng
            for (int i = 0; i < trainData.size(); i++){ /// Assign every client to a cluster
                idx = rn.nextInt(k); /// Get random index for a cluster
                clusters[idx].currentMembers.add(new Integer(i)); /// Add index of current client to randomly picked cluster
                clusterMembership[i] = idx;
            }
            
            /// Printing...
            showMembers();
                
            /// Compute initial prototypes
            computePrototypes();
            
            /// Print
            //System.err.println("Prototypes:");
            //showPrototypes();
        }
        
	public boolean train()
	{
            //implement k-means algorithm here:
            // Step 1: Select an initial random partioning with k clusters
            initialPartitioning();
            
            while (!stopCriterion){
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
            int ctrPrefetched = 0;
            int ctrHits = 0;
            int ctrRequests = 0;
            // iterate along all clients. Assumption: the same clients are in the same order as in the testData
            for (int member = 0; member < testData.size(); member++){
                for (int feature = 0; feature < dim; feature++){ // iterate along all dimensions
                    
                    int cluster = clusterMembership[member]; // for each client find the cluster of which it is a member
                    boolean prefetched = (clusters[cluster].prototype[feature] > prefetchThreshold ? true : false);
                    boolean requested = (testData.get(member)[feature] == (float)1 ? true : false); // get the actual testData (the vector) of this client
                    
                    if (prefetched){        /// A website is prefetched
                        if (requested){     /// A website is prefetched AND requested by the user
                            ctrHits++;      // count number of hits
                        }
                        ctrPrefetched++;    // and count prefetched htmls
                    }
                    ctrRequests++;          // count number of requests
                }
            }
            System.out.println("ctrRequests: " + ctrRequests + " ctrHits: " + ctrHits + " ctrPrefetched: " + ctrPrefetched);
            // set the global variables hitrate and accuracy to their appropriate value
            this.hitrate = (double) ((double) ctrHits / (double) ctrRequests);
            this.accuracy = (double) ((double) ctrHits / (double) ctrPrefetched);
		
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
            for (int i = 0; i < k; i++) {
                System.out.println("\nMembers cluster["+i+"] :" + clusters[i].currentMembers);
            }
	}
	
	public void showPrototypes()
	{
		for (int ic = 0; ic < k; ic++) {
                    System.out.print("\nPrototype cluster["+ic+"] :");
			
                    for (int ip = 0; ip < dim; ip++) {
                        System.out.print(clusters[ic].prototype[ip] + " ");
                        System.out.println();
                    }
		}
	}

	// With this function you can set the prefetch threshold.
	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}
