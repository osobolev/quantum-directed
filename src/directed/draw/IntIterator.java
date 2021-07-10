package directed.draw;

import java.util.Iterator;

abstract class IntIterator implements Iterator<String> {

    private final int num;
    private int i = 0;

    protected IntIterator(int num) {
        this.num = num;
    }

    public boolean hasNext() {
        return i < num;
    }

    public String next() {
        String ret = getString(i);
        i++;
        return ret;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract String getString(int i);
}
