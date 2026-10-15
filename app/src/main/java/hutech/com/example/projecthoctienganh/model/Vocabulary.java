package hutech.com.example.projecthoctienganh.model;

public class Vocabulary {
    private int id;
    private int topicId;
    private String word;
    private String phonetic;
    private String meaning;
    private String example;
    private String iconName;
    private boolean isLearned;

    public Vocabulary() {
    }

    public Vocabulary(int id, int topicId, String word, String phonetic, String meaning, String example, String iconName, boolean isLearned) {
        this.id = id;
        this.topicId = topicId;
        this.word = word;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.example = example;
        this.iconName = iconName;
        this.isLearned = isLearned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }
}