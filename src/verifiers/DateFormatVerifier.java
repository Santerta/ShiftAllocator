package verifiers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to verify format of given date.
 * @author Santeri Tammisto
 * @version 10.6.2023
 *
 */
public class DateFormatVerifier {

    /**
     * Default builder
     */
    public DateFormatVerifier() {
        //
    }
    
    /**
     * Checks that the given string is in the format YYYY-MM-DD
     * @param stringToVerify The string to be checked
     * @return null if everything is fine, an error if there is a mistake
     */
    public String verify(String stringToVerify) {
        Pattern p = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$");
        Matcher m = p.matcher(stringToVerify);
        boolean b = m.matches();
        
        if(b == false) return "Provide the date in format YYYY-MM-DD";
        
        return null;
    }
    
}
