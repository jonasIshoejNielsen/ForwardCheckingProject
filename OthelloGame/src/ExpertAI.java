public class ExpertAI implements IOthelloAI {

    @Override
    public Position decideMove(GameState s) {
        return new MinMaxWizardWithPruning().alphaBetaSearch(s);
    }

}
