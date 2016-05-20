package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Tag;
import eu.modernmt.model.Translation;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class AbsolutePositionTagAligner extends ProbabilisticTagAligner {

    private static final double HIT_SCORE = 1D;

    private int[] sourceTagInSentencePostions;
    private int[] targetTagInSentencePostions;

    public AbsolutePositionTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
        this.sourceTagInSentencePostions = getInSentencePositions(sourceTags);
        this.targetTagInSentencePostions = getInSentencePositions(targetTags);
    }

    private static int[] getInSentencePositions(Tag[] tags) {
        int numberOfTags = tags.length;
        int[] tagInSentencePostions = new int[numberOfTags];
        for (int tagIndex = 0; tagIndex < numberOfTags; tagIndex++) {
            tagInSentencePostions[tagIndex] = tags[tagIndex].getPosition() + tagIndex;
        }
        return tagInSentencePostions;
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            scores[targetTagIndex] = 1 / (1 + Math.abs(sourceTagId - targetTagIndex));
        }
    }

}
