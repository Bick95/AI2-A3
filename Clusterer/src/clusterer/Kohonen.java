package clusterer;

import java.util.*;

public class Kohonen extends ClusteringAlgorithm
{
    
        /// Progress bar on/off: on for running algo, off for parameter sweeping
        boolean progressbar;
    
	// Size of clustersmap
	private int n;

	// Number of epochs
	private int epochs;
	
	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;

	private double initialLearningRate; 
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and a memberlist with the ID's (Integer objects) of the datapoints that are member of that cluster.  
	private Cluster[][] clusters;

	// Vector which contains the train/test data
	private Vector<float[]> trainData;
	private Vector<float[]> testData;
        private Vector<int[]> groupBelonging;
	
	// Results of test()
	private double hitrate;
	private double accuracy;
	
	static class Cluster
	{
			float[] prototype;

			Set<Integer> currentMembers;

			public Cluster()
			{
				currentMembers = new HashSet<Integer>();
			}
	}
	
	public Kohonen(int n, int epochs, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.n = n;             /// NxN map
		this.epochs = epochs;
		prefetchThreshold = 0.5;
		initialLearningRate = 0.8;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;        /// Dim of input vectors
                this.groupBelonging = new Vector<int[]>();
		this.progressbar = true;
                
		Random rnd = new Random();

		// Here n*n new cluster are initialized
		clusters = new Cluster[n][n];
                for (int i = 0; i < n; i++)  {
			for (int i2 = 0; i2 < n; i2++) {
				clusters[i][i2] = new Cluster();
				clusters[i][i2].prototype = randomInit();
			}
		}
	}
	
        /// Constructor used for parameter sweeping
        public Kohonen(int n, int epochs, Vector<float[]> trainData, Vector<float[]> testData, int dim, double threshold)
	{
		this.n = n;             /// NxN map
		this.epochs = epochs;
		prefetchThreshold = threshold;
		initialLearningRate = 0.8;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;        /// Dim of input vectors
                this.groupBelonging = new Vector<int[]>();
		this.progressbar = false;
                
		Random rnd = new Random();

		// Here n*n new cluster are initialized
		clusters = new Cluster[n][n];
                for (int i = 0; i < n; i++)  {
			for (int i2 = 0; i2 < n; i2++) {
				clusters[i][i2] = new Cluster();
				clusters[i][i2].prototype = randomInit();
			}
		}
	}

        private float[] randomInit(){
            float[] flt = new float[dim];
            int min = 0;
            int max = 20;
            for (int i = 0; i < dim; i++)
                flt[i] = (float) (min + Math.random() * (max - min)); /// Min and Max value to prevent prototypes from being too far off the data set
            return flt;
        }
	
        private void printProgressBar(int t){
            /// Uncomment following to see development of progress bar
            //System.err.println("Epoch: " + t);
                
            int maxProgress = 10;
            int cntProgress = (int) Math.round((((double)t+(double)1)/(double)epochs)*(double)maxProgress);
            
            System.out.print("\rProgress: |");
            for (int prog = 0; prog < cntProgress; prog++){
                System.out.print("=");
            }
            for (int prog = cntProgress; prog < maxProgress; prog++){
                System.out.print(" ");
            }
            System.out.print("|");
        }
        
        private void computeMemberships(){
            for (int id = 0; id < trainData.size(); id++){ // Step 3: Every input vector is presented to the map (always in the same order)
                    
                float[] client = (float[]) trainData.get(id); /// Get client data

                // For each vector its Best Matching Unit is found, and :

                double distance = 0;
                double smallestDistance = -1; /// Initialization negative; Since distances are never negative, we have an indication that var has no real distance assigned yet
                int rw = 0, clmn = 0;

                for (int row = 0; row < n; row++){ /// Compute Euclidean distance from data point to all n*n prototypes
                    for (int column = 0; column < n; column++){
                        float[] prototype = clusters[row][column].prototype; /// Get current prototype

                        /// Compute Euclidean distance
                        for (int feature = 0; feature < dim; feature++){ /// Add each feature to Euclidean distance
                            distance += Math.pow( (client[feature] - prototype[feature]) , 2);
                        }
                        distance = Math.sqrt(distance);

                        /// Save smallest distance
                        if (distance < smallestDistance || smallestDistance == (double) -1){ /// If smaller distance found or uninitialized, store best match
                            smallestDistance = distance;
                            rw = row;
                            clmn = column;
                        }
                    }
                } /// Smallest Euclidean distance to a prototype detected... Coordinates/Position to/of nearest prototype: clusters[rw][clmn]

                /// Add data point to closest prototype's member list:
                clusters[rw][clmn].currentMembers.add(id);
                groupBelonging.add(new int[]{rw, clmn}); /// Make it easier to find cluster to which client belongs
                
            }
            
        }
        
	public boolean train()
	{
		// Step 1: initialize map with random vectors (A good place to do this, is in the initialisation of the clusters)
                /// Initialization done via constructor
            
            double learningRate;
            int radius;
            
            for (int t = 0; t < epochs; t++){   // Repeat 'epochs' times:
                // Since training kohonen maps can take quite a while, presenting the user with a progress bar would be nice
                if (progressbar)
                    printProgressBar(t);
                
                // Step 2: Calculate the squareSize and the learningRate, these decrease lineary with the number of epochs.
                learningRate = (double) initialLearningRate * ((double) 1 - ((double) t / (double) epochs));
                radius = (int) Math.round((double) (n / 2) * ((double) 1 - ((double) t / (double) epochs)));
                
                
                for (int id = 0; id < trainData.size(); id++){ // Step 3: Every input vector is presented to the map (always in the same order)
                    
                    float[] client = (float[]) trainData.get(id); /// Get client data
                    
                    // For each vector its Best Matching Unit is found, and :
                    
                    double distance = 0;
                    double smallestDistance = -1; /// Initialization negative; Since distances are never negative, we have an indication that var has no real distance assigned yet
                    int rw = 0, clmn = 0;
                    
                    for (int row = 0; row < n; row++){ /// Compute Euclidean distance from data point to all n*n prototypes
                        for (int column = 0; column < n; column++){
                            float[] prototype = clusters[row][column].prototype; /// Get current prototype
                            
                            /// Compute Euclidean distance
                            for (int feature = 0; feature < dim; feature++){ /// Add each feature to Euclidean distance
                                distance += Math.pow( (client[feature] - prototype[feature]) , 2);
                            }
                            distance = Math.sqrt(distance);
                            
                            /// Save smallest distance
                            if (distance < smallestDistance || smallestDistance == (double) -1){ /// If smaller distance found or uninitialized, store best match
                                smallestDistance = distance;
                                rw = row;
                                clmn = column;
                            }
                        }
                    } /// Smallest Euclidean distance to a prototype detected... Coordinates/Position to/of nearest prototype: clusters[rw][clmn]
                    
                    // Step 4: All nodes within the neighbourhood of the BMU are changed, you don't have to use distance relative learning.
                    
                    /// Find area in radius to be updated - Never go out of bounds
                    int minRow = (rw - radius >= 0 ? rw - radius : 0), maxRow = (rw + radius < n ? rw + radius : n);
                    int minCol = (clmn - radius >= 0 ? clmn - radius : 0), maxCol = (clmn + radius < n ? clmn + radius : n);
                    /// Update neighborhood
                    for (int row = minRow; row < maxRow; row++){
                        for (int column = minCol; column < maxCol; column++){
                            /// Update a prototype
                            for (int feature = 0; feature < dim; feature++){
                                clusters[row][column].prototype[feature] = (float)(( (double)1-learningRate) * (double)(clusters[row][column].prototype[feature]) 
                                                                                        + learningRate * (double)trainData.get(id)[feature]);
                            }
                        }
                    }
                }
                
            }
            computeMemberships();
            if (progressbar)
                System.out.println();
            return true;
	}
	
	public boolean test()
	{
            int prefetched = 0;
            int hits = 0;
            int requests = 0;
                
            // iterate along all clients
            for (int id = 0; id < testData.size(); id++){
                
                /// Assumption: Training- and Testing-clients are the same ones and in same order!
                int row = groupBelonging.get(id)[0]; /// groupBelonging[0] = row where cluster representing data point (client) is located
                int clmn = groupBelonging.get(id)[1]; /// groupBelonging[1] = column where cluster representing data point (client) is located
                // for each client find the cluster of which it is a member
                Cluster cluster = clusters[row][clmn];
                // get the actual testData (the vector) of this client
                float client[] = testData.get(id);
                // iterate along all dimensions
                for (int feature = 0; feature < dim; feature++){
                    if (cluster.prototype[feature] > prefetchThreshold){ // and count prefetched htmls
                        prefetched++; /// feature value of prototype is larger  than threshold, so increase prefetched-counter
                        if (client[feature] == (float)1){  /// client actually requested web page as predicted by prototype value -> hit!
                            hits++; // count number of hits
                        }
                    }
                    if (client[feature] == (float)1){  /// client actually requested web page - increase requests counter
                        requests++; // count number of requests
                    }
                }
            }
		
            // set the global variables hitrate and accuracy to their appropriate value
            
            hitrate = (float) hits / (float) requests;
            accuracy = (float) hits / (float) prefetched;
            
            return true;
	}


	public void showTest()
	{
		System.out.println("Initial learning Rate=" + initialLearningRate);
		System.out.println("Prefetch threshold=" + prefetchThreshold);
		System.out.println("Hitrate: " + hitrate);
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Hitrate+Accuracy=" + (hitrate + accuracy));
	}
 
 
	public void showMembers()
	{
		for (int i = 0; i < n; i++)
			for (int i2 = 0; i2 < n; i2++)
				System.out.println("\nMembers cluster["+i+"]["+i2+"] :" + clusters[i][i2].currentMembers);
	}

	public void showPrototypes()
	{
		for (int i = 0; i < n; i++) {
			for (int i2 = 0; i2 < n; i2++) {
				System.out.print("\nPrototype cluster["+i+"]["+i2+"] :");
				
				for (int i3 = 0; i3 < dim; i3++)
					System.out.print(" " + clusters[i][i2].prototype[i3]);
				
				System.out.println();
			}
		}
	}

	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
        
    public double getHitrate() {
        return hitrate;
    }

    public double getAccuracy() {
        return accuracy;
    }
}

