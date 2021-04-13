package student_player;

import boardgame.Board;
import boardgame.Player;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyTools {

    /**
     * generate all children nodes
     * @param node
     */
    public static void expand(SearchTreeNode node) {
        ArrayList<PentagoMove> moves = node.getState().getAllLegalMoves();
        for (PentagoMove m : moves) {
            node.getChildren().add(new SearchTreeNode(node, m));
        }
    }


    /**
     * default policy is randomness
     * @return either 0 or 1 depending on win / loss
     */
    public static int simulateRandomRollout(SearchTreeNode node, int player) { // default policy is randomness
        SearchTreeNode tmp = new SearchTreeNode(node);
//        SearchTreeNode tmp = node;
        PentagoBoardState tmpState = tmp.getState();
        if (tmpState.gameOver()) { // terminal node
            if (tmpState.getWinner() == player) { // win
                return 1;
            } else {    // loss
                return 0;
            }
        }
        while (!tmpState.gameOver()) {
//            ArrayList<PentagoMove> moves = tmp.state.getAllLegalMoves();
//            int index = (int)(Math.random() * moves.size()); // pick random move
            tmpState.processMove((PentagoMove) tmpState.getRandomMove());
        }
        int win = (tmpState.getWinner() == player) ? 1 : 0;
        return win;
    }


    /**
     * method to back progagate and adjust value of predecessor nodes
     * @param currentNode
     * @param playOutResult
     */
    public static void backPropagation(SearchTreeNode currentNode, int playOutResult) {
        SearchTreeNode tmp = currentNode;
        while (tmp != null) {
            tmp.incrementTotalSimulations();
            if (playOutResult == Board.DRAW) {
                tmp.halfIncrementnWins();
            } else if (playOutResult == 1) {
                tmp.incrementnWins();
            }
            tmp.computeUCB();
//            if (tmp.getParent() == null) {
//                System.out.println("total sims in root: " + tmp.getTotalSimulations());
//            }
            tmp = tmp.getParent();
        }
    }


    /**
     * return child with highest UCB
     * @param children
     * @return
     */
    public static SearchTreeNode getMaxUCB(ArrayList<SearchTreeNode> children) {
        for (SearchTreeNode child : children) {
            child.computeUCB();
        }
        return Collections.max(children,
                Comparator.comparing(c -> c.getUCB()));
    }


    /**
     * return the next best move
     * @param root
     */
    public static PentagoMove nextBestMove(SearchTreeNode root, int player) {
        long endTime = System.currentTimeMillis() + 2000;
        int result;
        while (System.currentTimeMillis() < endTime) {
            SearchTreeNode tmp;
            // getting best child of root
            if (root.getChildren().size() != 0) {
                tmp = getMaxUCB(root.getChildren());
            } else {
                tmp = root;
            }
            if (tmp.getTotalSimulations() == 0) { // not yet visited so don't expand
                result = simulateRandomRollout(tmp, player);
                backPropagation(tmp, result);
            } else {
                expand(tmp);
                SearchTreeNode randomChild = tmp.getChildren().get((int) (Math.random() * tmp.getChildren().size()));
                result = simulateRandomRollout(randomChild, player); // choosing first new child (fully arbitrary)
                backPropagation(randomChild, result);
            }
        }

        //SearchTreeNode toRemove = null;
        for(SearchTreeNode node: root.getChildren()) {
            if (node.getState().getWinner() == player)
                return node.getMove(); // assuring that winning move is taken if possible
            else if (node.getState().getWinner() == 1 - player) {
                System.out.println("win giveaway avoidance successful");
                // avoiding giving away win with own move
                //toRemove = node;
                root.removeChild(node);
            }
        }
        //root.removeChild(toRemove);

        SearchTreeNode bestNode = getMaxUCB(root.getChildren());

        // super super useful method
        int i = 0;
        while (potentialOpponentWin(bestNode, player)) { // while moves lead to potential opponent win
            root.removeChild(bestNode);
            bestNode = getMaxUCB(root.getChildren());
        }

        return bestNode.getMove();
    }


/////////////////////////////////////////////////////////////////// -----------------------
    // ADDITIONAL IMPROVEMENTS
/////////////////////////////////////////////////////////////////// ------------------------

    public static boolean potentialOpponentWin(SearchTreeNode bestNode, int player) {
        // avoid allowing potential win for opponent
        PentagoBoardState currentState = (PentagoBoardState) bestNode.getState().clone();
        ArrayList<PentagoMove> opponentLegalMoves = currentState.getAllLegalMoves();
        for (PentagoMove opponentMove : opponentLegalMoves) {
            currentState = (PentagoBoardState) bestNode.getState().clone();
            currentState.processMove(opponentMove);
            // opponent potential next move leads to win for them
            if (currentState.getWinner() == 1 - player)
                return true;
        }
        return false;
    }

}