package com.olap3.cubeexplorer.Similarity.Session;

import com.olap3.cubeexplorer.Similarity.Query.QueryCompJaccardStructureThresWithSeveralSelectionPerLevel;
import com.olap3.cubeexplorer.Similarity.Query.QueryComparison;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.julien.QuerySession;

import java.io.Serializable;

/**
 * Scoring matrix.
 * 
 * @author Elisa Turricchia
 */
public class Matrix implements Serializable {

    /**
     * Matrix id (or name)
     */
    private String id = null;
    /**
     * Scores
     */
    private double[][] scores = null;
    private double[][] increasing_function_matrix = null;
    private int n;
    private int m;
    private QueryComparison queryComparison;
    /*Weight for group by set*/
    private double alpha;
    /*Weight for selection*/
    private double beta;
    /*Weight for measure*/
    private double gamma;

    public int columnCount() {
        return m;
    }

    public int rowCount() {
        return n;
    }
    private QuerySession session1;
    private QuerySession session2;

    public Matrix(QuerySession s1, QuerySession s2, double alpha, double beta, double gamma) {
        this.session1 = s1;
        this.session2 = s2;
        n = s1.size() + 1;
        m = s2.size() + 1;
        scores = new double[n][m];
        increasing_function_matrix = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                increasing_function_matrix[i][j] = 1;
            }
        }
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;

    }

    private void setQueryComparison(Qfset qi, Qfset qj) {
        queryComparison = new QueryCompJaccardStructureThresWithSeveralSelectionPerLevel(qi, qj, alpha, beta, gamma);
    }

    public void fillMatrix_MatchMisMatch(double threshold) {

        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                double similarity = queryComparison.computeSimilarity().getSimilarity();

                /*if(similarity>=threshold) //calcolo similarità
                scores[i+1][j+1]=match;
                else
                scores[i+1][j+1]=mismatch;*/
                if (similarity == threshold) {
                    scores[i + 1][j + 1] = (double) 0.01;
                } else {
                    scores[i + 1][j + 1] = similarity - threshold;
                }
            }
        }
    }

    public void fillMatrix_Similarity() {
        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                //Get negative value
                double sim = queryComparison.computeSimilarity().getSimilarity();
                scores[i + 1][j + 1] = sim;
            }
        }
    }

    public void fillMatrix_Threshold(double threshold) {
        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                double similarity = queryComparison.computeSimilarity().getSimilarity();
                if (similarity >= threshold) //calcolo similarità
                {
                    scores[i + 1][j + 1] = similarity;
                } else {
                    scores[i + 1][j + 1] = similarity - threshold;
                }
            }
        }
    }

    public void fillMatrix_Threshold_Opposite(double threshold) {
        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                double similarity = queryComparison.computeSimilarity().getSimilarity();
                if (similarity >= threshold) {
                    scores[i + 1][j + 1] = similarity;
                } else {
                    scores[i + 1][j + 1] = -(1 - similarity);
                }
            }
        }
    }

    public void applyAsymmetricIncreasingFunction() {
        //1+1/(1+e^-x)
        int index = 0;
        for (int i = n - 1; i > 0; i--) {
            for (int j = 0; j < m; j++) //if(scores[i][j]>0)
            {
                scores[i][j] *= 1 + 1 / (1 + Math.exp(index));
            }
            index++;
        }
    }
    //Two-dimensional logistic sigmoid
    //Maximum time discount=0.66
    //Slope=4

    public void applySymmetricIncreasingFunction() {
        double MaxTimeDiscount = 0.1;
        int slope = 5;
        for (int k = 0; k < n - 1; k++) {
            int i = (n - 1) - k;
            for (int t = 0; t < m - 1; t++) {
                int j = (m - 1) - t;
                //if(scores[i][j]>0)
                increasing_function_matrix[k][t] = (double) (1 - ((1 - MaxTimeDiscount) / (1 + Math.exp(slope - k - t))));
                scores[i][j] *= increasing_function_matrix[k][t];
            }
        }

    }

    public Matrix(String id, double[][] scores) {
        this.id = id;
        this.scores = scores;
    }

    public QuerySession getSession1() {
        return session1;
    }

    public void setSession1(QuerySession session1) {
        this.session1 = session1;
    }

    public QuerySession getSession2() {
        return session2;
    }

    public void setSession2(QuerySession session2) {
        this.session2 = session2;
    }

    public double increasing_function(int i, int j) {
        return this.increasing_function_matrix[i][j];
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return Returns the scores.
     */
    public double[][] getScores() {
        return this.scores;
    }

    /**
     * 
     * @param a
     * @param b
     * @return score
     */
    public double getScore(int a, int b) {
        return this.scores[a][b];
    }
}