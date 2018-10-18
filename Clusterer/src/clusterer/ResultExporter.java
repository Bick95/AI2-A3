/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clusterer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author daniel
 */
public class ResultExporter {
    private double[] minMaxPFT;
    
    /// Data structire for KMeans
    private double[][] accuracyKMeans;
    private double[][] hitrateKMeans;
    
    private int[] minMaxK;
    
    /// Data structure for Kohonen
    private double[][][] accuracyKohonen;
    private double[][][] hitrateKohonen;
    private int[] minMaxN;
    private int[] minMaxEpochs;
    
    
    File output;
    
    private ResultExporter(){
        /// Initialize file
        output = new File(getPath());
        try {
            output.createNewFile();                  // Create file if necessary
        } catch (IOException ex) {
            Logger.getLogger(ResultExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /// Constructor used for saving Kohonen data
    public ResultExporter(double[][][] accuracy, double[][][] hitrate, int[] n, int[] epochs, double[] thresholds){
        this();
        this.accuracyKohonen = accuracy;
        this.hitrateKohonen = hitrate;
        this.minMaxN = n;
        this.minMaxEpochs = epochs;
        this.minMaxPFT = thresholds;
        saveKohonen();
    }
    
    /// Constructor used for saving KMeans data
    public ResultExporter(double[][] accuracy, double[][] hitrate, int[] minMaxK, double[] thresholds){
        this();
        this.hitrateKMeans = hitrate;
        this.accuracyKMeans = accuracy;
        this.minMaxK = minMaxK;
        this.minMaxPFT = thresholds;
        saveKMeans();
    }
    
/// Get Path to store export data
    private String getFileName(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        return "SaveFile" + timeStamp + ".csv";
    }
    
    private String getPath(){
        String separator = java.nio.file.FileSystems.getDefault().getSeparator();
        JFileChooser chooser = new JFileChooser(new File("").getAbsolutePath());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           System.out.println("You chose to open this file: " +
                chooser.getSelectedFile().getName());
        }
        return chooser.getSelectedFile().getName() + separator + getFileName();
    }
    
/// Actual saving
    
    /*private void saveKMeans(){
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds
        String newLine = System.getProperty("line.separator");
        try { 
            PrintWriter writer = new PrintWriter(output);
            /// minMaxK[0]: From ; minMaxK[1]: to
            writer.println("minMaxK;" + minMaxK[0] + ";" + minMaxK[1]);
            /// Print Threshold values
            writer.print("Thresholds");
            for (int i = 0; i < ths; i++){
                writer.print(";" + (minMaxPFT[0] + minMaxPFT[2]*(double)i));
            }
            writer.write(newLine);
            
            writer.println("- Now data -");
            writer.println("Epoch;Accuracy[clusterIdx][thresholdIdx]");
            writer.flush();
            writer.println("0.6499026606099935");
            writer.println((double)0.6499026606099935);
            /// First: Print accuracy
            for (int i = 0; i <= minMaxK[1]-minMaxK[0]; i++){
                writer.print((i+minMaxK[0]));
                for (int t = 0; t < ths; t++){
                    /// print to file: #clusters | accurary[clusterIndex][thresholdIndex]...
                    //writer.printf(";");
                    //writer.printf(";%f", (double)accuracyKMeans[i][t]);
                    //writer.print(";" + String.valueOf((double)accuracyKMeans[i][t]));
                    writer.printf(";%f", (double)accuracyKMeans[i][t]);
                }
                writer.write(newLine);
            }
            
            /// Now, print hitrate
            writer.println("Epoch;Hitrate[clusterIdx][thresholdIdx]");
            for (int i = 0; i <= minMaxK[1]-minMaxK[0]; i++){
                writer.print((i+minMaxK[0]));
                for (int t = 0; t < ths; t++){
                    /// print to file: #clusters | accurary[clusterIndex][thresholdIndex]...
                    //writer.printf(";");
                    //writer.printf(";%f", (double)hitrateKMeans[i][t]);
                    writer.printf(";%f", (double)hitrateKMeans[i][t]);
                }
                writer.write(newLine);
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error when saving to file! " + e.getMessage());
        }
    }*/
    
    private void saveKMeans(){
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds
        String newLine = System.getProperty("line.separator");
        try { 
            PrintWriter writer = new PrintWriter(output);
            /// minMaxK[0]: From ; minMaxK[1]: to
            writer.println("minMaxK;" + minMaxK[0] + ";" + minMaxK[1]);
            /// Print Threshold values
            writer.print("Thresholds");
            for (int i = 0; i < ths; i++){
                writer.print(";" + (minMaxPFT[0] + minMaxPFT[2]*(double)i));
            }
            writer.write(newLine);
            
            writer.println("- Now data -");
            writer.println("Epoch;Accuracy[clusterIdx][thresholdIdx]");
            /// First: Print accuracy
            for (int i = 0; i <= minMaxK[1]-minMaxK[0]; i++){
                String line = new String(Double.toString(i+minMaxK[0]));
                for (int t = 0; t < ths; t++){
                    System.out.println((double)accuracyKMeans[i][t]);
                    line = line + ";" + Double.toString((double)accuracyKMeans[i][t]);
                }
                System.out.println(line);
                writer.println(line);
            }
            
            /// Now, print hitrate
            writer.println("Epoch;Hitrate[clusterIdx][thresholdIdx]");
            for (int i = 0; i <= minMaxK[1]-minMaxK[0]; i++){
                String line = new String(Double.toString(i+minMaxK[0]));
                for (int t = 0; t < ths; t++){
                    System.out.println((double)hitrateKMeans[i][t]);
                    line = line + ";" + Double.toString((double)hitrateKMeans[i][t]);
                }
                System.out.println(line);
                writer.println(line);
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error when saving to file! " + e.getMessage());
        }
    }
    
    private void saveKohonen(){
        
    }
}
