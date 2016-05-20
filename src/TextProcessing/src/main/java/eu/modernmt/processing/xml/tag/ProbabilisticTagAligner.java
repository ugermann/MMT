package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Tag;
import eu.modernmt.model.Translation;
import eu.modernmt.model.Word;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public abstract class ProbabilisticTagAligner {

    protected final Translation translation;
    protected final Word[] sourceWords;
    protected final Tag[] sourceTags;
    protected final Word[] targetWords;
    protected final Tag[] targetTags;

    public ProbabilisticTagAligner(Translation translation) {
        this.translation = translation;
        this.sourceWords = translation.getSource().getWords();
        this.sourceTags = translation.getSource().getTags();
        this.targetWords = translation.getWords();
        this.targetTags = translation.getTags();
    }

    public abstract void initialize();

    public void computeNormalizedTagsScores(int sourceTagId, double[] scores) {
        this.computeTagsScores(sourceTagId, scores);
        normalizeScores(scores);
    }

    protected abstract void computeTagsScores(int sourceTagId, double[] scores);

    private void normalizeScores(double[] scores) {
        double total = DoubleStream.of(scores).sum();
        if (total > 0) {
            IntStream.range(0, scores.length).forEach(i -> scores[i] = scores[i] / total);
        }
    }
}
