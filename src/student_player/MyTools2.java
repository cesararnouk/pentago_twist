package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import boardgame.Board;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoMove;

public class MyTools2 {
    public static Node root;

    public static boolean AreBoardsEqual(Piece[][] board1, Piece[][] board2, int boardSize) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board1[i][j] != board2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}

class Node {
    private PentagoBoardState state;
    private Node parent;
    private ArrayList<Node> childArray;
    private PentagoMove move;
    private int numVisited;
    private int numWon;

    public Node(PentagoBoardState state) {
        this.state = state;
        childArray = new ArrayList<>();
        numVisited = 0;
    }

    public Node(PentagoBoardState state, Node parent, ArrayList<Node> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
    }

    public Node(Node node) {
        this.state = (PentagoBoardState) node.getState().clone();

        if (node.getParent() != null)
            this.parent = node.getParent();

        this.childArray = new ArrayList<>();
        ArrayList<Node> childArray = node.getChildArray();
        for (Node child : childArray) {
            this.childArray.add(new Node(child));
        }
    }

    public PentagoMove getMove() {
        return move;
    }

    public void setMove(PentagoMove move) {
        this.move = move;
    }

    public int getNumVisited() {
        return numVisited;
    }

    public void incrementNumVisited() {
        numVisited++;
    }

    public void incrementNumWon() {
        if (this.numWon != Integer.MIN_VALUE) {
            numWon++;
        }
    }

    public int getNumWon() {
        return numWon;
    }

    public void setNumWon(int numWon) {
        this.numWon = numWon;
    }

    public PentagoBoardState getState() {
        return state;
    }

    public void setState(PentagoBoardState state) {
        this.state = state;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public ArrayList<Node> getChildArray() {
        return childArray;
    }

    public void setChildArray(ArrayList<Node> childArray) {
        this.childArray = childArray;
    }

    public Node getRandomChild() {
        int noOfPossibleMoves = this.childArray.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        return this.childArray.get(selectRandom);
    }

    public Node getBestChildToPlay() {
        return Collections.max(this.childArray, Comparator.comparing(c -> {
            return c.getNumVisited();
        }));
    }

    public Node getBestChildToExpand() {
        Node node = this;
        while (node.getChildArray().size() != 0) {
            node = UCT.getChildWithBestUCTScore(node);
        }
        return node;
    }

    public void expand() {
        ArrayList<PentagoMove> possibleMoves = this.getState().getAllLegalMoves();

        for (PentagoMove move : possibleMoves) {
            PentagoBoardState clone = (PentagoBoardState) this.state.clone();
            clone.processMove(move);

            Node newNode = new Node(clone);
            newNode.setMove(move);
            newNode.setParent(this);
            this.getChildArray().add(newNode);
        }
    }

    public void backPropogation(boolean didWin) {
        Node tempNode = this;
        while (tempNode != null) {
            tempNode.incrementNumVisited();
            if (didWin) {
                tempNode.incrementNumWon();
            }
            tempNode = tempNode.getParent();
        }
    }

    public int simRandomGame(int playerNum) {
        Node tempNode = new Node(this);
        PentagoBoardState tempState = tempNode.getState();
        int winner = tempState.getWinner();

        if (winner == 1 - playerNum) {
            tempNode.getParent().setNumWon(Integer.MIN_VALUE);
            return winner;
        }

        while (winner == Board.NOBODY) {
            PentagoMove nextMove = (PentagoMove) tempState.getRandomMove();
            tempState.processMove(nextMove);
            winner = tempState.getWinner();
        }

        return winner;
    }
}

class UCT {
    public static double calculateUCTScore(int numVisitedTotal, int numWonNode, int numVisitedNode) {
        if (numVisitedNode == 0) {
            return Integer.MAX_VALUE;
        }
        return ((double) numWonNode / (double) numVisitedNode)
                + Math.sqrt(2) * Math.sqrt(Math.log(numVisitedTotal) / (double) numVisitedNode);
    }

    public static Node getChildWithBestUCTScore(Node parent) {
        int numVisitedParent = parent.getNumVisited();
        return Collections.max(parent.getChildArray(),
                Comparator.comparing(c -> calculateUCTScore(numVisitedParent, c.getNumWon(), c.getNumVisited())));
    }
}