/**
 * StringHelper.java
 * Created on February 16, 2007, 1:29 PM
 * @author rnagel
 */

package cubizer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/** 
 * <i>StringHelper</i> contains various <code>public static</code> methods that
 * simplify some <code>String</code>-related tasks. This class contains no constructors
 * and should never be instantiated.
 *
 * @author  rnagel
 * @version 1.0
 * @see     String
 * @since   JDK1.6.1
 */
public final class StringHelper
{
    // enum containing results for SmartCompareStrings.
    public static enum CompRes {LESS_THAN,EQUAL_TO,GREATER_THAN};
    
    //____________________PUBLIC STATIC METHODS____________________//
   
   /**
    * Compares two <code>String</code>s numerically if they both can be parsed into numbers,
    * or alphabetically if they cannot. Returns an enum value defined in this class.
    *
    * @param     value1    first <code>String</code> to compare.
    * @param     value2    second <code>String</code> to compare.
    * @return    <code>LESS_THAN</code>, <code>EQUAL_TO</code>, or <code>GREATER_THAN</code>.
    * @see       #smartCompareStrings(String, String, String)
    */
    public static CompRes smartCompareStrings(String value1, String value2)
    {
        //A empty String value should be counted as less than anything else.
        //Two empty Strings are, of course, identical.
        if (value1.equalsIgnoreCase("") && !value2.equalsIgnoreCase("")) return CompRes.LESS_THAN;
        if (value1.equalsIgnoreCase("") && value2.equalsIgnoreCase("")) return CompRes.EQUAL_TO;
        if (!value1.equalsIgnoreCase("") && value2.equalsIgnoreCase("")) return CompRes.GREATER_THAN;
        
        try
        { //Perform numeric evaluation
            double v1 = Double.parseDouble(value1);
            double v2 = Double.parseDouble(value2);
            if (v1 < v2)
                return CompRes.LESS_THAN;
            if (v1 > v2)
                return CompRes.GREATER_THAN;
            if (v1 == v2)
                return CompRes.EQUAL_TO;
        }
        catch (NumberFormatException ex)
        { //Perform alphabetical evaluation (case-insensitive)
            if (value1.compareTo(value2) < 0)
                return CompRes.LESS_THAN;
            if (value1.compareTo(value2) > 0)
                return CompRes.GREATER_THAN;
            if (value1.compareTo(value2) == 0)
                return CompRes.EQUAL_TO;
        }
        return null;
    }
    
   /**
    * Compares two <code>String</code>s numerically if they both can be parsed into numbers,
    * or alphabetically if they cannot. Returns a <code>boolean</code> value indicating
    * whether the expression created by <code>value1 operator value2</code> is <code>true</code>
    * or <code>false</code>.
    *
    * @param     value1    first <code>String</code> to compare.
    * @param     operator  a <code>String</code> containing one of the following values:
    *            "=="; "<"; ">"; "<="; ">="
    * @param     value2    second <code>String</code> to compare.
    * @return    <code>true</code> or <code>false</code>, depending on the expression.
    * @see       #smartCompareStrings(String, String)
    */
    public static boolean smartCompareStrings(String value1, String operator, String value2)
    {
        CompRes result = smartCompareStrings(value1, value2);
        switch (result)
        {
            case EQUAL_TO:
                return (operator.equalsIgnoreCase("==") || operator.equalsIgnoreCase("<=") || operator.equalsIgnoreCase(">="));
            case LESS_THAN:
                return (operator.equalsIgnoreCase("<") || operator.equalsIgnoreCase("<="));
            case GREATER_THAN:
                return (operator.equalsIgnoreCase(">") || operator.equalsIgnoreCase(">="));
        }
        return false;
    }
    
   /**
    * Truncates the specified number of <code>char</code>s from the beginning of a <code>String</code>.
    *
    * @param     original       the <code>String</code> to modify.
    * @param     charsToShave   the number of characters to remove from the beginning.
    * @return    the modified <code>String</code>.
    * @see       #shaveEnd(String, int)
    */
    public static String shaveStart(String original, int charsToShave)
    {
        return original.substring(charsToShave);
    }
    
   /**
    * Truncates the specified number of <code>char</code>s from the end of a <code>String</code>.
    *
    * @param     original       the <code>String</code> to modify.
    * @param     charsToShave   the number of characters to remove from the end.
    * @return    the modified <code>String</code>.
    * @see       #shaveStart(String, int)
    */
    public static String shaveEnd(String original, int charsToShave)
    {
        return original.substring(0, original.length() - charsToShave);
    }
     
   /**
    * Returns a <code>String</code> representation of an <code>int</code>, using zeros to pad out
    * the <code>String</code> until it becomes the specified length.
    * @param     number       the number to pad.
    * @param     totalDigits  the total number of digits to have (length of <code>number</code> + zeros).
    * @return    the <code>String</code> representation of the number.
    */
    public static String paddedInt(int number, int totalDigits)
    {
        StringBuffer IntStringBfr = new StringBuffer(((Integer)number).toString());
        int NumZeros = (totalDigits-IntStringBfr.length());
        for (int z = 0; z < NumZeros; z++)
        {
            IntStringBfr.insert(0, "0");
        }
        return IntStringBfr.toString();
    }
    
   /**
    * Converts a standard UNIX-type path into the correct representation
    * for the currently running platform by replacing forward slashes (<code>'/'</code>)
    * with the <code>java.io.File.separatorChar</code> for this system.
    *
    * @param     stdUnixPath       a standard UNIX-type path with FORWARD slashes.
    * @return    a path specific to the current platform.
    * @see       #createStdUnixPath(String)
    */
    public static String createPlatformPath(String stdUnixPath)
    {
        String platPath = stdUnixPath.replace('/', java.io.File.separatorChar);
        return platPath;
    }
    
   /**
    * Converts a platform-specific path to a standard UNIX-type path 
    * by replacing the any <code>java.io.File.separatorChar</code>s for this system
    * with forward slashes (<code>'/'</code>).
    *
    * @param     platformPath       a path specific to the current platform.
    * @return    a standard UNIX-type path with FORWARD slashes.
    * @see       #createPlatformPath(String)
    */
    public static String createStdUnixPath(String platformPath)
    {
        String stdUnixPath = platformPath.replace(java.io.File.separatorChar, '/');
        return stdUnixPath;
    }
    
   /**
    * Converts a platform-specific <code>String</code> path to a 
    * Uniform Resource Locator (URL).
    * If you are working with a standard UNIX-type path, first use
    * {@link #createPlatformPath(String)} to convert it to a platform-specific
    * path.
    *
    * @param     platformSpecificPath       a path specific to the current platform.
    * @return    a URL to the file indicated by the specified path,
    *            or <code>null</code> if the path is invalid.
    * @see       #createPlatformPath(String)
    */
    public static URL createURLFromPath(String platformSpecificPath)
    {
        try
        {
            return new File(platformSpecificPath).toURI().toURL();
        }
        catch (MalformedURLException ex)
        {
            return null;
        }
    }
    
   /**
    * Joins all <code>String</code> items in a <code>Collection</code> together 
    * into a single <code>String</code>, inserting a delimiting <code>String</code> between items.
    *
    * @param     strings       a <code>Collection</code> of <code>String</code> objects.
    * @param     delimiter     a <code>String</code> that will be inserted between
    *            items in the output <code>String</code>.
    * @return    a <code>String</code> containing the <code>Collection</code> items
    *            separated by the delimiting <code>String</code>.
    * @see       #join(String[], String)
    */ 
    public static String join(Collection<String> strings, String delimiter) 
    {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = strings.iterator();
        while (iter.hasNext()) 
        {
            buffer.append(iter.next());
            if (iter.hasNext()) 
            {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    
   /**
    * Joins all <code>String</code> items in an array together 
    * into a single <code>String</code>, inserting a delimiting <code>String</code> between items.
    *
    * @param     strings       an array of <code>String</code> objects.
    * @param     delimiter     a <code>String</code> that will be inserted between
    *            items in the output <code>String</code>.
    * @return    a <code>String</code> containing the array items
    *            separated by the delimiting <code>String</code>.
    * @see       #join(Collection<String>, String)
    */
    public static String join(String[] strings, String delimeter)
    {
        StringBuffer sb = new StringBuffer();   
        for( int x = 0; x < ( strings.length - 1 ); x++ )
        {
            sb.append( strings[x] );
            sb.append( delimeter );
        }
        sb.append( strings[ strings.length - 1 ] );
        return( sb.toString() );
    }
    
   /**
    * Converts a <code>String</code> to its title case representation:
    * The first letter of each word (delimited by spaces) is capitalized.
    * All other letters are lower-case.
    * 
    * @param     value    the <code>String</code> to convert
    * @return    the title-cased <code>String</code>
    * @see       #toSentenceCase(String)
    */
    public static String toTitleCase(String value)
    {
        String[] words = value.split(" ");
        for (int w = 0; w < words.length; w++)
        {
            StringBuffer wordBuff = new StringBuffer(words[w]);
            wordBuff.setCharAt(0, String.valueOf(wordBuff.charAt(0)).toUpperCase().charAt(0));
            for (int c = 1; c < wordBuff.length(); c++)
            {
                wordBuff.setCharAt(c, String.valueOf(wordBuff.charAt(c)).toLowerCase().charAt(0));
            }
            words[w] = new String(wordBuff);
        }
        return join(words, " ");
    }
    
   /**
    * Converts a <code>String</code> to its sentence case representation:
    * The first letter of the first word is capitalized.
    * All other letters are lower-case.
    * 
    * @param     value    the <code>String</code> to convert
    * @return    the sentence-cased <code>String</code>
    * @see       #toTitleCase(String)
    */
    public static String toSentenceCase(String value)
    {
        StringBuffer wordBuff = new StringBuffer(value);
        wordBuff.setCharAt(0, String.valueOf(wordBuff.charAt(0)).toUpperCase().charAt(0));
        for (int c = 1; c < wordBuff.length(); c++)
        {
            wordBuff.setCharAt(c, String.valueOf(wordBuff.charAt(c)).toLowerCase().charAt(0));
        }
        return new String(wordBuff);
    }

    public static String capitalize(String value)
    {
        if (value == null || value.length() < 1)
            return value;
        else
        {
            return Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }
    }

    public static String decapitalize(String value)
    {
        if (value == null || value.length() < 1)
            return value;
        else
        {
            return Character.toLowerCase(value.charAt(0)) + value.substring(1);
        }
    }

   /**
    * Returns an array of <code>String</code>s that match a specified
    * masking pattern. The mask is a <code>String</code> that may
    * contain special mask <code>char</code>s. A textual comparison
    * is performed; for each mask <code>char</code> present in
    * the mask <code>String</code>, the corresponding <code>char</code>
    * in the subject <code>String</code> will be ignored in
    * the comparsion.
    * 
    * @param     completeList    list of <code>String</code>s to search
    *            for matches
    * @param     mask            <code>String</code> containing any
    *            number of <code>maskChar</code>s
    * @param     maskChar        the masking <code>char</code> to use
    * @return    an array of <code>String</code>s that matched the
    *            mask pattern
    */
    public static String[] maskMatch(String[] completeList, String mask, char maskChar)
    {
        ArrayList<String> results = new ArrayList<String>();
        char[] maskC = mask.toCharArray();
        int shortestLength = maskC.length;
        for (String item:completeList)
        {
            boolean isMatch = true;
            char[] tokens = item.toCharArray();
            if (shortestLength > tokens.length) shortestLength = tokens.length;
            for (int c = 0; c < shortestLength; c++)
            {
                if (maskC[c] != maskChar)
                    if(maskC[c] != tokens[c])
                    {
                        isMatch = false;
                    }
            }
            if(isMatch) results.add(item);
        }
        return results.toArray(new String[0]);
    }
    
   /**
    * Returns a <code>String</code> consisting of the original <code>String</code>
    * repeated a certain number of times.
    * 
    * @param     original    the original <code>String</code>
    * @param     times       the number of times to repeat the original
    *            <code>String</code>
    * @return    the repeated <code>String</code>
    */
    public static String createRepeatedString(String original, int times)
    {
        StringBuffer buffer = new StringBuffer();
        for (int t = 1; t <= times; t++)
        {
            buffer.append(original);
        }
        return buffer.toString();
    }
    
   /**
    * Creates an HTML formatted <code>String</code> which uses HTML
    * tables to simulate multiline
    * text with tabs. Each tab (<code>'\t'</code>) is translated to
    * the start of a new table column <td>.
    * 
    * @param     textLines    the lines of text to convert to HTML.
    * @param     tabSize      the number of blank spaces to replace
    *            a tab with.
    * @return    an HTML-formatted <code>String</code>
    * @see       alignTabsOfLines(String, int)
    */
    public static String createTabbedMultilineHTML(String[] textLines, int tabSize)
    {
        StringBuffer finalText = new StringBuffer("<html><table cellspacing='0' cellpadding='0'>");
        for (int l = 0; l < textLines.length; l++)
        {
            finalText.append("<tr><td><span style='font-family:arial'>");
            finalText.append(textLines[l].replaceAll("\t", "</span></td><td>" + createRepeatedString("&nbsp", tabSize) + "</td><td><span style='font-family:arial'>"));
            finalText.append("</span></td></tr>");
        }
        finalText.append("</table></html>");
        return finalText.toString();
    }
    
    public static String createMultilineHTML(String[] textLines)
    {
        StringBuffer finalText = new StringBuffer("<html><body>");
        for (int l = 0; l < textLines.length; l++)
        {
            if (l > 0) finalText.append("<br/>");
            finalText.append(textLines[l]);
        }
        finalText.append("</body></html>");
        return finalText.toString();
    }
    
    public static String createMultilineHTML(String text)
    {
        StringBuffer finalText = new StringBuffer("<html><body><span style='font-size:1em;font-family:arial'>");
        finalText.append(text.replaceAll("\n", "<br>"));
        finalText.append("</span></body></html>");
        return finalText.toString();
    }
    
    public static String createTabbedHTMLLine(String textLine, int tabSize)
    {
        StringBuffer finalText = new StringBuffer("<table cellspacing='0' cellpadding='0'>");
        finalText.append("<tr><td><span style='font-family:arial'>");
        finalText.append(textLine.replaceAll("\t", "</span></td><td>" + createRepeatedString("&nbsp", tabSize) + "</td><td><span style='font-family:arial'>"));
        finalText.append("</span></td></tr>");
        finalText.append("</table>");
        return finalText.toString();
    }
    
   /**
    * Returns the number of times a target <code>String</code> occurs
    * inside another <code>String</code>.
    * 
    * @param     text      the text to search for the target
    * @param     target    the text to be found
    * @return    the number of times the target occurs
    */
    public static int countOccurences(String text, String target)
    {
        int c = -1;
        int s = 0;
        int p = 0;
        do
        {
            c++;
            p = text.indexOf(target, s);
            s = p+1;
            if (p!=-1 && s==text.length()) return c+1;
        } while (p != -1 && s < text.length());
        return c;
    }

    public static ArrayList<Integer> findOccurances(String text, String target)
    {
        ArrayList<Integer> v = new ArrayList<Integer>();
        int p = 0;
        do
        {
            p = text.indexOf(target, p);
            if (p > -1)
            {
                v.add(p);
                p++;
            }
        } while (p > -1 && p < text.length());
        return v;
    }


   /**
    * Returns the largest number of occurrences of a target <code>String</code>
    * found in any one <code>String</code> inside a <code>String</code> array.
    * 
    * @param     lines     the <code>String</code>s to search
    * @param     target    the <code>String</code> to locate
    * @return    the highest number of occurrences in any one element.
    * @see       findBatchOccurrences(int, String[], String)       
    */
    public static int findMultilineMaxOccurrences(String[] lines, String target)
    {
        int c = 0;
        for (String line:lines)
        {
            int ln = countOccurences(line, "\t");
            if (ln > c) c = ln;
        }
        return c;
    }
    
   /**
    * Returns an <code>int</code> array containing the indices at which
    * a target <code>String</code> is found
    * in each <code>String</code> of a <code>String</code> array. If
    * there are multiple occurrences, only the location of the first
    * occurrence is returned.
    * 
    * @param     start     the position in each line to start searching
    * @param     lines     the <code>String</code>s to search
    * @param     target    the <code>String</code> to search for
    * @return    an <code>int</code> array containing the indices at
    *            which the target was found.
    * @see       findBatchOccurrences(int[], String[], String)
    * @see       findMultilineMaxOccurrences(String[], String)
    */
    public static int[] findBatchOccurrences(int start, String[] lines, String target)
    {
        int[] finds = new int[lines.length];
        for (int c = 0; c < lines.length; c++)
        {
            finds[c] = lines[c].indexOf(target, start);
        }
        return finds;
    }
   
    /**
    * Returns an <code>int</code> array containing the indices at which
    * a target <code>String</code> is found
    * in each <code>String</code> of a <code>String</code> array. If
    * there are multiple occurrences, only the location of the first
    * occurrence is returned.
    * 
    * @param     starts    the positions within each line to start searching
    * @param     lines     the <code>String</code>s to search
    * @param     target    the <code>String</code> to search for
    * @return    an <code>int</code> array containing the indices at
    *            which the target was found.
    * @see       findBatchOccurrences(int, String[], String)
    * @see       findMultilineMaxOccurrences(String[], String)
    */
    public static int[] findBatchOccurrences(int[] starts, String[] lines, String target)
    {
        int[] finds = new int[lines.length];
        for (int c = 0; c < lines.length; c++)
        {
            finds[c] = lines[c].indexOf(target, starts[c]);
        }
        return finds;
    }
    
   /**
    * Returns an array of <code>StringBuffer</code>s created from an
    * array of <code>String</code>s.
    * 
    * @param     lines    an array of <code>String</code>s
    * @return    an array of <code>StringBuffer</code> objects
    */
    public static StringBuffer[] createBufferList(String[] lines)
    {
        StringBuffer[] list = new StringBuffer[lines.length];
        for(int line = 0; line < lines.length; line++)
        {
            list[line] = new StringBuffer(lines[line]);
        }
        return list;
    }
    
   /**
    * Replaces the first occurrence of a <code>String</code> inside
    * each line with the specified insertion text.
    * 
    * @param     lines        the <code>String</code>s to modify
    * @param     insertion    the text to insert
    * @param     oldText      the text to replace
    */
    public static void injectEachListLine(String[] lines, String insertion, String oldText)
    {
        for (int L = 0; L < lines.length; L++)
        {
            lines[L] = java.util.regex.Matcher.quoteReplacement(lines[L]).replaceFirst(java.util.regex.Matcher.quoteReplacement(oldText), insertion);
        }
    }
   
   /**
    * Replaces the first occurrence of a <code>String</code> inside
    * each line with the specified insertion text.
    * 
    * @param     lines        the <code>String</code>s to modify
    * @param     insertions   an array of <code>String</code>s to insert, one for each item in <code>lines</code>
    * @param     oldText      the text to replace
    */
    public static void injectEachListLine(String[] lines, String[] insertions, String oldText)
    {
        for (int L = 0; L < lines.length; L++)
        {
            lines[L] = java.util.regex.Matcher.quoteReplacement(lines[L]).replaceFirst(java.util.regex.Matcher.quoteReplacement(oldText), insertions[L]);
        }
    }
    
    public static String left(String str, int chars)
    {
        return str.substring(0, chars);
    }
    
    public static String right(String str, int chars)
    {
        return str.substring(str.length()-chars, str.length());
    }

    public static String replaceAny(String source, String[] targets, String replacement)
    {
        String result = source;
        for (int t = 0; t < targets.length; t++)
        {
            result = result.replaceAll(targets[t], replacement);
        }
        return result;
    }

    public static String[] getWords(String text)
    {
        StringBuffer alphaOnly = new StringBuffer(text.length());
        for (int c = 0; c < text.length(); c++)
        {
            if (Character.isLetter(text.charAt(c)))
                alphaOnly.append(Character.toLowerCase(text.charAt(c)));
            else
                alphaOnly.append(" ");
        }
        String alphaString = alphaOnly.toString();
        while (alphaString.contains("  "))
        {
            alphaString = alphaString.replaceAll("  ", " ");
        }
        return alphaString.split(" ");
    }

    public static boolean containsAll(String source, String[] targets)
    {
        boolean containsAll = true;
        for (int s = 0; s < targets.length; s++)
        {
            if (!source.contains(targets[s]))
                containsAll = false;
        }
        return containsAll;
    }

    public static String padLeft(String text, int newLength, char padChr)
    {
        int padLength = newLength - text.length();
        StringBuffer buffer = new StringBuffer();
        for (int c = 0; c < padLength; c++)
        {
            buffer.append(padChr);
        }
        return buffer + text;
    }

    public static String padRight(String text, int newLength, char padChr)
    {
        int padLength = newLength - text.length();
        StringBuffer buffer = new StringBuffer();
        for (int c = 0; c < padLength; c++)
        {
            buffer.append(padChr);
        }
        return text + buffer;
    }

}