/*-
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;


/**
 * General string utils
 */
public class StringUtils {

    private static final DecimalFormat decimalFormat;
    static {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.applyPattern("#.##");
    }

    private StringUtils() {}

    /**
     * Make a string representation of the exception.
     * @param e The exception to stringify
     * @return A string with exception name and call stack.
     */
    public static String stringifyException(Throwable e) {
        StringWriter stm = new StringWriter();
        PrintWriter wrt = new PrintWriter(stm);
        e.printStackTrace(wrt);
        wrt.close();
        return stm.toString();
    }

    /**
     * Given a full hostname, return the word upto the first dot.
     * @param fullHostname the full hostname
     * @return the hostname to the first dot
     */
    public static String simpleHostname(String fullHostname) {
        int offset = fullHostname.indexOf('.');
        if (offset != -1) {
            return fullHostname.substring(0, offset);
        }
        return fullHostname;
    }

    private static DecimalFormat oneDecimal = new DecimalFormat("0.0");

    /**
     * Given an integer, return a string that is in an approximate, but human
     * readable format.
     * @param number the number to format
     * @return a human readable form of the integer
     *
     * @deprecated use {@link TraditionalBinaryPrefix#long2String(long, String, int)}.
     */
    @Deprecated
    public static String humanReadableInt(long number) {
        return TraditionalBinaryPrefix.long2String(number, "", 1);
    }

    /** The same as String.format(Locale.ENGLISH, format, objects). */
    public static String format(final String format, final Object... objects) {
        return String.format(Locale.ENGLISH, format, objects);
    }

    /**
     * Format a percentage for presentation to the user.
     * @param done the percentage to format (0.0 to 1.0)
     * @param digits the number of digits past the decimal point
     * @return a string representation of the percentage
     */
    public static String formatPercent(double done, int digits) {
        DecimalFormat percentFormat = new DecimalFormat("0.00%");
        double scale = Math.pow(10.0, digits + 2);
        double rounded = Math.floor(done * scale);
        percentFormat.setDecimalSeparatorAlwaysShown(false);
        percentFormat.setMinimumFractionDigits(digits);
        percentFormat.setMaximumFractionDigits(digits);
        return percentFormat.format(rounded / scale);
    }

    /**
     * Given an array of strings, return a comma-separated list of its elements.
     * @param strs Array of strings
     * @return Empty string if strs.length is 0, comma separated list of strings
     * otherwise
     */

    public static String arrayToString(String[] strs) {
        if (strs.length == 0) {
            return "";
        }
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(strs[0]);
        for (int idx = 1; idx < strs.length; idx++) {
            sbuf.append(",");
            sbuf.append(strs[idx]);
        }
        return sbuf.toString();
    }

    /**
     * Given an array of bytes it will convert the bytes to a hex string
     * representation of the bytes
     * @param bytes
     * @param start start index, inclusively
     * @param end end index, exclusively
     * @return hex string representation of the byte array
     */
    public static String byteToHexString(byte[] bytes, int start, int end) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes == null");
        }
        StringBuilder s = new StringBuilder();
        for (int i = start; i < end; i++) {
            s.append(String.format("%02x", bytes[i]));
        }
        return s.toString();
    }

    /** Same as byteToHexString(bytes, 0, bytes.length). */
    public static String byteToHexString(byte bytes[]) {
        return byteToHexString(bytes, 0, bytes.length);
    }

    /**
     * Given a hexstring this will return the byte array corresponding to the
     * string
     * @param hex the hex String array
     * @return a byte array that is a hex string representation of the given
     *         string. The size of the byte array is therefore hex.length/2
     */
    public static byte[] hexStringToByte(String hex) {
        byte[] bts = new byte[hex.length() / 2];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bts;
    }

    /**
     *
     * @param uris
     */
    public static String uriToString(URI[] uris) {
        if (uris == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(uris[0].toString());
        for (int i = 1; i < uris.length; i++) {
            ret.append(",");
            ret.append(uris[i].toString());
        }
        return ret.toString();
    }

    /**
     *
     * @param str
     */
    public static URI[] stringToURI(String[] str) {
        if (str == null)
            return null;
        URI[] uris = new URI[str.length];
        for (int i = 0; i < str.length; i++) {
            try {
                uris[i] = new URI(str[i]);
            } catch (URISyntaxException ur) {
                System.out.println("Exception in specified URI's " + StringUtils.stringifyException(ur));
                //making sure its assigned to null in case of an error
                uris[i] = null;
            }
        }
        return uris;
    }


    /**
     *
     * Given a finish and start time in long milliseconds, returns a
     * String in the format Xhrs, Ymins, Z sec, for the time difference between two times.
     * If finish time comes before start time then negative valeus of X, Y and Z wil return.
     *
     * @param finishTime finish time
     * @param startTime start time
     */
    public static String formatTimeDiff(long finishTime, long startTime) {
        long timeDiff = finishTime - startTime;
        return formatTime(timeDiff);
    }

    /**
     *
     * Given the time in long milliseconds, returns a
     * String in the format Xhrs, Ymins, Z sec.
     *
     * @param timeDiff The time difference to format
     */
    public static String formatTime(long timeDiff) {
        StringBuilder buf = new StringBuilder();
        long hours = timeDiff / (60 * 60 * 1000);
        long rem = (timeDiff % (60 * 60 * 1000));
        long minutes = rem / (60 * 1000);
        rem = rem % (60 * 1000);
        long seconds = rem / 1000;

        if (hours != 0) {
            buf.append(hours);
            buf.append("hrs, ");
        }
        if (minutes != 0) {
            buf.append(minutes);
            buf.append("mins, ");
        }
        // return "0sec if no difference
        buf.append(seconds);
        buf.append("sec");
        return buf.toString();
    }

    /**
     * Formats time in ms and appends difference (finishTime - startTime)
     * as returned by formatTimeDiff().
     * If finish time is 0, empty string is returned, if start time is 0
     * then difference is not appended to return value.
     * @param dateFormat date format to use
     * @param finishTime fnish time
     * @param startTime start time
     * @return formatted value.
     */
    public static String getFormattedTimeWithDiff(DateFormat dateFormat, long finishTime, long startTime) {
        StringBuilder buf = new StringBuilder();
        if (0 != finishTime) {
            buf.append(dateFormat.format(new Date(finishTime)));
            if (0 != startTime) {
                buf.append(" (" + formatTimeDiff(finishTime, startTime) + ")");
            }
        }
        return buf.toString();
    }

    /**
     * Returns an arraylist of strings.
     * @param str the comma separated string values
     * @return the arraylist of the comma separated string values
     */
    public static String[] getStrings(String str) {
        Collection<String> values = getStringCollection(str);
        if (values.isEmpty()) {
            return null;
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Returns a collection of strings.
     * @param str comma separated string values
     * @return an <code>ArrayList</code> of string values
     */
    public static Collection<String> getStringCollection(String str) {
        List<String> values = new ArrayList<>();
        if (str == null)
            return values;
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken());
        }
        return values;
    }

    /**
     * Splits a comma separated value <code>String</code>, trimming leading and trailing whitespace on each value.
     * @param str a comma separated <String> with values
     * @return a <code>Collection</code> of <code>String</code> values
     */
    public static Collection<String> getTrimmedStringCollection(String str) {
        return Arrays.asList(getTrimmedStrings(str));
    }

    /**
     * Splits a comma separated value <code>String</code>, trimming leading and trailing whitespace on each value.
     * @param str a comma separated <String> with values
     * @return an array of <code>String</code> values
     */
    public static String[] getTrimmedStrings(String str) {
        if (null == str || "".equals(str.trim())) {
            return emptyStringArray;
        }

        return str.trim().split("\\s*,\\s*");
    }

    final public static String[] emptyStringArray = {};
    final public static char COMMA = ',';
    final public static char ESCAPE_CHAR = '\\';

    /**
     * Split a string using the default separator
     * @param str a string that may have escaped separator
     * @return an array of strings
     */
    public static String[] split(String str) {
        return split(str, ESCAPE_CHAR, COMMA);
    }

    /**
     * Split a string using the given separator
     * @param str a string that may have escaped separator
     * @param escapeChar a char that be used to escape the separator
     * @param separator a separator char
     * @return an array of strings
     */
    public static String[] split(String str, char escapeChar, char separator) {
        if (str == null) {
            return null;
        }
        ArrayList<String> strList = new ArrayList<>();
        StringBuilder split = new StringBuilder();
        int index = 0;
        while ((index = findNext(str, separator, escapeChar, index, split)) >= 0) {
            ++index; // move over the separator for next search
            strList.add(split.toString());
            split.setLength(0); // reset the buffer
        }
        strList.add(split.toString());
        // remove trailing empty split(s)
        int last = strList.size(); // last split
        while (--last >= 0 && "".equals(strList.get(last))) {
            strList.remove(last);
        }
        return strList.toArray(new String[strList.size()]);
    }

    /**
     * Finds the first occurrence of the separator character ignoring the escaped
     * separators starting from the index. Note the substring between the index
     * and the position of the separator is passed.
     * @param str the source string
     * @param separator the character to find
     * @param escapeChar character used to escape
     * @param start from where to search
     * @param split used to pass back the extracted string
     */
    public static int findNext(String str, char separator, char escapeChar, int start, StringBuilder split) {
        int numPreEscapes = 0;
        for (int i = start; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (numPreEscapes == 0 && curChar == separator) { // separator
                return i;
            } else {
                split.append(curChar);
                numPreEscapes = (curChar == escapeChar) ? (++numPreEscapes) % 2 : 0;
            }
        }
        return -1;
    }


    /**
     * This function splits the String s into multiple Strings using the
     * splitChar.  However, it provides an quoting facility: it is possible to
     * quote strings with the quoteChar.
     * If the quoteChar occurs within the quotedExpression, it must be prefaced
     * by the escapeChar
     *
     * @param s         The String to split
     * @param splitChar
     * @param quoteChar
     * @return An array of Strings that s is split into
     */
    public static String[] splitOnCharWithQuoting(String s, char splitChar, char quoteChar, char escapeChar) {
        List<String> result = new ArrayList<>();
        int i = 0;
        int length = s.length();
        StringBuilder b = new StringBuilder();
        while (i < length) {
            char curr = s.charAt(i);
            if (curr == splitChar) {
                // add last buffer
                if (b.length() > 0) {
                    result.add(b.toString());
                    b = new StringBuilder();
                }
                i++;
            } else if (curr == quoteChar) {
                // find next instance of quoteChar
                i++;
                while (i < length) {
                    curr = s.charAt(i);
                    if (curr == escapeChar) {
                        b.append(s.charAt(i + 1));
                        i += 2;
                    } else if (curr == quoteChar) {
                        i++;
                        break; // break this loop
                    } else {
                        b.append(s.charAt(i));
                        i++;
                    }
                }
            } else {
                b.append(curr);
                i++;
            }
        }
        if (b.length() > 0) {
            result.add(b.toString());
        }
        return result.toArray(new String[0]);
    }

    /**
     * Escape commas in the string using the default escape char
     * @param str a string
     * @return an escaped string
     */
    public static String escapeString(String str) {
        return escapeString(str, ESCAPE_CHAR, COMMA);
    }

    /**
     * Escape <code>charToEscape</code> in the string
     * with the escape char <code>escapeChar</code>
     *
     * @param str string
     * @param escapeChar escape char
     * @param charToEscape the char to be escaped
     * @return an escaped string
     */
    public static String escapeString(String str, char escapeChar, char charToEscape) {
        return escapeString(str, escapeChar, new char[] {charToEscape});
    }

    // check if the character array has the character
    private static boolean hasChar(char[] chars, char character) {
        for (char target : chars) {
            if (character == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param charsToEscape array of characters to be escaped
     */
    public static String escapeString(String str, char escapeChar, char[] charsToEscape) {
        if (str == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (curChar == escapeChar || hasChar(charsToEscape, curChar)) {
                // special char
                result.append(escapeChar);
            }
            result.append(curChar);
        }
        return result.toString();
    }

    /**
     * Unescape commas in the string using the default escape char
     * @param str a string
     * @return an unescaped string
     */
    public static String unEscapeString(String str) {
        return unEscapeString(str, ESCAPE_CHAR, COMMA);
    }

    /**
     * Unescape <code>charToEscape</code> in the string
     * with the escape char <code>escapeChar</code>
     *
     * @param str string
     * @param escapeChar escape char
     * @param charToEscape the escaped char
     * @return an unescaped string
     */
    public static String unEscapeString(String str, char escapeChar, char charToEscape) {
        return unEscapeString(str, escapeChar, new char[] {charToEscape});
    }

    /**
     * @param charsToEscape array of characters to unescape
     */
    public static String unEscapeString(String str, char escapeChar, char[] charsToEscape) {
        if (str == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(str.length());
        boolean hasPreEscape = false;
        for (int i = 0; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (hasPreEscape) {
                if (curChar != escapeChar && !hasChar(charsToEscape, curChar)) {
                    // no special char
                    throw new IllegalArgumentException(
                                    "Illegal escaped string " + str + " unescaped " + escapeChar + " at " + (i - 1));
                }
                // otherwise discard the escape char
                result.append(curChar);
                hasPreEscape = false;
            } else {
                if (hasChar(charsToEscape, curChar)) {
                    throw new IllegalArgumentException(
                                    "Illegal escaped string " + str + " unescaped " + curChar + " at " + i);
                } else if (curChar == escapeChar) {
                    hasPreEscape = true;
                } else {
                    result.append(curChar);
                }
            }
        }
        if (hasPreEscape) {
            throw new IllegalArgumentException(
                            "Illegal escaped string " + str + ", not expecting " + escapeChar + " in the end.");
        }
        return result.toString();
    }

    /**
     * Return hostname without throwing exception.
     * @return hostname
     */
    public static String getHostname() {
        try {
            return "" + InetAddress.getLocalHost();
        } catch (UnknownHostException uhe) {
            return "" + uhe;
        }
    }

    /**
     * Return a message for logging.
     * @param prefix prefix keyword for the message
     * @param msg content of the message
     * @return a message for logging
     */
    private static String toStartupShutdownString(String prefix, String[] msg) {
        StringBuilder b = new StringBuilder(prefix);
        b.append("\n/************************************************************");
        for (String s : msg)
            b.append("\n" + prefix + s);
        b.append("\n************************************************************/");
        return b.toString();
    }



    /**
     * The traditional binary prefixes, kilo, mega, ..., exa,
     * which can be represented by a 64-bit integer.
     * TraditionalBinaryPrefix symbol are case insensitive.
     */
    public enum TraditionalBinaryPrefix {
        KILO(10),
        MEGA(KILO.bitShift + 10),
        GIGA(MEGA.bitShift + 10),
        TERA(GIGA.bitShift + 10),
        PETA(TERA.bitShift + 10),
        EXA(PETA.bitShift + 10);

        public final long value;
        public final char symbol;
        public final int bitShift;
        public final long bitMask;

        private TraditionalBinaryPrefix(int bitShift) {
            this.bitShift = bitShift;
            this.value = 1L << bitShift;
            this.bitMask = this.value - 1L;
            this.symbol = toString().charAt(0);
        }

        /**
         * @return The TraditionalBinaryPrefix object corresponding to the symbol.
         */
        public static TraditionalBinaryPrefix valueOf(char symbol) {
            symbol = Character.toUpperCase(symbol);
            for (TraditionalBinaryPrefix prefix : TraditionalBinaryPrefix.values()) {
                if (symbol == prefix.symbol) {
                    return prefix;
                }
            }
            throw new IllegalArgumentException("Unknown symbol '" + symbol + "'");
        }

        /**
         * Convert a string to long.
         * The input string is first be trimmed
         * and then it is parsed with traditional binary prefix.
         * <p>
         * For example,
         * "-1230k" will be converted to -1230 * 1024 = -1259520;
         * "891g" will be converted to 891 * 1024^3 = 956703965184;
         *
         * @param s input string
         * @return a long value represented by the input string.
         */
        public static long string2long(String s) {
            s = s.trim();
            final int lastpos = s.length() - 1;
            final char lastchar = s.charAt(lastpos);
            if (Character.isDigit(lastchar))
                return Long.parseLong(s);
            else {
                long prefix;
                try {
                    prefix = TraditionalBinaryPrefix.valueOf(lastchar).value;
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid size prefix '" + lastchar
                            + "' in '" + s
                            + "'. Allowed prefixes are k, m, g, t, p, e(case insensitive)");
                }
                long num = Long.parseLong(s.substring(0, lastpos));
                if (num > (Long.MAX_VALUE / prefix) || num < (Long.MIN_VALUE / prefix)) {
                    throw new IllegalArgumentException(s + " does not fit in a Long");
                }
                return num * prefix;
            }
        }

        /**
         * Convert a long integer to a string with traditional binary prefix.
         *
         * @param n             the value to be converted
         * @param unit          The unit, e.g. "B" for bytes.
         * @param decimalPlaces The number of decimal places.
         * @return a string with traditional binary prefix.
         */
        public static String long2String(long n, String unit, int decimalPlaces) {
            if (unit == null) {
                unit = "";
            }
            //take care a special case
            if (n == Long.MIN_VALUE) {
                return "-8 " + EXA.symbol + unit;
            }

            final StringBuilder b = new StringBuilder();
            //take care negative numbers
            if (n < 0) {
                b.append('-');
                n = -n;
            }
            if (n < KILO.value) {
                //no prefix
                b.append(n);
                return (unit.isEmpty() ? b : b.append(" ").append(unit)).toString();
            } else {
                //find traditional binary prefix
                int i = 0;
                for (; i < values().length && n >= values()[i].value; i++) ;
                TraditionalBinaryPrefix prefix = values()[i - 1];

                if ((n & prefix.bitMask) == 0) {
                    //exact division
                    b.append(n >> prefix.bitShift);
                } else {
                    final String format = "%." + decimalPlaces + "f";
                    String s = format(format, n / (double) prefix.value);
                    //check a special rounding up case
                    if (s.startsWith("1024")) {
                        prefix = values()[i];
                        s = format(format, n / (double) prefix.value);
                    }
                    b.append(s);
                }
                return b.append(' ').append(prefix.symbol).append(unit).toString();
            }
        }
    }

    /**
     * Escapes HTML Special characters present in the string.
     * @param string
     * @return HTML Escaped String representation
     */
    public static String escapeHTML(String string) {
        if (string == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean lastCharacterWasSpace = false;
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (c == ' ') {
                if (lastCharacterWasSpace) {
                    lastCharacterWasSpace = false;
                    sb.append("&nbsp;");
                } else {
                    lastCharacterWasSpace = true;
                    sb.append(" ");
                }
            } else {
                lastCharacterWasSpace = false;
                switch (c) {
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '"':
                        sb.append("&quot;");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Return an abbreviated English-language desc of the byte length
     */
    public static String byteDesc(long len) {
        double val;
        String ending;
        if (len < 1024 * 1024) {
            val = (1.0 * len) / 1024;
            ending = " KB";
        } else if (len < 1024 * 1024 * 1024) {
            val = (1.0 * len) / (1024 * 1024);
            ending = " MB";
        } else if (len < 1024L * 1024 * 1024 * 1024) {
            val = (1.0 * len) / (1024 * 1024 * 1024);
            ending = " GB";
        } else if (len < 1024L * 1024 * 1024 * 1024 * 1024) {
            val = (1.0 * len) / (1024L * 1024 * 1024 * 1024);
            ending = " TB";
        } else {
            val = (1.0 * len) / (1024L * 1024 * 1024 * 1024 * 1024);
            ending = " PB";
        }
        return limitDecimalTo2(val) + ending;
    }

    public static synchronized String limitDecimalTo2(double d) {
        return decimalFormat.format(d);
    }

    /**
     * Concatenates strings, using a separator.
     *
     * @param separator Separator to join with.
     * @param strings Strings to join.
     */
    public static String join(CharSequence separator, Iterable<String> strings) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Concatenates strings, using whitespaces as separator.
     *
     * @param strings Strings to join.
     */
    public static String join(Iterable<String> strings) {
        return join(" ", strings);
    }

    /**
     * Concatenates stringified objects, using a separator.
     *
     * @param separator Separator to join with.
     * @param objects Objects to join.
     */
    public static String joinObjects(CharSequence separator, Iterable<? extends Object> objects) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object o : objects) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(String.valueOf(o));
        }
        return sb.toString();
    }

}
