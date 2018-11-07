/**
 * Classe abstraite dont héritent tous les messages qui passe la boite au lettres
 */
abstract class Message {
    /**
     * Estampille lamport
     */
    protected int lamport;
    /**
     * Emetteur du message
     */
    protected int from;
    /**
     * Contenu du message
     */
    protected Object payload;

    public int getLamport() {
        return lamport;
    }

    public void setLamport(int lamport) {
        this.lamport = lamport;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
