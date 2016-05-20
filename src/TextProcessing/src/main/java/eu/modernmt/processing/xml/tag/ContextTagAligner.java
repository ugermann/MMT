package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Tag;
import eu.modernmt.model.Translation;
import eu.modernmt.model.Word;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public class ContextTagAligner extends ProbabilisticTagAligner {

    private static final String EMPTY_CONTEXT = "";
    private static final double ICE_MULTIPLIER = 4;
    private static final double HIT_SCORE = 1D;
    private static final int CONTEXT_LENGTH = 4;

    private String[][] sourceContext;
    private String[][] targetContext;

    public ContextTagAligner(Translation translation) {
        super(translation);
    }

    @Override
    public void initialize() {
        this.sourceContext = getContext(sourceTags, sourceWords);
        this.targetContext = getContext(targetTags, targetWords);
    }

    private static String[][] getContext(Tag[] tags, Word[] words) {
        int numberOfTags = tags.length;
        int numberOfWords = words.length;
        String[][] tagIndex2context = new String[numberOfTags][2];
        for (int tagIndex = 0; tagIndex < numberOfTags; tagIndex++) {
            String[] context = new String[4];
            int tagPosition = tags[tagIndex].getPosition();
            int contextIndex = 0;
            for (int wordIndex = tagPosition - CONTEXT_LENGTH / 2; wordIndex < tagPosition + CONTEXT_LENGTH / 2; wordIndex++) {
                if (wordIndex <= 0) {
                    context[contextIndex] = EMPTY_CONTEXT;
                } else if (wordIndex >= numberOfWords) {
                    context[contextIndex] = EMPTY_CONTEXT;
                } else {
                    context[contextIndex] = words[wordIndex - 1].getText();
                }
                contextIndex++;
            }
            tagIndex2context[tagIndex] = context;
        }
        return tagIndex2context;
    }

    @Override
    public void computeTagsScores(int sourceTagId, double[] scores) {
        String[] sourceTagContext = this.sourceContext[sourceTagId];
        for (int targetTagIndex = 0; targetTagIndex < scores.length; targetTagIndex++) {
            String[] targetTagContext = this.targetContext[targetTagIndex];
            scores[targetTagIndex] = 0;
            for (int i = 0; i < CONTEXT_LENGTH; i++) {
                if (sourceTagContext[i].equals(targetTagContext[i])) {
                    scores[targetTagIndex] += HIT_SCORE;
                }
            }
            if (scores[targetTagIndex] > 1) {
                scores[targetTagIndex] *= scores[targetTagIndex];
            }
        }
    }

}
