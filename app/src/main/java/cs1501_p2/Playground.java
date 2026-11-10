package cs1501_p2;

import java.util.ArrayList;

public class Playground {
    public static void main(String[] args) {
        String dictFileName = "";
        AutoCompleter autoCompleter = new AutoCompleter(dictFileName);
        ArrayList<String> sugs = autoCompleter.nextChar('a');
        System.out.println(sugs.toString());
    }
}
