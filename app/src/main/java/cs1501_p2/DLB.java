/**
 * DLB structure class for CS1501 Project 2
 * @author  Arsenii Bobovnikov
 */
package cs1501_p2;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class DLB implements Dict {

    private static final char TERMINATOR = '\0';

    private DLBNode root = new DLBNode('ъ');
    private int itemCount = 0;
    private String searchingByChar = "";
    private int maxKeyLength = 0;

    public DLB() {
        this.root = new DLBNode('ъ');
        this.itemCount = 0;
        this.searchingByChar = "";
        this.maxKeyLength = 0;
    }

    public DLB(String path) {
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                add(scanner.nextLine());
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Error reading dictionary file");
            System.out.println(e.getMessage());
            this.root = new DLBNode('ъ');
            this.itemCount = 0;
            this.searchingByChar = "";
            this.maxKeyLength = 0;
        }
    }

    public void add(String key) {
        DLBNode currNode = root;
        for (int i = 0; i < key.length(); i++) {
            char currChar = key.charAt(i);
            DLBNode match = findMatch(currNode, currChar);
            if (match == null) {
                match = insert(currNode, currChar);
            }
            currNode = match;
        }

        if (findMatch(currNode, TERMINATOR) != null) return; // duplicate found
        insert(currNode, TERMINATOR);
        itemCount++;
        if (key.length() > maxKeyLength) maxKeyLength = key.length();
    }

    /**
     * Searches for a sibling character in the node's child's linked list
     * @param node node to begin with
     * @param c character to be found
     * @return returns a node representing this character, null if such a node doesn't exist
     */
    private DLBNode findMatch(DLBNode node, char c) {
        DLBNode curr = node.getDown();
        while (curr != null && curr.getLet() < c) {
            curr = curr.getRight();
        }
        return (curr != null && curr.getLet() == c) ? curr : null;
    }

    /**
     * Follows the path spelled by s from the root
     * @param s string to follow
     * @return the node for the last character of s (the root for an empty string),
     *         or null if the path does not exist
     */
    private DLBNode findNode(String s) {
        DLBNode curr = root;
        for (int i = 0; i < s.length() && curr != null; i++) {
            curr = findMatch(curr, s.charAt(i));
        }
        return curr;
    }

    /**
     * Inserts a character into the DLB; inserts into linked list, preserving its order
     * @param node node to begin with
     * @param c character to be inserted
     * @return returns a node representing inserted character
     */
    private DLBNode insert(DLBNode node, char c) {
        DLBNode newNode = new DLBNode(c);
        DLBNode prev = null;
        DLBNode curr = node.getDown();

        while (curr != null && curr.getLet() < c) {
            prev = curr;
            curr = curr.getRight();
        }

        newNode.setRight(curr);
        if (prev == null) node.setDown(newNode); else prev.setRight(newNode);
        return newNode;
    }

    public boolean contains(String key) {
        DLBNode node = findNode(key);
        return node != null && findMatch(node, TERMINATOR) != null;
    }

    public boolean containsPrefix(String pre) {
        DLBNode node = findNode(pre);
        if (node == null || node.getDown() == null) return false;
        DLBNode first = node.getDown();
        return first.getLet() != TERMINATOR || first.getRight() != null;
    }

    public int searchByChar(char next) {
        searchingByChar += next;
        boolean isWord = contains(searchingByChar);
        boolean isPrefix = containsPrefix(searchingByChar);
        if (!isWord && !isPrefix) return -1;
        else if (!isWord) return 0;
        else if (!isPrefix) return 1;
        else return 2;
    }

    public void resetByChar() {
        searchingByChar = "";
    }

    public ArrayList<String> suggest() {
        ArrayList<String> output = new ArrayList<String>();
        DLBNode node = findNode(searchingByChar);
        if (node == null) return output;
        char[] buildString = new char[maxKeyLength];
        for (int i = 0; i < searchingByChar.length(); i++) {
            buildString[i] = searchingByChar.charAt(i);
        }
        traverseRec(node.getDown(), buildString, searchingByChar.length(), output);
        return output;
    }

    public ArrayList<String> traverse() {
        char[] buildString = new char[maxKeyLength];
        ArrayList<String> output = new ArrayList<String>();
        traverseRec(root.getDown(), buildString, 0, output);
        return output;
    }

    /**
     * Recursive helper method to loop through the DLB trie
     * @param start first node of the sibling list to walk
     * @param buildString char array to build a string from characters extracted from the DLB
     * @param pos current insert position into buildString
     * @param output ArrayList of extracted words
     */
    private void traverseRec(DLBNode start, char[] buildString, int pos, ArrayList<String> output) {
        for (DLBNode curr = start; curr != null; curr = curr.getRight()) {
            if (curr.getLet() == TERMINATOR) {
                output.add(new String(buildString, 0, pos));
            } else {
                buildString[pos] = curr.getLet();
                traverseRec(curr.getDown(), buildString, pos + 1, output);
            }
        }
    }

    public int count() {
        return itemCount;
    }
}
