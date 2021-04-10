package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;

public class SearchTreeNode {
    public int nWins;
    public int totalSimulations;
    public SearchTreeNode parent;
    public PentagoMove move;
    public PentagoBoardState state;
    public ArrayList<SearchTreeNode> children;

    // constructors
    public SearchTreeNode(SearchTreeNode parent, PentagoMove move) {
        this.nWins = 0;
        this.totalSimulations = 0;
        this.parent = parent;
        this.move = move;
        // new state
        PentagoBoardState newState = (PentagoBoardState) parent.state.clone();
        newState.processMove(move);
        this.state = newState;
    }

    // method to compute the UCB value for a given node
    public double computeUCB(SearchTreeNode node) {
        if (totalSimulations == 0) return Integer.MAX_VALUE;
        return nWins / totalSimulations + Math.sqrt(2.0*Math.log(parent.totalSimulations)/totalSimulations);
    }
}
