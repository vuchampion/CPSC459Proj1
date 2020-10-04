// A place to put static methods used in our grading Test*.java programs.

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class UtilCOS {
  

    // The ProgramPerformanceLimits class was designed to simplify performance testing code by allowing
    // performance limits to be specified using literal matrices. 

    // This class is suitable for creating both memory and timing tests. The constructor
    // takes as input an array of fieldnames (given as Strings), and a table of limits (given as
    // a 2D array of doubles). The test client can then query the ProgramPerformanceLimit class
    // to see whether a particular test exists, can query whether a given piece of data obeys the PPL, 
    // and can request a reportString which provides a simple numerical description of how closely
    // the student is to obeying the appropriate PPL.

    // The archietcture of the code is a little strange (particularly the constructor), but it's all in service of 
    // making it simple to specifiy operation and memory count limits as literals. See the method descriptions
    // below for more details.


    // While the ideal solution would be literal 
    // specifications of some sort of Hashmap of Hashmaps, literal specification of HashMaps is not possible 
    // with the current version of Java. 


    // Columns 2 through M represent the permissable lower and upper bounds for field #0 through #F-1.
    // Column 2 represents the lower bound for field #0, and column 3 represents the upper bound for field #0.
    // Column 4 represents the lower bound for field #1, and so forth.

    // Once a ProgramPeformanceLimits object has been instantiated, the key methods that the client
    // (usually TestMemoryOfXXX.java or TimeXXX.java) will utilize are:

    // boolean testExists(double testID, String fieldName)
    // boolean obeysLimits(double testID, String fieldName, double data) 
    // String reportString(double testID, String fieldName, double data)

    // For more details on these methods, see the comments below. As an alternative to calling
    // these methods directly, one can also use the PPLTestResult class, which performs all
    // tests at once, and makes results available via separate (final) instance variables.

    public static class ProgramPerformanceLimits {
        double[][] performanceLimits; 
        HashMap<String, Integer> fieldNameToColumnNumber;

        // The constructor takes an array of fieldnames of arbitrary length F, and a 2D array
        // of doubles of size N x M, where N may be any number, and M = 2*F + 1. The first column of the table 
        // should be a unique identifier, which will typically the object under test. For example, for percolation,
        // the first column specifies the grid size. 

        // An example PPL instantiation is given below:

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // String [] fields = {"time", "union", "findPlus2xConnected", "constructor"};

        // double [][] limits = {
        ////   N,         time,                  union,                  find                constructor
        //    {8,     0,      100,        10,         1500,       15,         1500,       1,         3},
        //    {32,    0,      100,        150,        15000,      150,        20000,      1,         3},
        //    {128,   0,      100,        2500,       220000,     3000,       300000,     1,         3},
        //    {512,   0,      100,        35000,      3600000,    50000,      5000000,    1,         3},
        //    {1024,  0,      100,        150000,     15000000,   200000,     20000000,   1,         3} 
        //};

        //  UtilCOS.ProgramPerformanceLimits ppl = new UtilCOS.ProgramPerformanceLimits(fields, limits);
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        // Under the current (Fall 2012) assignment set, the only assignment
        // for which the first column is NOT a simple object size is 8Puzzle. For this assignment, the first
        // column is equal to the number in the relevant puzzle name (e.g. puzzle32.txt). 

        public ProgramPerformanceLimits(String[] fieldnames, double[][] limits)
        {
            int numRows = limits.length;
            assert (numRows > 0);
            
            int numFields = fieldnames.length;
            int numColumns = limits[0].length;

            int expectedNumberOfColumns = numFields * 2 + 1; //one for ID, two for each field.
            assert(expectedNumberOfColumns == numColumns);

            //Create copies of current field name
            //Create map from field name to column number in performance matrix

            fieldNameToColumnNumber = new HashMap<String, Integer>();
            int columnOfCurrentField = 1;

            for (int i = 0; i < fieldnames.length; i++) {
                fieldNameToColumnNumber.put(fieldnames[i], columnOfCurrentField);
                columnOfCurrentField += 2;
            }


            //copy the performance matrix
            performanceLimits = new double[numRows][numColumns];
            for (int r = 0; r < numRows; r++)
                for (int c = 0; c < numColumns; c++) 
                    performanceLimits[r][c] = limits[r][c];
        }

        // Determines whether or not a given test exists. 

        public boolean testExists(double testID, String fieldName)
        {
            boolean rowExists = false;

            int rowNum = testIDToRow(testID);
            if (rowNum >= 0)
                rowExists = true;

            boolean columnExists = fieldNameToColumnNumber.containsKey(fieldName);

            return columnExists && rowExists;

        }

        // Detemrines whether data lies within the bounds given in the PPL. If the
        // error factor given by reportString rounds to 1.0, then the student is
        // considered to have passed.

        public boolean obeysLimits(double testID, String fieldName, double data) {
            double[] limits = getLimits(testID, fieldName);

            double minValue = limits[0];
            double maxValue = limits[1];

            if ((data <= maxValue) && (data >= minValue))
                return true;

            String rs = reportString(testID, fieldName, data);
            if (rs.equals("        "))
                return true;

            return false;
        }

        // Returns a string which represents the factor by which the student
        // overshot (or undershot) the bounds given in this PPL. If 
        // the student is within the appropriate bounds, a blank string
        // consisting of only spaces is returned.

        public String reportString(double testID, String fieldName, double data) {
            double[] limits = getLimits(testID, fieldName);

            double minValue = limits[0];
            double maxValue = limits[1];

            String rs;


            if (data > maxValue)
                rs = floatingShorter("(%-3.1#x)", data / maxValue);
            else if (data < minValue)
                rs = floatingShorter("(%-3.1#x)", data / minValue);
            else
                rs = "        ";

            // This is a bit of a hack. If the error factor rounds to 1.0, then
            // the student is considered to have passed the test.

            if (rs.equals("(1.0x)"))
                rs = "        ";

            return rs;
        }        

        private int testIDToRow(double testID)
        {
            int numRows = performanceLimits.length;
            int returnValue = -1;

            for (int r = 0; r < numRows; r++)
                if (performanceLimits[r][0] == testID)
                    returnValue = r;

            return returnValue;
        }


        private int[] getRowAndColumn(double testID, String fieldName)
        {
            int rowNum = testIDToRow(testID);

            if (!fieldNameToColumnNumber.containsKey(fieldName))
                throw new RuntimeException("fieldName " + fieldName + " does not exist in program performance limit table!");
            
            int colNum = fieldNameToColumnNumber.get(fieldName);

            return new int[]{rowNum, colNum};            
        }

        private double[] getLimits(double testID, String fieldName)
        {
            int [] rowAndCol = getRowAndColumn(testID, fieldName);
            int rowNum = rowAndCol[0];
            int colNum = rowAndCol[1];

            double minValue = performanceLimits[rowNum][colNum];
            double maxValue = performanceLimits[rowNum][colNum+1];

            return new double[]{minValue, maxValue};
        }

        public double[] getTestIDs()
        {
            double [] testIDs = new double[performanceLimits.length];
            for (int i = 0; i < performanceLimits.length; i++)
            {
                testIDs[i] = performanceLimits[i][0];
            }
            return testIDs;
        }
    
        // This function outputs a double in the shorter of two formats, where
        // the two possible formats are %f and %g. The formatString is a
        // specially formatted string, where the # character represents the
        // position where the f or g should appear.

        // For example, floatingShorter("%.2#", someDouble) would return the shorter of
        // String.format("%.2f", someDouble)   and   String.format("%.2g", someDouble)

        public static String floatingShorter(String formatString, double value)
        {
        
            String firstFormatString = formatString.replace('#', 'f');
            String secondFormatString = formatString.replace('#', 'g');
            String firstString = String.format(firstFormatString, value);
            if (value == 0.0) //%g crashes with zero
                return firstString;    

            String secondString = String.format(secondFormatString, value);

            if (firstString.length() <= secondString.length())
                return firstString;
            else
                return secondString;
        }


    }

    //PPLTestResult provides a convenient class for storing pertinent PPL test results.
    //Effectively, this class avoids the need for the user to explicitly call testExists(),
    //obeysLimits(), and reportString(). It is particularly useful for when only a single
    //field is used for generating the entire output of a test, e.g. most memory tests.

    public static class PPLTestResult {
        public final String passString;
        public final String errorFactor;
        public final boolean testExists;
        public final boolean passed;

        public PPLTestResult(ProgramPerformanceLimits ppl, String fieldName, double testID, double data)
        {
            if (ppl.testExists(testID, fieldName))
            {
                testExists = true;

                if (ppl.obeysLimits(testID, fieldName, data))
                {   
                    passString = "=> passed";
                    passed = true;
                }
                else
                {
                    passString = "=> FAILED";
                    passed = false;
                }

                errorFactor = ppl.reportString(testID, fieldName, data);
            }
            else
            {
                passString = "=> Error in grading script. Grading script requested nonexistent test where" + fieldName + "=" + testID;
                errorFactor = "[ERROR]";
                passed = false;
                testExists = false;
            }
        }
    }

    // This method adds newlines to an inputString in order to restrict the number of characters per line.
    // If hardLimit is false, then newlines will only be inserted after space charcters. If it is true, then
    // then the maximum number of characters will be a firm limit, and newlines may split text onto multiple
    // lines.

    public static String restrictCharsPerLine(String inputString, int maxNumCharsPerLine, int indentCnt)
    {
        StringBuilder outputStringBuilder = new StringBuilder();

        int thisLineCount = 0;
        int nextSpaceCount = 0;

        for (int i = 0; i < inputString.length(); i++)
        {
            outputStringBuilder.append(inputString.charAt(i));
            thisLineCount++;

            boolean currentCharIsSpace = Character.isSpaceChar(inputString.charAt(i));
            if (currentCharIsSpace)
            {
                nextSpaceCount = thisLineCount;
                for (int scanAhead = i+1; scanAhead < inputString.length(); scanAhead++)
                {
                    if (Character.isSpaceChar(inputString.charAt(scanAhead)))
                        break;

                    nextSpaceCount++;
                }

                if (nextSpaceCount >= maxNumCharsPerLine)
                {
                    int lastCharIndex = outputStringBuilder.length();
                    outputStringBuilder.replace(lastCharIndex, lastCharIndex, "\n");
                    for (int j = 0; j < indentCnt; j++)
                        outputStringBuilder.append(" ");

                    thisLineCount = indentCnt;
                }
            }

        }

        return outputStringBuilder.toString();

    }




    // The main function of every TestXXX.java, TimeXXX.java, and MemoryOfXXX.java file 
    // should begin with a call to this function. Failure to make this call may
    // cause the python summarizing script to crash.

    public static void printTotalNumTests(int numTests) {
        printTotalNumTests(System.out, numTests);
    }

    public static void printTotalNumTests(PrintStream out, int numTests) {
        out.println("Running " + numTests + " total tests.\n");
    }

    // The main function of every TestXXX.java, TimeXXX.java, and MemoryOfXXX.java file 
    // should begin end with a call to this function. Failure to make this call may
    // cause the python summarizing script to crash.

    public static void printNumTestsPassed(int numPassed, int numTests) {
        printNumTestsPassed(System.out, numPassed, numTests);
    }

    public static void printNumTestsPassed(PrintStream out, int numPassed, int numTests)
    {
        out.printf("Total: %d/%d tests passed!\n\n", numPassed, numTests);
    }

    // This function may be used to summarize subtest results. It is not necessary
    // for proper python script functioning.

    public static void printTableSummary(int numPassed, int numTests) {
        System.out.printf("==> %d/%d tests passed\n", numPassed, numTests);
    }

    // Print a message and convert the boolean to an integer.
    public static int printPassFail(boolean b) {
        return printPassFail(System.out, b);
    }

    public static int printPassFail(PrintStream out, boolean b) {
        if (b) {
            out.println("==> passed");
            out.println();
            return 1;
        }
        else {
            out.println("==> FAILED");
            out.println();
            return 0;
        }
    }

    public static int printPassFail(int i) {
        if (i == 0)
            System.out.println("==> FAILED");
        else
            System.out.println("==> passed");
        System.out.println();
        return i;
    }

    public static void printError(Throwable e) {
        printError(System.out, e);
    }

    public static void printError(PrintStream out, Throwable e) {
        printError(out, e, 5, 5);
    }

    // print error message (first so many and last so many lines of stack trace)
    public static void printError(Throwable e, int first, int last) {
        printError(System.out, e, first, last);
    }

    // print error message (first so many and last so many lines of stack trace)
    public static void printError(PrintStream out, Throwable e, int first, int last) {
        Capture.end();  // in case Capture is in effect
        StackTraceElement[] elements = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("     " + e + "\n");

        // print full stack trace if not too big
        if (elements.length < first + last) {
            for (int i = 0; i < elements.length; i++)
                sb.append("     " + elements[i] + "\n");
        }

        // otherwise, print FIRST lines and LAST lines
        else {
            for (int i = 0; i < first; i++)
                sb.append("     " + elements[i] + "\n");
            if (last > 0) sb.append("     " + "...\n");
            for (int i = elements.length - last; i < elements.length; i++)
                sb.append("     " + elements[i] + "\n");

        }
        out.println(sb.toString());
    }

    
    // Given the entire output of a student solution (possibly multiple
    // lines), return the last floating point value.
    public static double getLastDouble(String student) {
        String[] lines = student.replaceAll("\r\n", "\n").split("[\n\r]");        
        
        // We only support so many floats on a single line
        final int MAX_PER_LINE = 256;
        double [] vals = new double[MAX_PER_LINE];

        for (int i = lines.length - 1; i >= 0; i--) {
            int num = getDoubles(lines[i], vals, MAX_PER_LINE);
            if (num > 0)
                return vals[num - 1];
        }
        return Double.NaN;
    }
    
    // Given a line of output, return up to max floating point values
    // parsed from the string.  Returns number of ints found.
    public static int getDoubles(String student, double [] vals, int max) {
        if (vals.length < max)
            return 0;        
        for (int i = 0; i < vals.length; i++)
            vals[i] = 0;
        
        int index = 0;
        String [] chunks = student.split("[^0123456789-e\\.]");
        for (String chunk : chunks) {
            if (chunk.length() > 0) {
                try {
                    double num = Double.parseDouble(chunk);
                    vals[index] = num;
                    index++;
                    if (index >= max)
                        return index;
                }
                catch (java.lang.NumberFormatException e) { }           
            }
        }
        return index;                
    }

    // Given a line of output, return up to max integer values
    // parsed from the string.  Returns number of ints found.
    public static int getInts(String student, int [] vals, int max) {
        if (vals.length < max)
            return 0;        
        for (int i = 0; i < vals.length; i++)
            vals[i] = 0;
        
        int index = 0;
        String [] chunks = student.split("[^0123456789-]");
        for (String chunk : chunks) {
            if (chunk.length() > 0) {
                try {
                    int num = Integer.parseInt(chunk);
                    vals[index] = num;
                    index++;
                    if (index >= max)
                        return index;
                }
                catch (java.lang.NumberFormatException e) { }            
            }
        }
        return index;        
    }

    // indent all lines by given number of spaces
    public static String indent(Object s, int indentation) {
        if (s == null) return indent(null, indentation);
        else           return indent(s.toString(), indentation);
    }
    public static String indent(String s, int indentation) {
        String spaces = "";
        for (int i = 0; i < indentation; i++)
            spaces += " ";
        if (s == null) return spaces + "null";
        String[] lines = s.split("[\n\r]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++)
        sb.append(spaces + lines[i] + "\n");
        return sb.toString();
     }

     // compare two Comparable Iterables as multisets (null treated as empty)
     public static <Key extends Comparable<Key>> boolean compareAsMultisets(Iterable<Key> studentIterable, Iterable<Key> referenceIterable) {
        // turn into Queue objects in case Iterable returns different answers each time it is iterated over
        Queue<Key> studentQueue = new Queue<Key>();
        Queue<Key> referenceQueue = new Queue<Key>();
        if (studentIterable != null) {
            for (Key key : studentIterable)
                studentQueue.enqueue(key);
        }
        if (referenceIterable != null) {
            for (Key key : referenceIterable)
                referenceQueue.enqueue(key);
        }

        return compareAsMultisets(studentQueue, referenceQueue);
    }

    // compare two Queues as multisets (null treated as empty)
    public static <Key extends Comparable<Key>> boolean compareAsMultisets(Queue<Key> studentQueue, Queue<Key> referenceQueue) {
        int M = 0;
        if (studentQueue != null) M = studentQueue.size();
        int N = referenceQueue.size();
        Comparable[] student   = new Comparable[M];
        Comparable[] reference = new Comparable[N];
        int i = 0, j = 0;
        if (studentQueue != null) {
            for (Key key : studentQueue)
                student[i++] = key;
        }
        if (referenceQueue != null) {
            for (Key key : referenceQueue)
                reference[j++] = key;
        }
        return compareAsMultisets(student, reference);
    }

    // compare two double arrays as multisets (null treated as empty)
    public static boolean compareAsMultisets(double[] student, double[] reference) {
        if (student  == null)  student   = new double[0];
        if (reference == null) reference = new double[0];
        Double[] stu = new Double[student.length];
        Double[] ref = new Double[reference.length];
        for (int i = 0; i < stu.length; i++) stu[i] = student[i];
        for (int i = 0; i < ref.length; i++) ref[i] = reference[i];
        return compareAsMultisets(stu, ref);
    }

    public static void exch(Object[] a, int i, int j) {
        Object temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    // compare two Comparable arrays as multisets (null treated as empty)
    public static boolean compareAsMultisets(Comparable[] student, Comparable[] reference) {
        if (student == null)   student   = new Comparable[0];
        if (reference == null) reference = new Comparable[0];
        boolean passed = true;

        // create copy because we need to mutate arrays
        Comparable[] stu = new Comparable[student.length];
        Comparable[] ref = new Comparable[reference.length];
        for (int i = 0; i < stu.length; i++) stu[i] = student[i];
        for (int i = 0; i < ref.length; i++) ref[i] = reference[i];
      
        // move nulls to right part of array because Arrays.sort() won't deal with them
        int M = stu.length;
        int N = ref.length;
        int studentNulls = 0, referenceNulls = 0;
        for (int i = 0; i < M; i++) {
            if (stu[i] == null) {
                studentNulls++;
                exch(stu, i--, --M);
            }
        }
        for (int j = 0; j < N; j++) {
            if (ref[j] == null) {
                referenceNulls++;
                exch(ref, j--, --N);
            }
        }

        Arrays.sort(stu, 0, M);
        Arrays.sort(ref, 0, N);

        Comparable falsePositive = null, falseNegative  = null;
        int falsePositives = 0, falseNegatives = 0;
        int i = 0, j = 0;
        while (i < M && j < N) {
            int cmp = stu[i].compareTo(ref[j]);
            if (cmp < 0) {
                falsePositives++;
                falsePositive = stu[i++];
            }
            else if (cmp > 0) {
                falseNegatives++;
                falseNegative = ref[j++];
            }
            else {
                i++;
                j++;
            }
        }

        while (i < M) {
            falsePositives++;
            falsePositive = stu[i++];
        }

        while (j < N) {
            falseNegatives++;
            falseNegative = ref[j++];
        }

        if (studentNulls != referenceNulls) {
            if (studentNulls == 1)   System.out.println("     -  student   solution has " + studentNulls     + " null entry");
            else                     System.out.println("     -  student   solution has " + studentNulls     + " null entries");
            if (referenceNulls == 1) System.out.println("     -  reference solution has " + referenceNulls   + " null entry");
            else                     System.out.println("     -  reference solution has " + referenceNulls   + " null entries");
        }

        if (falsePositives > 0 || falseNegatives > 0) {
            System.out.println("     -  student   solution has " + M  + " non-null entries");
            System.out.println("     -  reference solution has " + N  + " non-null entries");
        }

        if (falsePositives > 0) {
            if (falsePositives == 1) 
                System.out.println("     -  " + falsePositives + " extra entry in student solution: " + falsePositive);
            else
                System.out.println("     -  " + falsePositives + " extra entries in student solution, including: " + falsePositive);
        }

        if (falseNegatives > 0) {
            if (falseNegatives == 1)
                System.out.println("     -  " + falseNegatives + " missing entry in student solution: " + falseNegative);
            else
                System.out.println("     -  " + falseNegatives + " missing entries in student solution, including: " + falseNegative);
        }

        return falsePositives == 0 && falseNegatives == 0 && studentNulls == referenceNulls;
    }

     // compare two Iterables as sequences (null treated as empty)
     public static <Key> boolean compareAsSequences(Iterable<Key> studentIterable, Iterable<Key> referenceIterable) {

        // turn into Queue objects in case Iterable returns different answers each time it is iterated over
        Queue<Key> studentQueue = new Queue<Key>();
        Queue<Key> referenceQueue = new Queue<Key>();
        if (studentIterable != null) {
            for (Key key : studentIterable)
                studentQueue.enqueue(key);
        }
        if (referenceIterable != null) {
            for (Key key : referenceIterable)
                referenceQueue.enqueue(key);
        }

        return compareAsSequences(studentQueue, referenceQueue);
    }

    // compare two Queues as sequences (null treated as empty)
    public static <Key> boolean compareAsSequences(Queue<Key> studentQueue, Queue<Key> referenceQueue) {
        int M = 0;
        if (studentQueue != null) M = studentQueue.size();
        int N = referenceQueue.size();
        Object[] student   = new Object[M];
        Object[] reference = new Object[N];
        int i = 0, j = 0;
        if (studentQueue != null) {
            for (Key key : studentQueue)
                student[i++] = key;
        }
        if (referenceQueue != null) {
            for (Key key : referenceQueue)
                reference[j++] = key;
        }
        return compareAsSequences(student, reference);
    }


    // compare two arrays as sequences (null treated as empty)
    public static boolean compareAsSequences(Object[] student, Object[] reference) {
        if (student == null)   student   = new Object[0];
        if (reference == null) reference = new Object[0];
        boolean passed = true;

        int M = student.length;
        int N = reference.length;

        // compare sizes
        if (M != N) {
            System.out.println("     -  student   length = " + M);
            System.out.println("     -  reference length = " + N);
            passed = false;
        }

        // first mismatch
        for (int i = 0; i < Math.min(M, N); i++) {
            if (!reference[i].equals(student[i])) {
                System.out.println("     -  entry " + i + " of the two sequences are not equal");
                System.out.println("     -  student   entry = " + student[i]);
                System.out.println("     -  reference entry = " + reference[i]);
                passed = false;
                break;
            }
        }
        return passed;
    }

    // check observed[] vs. expected[]; typical p-value = 0.001
    public static boolean chiSquared(String[] names, double[] expected, long[] observed, double p) {
        int M = observed.length;

        double chiSquared = 0.0;
        long observedSize  = 0;
        double expectedSize  = 0;
        for (int i = 0; i < M; i++) {
            chiSquared += ((observed[i] - expected[i]) * (observed[i] - expected[i])) / expected[i];
            observedSize += observed[i];
            expectedSize += expected[i];
        }

        ChiSquareTest chi = new ChiSquareTest();
        double pvalue = chi.chiSquareTest(expected, observed);
        if (pvalue < p) {
        // if (pvalue <= 1.0) {
            System.out.println();
            System.out.print("     ");
            System.out.print("                   ");
            for (int i = 0; i < M; i++)
                System.out.printf("%5s ", names[i]);
            System.out.println();

            System.out.print("     ");
            System.out.print("                   ");
            for (int i = 0; i < M-1; i++)
                System.out.printf("------");
            System.out.printf("-----");
            System.out.println();

            System.out.print("     ");
            System.out.print("observed frequency ");
            for (int i = 0; i < M; i++)
                System.out.printf("%5d ", observed[i]);
            System.out.printf("  %d\n", observedSize);
            System.out.print("     ");
            System.out.print("expected frequency ");
            for (int i = 0; i < M; i++)
                System.out.printf("%5.0f ", expected[i]);
            System.out.printf("  %.0f\n", expectedSize);
            System.out.println();
            System.out.printf("     chi-squared = %.2f (p-value = %.6f, reject if p-value <= %.4f)\n\n", chiSquared, pvalue, p);
            return false;
        }

        return true;
    }
    

    public static void main(String[] args) {
        Integer[] a = new Integer[5];
        Integer[] b = new Integer[6];
        a[0] = 17;
        a[1] = 1334337;
        a[2] = 23;
        a[4] = 17;

        b[3] = 17;
        b[4] = 1334337;
        b[5] = 22;
        b[1] = 17;

        UtilCOS.compareAsMultisets(a, b);
    }
}
