import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    public int numRounds;
    public Set<Transaction> pendingTransactions;
    public boolean[] followees;
    public double p_malicious;
    public double p_txDistribution;
    public double p_graph;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.numRounds = numRounds;
        this.pendingTransactions = new HashSet<Transaction>();
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.p_graph= p_graph;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> getProposals() {
        return pendingTransactions;
    }

    public void receiveCandidates(ArrayList<Integer[]> candidates) {
        for(Integer[] cand : candidates) { 
            if (followees[cand[1]]){
                pendingTransactions.add(new Transaction(cand[0]));
            }
        }
    }
}
