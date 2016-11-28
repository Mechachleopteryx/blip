package ch.idsia.ipp.core.learn.solver.samp;

public class MIBSampler extends MISampler {

    protected double[] weight_r;

    int turn;

    @Override
    public int[] sample() {

        if (turn == 0) {
            turn += 1;
            return sampleWeighted(n, r, weight);
        } else if (turn == 1){
            turn += 1;
            return  sampleWeighted(n, r, weight_r);
        } else {
            turn = 0;
            return sample();
        }

    }

    public MIBSampler(String ph_dat, int n) {
        super(ph_dat, n);
    }

    @Override
    public void init() {
        super.init();

        weight_r = new double[n];
        for (int i = 0; i < n; i++)
            weight_r[i] = 1.0 / weight[i];
    }
}