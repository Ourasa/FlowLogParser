// File Name: FlowLogParser.java
// Author: Steven Pham

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class FlowLogParser {
    
    private HashMap<Integer, String> protocolTable = new HashMap<Integer, String>(); // Protocol Number -> Keyword
    private HashMap<String, HashMap<Integer, String>> lookupMap = new HashMap<>(); // Keyword -> (Dest port, tag)
    
    /**
     * Fills a HashMap for protocol number to keyword translation from a csv file.
     * 
     * Assumptions: 
     * - The first line of csv includes header data, and is skipped. 
     * - Each subsequent line starts with either:
     *      1) A single integer representing a protocol number, or
     *      2) A range of integers (formatted as "start-end").
     * 
     * - Each line also includes a corresponding protocol keyword.
     * - Lines without a keyword are ignored.
     * 
     * @param protocolFile - Path to .csv file containing protocol number -> keyword translation
     */
    public void prepareProtocolMappings(String protocolFile) {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(protocolFile));
            String line;
            br.readLine(); // Skip the header line
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                
                // Those with no keywords are ignored.
                if (parts[1].trim().equals("")) { 
                    continue;
                }
                
                // If it's a range, we add a mapping pair for each one
                if (parts[0].trim().contains("-")) { 
                    int first = Integer.parseInt( parts[0].substring(0, parts[0].indexOf("-")) );
                    int last = Integer.parseInt( parts[0].substring(parts[0].indexOf("-") + 1) );

                    for (int i = first; i <= last; i++) {
                        protocolTable.put(i, parts[1].trim().toLowerCase());
                    }

                } else { // It should be a 1-to-1 mapping here. This should be the most common case.
                    protocolTable.put(Integer.parseInt(parts[0]), parts[1].toLowerCase());
                }

            }

            br.close();

        } catch (Exception e) {
            System.out.println("A problem has occurred preparing the protocol mappings: " + e);
        }

    }



    /**
     * Fills a double-leveled HashMap using the lookup table from a csv file.
     * 
     * Assumptions: 
     * - The first line of csv includes header data, and is skipped. 
     * - Lookup tables has up to 10000 mapping.
     * - Each subsequent line contains, in this order:
     *      1.) Integer (dst port)
     *      2.) String (protocol keyword)
     *      3.) String (tag)
     * 
     * @param lookupTable - .csv file containing the lookup table info
     */
    public void prepareLookup(String lookupTable) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(lookupTable));
            String line;
            br.readLine(); // Skip the header line
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                
                String protocol = parts[1].trim();
                int dstPort = Integer.parseInt(parts[0].trim());
                String tag = parts[2].trim();

                if (!lookupMap.containsKey(protocol)) { // If not a key yet, add a new HashMap.
                    lookupMap.put(protocol, new HashMap<Integer, String>());
                } 

                lookupMap.get(protocol).put(dstPort, tag);
            }

            br.close();

        } catch (Exception e) {
            System.out.println("A problem has occurred preparing the lookup mappings: " + e);
        }

    }


    /**
     * Processes the flow log file and generates an output file containing the tag counts
     * and the port/protocol combination counts.
     * 
     * Assumptions: 
     * - There is no header lines.
     * - Logs are of the default (version 2) format only. 
     * - Files are only up to 10 MB in size.
     * 
     * @param flowLogFile - .txt file containing the flow logs
     */
    public void readFlowLog(String flowLogFile, String outputFile) {
        
        // Create temporary data structures to later write into a file. 
        HashMap<String, Integer> tagCounts = new HashMap<String, Integer>();
        HashMap<String, Integer> portProtocolCounts = new HashMap<String, Integer>();

        // Populate the first temporary structure
        for (String protocol : lookupMap.keySet()) {
            for (Integer dstPort : lookupMap.get(protocol).keySet()) {
                tagCounts.put(lookupMap.get(protocol).get(dstPort), 0);
            }
        }
        tagCounts.put("Untagged", 0);


        try {
            BufferedReader br = new BufferedReader(new FileReader(flowLogFile));
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                
                // Tag Count portion
                String protocol = protocolTable.get(Integer.parseInt(parts[7]));
                int dstport = Integer.parseInt(parts[6]);
                String tag = lookupMap.get(protocol).get(dstport);
                
                if ((protocol != null) && (tag != null) ) { 
                    tagCounts.put(tag, tagCounts.get(tag) + 1);

                } else { // Not part of lookup table
                    tagCounts.put("Untagged", tagCounts.get("Untagged") + 1);
                }

                // Port + Protocol Count porton
                String portProtocol = "" + dstport + "," + protocol;
                if (portProtocolCounts.containsKey(portProtocol)) {
                    portProtocolCounts.put(portProtocol, portProtocolCounts.get(portProtocol) + 1);
                } else {
                    portProtocolCounts.put(portProtocol, 1);
                }

            }

            br.close();
            writeOutput(tagCounts, portProtocolCounts, outputFile);

        } catch (Exception e) {
            System.out.println("A problem has occurred reading the flow logs: " + e);
        } 

    }

    /**
     * Writes into a file the given the counts for the tag and port/protocol combinations.  
     * 
     * @param tagCounts - HashMap containing the tag counts
     * @param portProtocolCounts - HashMap containing the port/protocol combination counts
     * @param outputFile - Name of the output file to write out to
     */
    public void writeOutput(HashMap<String, Integer> tagCounts, HashMap<String, Integer> portProtocolCounts, String outputFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile)); 
            writer.write("Tag Counts:\n");
            writer.write("Tag,Count\n");

            for (String tag: tagCounts.keySet()) {
                writer.write(tag + "," + tagCounts.get(tag) + "\n");
            }

            writer.write("\nCount of matches for each port/protocol combination:\n");
            writer.write("Port/Protocol Combination Counts:\n");
            writer.write("Port,Protocol,Count\n\n");

            for (String portProtocol : portProtocolCounts.keySet()) {
                writer.write(portProtocol + "," + portProtocolCounts.get(portProtocol) + "\n");
            }

            writer.close();

        } catch (Exception e) {
            System.out.println("A problem has occurred while writing the output file: " + e);
        }

    }

    /**
     * Gettermethod for the protocol number to keyword HashMap
     * @return protocolTable
     */
    public HashMap<Integer, String> getProtocolTable() {
        return protocolTable;
    }
    
    /**
     * Getter method for the two-layered lookup HashMap
     * @return lookupMap
     */
    public HashMap<String, HashMap<Integer, String>> getLookupMap() {
        return lookupMap;
    }
    


    /**
     * Main method that executes the program. 
     * Argument contains the file paths, which are then used to execute the program.
     * 
     * Expects the following arguments:
     *  1) Protocol mapping CSV
     *  2) Lookup table CSV
     *  3) Flow log file
     *  4) (Optional) Output file path (defaults to "output.txt")
     * 
     * @param args - String arguments containing file paths
     */
    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Jar Usage: java -jar FlowLogParser.jar <protocolFile> <lookupTable> <flowLogFile> [outputFile]");
            System.out.println("Example: java -jar FlowLogParser.jar protocol.csv lookup.csv log.txt custom-output.txt\n");

            System.out.println("Direct Java File Usage: java FlowLogParser.java <protocolFile> <lookupTable> <flowLogFile> [outputFile]");
            System.out.println("Example: java FlowLogParser.java protocol.csv lookup.csv log.txt custom-output.txt");
            return;
        }
    
        String protocolFile = args[0];
        String lookupFile = args[1];
        String flowLogFile = args[2];
        String outputFile = (args.length == 4) ? args[3] : "output.txt";
    
        System.out.println("Starting FlowLogParser...\n");
        System.out.println("Using the file paths:");
        System.out.println(" - Protocol file: " + protocolFile);
        System.out.println(" - Lookup table file: " + lookupFile);
        System.out.println(" - Flow log file: " + flowLogFile);
        System.out.println(" - Output file: " + outputFile + "\n");
    
        try {
            FlowLogParser parser = new FlowLogParser();
            parser.prepareProtocolMappings(protocolFile);
            parser.prepareLookup(lookupFile);
            parser.readFlowLog(flowLogFile, outputFile);
            System.out.println("Flow log parsing completed. Output written to: " + outputFile + "\n");

        } catch (Exception e) {
            System.err.println("A problem occurred while parsing: " + e.getMessage());
        }
    }
}