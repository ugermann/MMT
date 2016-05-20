package eu.modernmt.processing.xml.tag;

import eu.modernmt.model.Tag;
import eu.modernmt.model.Translation;
import eu.modernmt.model.Word;

/**
 * Created by lucamastrostefano on 17/05/16.
 */
public abstract class BooleanTagAligner {

    protected final Translation translation;
    protected final Word[] sourceWords;
    protected final Tag[] sourceTags;
    protected final Word[] targetWords;
    protected final Tag[] targetTags;

    public BooleanTagAligner(Translation translation) {
        this.translation = translation;
        this.sourceWords = translation.getSource().getWords();
        this.sourceTags = translation.getSource().getTags();
        this.targetWords = translation.getWords();
        this.targetTags = translation.getTags();
    }

    public abstract void initialize();

    public abstract boolean classify(int sourceTagId);

}
