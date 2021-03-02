/**
 * Original code:
 * Copyright © 2000–2017, Robert Sedgewick and Kevin Wayne.
 * <p>
 * Modifications:
 * Copyright (c) 2017. Phasmid Software
 */
package edu.neu.coe.info6205.union_find;

import edu.neu.coe.info6205.util.Benchmark_Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Weighted Quick Union with Path Compression
 */
public class MY_WQUPC implements UF {

    /**
     * Ensure that site p is connected to site q,
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     */
    public void connect(int p, int q) {
        if (!isConnected(p, q)) union(p, q);
    }

    /**
     * Initializes an empty union–find data structure with {@code n} sites
     * {@code 0} through {@code n-1}. Each site is initially in its own
     * component.
     *
     * @param n               the number of sites
     * @param path_halving    whether to perform path halving while path compression
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public MY_WQUPC(int n,boolean path_halving) {
        count = n;
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        this.path_halving =  path_halving;
    }

    public void show() {
        for (int i = 0; i < parent.length; i++) {
            System.out.printf("%d: %d, %d\n", i, parent[i], size[i]);
        }
    }

    /**
     * Returns the number of components.
     *
     * @return the number of components (between {@code 1} and {@code n})
     */
    public int components() {
        return count;
    }

    /**
     * Returns the component identifier for the component containing site {@code p}.
     *
     * @param p the integer representing one site
     * @return the component identifier for the component containing site {@code p}
     * @throws IllegalArgumentException unless {@code 0 <= p < n}
     */
    public int find(int p) {
        validate(p);
        int root = p;

        if(path_halving)
            root = doPathHalving(root);        // finding the root and performing path compression by linking the alternatives
        else{                                  // Just traversing and finding the root and then path compression
            while (root != parent[root])
                root = parent[root];
            while (p != root) {
                int newp = parent[p];
                parent[p] = root;
                p = newp;
            }
        }
        return root;
    }

    /**
     * Returns true if the the two sites are in the same component.
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @return {@code true} if the two sites {@code p} and {@code q} are in the same component;
     * {@code false} otherwise
     * @throws IllegalArgumentException unless
     *                                  both {@code 0 <= p < n} and {@code 0 <= q < n}
     */
    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }

    /**
     * Merges the component containing site {@code p} with the
     * the component containing site {@code q}.
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @throws IllegalArgumentException unless
     *                                  both {@code 0 <= p < n} and {@code 0 <= q < n}
     */
    public void union(int p, int q) {
        // CONSIDER can we avoid doing find again?
        mergeComponents(find(p), find(q));
        count--;
    }

    @Override
    public int size() {
        return parent.length;
    }

    /**
     * Used only by testing code
     *
     * @param path_halving true if you want path compression via path halving
     */
    public void setPathHalving(boolean path_halving) {
        this.path_halving = path_halving;
    }

    @Override
    public String toString() {
        return "MY_WQUPC:" + "\n  count: " + count +
                "\n  path halving? " + path_halving +
                "\n  parents: " + Arrays.toString(parent) +
                "\n  heights: " + Arrays.toString(size);
    }

    // validate that p is a valid index
    private void validate(int p) {
        int n = parent.length;
        if (p < 0 || p >= n) {
            throw new IllegalArgumentException("index " + p + " is not between 0 and " + (n - 1));
        }
    }

    private void updateParent(int p, int x) {
        parent[p] = x;
    }

    private void updateSize(int p, int x) {
        size[p] += size[x];
    }

    private final int[] parent;   // parent[i] = parent of i
    private final int[] size;   // size[i] = size of subtree rooted at i
    private int count;  // number of components
    private boolean path_halving;

    private void mergeComponents(int i, int j) {
        if(size[i]<size[j]){  //checking which height is taller
            updateParent(i,j);
            updateSize(j,i);
        }
        else{
            updateParent(j,i);
            updateSize(i,j);
        }
    }

    /**
     * This implements the single-pass path-halving mechanism of path compression
     */
    private int doPathHalving(int i) {
        while(i != parent[i]) {
            parent[i] = parent[parent[i]];    // update parent to grandparent =>  path compression
            i = parent[i];                    // Finding the next node
        }
        return i;
    }
    public static void performWQUPC(int n,boolean path_halving){
        Random random = new Random();
        MY_WQUPC obj = new MY_WQUPC(n,path_halving);
        while(true){
            int first = random.nextInt(n);
            int second = random.nextInt(n);

            if(!obj.connected(first,second)){
                obj.union(first,second);
            }
            if(obj.components() == 1) break;
        }

    }

    public static void main(String[] args){
        int n = 100000;
        for(int i = 0; i<5; i++ ){

            System.out.println("------------- n = "+n+"--------------------");

            int finalN = n;
            Integer[] dummy = new Integer[1];
            Supplier supplier= () -> dummy;

            // Benchmarking WQUPC using path halving method
            Benchmark_Timer<Integer[]> timer1 = new Benchmark_Timer<>("Path Halving",null,(xs) -> performWQUPC(finalN,true),null);
            double mean_time1 = timer1.runFromSupplier(supplier, 50);
            System.out.println("WQUPC - one pass n= "+n+" 50 reps: " + mean_time1 + " millisecs");

            // Benchmarking WQUPC using 2 loop method
            Benchmark_Timer<Integer[]> timer2 = new Benchmark_Timer<>("Double Loop",null,(xs) -> performWQUPC(finalN,false),null);
            double mean_time2 = timer2.runFromSupplier(supplier, 50);
            System.out.println("WQUPC - double loop n= "+n+"  50 reps: " + mean_time2 + " millisecs");

            n*=2;
        }
    }
}
