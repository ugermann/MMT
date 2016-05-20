package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Sentence;
import eu.modernmt.model.Tag;
import eu.modernmt.model.Token;
import eu.modernmt.model.Translation;
import eu.modernmt.processing.xml.XMLTagProjector;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class ProjectedTagAligner extends ProbabilisticTagAligner {

    private int[] autoPlacedTagsPositions;
    private int[] targetTagsPositions;

    public ProjectedTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
        Translation autoTaggedTranslation = new Translation(super.translation.getWords(), super.translation.getSource(),
                super.translation.getAlignment());
        XMLTagProjector.projectTags(autoTaggedTranslation);
        this.autoPlacedTagsPositions = getTagPositions(autoTaggedTranslation);
        this.targetTagsPositions = getTagPositions(super.translation);
    }

    private static int[] getTagPositions(Sentence sentence) {
        int[] tagsPositions = new int[sentence.getTags().length];
        int tokenIndex = 0;
        int tagIndex = 0;
        for (Token token : sentence) {
            if (token instanceof Tag) {
                tagsPositions[tagIndex] = tokenIndex;
                tagIndex++;
            }
            tokenIndex++;
        }
        return tagsPositions;
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        int autoPlacedTagsPosition = this.autoPlacedTagsPositions[sourceTagId];
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            int targetTagPosition = this.targetTagsPositions[targetTagIndex];
            scores[targetTagIndex] = 1 / (1 + Math.abs(autoPlacedTagsPosition - targetTagPosition));
        }
    }

}
