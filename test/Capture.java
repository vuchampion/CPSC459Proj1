// This class handles capturing standard output from a main program
// and returning it as a string.  It also does a comparison of the
// reference output and the student output, displaying differences
// so they are easy to spot.

import java.io.PrintStream;

public class Capture {
    
    /////////////////////////////////////////////////////////////////////
    // Flags passed into the output method to control how we normalize
    // the student and reference ouput
    
    // Strip leading and trailing whitespace, collapse inline to single space
    public static final int NORM_WHITE                = 1; 
    
    // Case insentive comparison of letters
    public static final int NORM_CASE_INSENSITIVE     = 2;
    
    // Found all floating point things to 4, 6 or 9 decimal places
    public static final int NORM_FLOAT_4PLACES        = 4;
    public static final int NORM_FLOAT_6PLACES        = 8;
    public static final int NORM_FLOAT_9PLACES        = 16;
    
    // Also version for scientific notation in 4 (NBody), 6 (NBody), or 9 decimal places
    public static final int NORM_SCIENTIFIC_4PLACES   = 32;
    public static final int NORM_SCIENTIFIC_6PLACES   = 64;
    public static final int NORM_SCIENTIFIC_9PLACES   = 128;
    
    // Strip trailing whitespace (mostly for checkerboard)
    public static final int NORM_TRAIL_WHITE          = 256;
    
    // Strip leading whitespace
    public static final int NORM_LEAD_WHITE           = 512;
    
    /////////////////////////////////////////////////////////////////////
    // Flags controlling the how the output method prints things
    
    // Use output that is above and below each other rather than side-by-side
    public static final int OUT_ABOVE_BELOW           = 1;
    
    // Don't output anything unless there is a difference
    public static final int OUT_QUIET                 = 2;
        
    // Don't limit the amount of output
    public static final int OUT_NO_LINE_LIMIT         = 4;
    
    // Don't actually compare the lines for a match
    public static final int OUT_IGNORE_DIFF           = 8;

    // Only output the first so many lines (TOP_LINES)
    public static final int OUT_TOP                   = 16;
    public static final int TOP_LINES                 = 10;

    // Only output the first (FIRST_LINES) lines and the last (LAST_LINES) lines
    public static final int OUT_FIRST_LAST            = 32;
    public static final int FIRST_LINES               = 8;
    public static final int LAST_LINES                = 4;

    // When truncating lines in side-by-side mode, include the beginning and ending segments
    public static final int OUT_NO_TRUNC            = 64;

    /////////////////////////////////////////////////////////////////////
    private static PrintStream   stdout     = System.out;
    private static CaptureStream capture    = new CaptureStream();
    private static PrintStream   captureOut = new PrintStream(capture, true);
    
    // Start capturing all standard output to a string
    public static void start() {
        capture.resetText();
        System.setOut(captureOut);
    }
    
    // Stop capturing and return the text since start() was called
    public static String end() {
        System.setOut(stdout);
        return capture.getText();
    }
    
    // Print out a line showing the total matching count given the array
    public static void outputMatch(int [] counts) {
        if (counts.length < 2)
            return;
        
        if (counts[0] != counts[1])
            System.out.println("*** MATCHED " + counts[0] + "/" + counts[1] + " ***\n");
        else
            System.out.println("matched " + counts[0] + "/" + counts[1] + "\n");
    }
    
    // Normalize a reference or student line to enable easier matching
    // Parameters:
    //  orig      - string to be normalized
    //  flags     - flags that control how we normalize the string
    public static String norm(String orig, int flags) {
        String result = "";
        
        result = orig;
        if ((flags & NORM_WHITE) != 0) {
            // Multiple whitespace to single space
            result = result.replaceAll("\\s+", " ");
            
            // No leading/trailing whitespace
            result = result.replaceAll("^\\s+", "");
            result = result.replaceAll("\\s+$", ""); 
        }
        if ((flags & NORM_TRAIL_WHITE) != 0) {
            // Bye bye trailing whitespace
            result = result.replaceAll("\\s+$", "");     
        }
        if ((flags & NORM_LEAD_WHITE) != 0) {
            // Bye bye leading whitespace
            result = result.replaceAll("^\\s+", "");     
        }
        
        if ((flags & NORM_CASE_INSENSITIVE) != 0) {
            // Case doesn't matter
            result = result.toLowerCase();
        }
        
        if (((flags & NORM_FLOAT_4PLACES) != 0) ||
            ((flags & NORM_FLOAT_6PLACES) != 0) ||
            ((flags & NORM_FLOAT_9PLACES) != 0) ||
            ((flags & NORM_SCIENTIFIC_4PLACES) != 0) ||
            ((flags & NORM_SCIENTIFIC_6PLACES) != 0) ||
            ((flags & NORM_SCIENTIFIC_9PLACES) != 0)) {
            // Normalize all floating points to six decimal places
            String [] chunks = result.split(" ");
            String [] normChunks = new String[chunks.length];
            
            result = "";
            for (int i = 0; i < chunks.length; i++) {
                String chunk = chunks[i];
                double num = 0.0;
                try {
                    if (chunk.indexOf(".") != -1) {
                        num = Double.parseDouble(chunk);
                        if ((flags & NORM_FLOAT_4PLACES) != 0)
                            chunk = String.format("%4f", num);
                        else if ((flags & NORM_FLOAT_6PLACES) != 0)
                            chunk = String.format("%6f", num);
                        else if ((flags & NORM_FLOAT_9PLACES) != 0)
                            chunk = String.format("%9f", num);
                        else if ((flags & NORM_SCIENTIFIC_4PLACES) != 0)
                            chunk = String.format("%4e", num);
                        else if ((flags & NORM_SCIENTIFIC_6PLACES) != 0)
                            chunk = String.format("%6e", num);
                        else if ((flags & NORM_SCIENTIFIC_9PLACES) != 0)
                            chunk = String.format("%9e", num);
                    }        
                }
                catch (java.lang.NumberFormatException e) {
                }
                if (result.length() > 0)
                    result += " ";
                result += chunk;
            }
        }
        
        // Always remove CR/LFs from the line
        result = result.replaceAll("\n", "");
        result = result.replaceAll("\r", "");
        
        return result;
    }

    public static String[] splitString(String s) {
        // Convert DOS style linefeeds to just \n, then split on CR/LF
        return s.replaceAll("\r\n", "\n").split("[\n\r]");
    }

    public static String[] normStrings(String[] s, int flagsNorm) {
        for (int i = 0; i < s.length; i++)
            s[i] = norm(s[i], flagsNorm);
        return s;
    }

    public static int output(String[] origRef, String[] origStudent,
                            int flagsNorm, int flagsOutput) {
        // This prevents runaway output
        int MAX_LINES = 100;
        
        // A flag can tell us to compare all lines of ouput
        if ((flagsOutput & OUT_NO_LINE_LIMIT) != 0)
            MAX_LINES = Integer.MAX_VALUE;
        
        boolean same = true;

        String[] normStudent = new String[origStudent.length];
        String[] normRef     = new String[origRef.length];
        
        // Create normalized versions of each line in both reference 
        // and student solutions
        for (int i = 0; i < origStudent.length; i++)
            normStudent[i] = norm(origStudent[i], flagsNorm);
        for (int i = 0; i < origRef.length; i++)
            normRef[i] = norm(origRef[i], flagsNorm);
        
        StringBuffer resultSideBySide = new StringBuffer();
        StringBuffer resultStudent    = new StringBuffer();
        StringBuffer resultRef        = new StringBuffer();
        int i = 0;
        boolean truncStudent = false;
                
        // Loop to the end of both the student and reference solution
        while (((i < origStudent.length) || (i < origRef.length)) && (i < MAX_LINES)) {
            String lineStudent = "";
            String lineNormStudent = "";
            String lineRef = "";
            String lineNormRef = "";
            String line = "";
            
            if (i < origStudent.length) {
                lineStudent = origStudent[i];
                lineNormStudent = normStudent[i];
            }
            if (i < origRef.length) {
                lineRef = origRef[i];
                lineNormRef = normRef[i];
            }
                         
            if ((flagsOutput & OUT_ABOVE_BELOW) != 0) {
                // Reference on top, student below it
                String flag = "   ";
                if (((flagsOutput & OUT_IGNORE_DIFF) == 0) && 
                    (!lineNormRef.equals(lineNormStudent))) {
                    flag = "@@ ";
                    same = false;
                }
                
                // only output first and last so many lines
                if ((flagsOutput & OUT_FIRST_LAST) != 0) {
                    if (i < FIRST_LINES || i >= origStudent.length - LAST_LINES)
                        resultStudent.append(flag + lineStudent + "\n");
                    if (i < FIRST_LINES || i >= origRef.length - LAST_LINES)
                        resultRef.append("   " + lineRef + "\n");
                }
                // We may only want to output the top lines
                else if (((flagsOutput & OUT_TOP) == 0) || (i < TOP_LINES)) {
                    resultStudent.append(flag + lineStudent + "\n");
                    resultRef.append("   " + lineRef + "\n");
                }
                
                // Output "..." when we truncate
                if ((((flagsOutput & OUT_TOP) != 0) && (i == TOP_LINES))
                        || (((flagsOutput & OUT_FIRST_LAST) != 0) && (i == FIRST_LINES))) {
                    if (((flagsOutput & OUT_FIRST_LAST) == 0) || (FIRST_LINES + LAST_LINES < origStudent.length))
                        resultStudent.append("...\n");
                    if (((flagsOutput & OUT_FIRST_LAST) == 0) || (FIRST_LINES + LAST_LINES < origRef.length))
                        resultRef.append("...\n");
                }

            }
            else {
                // Side-by-side output
                
                // Truncate so we can fit in 80 characters
                String shortRef = "";
                String shortStudent = "";
                if ((flagsOutput & OUT_NO_TRUNC) == 0) {
                    shortRef = lineRef.substring(0, 
                                            Math.min(37, lineRef.length())); 
                    shortStudent = lineStudent.substring(0, 
                                            Math.min(37, lineStudent.length()));
                }
                else {

                }
                
                if (lineStudent.length() > 37)
                    truncStudent = true;
                
                if (((flagsOutput & OUT_IGNORE_DIFF) != 0) || 
                    (lineNormRef.equals(lineNormStudent))) {
                    line = String.format("%-38s    %-38s\n", 
                                         shortRef, 
                                         shortStudent);
                }
                else {
                    line = String.format("%-38s @@ %-38s\n", 
                                         shortRef,
                                         shortStudent);
                    same = false;
                }

                // only output first and last so many lines
                if ((flagsOutput & OUT_FIRST_LAST) != 0) {
                    if (i < FIRST_LINES || i >= Math.max(origStudent.length, origRef.length) - LAST_LINES)
                        resultSideBySide.append(line);
                }
                // We may only want to output the top lines
                else if (((flagsOutput & OUT_TOP) == 0) || (i < TOP_LINES))
                    resultSideBySide.append(line);

                // Output "..." when we truncate
                if (((flagsOutput & OUT_TOP) != 0) && (i == TOP_LINES)) {
                    line = String.format("%-38s    %-38s\n", "...", "...");
                    resultSideBySide.append(line);
                }
                else if (((flagsOutput & OUT_FIRST_LAST) != 0) && (i == FIRST_LINES)) {
                    String s1 = "";
                    String s2 = "";
                    if (FIRST_LINES + LAST_LINES < origRef.length)
                        s1 = "...";
                    if (FIRST_LINES + LAST_LINES < origStudent.length)
                        s2 = "...";
                    if (s1.equals("...") || s2.equals("...")) {
                        line = String.format("%-38s    %-38s\n", s1, s2);
                        resultSideBySide.append(line);
                    }
                }

            }
            
            i++;
        }
        
        // Now output things, captializing things to flag if there is a difference
        if ((flagsOutput & OUT_ABOVE_BELOW) != 0) {
            // Reference on top, student below it

            // Either we are not in quiet mode, or it is quiet but we found a difference
            if (((flagsOutput & OUT_QUIET) == 0) || (!same)) {
                System.out.println("   --reference--");
                System.out.println(resultRef);
            
                if (same)
                    System.out.println("   --student--");
                else
                    System.out.println("   --STUDENT--");
            
                System.out.println(resultStudent);
            }
        }
        else {
            // Side-by-side            

            // Either we are not in quiet mode, or it is quiet but we found a difference
            if (((flagsOutput & OUT_QUIET) == 0) || (!same)) {
                if (same)
                    System.out.printf("%-38s    %-38s\n", "--reference--", "--student--");
                else
                    System.out.printf("%-38s @@ %-38s\n", "--reference--", "--STUDENT--");        
                System.out.println(resultSideBySide);

                // Dump out the full result if not a match (but not in top mode)
                if ((truncStudent) && (!same) && ((flagsOutput & OUT_TOP) == 0) && ((flagsOutput & OUT_FIRST_LAST) == 0)) {
                    System.out.println("--full student---");
                    for (i = 0; i < origStudent.length; i++)
                        System.out.println(origStudent[i]);
                    System.out.println();
                }
            }
        }

        if (i == MAX_LINES) {
            System.out.println("@@ OUTPUT TOO LONG, stopped at " + MAX_LINES + " lines");
            same = false;
        }            
        
        if (same)
            return 1;
        return 0;        

    }
    
    // Display the reference and student solution, highlighting any 
    // differences with @@.  Truncates soltuions to 37 characters
    // in width.  If not identical, will display the students 
    // untruncated solution after the side-by-side diff.
    //
    // Parameters:
    //   ref           - reference output
    //   student       - student output
    //   flagsNorm     - controls how we normalize the text
    //   flagsOutput   - control output formatting
    // Returns 1 if match, 0 if mismatched.
    public static int output(String ref, 
                             String student,
                             int flagsNorm,
                             int flagsOutput) {
        return output(splitString(ref), splitString(student), flagsNorm, flagsOutput);
    }    
    
    // Given the final counter array for a set of tests, just print out FAILED or passed.
    // This makes it easy for people to grep for the same string in the output files.
    public static int printPassFail(int[] counts) {
        if (counts.length != 2)
            return 0;

        if (counts[0] != counts[1])
            System.out.println("*** MATCHED " + counts[0] + "/" + counts[1] + " ***");
        else
            System.out.println("matched " + counts[0] + "/" + counts[1] + "");
        
        if (counts[0] < counts[1]) {
            System.out.println("===> FAILED");
            System.out.println();
            return 0;
        }
        else {
            System.out.println("===> passed");
            System.out.println();
            return 1;
        }
    }
   
}
