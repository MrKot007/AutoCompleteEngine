/**
 * Auto Completer class implementation for CS1501 Project 2
 * @author  Arsenii Bobobvnikov
 */
package cs1501_p2;

import java.util.ArrayList;

public class AutoCompleter implements AutoComplete_Inter {

    private DLB dictionary;
    private UserHistory userHistory;

    public AutoCompleter(String dictionary, String history) {
        this.dictionary = new DLB(dictionary);
        this.userHistory = new UserHistory(history);
    }

    public AutoCompleter(String dictionary) {
        this.dictionary = new DLB(dictionary);
        this.userHistory = new UserHistory();
    }

    public ArrayList<String> nextChar(char next) {

        dictionary.searchByChar(next);
        userHistory.searchByChar(next);

        ArrayList<String> result = userHistory.suggest();
        ArrayList<String> dictSugs = dictionary.suggest();
        for (int i = 0; i < dictSugs.size() && result.size() < 5; i++) {
            String word = dictSugs.get(i);
            if (!result.contains(word)) {
                result.add(word);
            }
        }
        return result;
    }

    public void finishWord(String cur) {
        userHistory.add(cur);
        dictionary.resetByChar();
        userHistory.resetByChar();
    }

    public void saveUserHistory(String fname) {
        userHistory.save(fname);
    }
}
