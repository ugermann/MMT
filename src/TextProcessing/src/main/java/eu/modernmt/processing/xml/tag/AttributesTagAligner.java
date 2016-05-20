package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Translation;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class AttributesTagAligner extends ProbabilisticTagAligner {

    private static final double HIT_SCORE = 1D;

    public AttributesTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        String sourceTagAttributes = sourceTags[sourceTagId].getName();
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            String targetTagAttributes = targetTags[targetTagIndex].getAttributes();
            if (targetTagAttributes != null && targetTagAttributes.equals(sourceTagAttributes)) {
                scores[targetTagIndex] = HIT_SCORE;
            } else {
                scores[targetTagIndex] = 0;
            }
        }
    }

}
