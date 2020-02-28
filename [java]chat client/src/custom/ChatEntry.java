package custom;

/**
 *
 * @author me
 */
public class ChatEntry {
    private String messageSource;
    private String messageType;
    private String messageContent;
    public ChatEntry(String messageSource, String messageContent, String messageType) {
        this.messageSource = messageSource;
        this.messageContent = messageContent;
        this.messageType = messageType;
    }
    public String getMessageSource() {
        return messageSource;
    }
    public String getMessageType() {
        return messageType;
    }
    public String getMessageContent(){
        return messageContent;
    }
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}