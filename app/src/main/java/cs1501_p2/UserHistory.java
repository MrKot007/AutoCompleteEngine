/**
 * UserHistory class for CS1501 Project 2
 * @author  Arsenii Bobovnikov
 */
package cs1501_p2;

import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;    
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class UserHistory implements Dict {

    private static final char TERMINATOR = '\0';

    private class CountNode extends DLBNode {
        int count;

        CountNode(int count) {
            super(TERMINATOR);
            this.count = count;
        }
    }
 
    private HeapNode[] heap;
    private int heapSize;
    private DLBNode root;
    private int itemCount;
    private String searchingByChar;
    private int maxKeyLength;

    public UserHistory() {
        clear();
    }

    public UserHistory(String path) {
        this();
        try {
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int colon = line.lastIndexOf(':');
                if (colon < 0) continue;
                String val = line.substring(0, colon);
                int count = Integer.parseInt(line.substring(colon + 1).trim());
                addWithCount(val, count);
            }
            scanner.close();
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading user history file");
            clear();
        }
    }

    /**
     * reset the DLB
     */
    private void clear() {
        this.root = new DLBNode('ъ');
        this.itemCount = 0;
        this.searchingByChar = "";
        this.maxKeyLength = 0;
        this.heap = new HeapNode[16];
        this.heapSize = 0;
    }

    public void add(String key) {
        addWithCount(key, 1); 
    }

    /**
     * Adds key to the history, increasing its count by amount
     * @param key the word
     * @param amount how many times used
     */
    private void addWithCount(String key, int amount) {       
        DLBNode currNode = root;
        for (int i = 0; i < key.length(); i++) {
            char currChar = key.charAt(i);
            DLBNode match = findMatch(currNode, currChar);
            if (match == null) {
                match = insert(currNode, new DLBNode(currChar));
            }
            currNode = match;
        }

        DLBNode term = findMatch(currNode, TERMINATOR);
        if (term != null) {
            ((CountNode) term).count += amount; // seen before: just bump its count
            return;
        }
        insert(currNode, new CountNode(amount));
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
     * @return the node for the last character of s (the root for an empty string),
     *         or null if the path does not exist
     */
    private DLBNode findNode(String s) {
        DLBNode currNode = root;
        for (int i = 0; i < s.length() && currNode != null; i++) {
            currNode = findMatch(currNode, s.charAt(i));
        }
        return currNode;
    }

    /**
     * Inserts a node into the parent's child list, preserving its order
     * @param node parent whose child list receives the new node
     * @param newNode node to insert (a plain DLBNode or a CountNode)
     * @return the inserted node
     */
    private DLBNode insert(DLBNode node, DLBNode newNode) {
        char c = newNode.getLet();
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

        heapSize = 0; // start from an empty heap on every call
        collectRec(node.getDown(), buildString, searchingByChar.length());
        while (output.size() < 5 && heapSize > 0) {
            output.add(getMaxElement().getVal());
        }
        heapSize = 0;
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

    /**
     * Same traversal as in traverse(), but this time adding elements to the heap
     * @param start node to begin with
     * @param buildString building a string when extracting from the trie
     * @param pos current insert position into buildString
     */
    private void collectRec(DLBNode start, char[] buildString, int pos) {
        for (DLBNode curr = start; curr != null; curr = curr.getRight()) {
            if (curr.getLet() == TERMINATOR) {
                heapInsert(new HeapNode(new String(buildString, 0, pos), ((CountNode) curr).count));
            } else {
                buildString[pos] = curr.getLet();
                collectRec(curr.getDown(), buildString, pos + 1);
            }
        }
    }

    public int count() {
        return itemCount;
    }

    /**
     * Writes every word and its count to fname, one "word:count" per line.
     * @param fname
     * name of the user history file
     */
    public void save(String fname) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fname))) {
            saveRec(root.getDown(), new char[maxKeyLength], 0, out);
        } catch (IOException e) {
            System.out.println("Error writing user history file");
        }
    }

    /**
     * DLB traversal with save to the file
     * @param start 
     * @param buildString
     * @param pos
     * @param out
     */
    private void saveRec(DLBNode start, char[] buildString, int pos, PrintWriter out) {
        for (DLBNode curr = start; curr != null; curr = curr.getRight()) {
            if (curr.getLet() == TERMINATOR) {
                out.println(new String(buildString, 0, pos) + ":" + ((CountNode) curr).count);
            } else {
                buildString[pos] = curr.getLet();
                saveRec(curr.getDown(), buildString, pos + 1, out);
            }
        }
    }

    // HEAP METHODS

    private int getParentIndex(int pos) {
        return (pos - 1) / 2;
    }

    private int getLeftChildIndex(int pos) {
        return (2 * pos) + 1;
    }

    private int getRightChildIndex(int pos) {
        return (2 * pos) + 2;
    }

    private boolean higher(HeapNode a, HeapNode b) {
        if (a.getCount() != b.getCount()) return a.getCount() > b.getCount();
        return a.getVal().compareTo(b.getVal()) < 0;
    }

    private void swap(int pos1, int pos2) {
        HeapNode temp = heap[pos1];
        heap[pos1] = heap[pos2];
        heap[pos2] = temp;
    }

    /**
     * Maintains heap property of a subtree
     * @param pos position of the starting point
     */
    private void heapify(int pos) {
        int largest = pos;
        int left = getLeftChildIndex(pos);
        int right = getRightChildIndex(pos);
        if (left < heapSize && higher(heap[left], heap[largest])) largest = left;
        if (right < heapSize && higher(heap[right], heap[largest])) largest = right;
        if (largest != pos) {
            swap(pos, largest);
            heapify(largest);
        }
    }

    private void heapInsert(HeapNode element) {
        if (heapSize == heap.length) {
            HeapNode[] bigger = new HeapNode[heap.length * 2];
            for (int i = 0; i < heapSize; i++) {
                bigger[i] = heap[i];
            }
            heap = bigger;
        }
        heap[heapSize] = element;
        int curr = heapSize;
        heapSize++;
        while (curr > 0 && higher(heap[curr], heap[getParentIndex(curr)])) {
            swap(curr, getParentIndex(curr));
            curr = getParentIndex(curr);
        }
    }

    /**
     * Removal and acquisition of the root
     */
    private HeapNode getMaxElement() {                                  
        if (heapSize == 0) return null;
        HeapNode el = heap[0];
        heapSize--;
        heap[0] = heap[heapSize];
        heap[heapSize] = null;
        if (heapSize > 0) heapify(0);
        return el;
    }
}
