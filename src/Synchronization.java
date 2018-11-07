//Flavien Stemmelen - Thomas De Iseppi

/**
 * Message de synchronisation
 */
public class Synchronization{

    /**
     * Horloge de Lamport
     */
    protected int lamport;
    /**
     * Emetteur du message
     */
    protected int from;

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

    /**
     *
     * @param lamport estampille de l'horloge de Lamport
     * @param from emetteur du message
     */
    public Synchronization(int lamport, int from) {
        this.lamport = lamport;
        this.from = from;
    }
}
