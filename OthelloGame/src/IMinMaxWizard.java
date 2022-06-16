import java.util.List;

public interface IMinMaxWizard {
    Position minMaxDecision(GameState gs);
    int maxValue(GameState gs, int depth);
    int minValue(GameState gs, int depth);
    int utility(GameState gs);
    List<Position> actions(GameState gs);
    boolean terminalTest(GameState gs, int depth);
    GameState result(GameState gs, Position a);
}
