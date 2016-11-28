package ch.idsia.ipp.core.learn.solver.src;

import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.learn.solver.BaseSolver;
import ch.idsia.ipp.core.utils.Pair;
import ch.idsia.ipp.core.utils.ParentSet;
import ch.idsia.ipp.core.utils.data.SIntSet;
import ch.idsia.ipp.core.utils.data.array.TIntArrayList;

import java.util.*;

import static ch.idsia.ipp.core.utils.data.ArrayUtils.*;

public class BrutalOldGreedySearcher extends ObsSearcher {

    public int tw;

    public int[] vars;

    public BayesianNetwork new_bn;

    public ParentSet[] new_str;

    public double new_sk;

    // Near-maximal clique (size tw), for adding new variables
    protected TreeSet<SIntSet> handles;

    // Variables in the initial clique
    public int[] initCl;

    public BrutalOldGreedySearcher(BaseSolver solver, int tw) {
        super(solver);
        this.tw = tw;
    }

    // Greedily optimize a network!
    @Override
    public ParentSet[] search(int[] vars) {

        this.vars = vars;

        // clear all
        clear();

        // Init the first maximal clique
        initClique();

        Pair<ParentSet, SIntSet> res;

        // For every new variable, add a new maximal clique
        for (int i = tw + 1; i < n_var; i++) {
            int v = vars[i];

            // Search the best parent set given the handlers
            // (best parent set inside available quasi-maximal cliques)
            res = bestHandler(v);
            // update the chosen parent set
            update(v, res.getFirst());
            // add the new handlers
            // add new handler = new clique with size tw
            // created  just now
            SIntSet h = res.getSecond();
            for (int elim : h.set) {
                addHandler(new SIntSet(reduceAndIncreaseArray(h.set, v, elim)));
            }

            solver.checkTime();
            if (!solver.still_time) {
                return null;
            }

            /*
            BayesianNetwork best = new BayesianNetwork(new_str);
            best.writeGraph("/home/loskana/Desktop/what/" + i);
            solver.writeStructure("/home/loskana/Desktop/what/" + i, -10, new_str);

            try {
                best.checkTreeWidth(tw);
                best.checkAcyclic();
            } catch (Exception ex) {
                ex.printStackTrace();
                best.writeGraph("/home/loskana/Desktop/what/final");
            }*/

        }

        return new_str;
    }

    void initClique() {
        // Initial handler: best DAG with (1..tw+1) variables
        initCl = new int[tw+1];
        System.arraycopy(vars, 0, initCl, 0, tw +1);

        // Find best, do some ASOBS iterations
        ParentSet[] best_str = exploreAll();

        // Update parent se
        for (int v: initCl) {
            update(v, best_str[v]);
        }

        // Add new handlers
        for (int v : initCl) {
            addHandler(new SIntSet(reduceArray(initCl,v)));
        }
    }

    protected void addHandler(SIntSet sIntSet) {
        handles.add(sIntSet);
    }

    // DO ALL THE PERMUTATIONS!!
    private ParentSet[] exploreAll() {

        TIntArrayList init = new TIntArrayList(initCl.length);
        for (int i : initCl)
            init.add(i);

        double best_sk = -Double.MAX_VALUE;
        ParentSet[] best_str = null;

        Random r = new Random(System.currentTimeMillis());

        for (int i = 0; i < Math.pow(tw, 3); i++) {
            init.shuffle(r);
            // Search current
            super.search(init.toArray());
            //  p(Arrays.toString(initCl));

            if (last_sk > best_sk) {
                best_str = last_str;
                best_sk = last_sk;
            }
        }

        return best_str;
    }

    /*
    private ParentSet[] exploreAll() {

        Arrays.sort(initCl);
        super.search(initCl);
        double best_sk = last_sk;
        ParentSet[] best_str = last_str;

        int n = initCl.length;
        int[] p = new int[n];  // Weight index control array initially all zeros. Of course, same size of the char array.
        int i = 0; //Upper bound index. thread.e: if string is "abc" then index thread could be at "c"
        Arrays.sort(initCl);

        while (i < n) {
            if (p[i] < i) { //if the weight index is bigger or the same it means that we have already switched between these thread,j (one iteration before).
                int j = ((i % 2) == 0) ? 0 : p[i];//Lower bound index. thread.e: if string is "abc" then j index will always be 0.
                swap(initCl, i, j);
                // Search current
                super.search(initCl);
                //  p(Arrays.toString(initCl));
                if (last_sk > best_sk) {
                    best_str = last_str;
                    best_sk = last_sk;
                }
                p[i]++; //Adding 1 to the specific weight that relates to the char array.
                i = 1; //if thread was 2 (for example), after the swap we now need to swap for thread=1
            }
            else {
                p[i] = 0;//Weight index will be zero because one iteration before, it was 1 (for example) to indicate that char array a[thread] swapped.
                i++;//thread index will have the option to ex forward in the char array for "longer swaps"
            }
        }

        return best_str;
    } */

    /**
     * Override, add check that has to be in the initial clique
     */
    @Override
    protected boolean acceptable(int v, int[] ps, BitSet fb) {
        for (int p : ps) {
            if (!find(p, initCl))
                return false;
        }

        return super.acceptable(v, ps, fb);
    }

    protected void clear() {
        new_bn = new BayesianNetwork(n_var);
        new_str = new ParentSet[n_var];
        new_sk = 0;

        // List of handlers for parent sets
        // (list of quasi-maximal cliques, where a variable
        // can choose its parent sets)
        handles = new TreeSet<SIntSet>();
    }

    protected void update(int v, ParentSet ps) {
        new_bn.setParents(v, ps.parents);
        new_str[v] = ps;
        new_sk += ps.sk;
    }

    // Check if every element of ps is contained into h
    protected static boolean containsAll(int[] ps, int[] h) {
        for (int p : ps) {
            if (Arrays.binarySearch(h, p) < 0) {
                return false;
            }
        }

        return true;
    }

    protected Pair<ParentSet, SIntSet> bestHandler(int v) {

       //  p(m_scores[v][0]);

        for (ParentSet p : m_scores[v]) {

            for (SIntSet h : handles) {
                if (containsAll(p.parents, h.set)) {
                    return new Pair<ParentSet, SIntSet>(p, h);
                }
            }
        }

        return new Pair<ParentSet, SIntSet>(new ParentSet(), new SIntSet());
    }

}