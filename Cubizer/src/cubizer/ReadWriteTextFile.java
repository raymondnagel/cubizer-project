/**
 * ReadWriteTextFile.java
 * Created on April 4, 2007, 11:55 AM
 * @author rnagel
 */

package cubizer;

import java.io.*;

/**
 * Contains two convenience methods for simple text file I/O.
 * Methods are <code>static public</code>; there is no reason to instantiate
 * this class.
 */
public class ReadWriteTextFile 
{
    /**
     * Fetch the entire contents of a text file, and return it in a String.
     * This style of implementation does not throw Exceptions to the caller.
     *
     * @param aFile is a file which already exists and can be read.
     */
    static public String getContents(File aFile)
    {
        //...checks on aFile are elided
        StringBuffer contents = new StringBuffer();
        
        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try
        {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            input = new BufferedReader( new FileReader(aFile) );
            String line = null; //not declared within while loop
            /*
             * readLine is a bit quirky :
             * it returns the content of a line MINUS the newline.
             * it returns null only for the END of the stream.
             * it returns an empty String if two newlines appear in a row.
             */
            while (( line = input.readLine()) != null)
            {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if (input!= null)
                {
                    //flush and close both "input" and its underlying FileReader
                    input.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return contents.toString();
    }

    /**
    * Change the contents of text file in its entirety, overwriting any
    * existing text.
    *
    * This style of implementation throws all exceptions to the caller.
    *
    * @param aFile  an existing file which can be written to.
    * @throws IllegalArgumentException if param does not comply.
    * @throws FileNotFoundException if the file does not exist.
    * @throws IOException if problem encountered during write.
    */
    static public void setContents(File aFile, String aContents)
                                 throws FileNotFoundException, IOException 
    {
        if (aFile == null)
        {
            throw new IllegalArgumentException("File should not be null.");
        }
        if (!aFile.exists())
        {
            throw new FileNotFoundException("File does not exist: " + aFile);
        }
        if (!aFile.isFile())
        {
            throw new IllegalArgumentException("Should not be a directory: " + aFile);
        }
        if (!aFile.canWrite())
        {
            throw new IllegalArgumentException("File cannot be written: " + aFile);
        }

        //declared here only to make visible to finally clause; generic reference
        Writer output = null;
        try
        {
            //use buffering
            //FileWriter always assumes default encoding is OK!
            output = new BufferedWriter( new FileWriter(aFile) );
            output.write( aContents );
        }
        finally
        {
            //flush and close both "output" and its underlying FileWriter
            if (output != null) output.close();
        }
    }
  
}

