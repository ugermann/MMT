package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Tag;
import eu.modernmt.model.Translation;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class RelativePositionTagAligner extends ProbabilisticTagAligner {

    private static final double HIT_SCORE = 1D;

    private int[] sourceTagRelativePostions;
    private int[] targetTagRelativePostions;

    public RelativePositionTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
        this.sourceTagRelativePostions = getRelativePositions(sourceTags);
        this.targetTagRelativePostions = getRelativePositions(targetTags);
    }

    private static int[] getRelativePositions(Tag[] tags) {
        int numberOfTags = tags.length;
        int[] tagRelativePositions = new int[numberOfTags];
        if (numberOfTags > 1) {
            for (int tagIndex = 0; tagIndex < numberOfTags; tagIndex++) {
                if (tagIndex == 0) {
                    tagRelativePositions[tagIndex] = 0;
                } else {
                    tagRelativePositions[tagIndex] = tags[tagIndex + 1].getPosition() - tags[tagIndex].getPosition();
                }
            }
        }
        return tagRelativePositions;
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        int sourceTagRelativePosition = this.sourceTagRelativePostions[sourceTagId];
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            int targetTagRelativePosition = this.targetTagRelativePostions[targetTagIndex];
            scores[targetTagIndex] = 1 / (1 + Math.abs(sourceTagRelativePosition - targetTagRelativePosition));
        }
    }

}
