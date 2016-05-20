package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Translation;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class NameTagAligner extends ProbabilisticTagAligner {

    private static final double HIT_SCORE = 1D;

    public NameTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        String sourceTagName = sourceTags[sourceTagId].getName();
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            if (targetTags[targetTagIndex].getName().equals(sourceTagName)) {
                scores[targetTagIndex] = HIT_SCORE;
            } else {
                scores[targetTagIndex] = 0;
            }
        }
    }

}
