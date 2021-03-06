package Util;

public class GetOpt {

    private String[] theArgs = null;
    private int argCount = 0;
    private String optString = null;

    public GetOpt(String[] args, String opts) {
        theArgs = args;
        argCount = theArgs.length;
        optString = opts;
    }
    
    public int processArg(String arg, int n) {
        int value;
        try {
            value = Integer.parseInt(arg);
        }
        catch (NumberFormatException e) {
            if (optErr)
                System.err.println("processArg cannot process " + arg //NOI18N
                                   + " as an integer"); //NOI18N
            return n;
        }
        return value;
    }
    
    public String processArg(String arg, String a) {
        String db;
        try {
            db = arg;
        }
        catch (NumberFormatException e) {
            if (optErr)
                System.err.println("processArg cannot process " + arg //NOI18N
                                   + " as an integer"); //NOI18N
            return a;
        }
        return db;
    }

    // user can toggle this to control printing of error messages
    public boolean optErr = false;
    
    private static void writeError(String msg, char ch) {
        System.err.println("GetOpt: " + msg + " -- " + ch); //NOI18N
    }

    public static final int optEOF = -1;

    private int optIndex = 0;

    public int optIndexGet() {
        return optIndex;
    }

    public void optIndexSet(int i) {
        optIndex = i;
    }

    private String optArg = null;

    public String optArgGet() {
        return optArg;
    }

    private int optPosition = 1;

    public int getopt() {
        optArg = null;
        if (theArgs == null || optString == null)
            return optEOF;
        if (optIndex < 0 || optIndex >= argCount)
            return optEOF;
        String thisArg = theArgs[optIndex];
        int argLength = thisArg.length();
        // handle special cases
        if (argLength <= 1 || thisArg.charAt(0) != '-') {
            // e.g., "", "a", "abc", or just "-"
            return optEOF;
        }
        else if (thisArg.equals("--")) {//NOI18N
            // end of non-option args
            optIndex++;
            return optEOF;
        }
        // get next "letter" from option argument
        char ch = thisArg.charAt(optPosition);
        // find this option in optString
        int pos = optString.indexOf(ch);
        if (pos == -1 || ch == ':') {
            if (optErr) {
                writeError("illegal option", ch); //NOI18N
            }
            ch = '?';
        }
        else { // handle colon, if present
            if (pos < optString.length() - 1 && optString.charAt(pos + 1) == ':') {
                if (optPosition != argLength - 1) {
                    // take rest of current arg as optArg
                    optArg = thisArg.substring(optPosition + 1);
                    optPosition = argLength - 1; // force advance to next arg below
                }
                else { // take next arg as optArg
                    optIndex++;
		    //System.out.println("O"+optIndex + "AC"+argCount + "Arr"+theArgs[optIndex] + "OS"+optString + "no" + optString.indexOf(theArgs[optIndex].charAt(1)));
                    if (optIndex < argCount
                            && (theArgs[optIndex].charAt(0) != '-' ||
                            theArgs[optIndex].length() >= 2 &&
                            (optString.indexOf(theArgs[optIndex].charAt(1)) == -1
                            || theArgs[optIndex].charAt(1) == ':'))) {
                        optArg = theArgs[optIndex];
                    }
                    else {
                        if (optErr) {
                            writeError("option requires an argument", ch); //NOI18N
                        }
                        optArg = null;
                        ch = ':'; // Linux man page for getopt(3) says : not ?
                    }
                }
            }
        }
        // advance to next option argument,
        // which might be in thisArg or next arg
        optPosition++;
        if (optPosition >= argLength) {
            optIndex++;
            optPosition = 1;
        }
        return ch;
    }
    
    }