package com.amine.pfe.georef_module.gcp.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;
import com.amine.pfe.georef_module.gcp.service.port.ResidualsService;

@Service
public class ResidualsServiceImpl implements ResidualsService {

    @Override
    public int getMinimumPointsRequired(TransformationType transformationType) {
        switch (transformationType) {
            case POLYNOMIALE_1:
                return 3;
            case POLYNOMIALE_2:
                return 6;
            case POLYNOMIALE_3:
                return 10;
            default:
                return 3;
        }
    }

    @Override
    public boolean hasEnoughGCPs(List<GcpDto> gcps, TransformationType type) {
        int minPoints = getMinimumPointsRequired(type);
        return gcps.size() >= minPoints;
    }

    @Override
    public ResidualsResult computeResiduals(List<GcpDto> gcps, TransformationType type, Srid srid) {
        int degree = switch (type) {
            case POLYNOMIALE_1 -> 1;
            case POLYNOMIALE_2 -> 2;
            case POLYNOMIALE_3 -> 3;
        };

        int n = gcps.size();
        double[] X = new double[n];
        double[] Y = new double[n];
        double[] mapX = new double[n];
        double[] mapY = new double[n];

        for (int i = 0; i < n; i++) {
            GcpDto gcp = gcps.get(i);
            X[i] = gcp.getSourceX();
            Y[i] = gcp.getSourceY();
            mapX[i] = gcp.getMapX();
            mapY[i] = gcp.getMapY();
        }

        // Création de la matrice A (matrice de design)
        double[][] A = buildDesignMatrix(X, Y, degree);

        // Résolution des moindres carrés avec une méthode plus robuste
        double[] paramsX = solveLeastSquaresQR(A, mapX);
        double[] paramsY = solveLeastSquaresQR(A, mapY);

        // Application de la transformation pour obtenir les points estimés
        double[] estimatedX = new double[n];
        double[] estimatedY = new double[n];

        for (int i = 0; i < n; i++) {
            double[] features = getFeatures(X[i], Y[i], degree);
            estimatedX[i] = dotProduct(features, paramsX);
            estimatedY[i] = dotProduct(features, paramsY);
        }

        // Calcul des résidus
        List<Double> residuals = new ArrayList<>();
        double sumSquaredResiduals = 0.0;

        for (int i = 0; i < n; i++) {
            double residual;
            if (srid == Srid._4326) {
                residual = haversineDistance(mapY[i], mapX[i], estimatedY[i], estimatedX[i]);
            } else {
                residual = euclideanDistance(mapX[i], mapY[i], estimatedX[i], estimatedY[i]);
            }

            residuals.add(residual);
            sumSquaredResiduals += residual * residual;
        }

        double rmse = Math.sqrt(sumSquaredResiduals / n);

        return new ResidualsResult(residuals, rmse);
    }

    private double[][] buildDesignMatrix(double[] X, double[] Y, int degree) {
        int n = X.length;
        double[][] A;
        if (degree == 1) {
            A = new double[n][3];
            for (int i = 0; i < n; i++) {
                A[i][0] = X[i];
                A[i][1] = Y[i];
                A[i][2] = 1;
            }
        } else if (degree == 2) {
            A = new double[n][6];
            for (int i = 0; i < n; i++) {
                A[i][0] = X[i] * X[i];
                A[i][1] = Y[i] * Y[i];
                A[i][2] = X[i] * Y[i];
                A[i][3] = X[i];
                A[i][4] = Y[i];
                A[i][5] = 1;
            }
        } else if (degree == 3) {
            A = new double[n][10];
            for (int i = 0; i < n; i++) {
                A[i][0] = X[i] * X[i] * X[i];
                A[i][1] = Y[i] * Y[i] * Y[i];
                A[i][2] = X[i] * X[i] * Y[i];
                A[i][3] = X[i] * Y[i] * Y[i];
                A[i][4] = X[i] * X[i];
                A[i][5] = Y[i] * Y[i];
                A[i][6] = X[i] * Y[i];
                A[i][7] = X[i];
                A[i][8] = Y[i];
                A[i][9] = 1;
            }
        } else {
            throw new IllegalArgumentException("Degree must be 1, 2 or 3");
        }
        return A;
    }

    /**
     * Résout le problème des moindres carrés Ax = b en utilisant la décomposition
     * QR,
     * qui est numériquement plus stable que la méthode d'élimination de Gauss.
     * Cette méthode est plus proche de l'implémentation scipy.linalg.lstsq de
     * Python.
     */
    private double[] solveLeastSquaresQR(double[][] A, double[] b) {
        int m = A.length; // Nombre de lignes (points)
        int n = A[0].length; // Nombre de colonnes (paramètres)

        // Implémentation de la décomposition QR avec Householder
        double[][] Q = new double[m][m];
        double[][] R = new double[m][n];

        // Copie de A dans R pour commencer
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, R[i], 0, n);
        }

        // Initialiser Q à la matrice identité
        for (int i = 0; i < m; i++) {
            Q[i][i] = 1.0;
        }

        // Effectuer la décomposition QR
        for (int k = 0; k < n; k++) {
            // Construire le vecteur de Householder
            double[] u = new double[m - k];
            double norm = 0.0;

            for (int i = k; i < m; i++) {
                u[i - k] = R[i][k];
                norm += u[i - k] * u[i - k];
            }
            norm = Math.sqrt(norm);

            if (norm > 1e-10) { // Éviter la division par zéro
                // Ajuster le signe pour améliorer la stabilité numérique
                if (u[0] >= 0) {
                    u[0] += norm;
                } else {
                    u[0] -= norm;
                }

                // Recalculer la norme de u
                double uNorm = 0.0;
                for (double val : u) {
                    uNorm += val * val;
                }
                uNorm = Math.sqrt(uNorm);

                // Normaliser u
                for (int i = 0; i < u.length; i++) {
                    u[i] /= uNorm;
                }

                // Mettre à jour R
                for (int j = k; j < n; j++) {
                    double dot = 0.0;
                    for (int i = 0; i < u.length; i++) {
                        dot += u[i] * R[i + k][j];
                    }

                    for (int i = 0; i < u.length; i++) {
                        R[i + k][j] -= 2.0 * u[i] * dot;
                    }
                }

                // Mettre à jour Q
                for (int j = 0; j < m; j++) {
                    double dot = 0.0;
                    for (int i = 0; i < u.length; i++) {
                        dot += u[i] * Q[j][i + k];
                    }

                    for (int i = 0; i < u.length; i++) {
                        Q[j][i + k] -= 2.0 * u[i] * dot;
                    }
                }
            }
        }

        // Transposer Q pour obtenir Q^T
        double[][] Qt = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Qt[i][j] = Q[j][i];
            }
        }

        // Calculer Q^T * b
        double[] Qtb = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Qtb[i] += Qt[i][j] * b[j];
            }
        }

        // Résoudre R * x = Q^T * b par substitution arrière
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += R[i][j] * x[j];
            }
            x[i] = (Qtb[i] - sum) / R[i][i];
        }

        return x;
    }

    /**
     * Alternative utilisant l'approche SVD (Singular Value Decomposition) pour
     * résoudre
     * le problème des moindres carrés, similaire à scipy.linalg.lstsq.
     * Cela nécessite une bibliothèque externe comme Apache Commons Math.
     */
    /*
     * private double[] solveLeastSquaresSVD(double[][] A, double[] b) {
     * // Pour utiliser cette méthode, il faudrait ajouter Apache Commons Math comme
     * dépendance
     * // et utiliser le code ci-dessous:
     * 
     * // RealMatrix matrixA = new Array2DRowRealMatrix(A);
     * // SingularValueDecomposition svd = new SingularValueDecomposition(matrixA);
     * // RealVector vectorB = new ArrayRealVector(b);
     * // DecompositionSolver solver = svd.getSolver();
     * // RealVector solution = solver.solve(vectorB);
     * // return solution.toArray();
     * 
     * // Pour l'instant, rediriger vers notre implémentation QR
     * return solveLeastSquaresQR(A, b);
     * }
     */

    private double[] getFeatures(double x, double y, int degree) {
        if (degree == 1) {
            return new double[] { x, y, 1 };
        } else if (degree == 2) {
            return new double[] { x * x, y * y, x * y, x, y, 1 };
        } else {
            return new double[] {
                    x * x * x, y * y * y, x * x * y, x * y * y, x * x, y * y, x * y, x, y, 1
            };
        }
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Rayon Terre en mètres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
