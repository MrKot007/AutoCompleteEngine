package cs1501_p2;

public class HeapNode {
    private int count;
    private String val;

    public HeapNode(String val, int count) {
        this.val = val;
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    public String getVal() {
        return this.val;
    }
}
