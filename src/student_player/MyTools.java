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
            if (tmp.getParent() == null) {
                System.out.println("total sims in root: " + tmp.getTotalSimulations());
            }
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
        long endTime = System.currentTimeMillis() + 1800;
//        SearchTreeNode tmp = new SearchTreeNode(root);
//        SearchTreeNode tmp = root;
        int result;
        while (System.currentTimeMillis() < endTime) {
            if (root.getChildren() == null || root.getChildren().isEmpty()) { // leaf node
                if (root.getTotalSimulations() == 0) { // not yet explored
                    result = simulateRandomRollout(root, player);
                    backPropagation(root, result);
//                    System.out.println("got here");
//                    System.out.println("root wins: " + root.getnWins());
//                    System.out.println("root ni: " + root.getTotalSimulations());
                } else {
                    expand(root);
//                    System.out.println("how many children in tmp? : " + tmp.children.size());
//                    SearchTreeNode randomChild = root.getChildren().get(0);
                    SearchTreeNode randomChild = root.getChildren().get((int) (Math.random() * root.getChildren().size()));
                    result = simulateRandomRollout(randomChild, player); // choosing first new child (fully arbitrary)
                    backPropagation(randomChild, result);
                }
            } else {
//                System.out.println("did i get here?");
                root = getMaxUCB(root.getChildren());
            }
        }

        return root.getMove();

    }

}