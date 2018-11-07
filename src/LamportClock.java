import java.util.concurrent.Semaphore;

/**
 * Permet la gestion de l'horloge de Lamport
 */
abstract class LamportClock {
    /**
     * Horloge de Lamport
     */
    protected int clock;
    /**
     * Semaphore d'accès à la section critique de l'horloge de Lamport
     */
    protected Semaphore sem;

    public LamportClock(){
        this.clock = 0;
        this.sem = new Semaphore(1);
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.lock_clock();
        this.clock = clock;
        this.unlock_clock();
    }


    private void unlock_clock(){
        sem.release();
    }

    private void lock_clock(){
        try{
            sem.acquire();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
