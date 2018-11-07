//Flavien Stemmelen - Thomas De Iseppi
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Integer.max;
/**
 * Communicateur lié à un process
 */
public class Communicateur {
    /**
     * Permet la gestion de l'horloge de Lamport
     */
    private LamportClock clock;
    /**
     * Permet le partage de message de manière asynchrone
     */
    private EventBusService bus;
    /**
     * Permet de stoker les messages reçus
     */
    private ArrayList<Message> boiteAuxLettre = new ArrayList<Message>();
    /**
     * Permet la gestion des message de synchronisation
     */
    private ArrayList<MessageToSync> boiteAuxLettreSync = new ArrayList<MessageToSync>();
    /**
     * Permet la gestion des sections critiques
     */
    private String etat = "null";
    /**
     * Correspond au nombre de communicateurs
     */
    private static int nbComm = 0;
    /**
     * Correspond à l'id du communicateur
     */
    private int id = Communicateur.nbComm++;
    /**
     * Correspond à la liste des communicateurs, cette liste permet la gestion de la barrière de synchronisation.
     */
    private int[] listCommSync;

    /**
     *
     * @param clock Correspond à l'holorge de Lamport associée au communicateur
     */
    public Communicateur(LamportClock clock) {
        this.clock = clock;

        this.bus = EventBusService.getInstance();
        this.bus.registerSubscriber(this); // Auto enregistrement sur le bus afin que les methodes "@Subscribe" soient invoquees automatiquement.

    }

    /**
     * Permet de se désenregistrer du bus de communication
     */
    public void unregister(){
        Communicateur.nbComm--;
        this.bus.unRegisterSubscriber(this);
        this.bus = null;
    }

    /**
     * Gère le token pour la section critique, le communicateur renvoi le token si le process n'en a pas besoin
     * et sinon le garde durant la section critique, puis le renvoi.
     * @param t Le token
     */
    @Subscribe
    private void onToken(Token t){
        if (t.getOwner() == this.id){
            if(etat.equals("request")) {
                etat = "SC";
                while (!etat.equals("release")) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                etat = "null";
            }
            if(nbComm != 0){
                Token t2 = new Token((this.id+1)%nbComm);
                //t.setOwner((this.id+1)%nbComm);
                bus.postEvent(t2);
            }
        }
    }

    /**
     * Gère la réception des messages de synchronisation.
     * @param sync L'objet de synchronisation
     */
    @Subscribe
    private void onSynchronize(Synchronization sync){
        if(this.listCommSync == null){
            this.listCommSync = new int[Communicateur.nbComm];
            Arrays.fill(this.listCommSync, 0);
        }
        if(sync.getFrom() != this.id){
            this.listCommSync[sync.from] = sync.lamport;
        }
    }

    /**
     * Gère la réception des messages broadcast.
     * Si le communicateur n'est pas l'émetteur il place le message dans la boite aux lettres.
     * @param m Le message envoyé en broadcast
     */
    @Subscribe
    public void onBroadcast(BroadcastMessage m){
        if(m.getFrom() != this.id){
            this.clock.setClock(this.clock.getClock()+1);
            this.boiteAuxLettre.add(m);
        }
    }

    /**
     * Gère la réception des messages dédiés asynchrones.
     * Si le message est pour lui, le communicateur le place dans la boite aux lettres.
     * @param m Le message dédié asynchrone
     */
    @Subscribe
    public void onReceive(MessageTo m){
        if(m.getTo() == this.id){
            this.clock.setClock(this.clock.getClock()+1);
            this.boiteAuxLettre.add(m);
        }
    }

    /**
     * Gère la réception des messages dédiés synchrones.
     * Si le message est pour lui, le communicateur le place dans la boite aux lettres.
     * @param m Le message dédié synchrone
     */
    @Subscribe
    public void onReceive(MessageToSync m){
        if(m.getTo() == this.id){
            this.clock.setClock(this.clock.getClock()+1);
            this.boiteAuxLettreSync.add(m);
        }
    }

    /**
     * Envoi un message en broadcast
     * @param o Le contenu du message
     */
    public void broadcast(Object o){
        this.clock.setClock(this.clock.getClock()+1);
        BroadcastMessage bm = new BroadcastMessage(o);
        bm.setFrom(this.id);
        bm.setLamport(this.clock.getClock());
        bus.postEvent(bm);
    }

    /**
     * Envoi un message dédié asynchrone
     * @param o Le contenu du message
     * @param to Le destinataire du message
     */
    public void sendTo(Object o, int to){
        if(to != this.id){
            this.clock.setClock(this.clock.getClock()+1);
            MessageTo mt = new MessageTo(o, to);
            mt.setFrom(this.id);
            mt.setLamport(this.clock.getClock());
            bus.postEvent(mt);
        }
    }

    /**
     * Gère les requête de section critique
     */
    public void requestSC(){
        while(!etat.equals("null")){
            try{
                Thread.sleep(100);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        etat = "request";
        while(!etat.equals("SC")){
            try{
                Thread.sleep(100);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère la fin d'une section critique
     */
    public void releaseSC(){
        etat = "release";
    }

    /**
     * Gère la barrière de synchronisation.
     */
    public void synchronize(){
        if(this.listCommSync == null){
            this.listCommSync = new int[Communicateur.nbComm];
            Arrays.fill(this.listCommSync, 0);
        }
        this.clock.setClock(this.clock.getClock()+1);
        Synchronization sync = new Synchronization(this.clock.getClock(), this.id);
        bus.postEvent(sync);
        this.listCommSync[this.id] = this.clock.getClock();
        while (Arrays.binarySearch(this.listCommSync, 0) != -1) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.clock.setClock(Arrays.stream(this.listCommSync).max().getAsInt() + 1);
        this.listCommSync = null;
    }


    /**
     * Envoi un message dédié synchrone
     * @param o Le contenu du message
     * @param dest Le destinataire du message
     */
    public void sendToSync(Object o, int dest){
        this.clock.setClock(this.clock.getClock()+1);
        MessageToSync mts = new MessageToSync(o, dest);
        mts.setFrom(this.id);
        mts.setLamport(this.clock.getClock());
        bus.postEvent(mts);
        int index = -1;

        while (index == -1) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < this.boiteAuxLettreSync.size(); i++){
                if(boiteAuxLettreSync.get(i).from == dest){
                    index = i;
                }
            }
        }

        int msgClock = this.boiteAuxLettreSync.get(index).getLamport();
        this.boiteAuxLettreSync.remove(index);

        this.clock.setClock(max(this.clock.getClock(), msgClock));
    }

    /**
     * Gère la réception des messages asynchrones
     * @param from L'emetteur du message asynchrone attendu
     * @return Le contenu du message
     */
    public Object recevFromSync(int from){

        int index = -1;

        while (index == -1) {
            for (int i = 0; i < this.boiteAuxLettreSync.size(); i++){
                if(boiteAuxLettreSync.get(i).from == from){
                    index = i;
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object o = this.boiteAuxLettreSync.get(index).getPayload();
        int msgClock = this.boiteAuxLettreSync.get(index).getLamport();
        boiteAuxLettreSync.remove(index);

        MessageToSync mts = new MessageToSync("reçu", from);
        mts.setFrom(this.id);
        mts.setLamport(this.clock.getClock());
        bus.postEvent(mts);

        this.clock.setClock(max(this.clock.getClock(), msgClock));
        return o;
    }

    public ArrayList<Message> getBoiteAuxLettre() {
        return boiteAuxLettre;
    }
}


