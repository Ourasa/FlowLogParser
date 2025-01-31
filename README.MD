# Flow Log Parser by Steven Pham

## Overview
The Flow Log Parser is a Java-based command-line tool that reads and analyzes network flow logs. It uses tags to categorize network flows based on protocol and destination port mappings. It counts the occurrences of each tag, and outputs the results to a .txt file.

The main file containing the code which execute this functionality is in `FlowLogParser.java`.  
The test code can be found in the file `TestFlowLogParser.java`.


## Requirements
- **Java 8 or higher installed** 
  - If your device does not have Java installed, you may download and install it from Oracle's website: https://www.oracle.com/in/java/technologies/downloads/. 
  - Java can also be installed through other means, such as through Homebrew.
  

## Assumptions
- When running the programs, all input file contents are formatted properly with the correct file extensions.

- Regarding the **Protocol Numbers and Keywords** in a csv file
  - The first line of csv includes header data, and is skipped
  - Each subsequent line after the header contains:
    1. A single integer representing a protocol number OR a range of integers (formatted as "start-end") 
    2. A corresponding protocol keyword string. Those without keywords are ignored.
    - Note that any subsequent data past these first two are ignored.  

    For an example of a valid csv: 
    ```
    Decimal,Keyword,Protocol,IPv6 Extension Header,Reference
    0,HOPOPT,IPv6 Hop-by-Hop Option,Y,[RFC8200]
    1,ICMP,Internet Control Message,,[RFC792]
    ...
    ```


- Regarding the **Lookup Table** in a csv file
  - The first line of csv includes header data, and is skipped. 
  - Lookup tables has up to 10000 mapping.
  - Each subsequent line contains, in this order:
    1. Integer (dst port)
    2. String (protocol keyword)
    3. String (tag)
    
    For an example of a valid csv: 
    ```
    dstport,protocol,tag 
    25,tcp,sv_P1 
    68,udp,sv_P2 
    ...
    ```


- Regarding the **Flow Data Log** in a txt file
  - There is no header lines.
  - Logs are of the default (version 2) format only. 
  - Files are only up to 10 MB in size.
  - The important data are specifically found in index 6 and 7 (dst port and protocol), given that index begins at 0.
    
    For an example of a valid txt: 
    ```
    2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 49153 6 25 20000 1620140761 1620140821 ACCEPT OK 
    2 123456789012 eni-4d3c2b1a 192.168.1.100 203.0.113.101 23 49154 6 15 12000 1620140761 1620140821 REJECT OK 
    2 123456789012 eni-5e6f7g8h 192.168.1.101 198.51.100.3 25 49155 6 10 8000 1620140761 1620140821 ACCEPT OK 
    ...
    ```




## How to use 
Note: These instructions were done while using a MacOS computer. 

In order to use the program, please first download or clone the repository's files.  
Then, using your terminal, navigate into the folder that contains these files.

From here, there are 2 main approaches to use the program.

1. **Using the Jar file** - This requires an up-to-date Java Runtime. Run the command:  
```java -jar FlowLogParser.jar <path-to-protocol-csv> <path-to-lookup-csv> <path-to-flow-log-txt> <output-file-path>```

   - **Example command, using the files in the repository:**  
    ```java -jar FlowLogParser.jar protocol-numbers-1.csv Lookup_Tables/lookup-1.csv Logs/flow-log-data-1.txt output.txt```

1. **Using the Java class file directly** - Run the commands:  
 ```javac *.java``` <br>
 ```java FlowLogParser <path-to-protocol-csv> <path-to-lookup-csv> <path-to-flow-log-txt> <output-file-path>```

   - **Example command after compiling with javac, using the files in the repository:**  
    ```java FlowLogParser protocol-numbers-1.csv Lookup_Tables/lookup-1.csv Logs/flow-log-data-1.txt output.txt```


For both of the above cases:
   - Replace `path-to-protocol-csv` with the path to the protocol numbers and keywords csv file.
   - Replace `path-to-lookup-csv` with the path to the lookup table csv file.
   - Replace `path-to-flow-log-txt` with the path to the flow data log txt file.
   - Optional: Replace `output-file-path` with the desired output file path for the results. Otherwise, it defaults to `output.txt`
 

## How to run the test file
The test file already has preset file paths to conduct its tests. As such, the only thing that needs to be done is to call it to run, no arguments needed. 

1. **Using the Jar file** - This requires an up-to-date Java Runtime. Run the command:  
    ```java -jar TestFlowLogParser.jar```

2. **Using the Java class file directly** - Run the commands:  
    ```javac *.java```  <br>
    ```java TestFlowLogParser```


For more details regarding the tests conducted for the program, please refer to `TestFlowLogParser.java`.

## Project Approach
The idea was to split the project into 4 major sections. After each section, test cases must be written and ran in order to ensure the functionality of the recently written code.  

1.  Parsing the protocol number to protocol keyword file. For this, a HashMap was chosen due to the pairing nature of the number and their keyword, and for the data structure's O(1) lookup speed.
    - File/data is obtained from: https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml  

2. Parsing the lookup table file. For this, a two-layed HashMap was chosen as the means to store this. The idea is to further optimize the search speed by greatly reducing the search space in between layers. 
   - There are up to 256 protocol numbers, each of which can be paired with a destination port, of which there are 65535 possibilities of. These port and protocol pairs then map to a single tag.
   - Assuming ideal hashing takes place, this effectively allows us to search any combination in two lookups, each of which is O(1) speed.

3. After the 2 initial steps, the flow data log can finally be processed. For this, 2 temporary HashMap data structures are created to keep track of tag counts (done using the look) and port+protocol counts. 
   - Using the protocol HashMap, the protocol number is translated to its keyword counterpart.
   - Using the lookup table HashMap, the keyword is used to retrieve its corresponding HashMap (e.g. hashmap1). Finally, the destination port is used in that hashmap (e.g. hashmap1) to retrieve the tag.
   - If found, the tag's count is incremented. If not, a special tag "Untagged" is incremented instead. 
   - Regardless of whether or not the tag is found, the port+protocol combination count is incremented.

4. After processing the flow log, the output is written into a file. This is done by iterating through each entry in the temporary HashMaps used, and writing the results. 

<br>

## Other notes 
- For the original problem statement, there was a sample set of inputs and output. However, the output seems to be mismatched with the given inputs. Most notably, the port-protocol combination of 22, tcp (6) was not in the input file, yet the output seems to indicate that it is present.  

- For a slight improvement in performance during the reading of the flow log data, one can remove the step of prepopulating temporary data structure for tag count, and instead add a new entry if not found.

- JUnit was originally planned to be utilized as a test framework to facilitate the testing process. However, given that the problem statement asks that we avoid adding external packages/libraries/dependencies, it was avoided. 