// Class that can be used to capture a PrintStream like STDOUT 
// and store it as a string.  This is used in order
// to get a program's main() output into something we can compare
// inside the java test program.

import java.io.*;

public class CaptureStream extends ByteArrayOutputStream { 
    private StringBuffer captured = new StringBuffer();
    public String getText() {
        return captured.toString();
    }

    public void resetText() {
        captured = new StringBuffer();
    }

    public void flush() throws IOException {                   
        String record; 
        synchronized(this) { 
            super.flush(); 
            record = this.toString(); 
            super.reset(); 
            
            if (record.length() == 0) {
                // avoid empty records 
                return; 
            } 
            
            captured.append(record);
        }             
    } 
}
