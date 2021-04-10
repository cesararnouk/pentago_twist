package student_player;

import boardgame.Board;
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
        ArrayList<PentagoMove> moves = node.state.getAllLegalMoves();
        for (PentagoMove m : moves) {
            node.children.add(new SearchTreeNode(node, m));
        }
    }


    /**
     * default policy is randomness
     * @return either 0 or 1 depending on win / loss
     */
    public static int simulateRandomRollout(SearchTreeNode node) { // default policy is randomness
        SearchTreeNode tmp = node;
        PentagoBoardState tmpState = (PentagoBoardState) node.state.clone();
        if (tmp.state.gameOver()) { // terminal node
            if (tmp.state.getWinner() == tmp.state.getTurnPlayer()) { // win
                return 1;
            } else {    // loss
                return 0;
            }
        }
        while (tmpState.getWinner() == Board.NOBODY) {
            ArrayList<PentagoMove> moves = tmp.state.getAllLegalMoves();
            int index = (int)(Math.random() * moves.size()); // pick random move
            tmpState.processMove(moves.get(index));
        }

        return (tmpState.getWinner() == tmp.state.getTurnPlayer()) ? 1 : 0;

    }


    /**
     * method to back progagate and adjust value of predecessor nodes
     * @param currentNode
     * @param playOutResult
     */
    public static void backPropagation(SearchTreeNode currentNode, int playOutResult) {
        SearchTreeNode tmp = currentNode;
        while (tmp != null) {
            if (playOutResult == Board.DRAW) {
                tmp.nWins += 0.5;
            } else {
                tmp.nWins += playOutResult;
            }
            tmp.totalSimulations++;
            tmp = tmp.parent;
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
                Comparator.comparing(c -> c.UCB));
    }


    /**
     * return the next best move
     * @param root
     */
    public static PentagoMove nextBestMove(SearchTreeNode root) {
        long endTime = System.currentTimeMillis() + 1800;
        SearchTreeNode tmp = root;
        int result;
        while (System.currentTimeMillis() < endTime) {
            if (tmp.children.isEmpty()) { // leaf node
                if (tmp.totalSimulations == 0) { // not yet explored
                    result = simulateRandomRollout(tmp);
                    backPropagation(tmp, result);
                } else {
                    expand(tmp);
                    SearchTreeNode child = tmp.children.get(0);
                    result = simulateRandomRollout(child); // choosing first new child (fully arbitrary)
                    backPropagation(child, result);
                }
            } else {
                tmp = getMaxUCB(tmp.children);
            }
        }

        return tmp.move;

    }

}