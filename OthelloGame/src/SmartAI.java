import java.util.ArrayList;


public class SmartAI implements IOthelloAI {

    public Position decideMove(GameState s){
        return new MinMaxWizard().minMaxDecision(s);
    }

}
