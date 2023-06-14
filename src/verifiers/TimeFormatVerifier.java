package verifiers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to verify format of given time.
 * @author Santeri Tammisto
 * @version 10.6.2023
 *
 */
public class TimeFormatVerifier {

    /**
     * Default builder
     */
    public TimeFormatVerifier() {
        //
    }
    
    /**
     * Checks if the given string is in the format "xx:xx" and within the range of 00:00-23:59.
     * @param stringToVerify The string to be checked.
     * @return null if everything is fine, an error if there is a mistake.
     */
    public String verify(String stringToVerify) {
        Pattern p = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
        Matcher m = p.matcher(stringToVerify);
        boolean b = m.matches();
        
        if(b == false) return "Provide the time in the format of XX:XX";
        
        return null;
    }
}
