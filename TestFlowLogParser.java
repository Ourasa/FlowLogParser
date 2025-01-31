// File Name: TestFlowLogParser.java
// Author: Steven Pham

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class TestFlowLogParser {

    // Static strings containing what should be the content of the outputted file
    private static final String EXPECTED_TAG_COUNTS = "Tag Counts:\nTag,Count\nsv_P2,1\nSV_P3,0\nsv_P1,2\nsv_P5,0\nsv_P4,0\nemail,3\nUntagged,8";
    private static final String EXPECTED_PORT_PROTOCOL_COUNTS = "Count of matches for each port/protocol combination:\nPort/Protocol Combination Counts:\nPort,Protocol,Count\n\n23,tcp,1\n993,tcp,1\n80,tcp,1\n25,tcp,1\n49157,tcp,1\n443,tcp,1\n49155,tcp,1\n110,tcp,1\n49153,tcp,1\n49158,tcp,1\n143,tcp,1\n49156,tcp,1\n49154,tcp,1\n1024,tcp,1";

    // - - - Protocol table tests - - -

    /**
     * Takes the protocol number to keyword mappings and runs them through the tests.
     * There are both positive and negative test cases.
     * 
     * @param protocolTable - HashMap containing protocol number to keyword mappings
     */
    public void testPrepareProtocolMappings(HashMap<Integer, String> protocolTable) {
        System.out.println("--- Beginning Protocol Table Tests ---");
        validMap1(protocolTable);
        validMap2(protocolTable);
        invalidMap1(protocolTable);
        invalidMap2(protocolTable);
        System.out.println("--- Ending Protocol Table Tests ---\n");
    }

    /**
     * Verifies that protocol number 6 maps correctly to the "tcp" keyword.
     * 
     * @param protocolTable - HashMap containing protocol number to keyword mappings
     */
    private void validMap1(HashMap<Integer, String> protocolTable) {
        if (protocolTable.containsKey(6) && protocolTable.get(6).equals("tcp")) {
            System.out.println("Test Passed: Protocol 6 maps to tcp");
        } else {
            System.out.println("(X) Test Failed: Protocol 6 does not map to tcp");
        }
    }

    /**
     * Verifies that protocol number 17 maps correctly to the "udp" keyword.
     * 
     * @param protocolTable - HashMap containing protocol number to keyword mappings
     */
    private void validMap2(HashMap<Integer, String> protocolTable) {
        if (protocolTable.containsKey(17) && protocolTable.get(17).equals("udp")) {
            System.out.println("Test Passed: Protocol 17 maps to udp");
        } else {
            System.out.println("(X) Test Failed: Protocol 17 does not map to udp");
        }
    }

    /**
     * Validates that an invalid protocol number (99) does not exist in the mapping.
     * 
     * @param protocolTable - HashMap containing protocol number to keyword mappings
     */
    private void invalidMap1(HashMap<Integer, String> protocolTable) {
        if (!protocolTable.containsKey(99)) {
            System.out.println("Test Passed: Protocol 99 is not included in the mapping");
        } else {
            System.out.println("(X) Test Failed: Protocol 99 should not be included");
        }
    }

    /**
     * Validates that an invalid protocol number (200) does not exist in the mapping.
     * 
     * @param protocolTable - HashMap containing protocol number to keyword mappings
     */
    private void invalidMap2(HashMap<Integer, String> protocolTable) {
        if (!protocolTable.containsKey(200)) {
            System.out.println("Test Passed: Protocol 200 is not included in the mapping");
        } else {
            System.out.println("(X) Test Failed: Protocol 200 should not be included");
        }
    }

    // - - - End of protocol table tests - - -



    // - - - Lookup table tests - - -

    /**
     * Takes the lookup mappings and runs them through the tests.
     * There are both positive and negative test cases.
     * 
     * @param lookupMap - HashMap containing the lookup mappings
     */
    public void testPrepareLookup(HashMap<String, HashMap<Integer, String>> lookupMap) {
        System.out.println("--- Beginning Lookup Table Tests ---");
        validLookup1(lookupMap);
        validLookup2(lookupMap);
        invalidLookup1(lookupMap);
        invalidLookup2(lookupMap);
        System.out.println("--- Ending Lookup Table Tests ---\n");
    }

    /**
     * Verifies that the "tcp" protocol contains a destination port 25 with the expected tag "sv_P1".
     * 
     * @param lookupMap - HashMap containing the lookup mappings
     */
    private void validLookup1(HashMap<String, HashMap<Integer, String>> lookupMap) {
        if (lookupMap.containsKey("tcp") 
        && lookupMap.get("tcp").containsKey(25)
        && lookupMap.get("tcp").get(25).equals("sv_P1")) {
            System.out.println("Test Passed: TCP protocol has destination port 25, with tag sv_P1");
        } else {
            System.out.println("(X) Test Failed: TCP protocol should have destination port 25 with tag sv_P1");
        }
    }

    /**
     * Verifies that the "udp" protocol contains a destination port 68 with the expected tag "sv_P2".
     * 
     * @param lookupMap - HashMap containing the lookup mappings
     */
    private void validLookup2(HashMap<String, HashMap<Integer, String>> lookupMap) {
        if (lookupMap.containsKey("udp") 
        && lookupMap.get("udp").containsKey(68)
        && lookupMap.get("udp").get(68).equals("sv_P2")) {
            System.out.println("Test Passed: UDP protocol has destination port 68 with tag sv_P2");
        } else {
            System.out.println("(X) Test Failed: UDP protocol should have destination port 68 with tag sv_P2");
        }
    }

    /**
     * Verifies that the "epg" protocol does not exist in the lookup table.
     * 
     * @param lookupMap - HashMap containing the lookup mappings
     */
    private void invalidLookup1(HashMap<String, HashMap<Integer, String>> lookupMap) {
        if (!lookupMap.containsKey("epg")) {
            System.out.println("Test Passed: EPG protocol is not included in the lookup map");
        } else {
            System.out.println("(X) Test Failed: EPG protocol should not be in the lookup map");
        }
    }

    /**
     * Verifies that the "tcp" protocol does not contain destination port 9999.
     * 
     * @param lookupMap - HashMap containing the lookup mappings
     */
    private void invalidLookup2(HashMap<String, HashMap<Integer, String>> lookupMap) {
        if (!lookupMap.get("tcp").containsKey(9999)) {
            System.out.println("Test Passed: TCP protocol does not have destination port 9999");
        } else {
            System.out.println("(X) Test Failed: TCP protocol should not have destination port 9999");
        }
    }

    // - - - End of protocol table tests - - -



    // - - - Output of reading flow log test - - -

    /**
     * Tests the output file by comparing its content against expected values.
     * Reads the file and checks if the content matches the expected output for the flow log processing.
     * 
     * @param fileName - The name of the file to be tested for correct output
     */
    public void testOutput(String fileName) {
        System.out.println("--- Beginning File Output Tests ---");

        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            StringBuilder fileContent = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            reader.close();
            
            String actualContent = fileContent.toString().trim();  // Read the file content
            
            // Check if actual content matches expected content
            if (actualContent.equals(EXPECTED_TAG_COUNTS + "\n\n" + EXPECTED_PORT_PROTOCOL_COUNTS)) {
                System.out.println("Test Passed: Output matches expected content.");
            } else {
                System.out.println("(X) Test Failed: Output does not match expected content.");
            }

        } catch (Exception e) {
            System.out.println("(X) Test Failed: An unaccounted for problem has occurred" + e);
        }

        System.out.println("--- Ending File Output Tests ---\n");
    }

    // - - - End output of reading flow log test - - -



    // - - - Misc. methods - - -

    /**
     * Deletes the test results file after testing is complete.
     * This method ensures that old files do not persist after tests have been run.
     * 
     * @param fileName - The name of the file to be deleted
     */
    private void deleteTestResultsFile(String fileName) {
        File file = new File(fileName);
        if (file.delete()) {
            System.out.println("Test results file deleted successfully.");
        } else {
            System.out.println("Failed to delete the test results file.");
        }
    }


    /**
     * Main method that runs the tests for protocol mapping, lookup table, and flow log output.
     * 
     * @param args - String arguments (unused)
     */
    public static void main(String[] args) {
        FlowLogParser parser = new FlowLogParser();
        TestFlowLogParser tester = new TestFlowLogParser();

        // String paths to be used. The tests will only behave as intended with these paths.
        String protocolFile = "protocol-numbers-1.csv";
        String lookupTableFile = "Lookup_Tables/lookup-1.csv";
        String flowLogFile = "Logs/flow-log-data-1.txt";
        String outputFile = "test_results.txt";

        // Protocol File parsing
        parser.prepareProtocolMappings(protocolFile);
        tester.testPrepareProtocolMappings(parser.getProtocolTable());

        // Lookup Table parsing 
        parser.prepareLookup(lookupTableFile);
        tester.testPrepareLookup(parser.getLookupMap());

        // Flow Log parsing
        parser.readFlowLog(flowLogFile, outputFile);
        tester.testOutput(outputFile);
        tester.deleteTestResultsFile(outputFile);

    }
    
}
