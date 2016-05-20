package eu.modernmt.processing.xml;

import eu.modernmt.model.*;
import eu.modernmt.processing.framework.ProcessingException;
import eu.modernmt.processing.framework.TextProcessor;
import eu.modernmt.processing.xml.tag.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by lucamastrostefano on 4/04/16.
 */
public class XMLTagProjector implements TextProcessor<Translation, Void> {

    public static class XMLTagProjectorProcessor implements TextProcessor<Translation, Void> {

        @Override
        public Void call(Translation translation, Map<String, Object> metadata) throws ProcessingException {
            projectTags(translation);
            return null;
        }

        @Override
        public void close() {
            // Nothing to do
        }
    }


    private static class ExtendedTag implements Comparable<ExtendedTag> {

        private Tag tag;
        private int sourcePosition;
        private int sourceTagIndex;
        private int targetTagIndex;
        private int targetPosition;

        public ExtendedTag(Tag tag, int sourcePosition, int sourceTagIndex, int targetPosition) {
            this.tag = tag;
            this.sourcePosition = sourcePosition;
            this.sourceTagIndex = sourceTagIndex;
            this.targetPosition = targetPosition;
        }

        @Override
        public int compareTo(ExtendedTag extendedTag) {
            int c = this.tag.compareTo(extendedTag.tag);
            if (c == 0) {
                c = this.sourceTagIndex - extendedTag.sourceTagIndex;
            }
            return c;
        }

        @Override
        public boolean equals(Object o) {
            return this.sourceTagIndex == ((ExtendedTag) o).sourceTagIndex;
        }

        @Override
        public int hashCode() {
            int result = tag != null ? tag.hashCode() : 0;
            result = 31 * result + sourceTagIndex;
            return result;
        }

    }

    @Override
    public Void call(Translation translation, Map<String, Object> metadata) throws ProcessingException {
        projectTags(translation);
        return null;
    }

    public static void projectTags(Translation translation) {
        projectTags(translation, null);
    }

    public static void projectTags(Translation translation, Translation hint) {
        Translation original_translation = translation;
        Sentence source = translation.getSource();
        boolean useHint = (hint != null && hint.hasWords() && hint.hasTags() && hint.hasAlignment());
        if (source.hasTags()) {
            if (source.hasWords()) {
                if (translation.hasAlignment()) {
                    if (useHint) {
                        //Allineo gli id dei tag nell'hint che € |tag_hint| && |tag_originali|
                        //I tag si allineano cercando nella traduzione quello più vicino alle parole allineante vicine a quello della source.

                        //Rimuovo dall'hint i tag non allineati con la source originale

                        //Sistemo nella source originale i tag (e spazi) € (|tag_hint| && |tag_originali| - |tag_originali|)

                        //Mappo dall source i tag (e spazi) € |tag_originali| - |tag_hint|

                        //Creo un oggetto translatoin "hint -> traduzione" e calcolo spazi e tag su questa
                        //translation = new Translation(translation.getWords(), hint, alignments);

                        //unisco i tag mappati: |tag_hint| && |tag_originali| from hint  U  |tag_originali| - |tag_hint| dall'original

                    } else {
                        computeProjection(translation);
                        simpleSpaceAnalysis(translation);
                    }
                }
            } else {
                Tag[] tags = source.getTags();
                Tag[] copy = Arrays.copyOf(tags, tags.length);
                translation.setTags(copy);
            }
        }
    }

    private static List<ExtendedTag> computeProjection(Translation translation) {
        Tag[] sourceTags = translation.getSource().getTags();
        Token[] sourceWord = translation.getSource().getWords();
        Token[] targetTokens = translation.getWords();
        int[][] alignments = translation.getAlignment();
        List<ExtendedTag> translationTags = new ArrayList<>(sourceTags.length);
        Map<Integer, Integer> closing2opening = new HashMap<>();

        Set<Integer> sourceLeftToken = new HashSet<>();
        Set<Integer> sourceRightToken = new HashSet<>();
        Set<Integer> targetLeftToken = new HashSet<>();
        Set<Integer> targetRightToken = new HashSet<>();
        Set<Integer> leftTokenIntersection = new HashSet<>();
        Set<Integer> rightTokenIntersection = new HashSet<>();

        Set<Integer> alignedTags = new HashSet<Integer>();

        for (int tagIndex = 0; tagIndex < sourceTags.length; tagIndex++) {
            Tag sourceTag = sourceTags[tagIndex];
            //If the tag has been already mapped (such as well formed closing tags), then continue
            if (alignedTags.contains(tagIndex)) {
                continue;
            }

            int sourcePosition = sourceTag.getPosition();
            boolean singleTag = false;

            int closingTagIndex = getClosingTagIndex(sourceTag, tagIndex, sourceTags);
            //If the current tag has a closing tag
            if (closingTagIndex != -1) {
                Tag closingTag = sourceTags[closingTagIndex];
                int closePosition = closingTag.getPosition();
                int minPos = Integer.MAX_VALUE;
                int maxPos = -1;

                //Check if they contain some aligned words
                for (int[] align : alignments) {
                    if (align[0] >= sourcePosition && align[0] < closePosition) {
                        minPos = Math.min(minPos, align[1]);
                        maxPos = Math.max(maxPos, align[1]);
                    }
                }

                //If they contain no aligned words, treat the current tag as a self-closing tag
                if (minPos == Integer.MAX_VALUE || maxPos == -1) {
                    singleTag = true;
                } else {
                    //Else map both the tags in order to enclose the words aligned
                    // to those contained in the source sentence
                    maxPos += 1;
                    Tag targetTag = Tag.fromTag(sourceTag);
                    targetTag.setPosition(minPos);
                    translationTags.add(new ExtendedTag(targetTag, sourceTag.getPosition(), tagIndex, minPos));

                    Tag closingTargetTag = Tag.fromTag(closingTag);
                    closingTargetTag.setPosition(maxPos);
                    translationTags.add(new ExtendedTag(closingTargetTag, closingTag.getPosition(), closingTagIndex, maxPos));

                    alignedTags.add(tagIndex);
                    alignedTags.add(closingTagIndex);
                }
            } else {
                //If not closing tag has been found, treat this tag as a self-closing tag
                singleTag = true;
            }

            //If it is a self-closing tag
            if (singleTag) {
                sourceLeftToken.clear();
                sourceRightToken.clear();
                //Words that are at the left of the tag in the source sentence, should be at left of the mapped tag
                //in the translation. Some reasoning for those that are at the right.
                for (int[] align : alignments) {
                    //If the word is at the left of the current tag
                    if (align[0] < sourcePosition) {
                        if (!sourceRightToken.contains(align[1])) {
                            //Remember that it should be at the left also in the translation
                            sourceLeftToken.add(align[1]);
                        }
                    } else {
                        //It the word is at the right of the current tag
                        if (!sourceLeftToken.contains(align[1])) {
                            //Remember that it should be at the right also in the translation
                            sourceRightToken.add(align[1]);
                        }
                    }
                }
                boolean openingTag = sourceTag.isOpeningTag();
                //Find the mapped position that respects most of the left-right word-tag relationship as possible.
                targetLeftToken.clear();
                targetRightToken.clear();
                leftTokenIntersection.clear();
                rightTokenIntersection.clear();

                for (int i = 0; i < targetTokens.length; i++) {
                    targetRightToken.add(i);
                }
                rightTokenIntersection.addAll(sourceRightToken);
                rightTokenIntersection.retainAll(targetRightToken);
                int maxScore = rightTokenIntersection.size();
                int bestPosition = 0;

                int actualPosition = 0;
                for (int i = 0; i < targetTokens.length; i++) {
                    actualPosition++;

                    targetLeftToken.add(i);
                    targetRightToken.remove(i);
                    leftTokenIntersection.clear();
                    rightTokenIntersection.clear();

                    leftTokenIntersection.addAll(sourceLeftToken);
                    leftTokenIntersection.retainAll(targetLeftToken);
                    rightTokenIntersection.addAll(sourceRightToken);
                    rightTokenIntersection.retainAll(targetRightToken);
                    int score = leftTokenIntersection.size() + rightTokenIntersection.size();

                    //Remember the best position and score (for opening tag prefer to shift them to the right)
                    if ((openingTag && score >= maxScore) || (!openingTag && score > maxScore)) {
                        maxScore = score;
                        bestPosition = actualPosition;
                    } else if (score < maxScore) {
                        //The function that describes the score should be concave,
                        // so we can break as soon it starts decreasing
                        //break
                    }
                }
                Integer openingPosition = closing2opening.get(tagIndex);
                if (openingPosition != null) {
                    bestPosition = Math.max(openingPosition, bestPosition);
                } else if (closingTagIndex != -1) {
                    closing2opening.put(closingTagIndex, bestPosition);
                }
                //Map the tag to the best position
                Tag targetTag = Tag.fromTag(sourceTag);
                targetTag.setPosition(bestPosition);
                translationTags.add(new ExtendedTag(targetTag, sourceTag.getPosition(), tagIndex, bestPosition));
                alignedTags.add(tagIndex);
            }
        }

        //Sort the tag in according to their position and order in the source sentence
        Collections.sort(translationTags);
        for (int i = 0; i < translationTags.size(); i++) {
            translationTags.get(i).targetTagIndex = i;
        }

        Tag[] result = new Tag[translationTags.size()];
        for (int i = 0; i < translationTags.size(); i++) {
            result[i] = translationTags.get(i).tag;
        }
        translation.setTags(result);

        return translationTags;
    }

    private static int getClosingTagIndex(Tag openingTag, int tagIndex, Tag[] tags) {
        int minIndex = tagIndex + 1;
        int open = 1;
        for (int index = minIndex; index < tags.length; index++) {
            Tag tag = tags[index];
            if (openingTag == tag) {
                continue;
            }
            if (openingTag.getName().equals(tag.getName()) && tag.isOpeningTag()) {
                open++;
            }
            if (openingTag.opens(tag)) {
                open--;
                if (open == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    public static void simpleSpaceAnalysis(Translation translation) {

        //Add whitespace between the last word and the next tag if the latter has left space
        Tag[] tags = translation.getTags();
        Word[] words = translation.getWords();
        int firstTagAfterLastWord = tags.length - 1;
        boolean found = false;
        while (firstTagAfterLastWord >= 0 && tags[firstTagAfterLastWord].getPosition() == words.length) {
            firstTagAfterLastWord--;
            found = true;
        }
        if (found) {
            firstTagAfterLastWord++;
            if (tags[firstTagAfterLastWord].hasLeftSpace() && !words[words.length - 1].hasRightSpace()) {
                words[words.length - 1].setRightSpace(" ");
            }
        }

        //Remove whitespace between word and the next tag, if the first has no right space.
        //Left trim first token if it is a tag
        Token previousToken = null;
        for (Token token : translation) {
            if (token instanceof Tag) {
                Tag tag = (Tag) token;
                if (previousToken != null && previousToken.hasRightSpace() && !tag.hasLeftSpace()) {
                    if (!tag.hasRightSpace()) {
                        tag.setRightSpace(previousToken.getRightSpace());
                    }
                    previousToken.setRightSpace(null);
                } else if (previousToken == null) {
                    //Remove first whitespace
                    tag.setLeftSpace(false);
                }
            }
            previousToken = token;

        }
        //Remove the last whitespace
        if (previousToken != null) {
            previousToken.setRightSpace(null);
        }
    }

    private static void adjustHintTags(Translation translation, Translation hint) {
        Tag[] sourceTags = translation.getSource().getTags();
        Tag[] hintTranslationTags = hint.getTags();

        double[][] source2sourceScores = alignSourceSourceTags(translation.getSource(), hint.getSource());
        Translation original2hint = new Translation(translation.getWords(), sourceTags, hint, null);
        double[][] source2targetScores = alignTranslationTags(hint);

        int[] source2sourceAlignments = computeBestAlignments(source2sourceScores);
        int[] hintTagsAlignments = computeBestAlignments(source2targetScores);
        int[] translation2hintAlignments = computePivotedAlignments(source2sourceAlignments, hintTagsAlignments);

        List<Tag> filteredTags = new ArrayList<>(translation2hintAlignments.length);
        for (int sourceIndex = 0; sourceIndex < translation2hintAlignments.length; sourceIndex++) {
            int hintTagIndex = translation2hintAlignments[sourceIndex];
            if (hintTagIndex != -1) {
                Tag hintTag = hintTranslationTags[hintTagIndex];
                Tag newTag = Tag.fromText(sourceTags[sourceIndex].getText(),
                        hintTag.hasLeftSpace(), hintTag.getRightSpace(), hintTag.getPosition());
                filteredTags.add(newTag);
            }
        }

        Tag[] tags = new Tag[filteredTags.size()];
        filteredTags.toArray(tags);
        hint.setTags(tags);
    }

    private static double[][] alignTags(Translation translation, Map<ProbabilisticTagAligner, Double> tagAligners2weights) {
        Tag[] sourceTags = translation.getSource().getTags();
        Tag[] targetTags = translation.getTags();
        double[][] sourceTag2possibleAlignments = new double[sourceTags.length][targetTags.length];

        for (ProbabilisticTagAligner tagAligner : tagAligners2weights.keySet()) {
            tagAligner.initialize();
        }

        int targetTranslationTagsCount = targetTags.length;
        double[] tagAlignerScores = new double[targetTranslationTagsCount];
        for (int sourceTagIndex = 0; sourceTagIndex < sourceTags.length; sourceTagIndex++) {
            double[] scores = new double[targetTranslationTagsCount];
            for (Map.Entry<ProbabilisticTagAligner, Double> tagAligner2weight : tagAligners2weights.entrySet()) {
                ProbabilisticTagAligner tagAligner = tagAligner2weight.getKey();
                double weight = tagAligner2weight.getValue();
                tagAligner.computeNormalizedTagsScores(sourceTagIndex, tagAlignerScores);
                for (int targetTagIndex = 0; targetTagIndex < targetTranslationTagsCount; targetTagIndex++) {
                    if (Double.isInfinite(weight)) {
                        if (tagAlignerScores[targetTagIndex] <= 0) {
                            scores[targetTagIndex] = 0;
                        }
                    } else {
                        scores[targetTagIndex] *= Math.pow(tagAlignerScores[targetTagIndex], 1 / weight);
                    }
                }
            }
            sourceTag2possibleAlignments[sourceTagIndex] = scores;
        }

        for (double[] d : sourceTag2possibleAlignments) {
            System.out.println(Arrays.toString(d));
        }
        return sourceTag2possibleAlignments;
    }

    private static double[][] alignSourceSourceTags(Sentence sourceSentence, Sentence targetSentence) {
        Translation translation = new Translation(sourceSentence.getWords(), sourceSentence.getTags(), targetSentence, null);
        Map<ProbabilisticTagAligner, Double> tagAligners2weights = new HashMap<>();
        tagAligners2weights.put(new TypeTagAligner(translation), 1D);
        tagAligners2weights.put(new ContextTagAligner(translation), Double.POSITIVE_INFINITY);
        tagAligners2weights.put(new AbsolutePositionTagAligner(translation), 1D);
        tagAligners2weights.put(new RelativePositionTagAligner(translation), 1D);
        tagAligners2weights.put(new NameTagAligner(translation), 1D);
        tagAligners2weights.put(new AttributesTagAligner(translation), 0.5D);
        return alignTags(translation, tagAligners2weights);
    }

    private static double[][] alignTranslationTags(Translation translation) {
        Map<ProbabilisticTagAligner, Double> tagAligners2weights = new HashMap<>();
        tagAligners2weights.put(new ProjectedTagAligner(translation), 1D);
        return alignTags(translation, tagAligners2weights);
    }

    private static int[] computePivotedAlignments(int[] firstAlignments, int[] secondAlignments) {
        int[] pivotedAlignments = new int[firstAlignments.length];
        for (int originalSourceIndex = 0; originalSourceIndex < firstAlignments.length; originalSourceIndex++) {
            int firstAligned = firstAlignments[originalSourceIndex];
            pivotedAlignments[originalSourceIndex] = secondAlignments[firstAlignments[originalSourceIndex]];
        }
        return pivotedAlignments;
    }

    private static int[] computeBestAlignments(double[][] source2targetScores) {
        //gli score non si devono combinare, source2target è fissato. non dipende dagli altri.
        return null;
    }

    private static Map<Integer, Integer> alignTags_old(Translation translation) {
        Sentence sentence = translation.getSource();
        Token[] sentenceTokens = new Token[sentence.length()];
        int pos = 0;
        for (Token t : sentence) {
            sentenceTokens[pos++] = t;
        }

        Map<Integer, Integer> wordPosition2tokenPosition = new HashMap<>(translation.getWords().length);
        Token[] translationTokens = new Token[translation.length()];
        int numberOfTags = pos = 0;
        for (Token t : translation) {
            if (t instanceof Tag) {
                numberOfTags++;
            } else {
                wordPosition2tokenPosition.put(pos - numberOfTags, pos);
            }
            translationTokens[pos++] = t;
        }

        Map<Integer, Integer> tagIndex2tagIndex = new HashMap<>(sentence.getTags().length);
        int[][] alignments = translation.getAlignment();
        Arrays.sort(alignments, (a1, a2) -> {
            int c = a1[0] - a2[0];
            if (c == 0) {
                c = a1[1] - a2[1];
            }
            return c;
        });

        int lastAlignIndex = 0;
        int positionPrevWord = 0;
        int positionNextWord = 0;
        Map<Integer, Double[]> sourceTag2possibleAlignments = new HashMap<>();
        boolean wordFound = false;
        List<Tag> tagToBeAligned = new ArrayList<>();
        for (pos = 0; pos < sentenceTokens.length; pos++) {
            Token token = sentenceTokens[pos];
            if (token instanceof Tag) {
                if (wordFound) {
                    positionNextWord = -1;
                    if (!tagToBeAligned.isEmpty()) {
                        for (Tag tag : tagToBeAligned) {
                            //alignTag();
                        }
                    }
                    wordFound = false;
                }
                tagToBeAligned.add((Tag) token);
            } else {
                if (!wordFound) {
                    wordFound = true;
                }
                if (tagToBeAligned.isEmpty()) {
                    positionPrevWord = pos;
                } else {
                    positionNextWord = pos;
                    for (Tag tag : tagToBeAligned) {
                        //alignTag();
                    }
                    positionPrevWord = pos;
                }
            }
        }
        if (!tagToBeAligned.isEmpty()) {
            positionNextWord = Integer.MAX_VALUE;
            for (Tag tag : tagToBeAligned) {
                //alignTag();
            }
        }
        return tagIndex2tagIndex;
    }

    private static void alignTag_old(int sourceTagIndex, Tag sourceTagToBeAligned, int translationPrevWordIndex,
                                     int translationNextWordIndex, Tag[] translationTags,
                                     Map<Integer, Double[]> sourceTag2possibleAlignments) {
        if (translationPrevWordIndex > translationNextWordIndex) {
            int tmp = translationPrevWordIndex;
            translationPrevWordIndex = translationNextWordIndex;
            translationNextWordIndex = tmp;
        }

        Double[] translationTagsScores = sourceTag2possibleAlignments.get(sourceTagIndex);
        if (translationTagsScores == null) {
            translationTagsScores = new Double[translationTags.length];
            sourceTag2possibleAlignments.put(sourceTagIndex, translationTagsScores);
        }

        for (int index = 0; index < translationTags.length; index++) {
            Tag translationTag = translationTags[index];

        }
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    public static void main(String[] args) throws Throwable {
        // SRC: hello <b>world</b><f />!

        System.out.println(Double.NEGATIVE_INFINITY);
        System.out.println(Double.NEGATIVE_INFINITY * 0);
        System.out.println(Double.NEGATIVE_INFINITY * 1);
        System.out.println(Double.NEGATIVE_INFINITY * 10);
        System.out.println(Double.NEGATIVE_INFINITY / 10);
        System.out.println(Double.NEGATIVE_INFINITY + 10);
        System.out.println(Double.NEGATIVE_INFINITY - 10);

        Sentence source = new Sentence(new Word[]{
                new Word("It", " "),
                new Word("is", " "),
                new Word("often", " "),
                new Word("*99***1#", null),
                new Word(".", null)
        }, new Tag[]{
                Tag.fromText("<i>", true, null, 3),
                Tag.fromText("</i>", false, null, 4)
        });
        Translation translation = new Translation(new Word[]{
                new Word("Spesso", " "),
                new Word("corrisponde", " "),
                new Word("a", " "),
                new Word("*99***1#", null),
                new Word(".", null)
        }, source, new int[][]{
                {1, 1},
                {1, 2},
                {2, 0},
                {3, 3},
                {4, 4}
        });


        System.out.println("SRC:                     " + source);
        System.out.println("SRC (stripped):          " + source.getStrippedString(false));
        System.out.println();

        XMLTagProjector mapper = new XMLTagProjector();
        mapper.call(translation, null);

        System.out.println("TRANSLATION:             " + translation);
        System.out.println("TRANSLATION (stripped):  " + translation.getStrippedString(false));
    }
}