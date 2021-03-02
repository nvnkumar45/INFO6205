/**
 * Original code:
 * Copyright © 2000–2017, Robert Sedgewick and Kevin Wayne.
 * <p>
 * Modifications:
 * Copyright (c) 2017. Phasmid Software
 */
package edu.neu.coe.info6205.union_find;

import edu.neu.coe.info6205.util.Benchmark_Timer;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Depth-weighted Quick Union
 */
public class Depth_WQU implements UF {

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
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public Depth_WQU(int n) {
        count = n;
        parent = new int[n];
        depth = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            depth[i] = 1;
        }

    }


    public void show() {
        for (int i = 0; i < parent.length; i++) {
            System.out.printf("%d: %d, %d\n", i, parent[i], depth[i]);
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
         while (root != parent[root])
                root = parent[root];
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

    private void updateDepth(int p, int x) {
        depth[p] += depth[x];
    }

    private final int[] parent;   // parent[i] = parent of i
    private final int[] depth;   // Depth[i] = Depth of subtree rooted at i
    private int count;  // number of components

    private void mergeComponents(int i, int j) {
        if(depth[i]< depth[j]){  //checking which Depth is taller
            updateParent(i,j);
            updateDepth(j,i);
        }
        else{
            updateParent(j,i);
            updateDepth(i,j);
        }
    }


    public static void performDWQU(int n){
        Random random = new Random();
        Depth_WQU obj = new Depth_WQU(n);
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

            // Benchmarking Depth weighted quick union
            Benchmark_Timer<Integer[]> timer1 = new Benchmark_Timer<>("Depth weighted quick union",null,(xs) -> performDWQU(finalN),null);
            double mean_time1 = timer1.runFromSupplier(supplier, 50);
            System.out.println("DWQU n= "+n+" 50 reps: " + mean_time1 + " millisecs");

            n*=2;
        }
    }

}
