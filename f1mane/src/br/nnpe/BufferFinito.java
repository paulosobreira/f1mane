package br.nnpe;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Sobreira
 * Criado em 30/03/2006
 */
public class BufferFinito {
    private List buff = new LinkedList();
    private int tamMax = 0;
    
    public BufferFinito(int numThreads) {
        this.tamMax=numThreads;
    }

    public BufferFinito() {
        tamMax = Integer.MAX_VALUE;
    }

    public int getTamMax() {
        return tamMax;
    }

    public void setTamMax(int tamMax) {
        this.tamMax = tamMax;
    }

    public synchronized void add(Object x) {
        while (isFull()) {
            try {
                wait();
                //Thread.currentThread().wait();
                //buff.wait();
            } catch (InterruptedException annoying) {
                //System.out.println("annoying" + annoying.getMessage());
            }
        }

        buff.add(x);
        notifyAll();
    }

    private boolean isFull() {
        return buff.size()>=tamMax;
    }

    public synchronized Object remove() {
        while (isEmpty()) {
            try {
                wait();
                //Thread.currentThread().wait();
                //buff.wait();
            } catch (InterruptedException annoying) {
                //System.out.println("annoying" + annoying.getMessage());
            }
        }

        notifyAll();

        return buff.remove(0);
    }

    private boolean isEmpty() {
        return buff.isEmpty();
    }
}
