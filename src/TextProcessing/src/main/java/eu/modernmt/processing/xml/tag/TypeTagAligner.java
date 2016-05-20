package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Tag;
import eu.modernmt.model.Translation;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class TypeTagAligner extends ProbabilisticTagAligner {

    private static final double HIT_SCORE = 1D;

    public TypeTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        Tag.Type sourceTagType = sourceTags[sourceTagId].getType();
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            if (targetTags[targetTagIndex].getName().equals(sourceTagType)) {
                scores[targetTagIndex] = HIT_SCORE;
            } else {
                scores[targetTagIndex] = 0;
            }
        }
    }

}
