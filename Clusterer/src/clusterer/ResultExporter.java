/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clusterer;

import java.io.File;
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
    private String algo;
    
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
    
    private ResultExporter(String algo){
        this.algo = algo;
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
        this("Kohonen");
        this.accuracyKohonen = accuracy;
        this.hitrateKohonen = hitrate;
        this.minMaxN = n;
        this.minMaxEpochs = epochs;
        this.minMaxPFT = thresholds;
        saveKohonen();
    }
    
    /// Constructor used for saving KMeans data
    public ResultExporter(double[][] accuracy, double[][] hitrate, int[] minMaxK, double[] thresholds){
        this("KMeans");
        this.hitrateKMeans = hitrate;
        this.accuracyKMeans = accuracy;
        this.minMaxK = minMaxK;
        this.minMaxPFT = thresholds;
        saveKMeans();
    }
    
/// Get Path to store export data
    private String getFileName(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        return "SaveFile_" + algo + "_" + timeStamp + ".csv";
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
    
    private void saveKMeans(){
        int ths = (int) Math.round(((minMaxPFT[1]-minMaxPFT[0])/minMaxPFT[2])) + 1; /// Compute nr of thresholds
        
        try { 
            PrintWriter writer = new PrintWriter(output);
            
            /// Store meta-data
            writer.print("Thresholds");
            for (int i = 0; i < ths; i++){
                writer.print(";" + (minMaxPFT[0] + minMaxPFT[2]*(double)i));
            }
            
            writer.println();
            writer.print("k");
            for (int i = 0; i < minMaxK.length; i++){
                writer.print(";" + minMaxK[i]);
            }
            
            /// Store actual data
            /// Accuracy
            writer.println();
            writer.println("Epoch;Accuracy[row: k][column: threshold];...");
            for (int k = 0; k <= minMaxK[1] - minMaxK[0]; k++){
                writer.print((k + minMaxK[0]));
                for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                    writer.print(";" + accuracyKMeans[k][idx]);
                }
                writer.println();
            }
            
            /// Hitrate
            writer.println();
            writer.println("Epoch;Hitrate[row: k][column: threshold];...");
            for (int k = 0; k <= minMaxK[1] - minMaxK[0]; k++){
                writer.print((k + minMaxK[0]));
                for (int idx = 0; idx < ths; idx++){ /// For each threshold value
                    writer.print(";" + hitrateKMeans[k][idx]);
                }
                writer.println();
            }
            
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("Error when saving to file! " + e.getMessage());
        }
    }
    
    private void saveKohonen(){
        
    }
}
