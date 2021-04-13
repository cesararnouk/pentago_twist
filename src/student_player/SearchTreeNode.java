package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;

public class SearchTreeNode {
    private int nWins;
    private int totalSimulations;
    private SearchTreeNode parent;
    private PentagoMove move;
    private PentagoBoardState state;
    private ArrayList<SearchTreeNode> children;
    private double UCB;


    // access methods (getters and setters)
    public int getnWins() {
        return nWins;
    }

    public void setnWins(int nWins) {
        this.nWins = nWins;
    }

    public void incrementnWins() {
        this.nWins++;
    }

    public void halfIncrementnWins() {
        this.nWins += 0.5;
    }

    public int getTotalSimulations() {
        return totalSimulations;
    }

    public void setTotalSimulations(int totalSimulations) {
        this.totalSimulations = totalSimulations;
    }

    public void incrementTotalSimulations() {
        this.totalSimulations++;
    }

    public SearchTreeNode getParent() {
        return parent;
    }

    public void setParent(SearchTreeNode parent) {
        this.parent = parent;
    }

    public PentagoMove getMove() {
        return move;
    }

    public void setMove(PentagoMove move) {
        this.move = move;
    }

    public PentagoBoardState getState() {
        return state;
    }

    public void setState(PentagoBoardState state) {
        this.state = state;
    }

    public ArrayList<SearchTreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<SearchTreeNode> children) {
        this.children = children;
    }

    public double getUCB() {
        return UCB;
    }

    public void setUCB(double UCB) {
        this.UCB = UCB;
    }

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
        this.children = new ArrayList<SearchTreeNode>();
        this.UCB = Integer.MAX_VALUE;
    }

    public SearchTreeNode(PentagoBoardState boardState) {
        this.nWins = 0;
        this.totalSimulations = 0;
        this.parent = null;
        this.move = null;
        this.state = (PentagoBoardState) boardState.clone();
        this.children = new ArrayList<SearchTreeNode>();
    }

    public SearchTreeNode(SearchTreeNode current) {
        this.state = (PentagoBoardState) current.state.clone();
        if (current.parent != null)
            this.parent = current.parent;
        this.children = new ArrayList<>();
        ArrayList<SearchTreeNode> currentChildren = current.children;
        for (SearchTreeNode child : currentChildren) {
            this.children.add(child);
        }
    }

    // method to compute the UCB value for a given node
    public void computeUCB() {
        if (this.parent != null) {
            if (totalSimulations == 0) this.UCB = Integer.MAX_VALUE;
            else {
                this.UCB = nWins / totalSimulations + Math.sqrt(2.0 * Math.log(parent.totalSimulations) / totalSimulations);
            }
        }
    }

    public void removeChild(SearchTreeNode node) {
        this.children.remove(node);
    }
}
