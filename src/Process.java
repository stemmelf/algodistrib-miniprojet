//Flavien Stemmelen - Thomas De Iseppi
/**
 * Cette classe représente les process
 */
public class Process extends LamportClock implements Runnable {
    private Thread thread;
    private boolean alive;
    private boolean dead;
    private Communicateur com;
    private static int variableCommune = 5;

    /**
     *
     * @param name Le nom du process
     */
    public Process(String name){
        super();
        this.thread = new Thread(this);
        this.thread.setName(name);
        this.alive = true;
        this.dead = false;
        this.com = new Communicateur(this);
        this.thread.start();
    }

    /**
     * La fonction éxécutée par tous les process
     */
    public void run(){
        int loop = 0;

        System.out.println(this.clock + " : Process " + this.thread.getName() + " débute.");

        while(this.alive){
            try{
                Thread.sleep(500);

                if(this.thread.getName() == "P1" ){
                    com.broadcast("Je suis P1 et je broadcast");
                }

                if(this.thread.getName() == "P2" ){
                    com.requestSC();
                    Process.variableCommune = 9;
                    com.releaseSC();
                }

                if(this.thread.getName() == "P3" ){
                    com.sendTo("P3 to P2", 1);
                }

                com.synchronize();

                this.lireBaL();

                System.out.println(this.clock + " : " + Thread.currentThread().getName() + " => VC = " + Process.variableCommune);


                if(this.thread.getName() == "P1" ){
                    com.requestSC();
                    Process.variableCommune = 2;
                    com.releaseSC();
                    String message = com.recevFromSync(1).toString();
                    System.out.println(this.clock + " : " + this.thread.getName() + " recieve : " + message);
                }

                if(this.thread.getName() == "P2" ){
                    String mts = "Message synchrone à P1.";
                    System.out.println(this.clock + " : " + Thread.currentThread().getName() + " send to P1 : " + mts);
                    com.sendToSync(mts, 0);
                }

                System.out.println(this.clock + " : " + Thread.currentThread().getName() + " => VC = " + Process.variableCommune);

                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            loop++;
        }
        // liberation du bus
        com.unregister();
        System.out.println(this.clock + " : " + Thread.currentThread().getName() + " stoped");
        this.dead = true;
    }

    /**
     * Permet de stopper les process proprement
     */
    public void waitStoped(){
        while(!this.dead){
            try{
                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Met fin à la boucle infinie de run()
     */
    public void stop(){
        this.alive = false;
    }

    /**
     * Permet de lire la boite aux lettres
     */
    public void lireBaL(){
        while(com.getBoiteAuxLettre().size() != 0){
            System.out.println(this.clock + " : " + this.thread.getName() + " recieve : " + com.getBoiteAuxLettre().get(0).getPayload());
            com.getBoiteAuxLettre().remove(0);
        }
    }

}