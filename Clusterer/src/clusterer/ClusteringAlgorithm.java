package clusterer;

public abstract class ClusteringAlgorithm
{
	public abstract boolean train();

	public abstract boolean test();
	
	public abstract void setPrefetchThreshold(double prefetchThreshold);    
	
	public abstract void showTest();
	
	public abstract void showMembers();
	
	public abstract void showPrototypes();
        
        public abstract double getAccuracy();
        
        public abstract double getHitrate();
}
