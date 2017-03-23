package ch.idsia.blip.api.utils;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.utils.KTreeScore;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Creates n-version of the given file score, each one guaranteed to give at maximum tw treewidth
 */
public class KTreeScoreApi extends Api {

    private static final Logger log = Logger.getLogger(
            KTreeScoreApi.class.getName());

    @Option(name="-s", required = true, usage="Scores input file (in jkl format)")
    private String ph_scores;

    @Option(name="-w", required = true, usage="maximum treewidth")
    private int max_tw;

    @Option(name="-o", required = true, usage="path of reduced scores")
    private String ph_output;

    @Option(name="-m", required = true, usage="number of reduced scores to graph")
    private int num_outputs;

    public static void main(String[] args) {
        defaultMain(args, new KTreeScoreApi(), log);
    }

    @Override
    public void exec() throws IOException {

        KTreeScore k = new KTreeScore();
        k.seed = seed;
        k.init(max_tw, num_outputs, ph_output, ph_scores);
        k.go();
    }

}
