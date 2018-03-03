package cs455.scaling.util;


public class CheckInteger {
    private static CheckInteger checkInteger = null;

    private CheckInteger(){}

    public static boolean isInteger( String input ) {
        if(checkInteger == null) {
            checkInteger = new CheckInteger();
        }
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }
}
