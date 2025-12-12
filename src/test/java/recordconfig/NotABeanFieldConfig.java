package recordconfig;

public record NotABeanFieldConfig(NotABean notBean) {
    public static class NotABean {
        int stuff;
    }
}
