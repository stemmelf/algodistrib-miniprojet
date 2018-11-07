//Flavien Stemmelen - Thomas De Iseppi
/**
 * Message synchrone
 */
public class MessageToSync extends Message {
    /**
     * Destinataire du message
     **/
    private int to;

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    /**
     *
     * @param payload contenu du message
     * @param to destinataire du message
     */
    public MessageToSync(Object payload, int to) {
        this.payload = payload;
        this.to = to;
    }

}
