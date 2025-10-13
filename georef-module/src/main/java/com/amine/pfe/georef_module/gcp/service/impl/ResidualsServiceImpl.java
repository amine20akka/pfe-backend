package com.amine.pfe.georef_module.gcp.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;
import com.amine.pfe.georef_module.gcp.service.port.ResidualsService;

/**
 * Implementation of ResidualsService for computing transformation residuals and
 * RMSE
 * in the image georeferencing module.
 * 
 * <p>
 * This service performs polynomial transformations of varying degrees (1st,
 * 2nd, 3rd)
 * and calculates accuracy metrics to assess georeferencing quality. It uses QR
 * decomposition
 * via Householder reflections for numerically stable least squares solutions.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Support for polynomial transformations (affine, 2nd degree, 3rd
 * degree)</li>
 * <li>Numerically stable QR decomposition for parameter estimation</li>
 * <li>Handles both projected (Euclidean) and geographic (WGS84) coordinate
 * systems</li>
 * <li>Computes individual residuals and overall RMSE</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see ResidualsService
 */
@Service
public class ResidualsServiceImpl implements ResidualsService {

    /**
     * Determines the minimum number of Ground Control Points required for each
     * transformation type.
     * 
     * <p>
     * The minimum number corresponds to the number of coefficients in the
     * polynomial transformation:
     * </p>
     * <ul>
     * <li>Polynomial 1st degree: 3 parameters → 3 points minimum</li>
     * <li>Polynomial 2nd degree: 6 parameters → 6 points minimum</li>
     * <li>Polynomial 3rd degree: 10 parameters → 10 points minimum</li>
     * </ul>
     * 
     * @param transformationType the type of polynomial transformation
     * @return the minimum number of GCPs required
     */
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

    /**
     * Validates whether sufficient GCPs are provided for the requested
     * transformation type.
     * 
     * @param gcps the list of Ground Control Points
     * @param type the transformation type to validate against
     * @return true if the number of GCPs meets or exceeds the minimum requirement
     */
    @Override
    public boolean hasEnoughGCPs(List<GcpDto> gcps, TransformationType type) {
        int minPoints = getMinimumPointsRequired(type);
        return gcps.size() >= minPoints;
    }

    /**
     * Computes transformation residuals and Root Mean Square Error (RMSE) for the
     * given GCPs.
     * 
     * <p>
     * This method implements the complete workflow for residual computation:
     * </p>
     * <ol>
     * <li>Extract source (image) and map coordinates from GCPs</li>
     * <li>Build polynomial design matrix based on transformation degree</li>
     * <li>Solve least squares problem using QR decomposition</li>
     * <li>Apply transformation to compute estimated map coordinates</li>
     * <li>Calculate residuals (distance between actual and estimated
     * positions)</li>
     * <li>Compute overall RMSE</li>
     * </ol>
     * 
     * <p>
     * Distance calculation method depends on the SRID:
     * </p>
     * <ul>
     * <li>SRID 4326 (WGS84): Haversine formula for geographic coordinates</li>
     * <li>Other SRIDs: Euclidean distance for projected coordinates</li>
     * </ul>
     * 
     * @param gcps the list of Ground Control Points containing source and map
     *             coordinates
     * @param type the polynomial transformation type (determines degree)
     * @param srid the Spatial Reference System Identifier (affects distance
     *             calculation)
     * @return ResidualsResult containing individual residuals and RMSE
     * @throws IllegalArgumentException if the transformation type is invalid
     */
    @Override
    public ResidualsResult computeResiduals(List<GcpDto> gcps, TransformationType type, Srid srid) {
        // Determine polynomial degree from transformation type
        int degree = switch (type) {
            case POLYNOMIALE_1 -> 1;
            case POLYNOMIALE_2 -> 2;
            case POLYNOMIALE_3 -> 3;
        };

        // Extract coordinates from GCPs
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

        // Build the design matrix (polynomial feature matrix)
        double[][] A = buildDesignMatrix(X, Y, degree);

        // Solve least squares problem using QR decomposition
        // Separate parameter sets for X and Y transformations
        double[] paramsX = solveLeastSquaresQR(A, mapX);
        double[] paramsY = solveLeastSquaresQR(A, mapY);

        // Apply transformation to obtain estimated map coordinates
        double[] estimatedX = new double[n];
        double[] estimatedY = new double[n];

        for (int i = 0; i < n; i++) {
            double[] features = getFeatures(X[i], Y[i], degree);
            estimatedX[i] = dotProduct(features, paramsX);
            estimatedY[i] = dotProduct(features, paramsY);
        }

        // Calculate residuals (distance between actual and estimated positions)
        List<Double> residuals = new ArrayList<>();
        double sumSquaredResiduals = 0.0;

        for (int i = 0; i < n; i++) {
            double residual;
            // Choose distance calculation method based on coordinate system
            if (srid == Srid._4326) {
                // Geographic coordinates: use Haversine formula
                residual = haversineDistance(mapY[i], mapX[i], estimatedY[i], estimatedX[i]);
            } else {
                // Projected coordinates: use Euclidean distance
                residual = euclideanDistance(mapX[i], mapY[i], estimatedX[i], estimatedY[i]);
            }

            residuals.add(residual);
            sumSquaredResiduals += residual * residual;
        }

        // Calculate Root Mean Square Error
        double rmse = Math.sqrt(sumSquaredResiduals / n);

        return new ResidualsResult(residuals, rmse);
    }

    /**
     * Constructs the design matrix (A) for polynomial transformation.
     * 
     * <p>
     * The design matrix contains polynomial features for each GCP:
     * </p>
     * <ul>
     * <li>Degree 1 (Affine): [x, y, 1] - 3 coefficients</li>
     * <li>Degree 2: [x², y², xy, x, y, 1] - 6 coefficients</li>
     * <li>Degree 3: [x³, y³, x²y, xy², x², y², xy, x, y, 1] - 10 coefficients</li>
     * </ul>
     * 
     * <p>
     * The matrix A has dimensions [n × m] where n is the number of points
     * and m is the number of coefficients for the given degree.
     * </p>
     * 
     * @param X      array of x-coordinates (source image coordinates)
     * @param Y      array of y-coordinates (source image coordinates)
     * @param degree polynomial degree (1, 2, or 3)
     * @return 2D array representing the design matrix
     * @throws IllegalArgumentException if degree is not 1, 2, or 3
     */
    private double[][] buildDesignMatrix(double[] X, double[] Y, int degree) {
        int n = X.length;
        double[][] A;

        if (degree == 1) {
            // Affine transformation: 3 parameters
            A = new double[n][3];
            for (int i = 0; i < n; i++) {
                A[i][0] = X[i];
                A[i][1] = Y[i];
                A[i][2] = 1;
            }
        } else if (degree == 2) {
            // 2nd degree polynomial: 6 parameters
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
            // 3rd degree polynomial: 10 parameters
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
     * Solves the least squares problem Ax = b using QR decomposition via
     * Householder reflections.
     * 
     * <p>
     * This method is numerically more stable than solving the normal equations (A^T
     * A x = A^T b)
     * and is consistent with scientific libraries like SciPy's lstsq function.
     * </p>
     * 
     * <p>
     * Algorithm steps:
     * </p>
     * <ol>
     * <li>Perform QR decomposition: A = QR where Q is orthogonal and R is upper
     * triangular</li>
     * <li>Use Householder reflections for numerical stability</li>
     * <li>Compute Q^T * b</li>
     * <li>Solve R * x = Q^T * b by back substitution</li>
     * </ol>
     * 
     * <p>
     * Numerical stability features:
     * </p>
     * <ul>
     * <li>Threshold check (norm > 1e-10) to avoid division by zero</li>
     * <li>Sign adjustment in Householder vector construction for improved
     * stability</li>
     * <li>Proper normalization of Householder vectors</li>
     * </ul>
     * 
     * @param A the design matrix [m × n] where m ≥ n
     * @param b the right-hand side vector [m × 1]
     * @return solution vector x [n × 1] that minimizes ||Ax - b||²
     */
    private double[] solveLeastSquaresQR(double[][] A, double[] b) {
        int m = A.length; // Number of rows (points)
        int n = A[0].length; // Number of columns (parameters)

        // Initialize Q as identity matrix and R as copy of A
        double[][] Q = new double[m][m];
        double[][] R = new double[m][n];

        // Copy A into R
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, R[i], 0, n);
        }

        // Initialize Q to identity matrix
        for (int i = 0; i < m; i++) {
            Q[i][i] = 1.0;
        }

        // Perform QR decomposition using Householder reflections
        for (int k = 0; k < n; k++) {
            // Construct Householder vector for column k
            double[] u = new double[m - k];
            double norm = 0.0;

            // Extract column k from row k onwards
            for (int i = k; i < m; i++) {
                u[i - k] = R[i][k];
                norm += u[i - k] * u[i - k];
            }
            norm = Math.sqrt(norm);

            if (norm > 1e-10) { // Avoid division by zero
                // Adjust sign for numerical stability
                if (u[0] >= 0) {
                    u[0] += norm;
                } else {
                    u[0] -= norm;
                }

                // Recalculate norm of u
                double uNorm = 0.0;
                for (double val : u) {
                    uNorm += val * val;
                }
                uNorm = Math.sqrt(uNorm);

                // Normalize u to create unit Householder vector
                for (int i = 0; i < u.length; i++) {
                    u[i] /= uNorm;
                }

                // Update R: R = (I - 2uu^T)R
                for (int j = k; j < n; j++) {
                    double dot = 0.0;
                    for (int i = 0; i < u.length; i++) {
                        dot += u[i] * R[i + k][j];
                    }

                    for (int i = 0; i < u.length; i++) {
                        R[i + k][j] -= 2.0 * u[i] * dot;
                    }
                }

                // Update Q: Q = Q(I - 2uu^T)
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

        // Transpose Q to obtain Q^T
        double[][] Qt = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Qt[i][j] = Q[j][i];
            }
        }

        // Compute Q^T * b
        double[] Qtb = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Qtb[i] += Qt[i][j] * b[j];
            }
        }

        // Solve R * x = Q^T * b by back substitution
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
     * Generates polynomial features for a single coordinate pair.
     * 
     * <p>
     * Creates a feature vector matching the design matrix structure:
     * </p>
     * <ul>
     * <li>Degree 1: [x, y, 1]</li>
     * <li>Degree 2: [x², y², xy, x, y, 1]</li>
     * <li>Degree 3: [x³, y³, x²y, xy², x², y², xy, x, y, 1]</li>
     * </ul>
     * 
     * @param x      the x-coordinate
     * @param y      the y-coordinate
     * @param degree the polynomial degree (1, 2, or 3)
     * @return feature vector for the given coordinate pair
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

    /**
     * Computes the dot product of two vectors.
     * 
     * <p>
     * Used to apply transformation parameters to feature vectors:
     * result = a · b = Σ(aᵢ * bᵢ)
     * </p>
     * 
     * @param a first vector
     * @param b second vector (must have same length as a)
     * @return dot product of the two vectors
     */
    private double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    /**
     * Calculates Euclidean (straight-line) distance between two points in projected
     * coordinate systems.
     * 
     * <p>
     * Formula: distance = √((x₂-x₁)² + (y₂-y₁)²)
     * </p>
     * 
     * <p>
     * Used for metric coordinate systems such as UTM, State Plane, Web Mercator,
     * etc.
     * </p>
     * 
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return Euclidean distance between the two points (in coordinate system
     *         units)
     */
    private double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculates great-circle distance between two points on Earth's surface using
     * the Haversine formula.
     * 
     * <p>
     * The Haversine formula provides accurate distance calculations for geographic
     * coordinates (lat/lon)
     * by accounting for Earth's curvature. This is essential for WGS84 (EPSG:4326)
     * coordinate systems.
     * </p>
     * 
     * <p>
     * Formula:
     * </p>
     * 
     * <pre>
     * a = sin²(Δlat/2) + cos(lat₁) * cos(lat₂) * sin²(Δlon/2)
     * c = 2 * atan2(√a, √(1-a))
     * distance = R * c
     * </pre>
     * 
     * <p>
     * Where R is Earth's radius (6,371 km or 6,371,000 meters).
     * </p>
     * 
     * @param lat1 latitude of first point (in degrees)
     * @param lon1 longitude of first point (in degrees)
     * @param lat2 latitude of second point (in degrees)
     * @param lon2 longitude of second point (in degrees)
     * @return great-circle distance between the two points in meters
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth's radius in meters

        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}