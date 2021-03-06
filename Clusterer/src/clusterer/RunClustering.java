package clusterer;

import java.io.*;
import java.util.*;

public class RunClustering
{
	// This class reads the neccesay parameters and runs the clusteringAlgorithms
	// Students don't have to edit this to complete their assignments

	public static ClusteringAlgorithm ca;   //handle to the actual clustering algorithm

	private static int dim;                 // dimensionality of the data and clusters
  
	private static Vector<float[]> trainData;
	private static Vector<float[]> testData;
	
	// Data from these vectors is not used in this implementation. 
	// For generality they are included
	private static Vector<String> requestsVector;
	private static Vector<String> clientsVector;
	
	public static BufferedReader in;

	// ***** Main *****
	public static void main(String[] args)
	{
		// Read data from files
		if (args.length==4) {  //use user defined file names
			readData(args[0],args[1],args[2],args[3]);
		} else if (System.getProperties().get("user.name").equals("daniel")){
                    args = new String[4];
                    args[0] = "/home/daniel/Uni/ThirdYear/AI2/A3/Data/webuser-clustering/train.dat";
                    args[1] = "/home/daniel/Uni/ThirdYear/AI2/A3/Data/webuser-clustering/test.dat";
                    args[2] = "/home/daniel/Uni/ThirdYear/AI2/A3/Data/webuser-clustering/requests.dat";
                    args[3] = "/home/daniel/Uni/ThirdYear/AI2/A3/Data/webuser-clustering/clients.dat";
                    readData(args[0],args[1],args[2],args[3]);
		} else if (System.getProperties().get("user.name").equals("Bálint")){
                    args = new String[4];
                    args[0] = "C:\\Users\\Bálint\\Desktop\\Bálint\\RUG\\3A\\AI2\\AI2-A3\\data/train.dat";
                    args[1] = "C:\\Users\\Bálint\\Desktop\\Bálint\\RUG\\3A\\AI2\\AI2-A3\\data/test.dat";
                    args[2] = "C:\\Users\\Bálint\\Desktop\\Bálint\\RUG\\3A\\AI2\\AI2-A3\\data/requests.dat";
                    args[3] = "C:\\Users\\Bálint\\Desktop\\Bálint\\RUG\\3A\\AI2\\AI2-A3\\data/clients.dat";
                    readData(args[0],args[1],args[2],args[3]);

		}else {
			System.out.println("No files where defined (java runClustering [traindata, testdata, requests, clients]), using defaults");
			readData();
		} 

		// data is located in the appropriate vectors now
		
		// Prepare to read from command line
		in = new BufferedReader(new InputStreamReader(System.in));
		
		
		while (true) {
                    // Ask for algoritm ans its apropriate parameters. Algorithm gets copies of the datavectors (fail-prove)
                    if (initializeAlgorithm()){ /// Only interact with user in usual way if choice is not parameter sweeping, otherwise choose other mode
                        
                        // Training
                        System.out.print("Perform the actual training! (hit enter)"); 
                        // You wait for authorisation because in real applications,training and or testing may take days.
                        waitForAuthorisation();   
                        System.out.println("Training ...");
                        ca.train();
                        System.out.println("Training finished.");

                        // Testing
                        System.out.print("Perform the testing! (hit enter)");  
                        waitForAuthorisation();                     
                        System.out.println("Testing...");
                        ca.test();
                        System.out.println("Testing finished.");

                        // Start interacting with the user
                        boolean startUp = true;
                        while (startUp) {
                            // Show of results
                            startUp = showResult(); // ask what information should be shown. (Or train another algorithm)
                        }
			
                    }
			
		}
	}
	
	public static int chooseAlgorithm()
	{
		int algID=4;
		while (true) {
			System.out.print("Run K-means (1), Leader-Follower(2), Kohonen SOM (3), Quit(4) or Parameter sweeping (5) ? ");
			String line="";
			try {
				if ((line = in.readLine()) == null)
					break;
				
				algID = (new Integer(line)).intValue();
				
				if ((algID > 0) && (algID < 6))
					break;
			}
			catch (Exception e) {
				System.out.println();
			}
		}
		
		return algID;
	}

	public static int chooseResult()
	{
		int resultID=0;
		while (true) {
			System.out.print("Show output printTest(1), vector members(2), vector prototypes(3), Quit(4) or set prefetchThreshold(5)? ");
			String line="";
			try {
				resultID = (new Integer(in.readLine())).intValue();
				if ((resultID > 0)&&(resultID < 6)) 
					break;
			}
			catch(Exception e){
				System.out.println();      
			} 
		}

		return resultID;
	}
	
	public static boolean initializeAlgorithm()
	{
		// determine which algorithm is requested (chooseAlgorithm), and ask for corresponding parameters
		switch (chooseAlgorithm()) {
			case 1:
				kmeansInit();
				break;
			case 2:
				leaderFollowerInit();
				break;
			case 3:
				kohonenInit();
				break;
			case 4:
				System.exit(0);
                        case 5:
                                paramSweepInit();
                                return false;
		}
                return true;
	 }
	
        public static void paramSweepInit(){
            GridSearcher gs = new GridSearcher(trainData, testData, dim);
            gs.runParameterSweeping();
        }
        
	public static void kmeansInit()
	{
		int k = 0;
		while (true) {  // get k
			System.out.print("How many clusters (k) ? ");
			try{ 
				k = (new Integer(in.readLine())).intValue(); 
				break;
			}
			catch (Exception e){
				System.out.println();
			} 
		}
		
		// The k-means model is now created
		// java doesn't have a 'const' specifier, so for safety objects are copied
		ca = new KMeans(k, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim);
	}
	
	public static void leaderFollowerInit()
	{
		double dis = 0;
		while (true) {  
			System.out.print("Cluster distance ? ");
			try{ 
				dis = (new Double(in.readLine())).doubleValue(); 
				break;
			}
			catch (Exception e){
				System.out.println();
			}
		}
		
		// The leader-follower model is now created
		// java doesn't have a 'const' specifier, so for safety objects are passed by reference
		ca = new LeaderFollower(dis, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim);
	}
	
	public static void kohonenInit()
	{
		int n = 0;
		int epochs = 0;
		while (true) {  
			System.out.print("Map size (N*N) ? ");
			try{ 
				n = (new Integer(in.readLine())).intValue(); 
				break;
			}
			catch (Exception e){
				System.out.println();            
			}

		}

		while (true) {  
			System.out.print("Number of training epochs ? ");
			try{ 
				epochs = (new Integer(in.readLine())).intValue(); 
				break;
			}
			catch(Exception e){ 
				System.out.println();
			}
		}
		
		// The kohonen model is now created
		// java doesn't have a 'const' specifier, so for safety objects are passed by reference
		ca = new Kohonen(n, epochs, new Vector<float[]>(trainData), new Vector<float[]>(testData), dim);
	}
	
	public static boolean showResult()
	{
		boolean ret_val=true;
		switch(chooseResult()){
			case 1:
				ca.showTest();
				break;
			case 2:
				ca.showMembers();
				break;
			case 3:
				ca.showPrototypes();
				break;
			case 4:
				ret_val = false;
				break;
			case 5:
				try {
					System.out.print("PrefetchThreshold = ");                    
					double prefetchThreshold = (new Double(in.readLine())).doubleValue();
					ca.setPrefetchThreshold(prefetchThreshold);
					System.out.println("Testing algorithm with prefetchThreshold = "+prefetchThreshold+"...");               
					ca.test();
				}
				catch (Exception e){}
		}  
		return ret_val;
	}

	public static void readData()
	{
		trainData = new Vector<float[]>();
		readTrainData("train.dat");

		testData  = new Vector<float[]>();
		readTestData("test.dat");
	}

	public static void readData(String trainFileName, String testFileName, String requestFileName, String clientFileName)
	{
		requestsVector = new Vector<String>();
		readRequests(requestFileName);

		clientsVector  = new Vector<String>();
		readClients(clientFileName);
		
		trainData = new Vector<float[]>();
		readTrainData(trainFileName);
		
		testData  = new Vector<float[]>();
		readTestData(testFileName);
	}
	 
	private static void readTrainData(String trainFileName)
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(trainFileName));
			String line = "";
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f,");
				
				if (dim == 0)
					dim = st.countTokens();

				else if (dim != st.countTokens()) {
					System.out.println("traindata vectors have different size");
					System.exit(1);
				}

				float[] data = new float[dim];
				for (int i = 0; i < dim; i++)
					data[i] = Float.parseFloat(st.nextToken());
				
				trainData.addElement(data);
			}
			
			br.close();
		}
		catch(Exception e){
			System.out.println("error occured while reading traindata:"+e);
			System.exit(1);
		}
	}
	
	private static void readTestData(String testFileName)
	{
		try{
			BufferedReader br = new BufferedReader(new FileReader(testFileName));
			String line = "";
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f,");
				if (dim == 0)
					dim = st.countTokens();

				else if (dim != st.countTokens()) {
					System.out.println("test vectors have different size");
					System.exit(1);
				}

				float[] data = new float[dim];
				for (int i = 0; i < dim; i++)
					data[i] = Float.parseFloat(st.nextToken());

				testData.addElement(data);
			}
			br.close();
		}
		catch (Exception e) {
			System.out.println("error occured while reading testdata:"+e);
			System.exit(1);
		}
	}

	private static void readRequests(String requestsFileName)
	{
		try{
			BufferedReader br = new BufferedReader(new FileReader(requestsFileName));
			String line = "";

			while ((line = br.readLine()) != null)
				requestsVector.addElement(line);
			
			br.close();
		}
		catch (Exception e){
			System.out.println("error occured while reading requestsdata:"+e);
			System.exit(1);
		}
	}

	private static void readClients(String clientsFileName)
	{
		try{
			BufferedReader br = new BufferedReader(new FileReader(clientsFileName));
			String line = "";

			while ((line = br.readLine()) != null)
				clientsVector.addElement(line);

			br.close();
		}
		catch (Exception e) {
			System.out.println("error occured while reading clientsdata:"+e);
			System.exit(1);
		}
	}
	
	private static void waitForAuthorisation()
	{
		try {
			while(in.readLine()==null);
		}
		catch (Exception e) {
			// Don't let Frank see this ;)
		}
   }
}
