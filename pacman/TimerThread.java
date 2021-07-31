package games.pacman;

public class TimerThread extends Thread {
    PacmanEngine pacman;


    public TimerThread(PacmanEngine pm){
        pacman = pm;
    }
    @Override
    public void run(){
        while(true) {
            try {
                Thread.sleep(PacmanEngine.TICK_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(pacman.tick()){
                break;
            }


        }
    }
}
