//From: http://introcs.cs.princeton.edu/java/95linear/Matrix.java.html

/*************************************************************************
 *  Compilation:  javac Matrix.java
 *  Execution:    java Matrix
 *
 *  A bare-bones immutable data type for M-by-N matrices.
 *
 *************************************************************************/

package zapu.net.render;

final public class Matrix {
    private final int M;             // number of rows
    private final int N;             // number of columns
    private final double[][] data;   // M-by-N array

    // create M-by-N matrix of 0's
    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new double[M][N];
    }
    
    public double value(int x, int y) {
    	return data[x][y];
    }
    
    public void set(int x, int y, double val) {
    	data[x][y] = val;
    }

    // create matrix based on 2d array
    public Matrix(double[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }

    // copy constructor
    public Matrix(Matrix A) { this(A.data); }

    // create and return a random M-by-N matrix with values between 0 and 1
    public static Matrix random(int M, int N) {
        Matrix A = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[i][j] = Math.random();
        return A;
    }

    // create and return the N-by-N identity matrix
    public static Matrix identity(int N) {
        Matrix I = new Matrix(N, N);
        for (int i = 0; i < N; i++)
            I.data[i][i] = 1;
        return I;
    }

    // swap rows i and j
    private void swap(int i, int j) {
        double[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public Matrix transpose() {
        Matrix A = new Matrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public Matrix plus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j];
        return C;
    }


    // return C = A - B
    public Matrix minus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j];
        return C;
    }

    // does A = B exactly?
    public boolean eq(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // return C = A * B
    public Matrix times(Matrix B) {
        Matrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }


    // return x = A^-1 b, assuming A is square and has full rank
    public Matrix solve(Matrix rhs) {
        if (M != N || rhs.M != N)// || rhs.N != 1)
            throw new RuntimeException("Illegal matrix dimensions.");

        // create copies of the data
        Matrix A = new Matrix(this);
        Matrix b = new Matrix(rhs);

        // Gaussian elimination with partial pivoting
        for (int i = 0; i < N; i++) {

            // find pivot row and swap
            int max = i;
            for (int j = i + 1; j < N; j++)
                if (Math.abs(A.data[j][i]) > Math.abs(A.data[max][i]))
                    max = j;
            A.swap(i, max);
            b.swap(i, max);

            // singular
            if (A.data[i][i] == 0.0) throw new RuntimeException("Matrix is singular.");

            // pivot within b
            for (int j = i + 1; j < N; j++)
                b.data[j][0] -= b.data[i][0] * A.data[j][i] / A.data[i][i];

            // pivot within A
            for (int j = i + 1; j < N; j++) {
                double m = A.data[j][i] / A.data[i][i];
                for (int k = i+1; k < N; k++) {
                    A.data[j][k] -= A.data[i][k] * m;
                }
                A.data[j][i] = 0.0;
            }
        }

        // back substitution
        Matrix x = new Matrix(N, 1);
        for (int j = N - 1; j >= 0; j--) {
            double t = 0.0;
            for (int k = j + 1; k < N; k++)
                t += A.data[j][k] * x.data[k][0];
            x.data[j][0] = (b.data[j][0] - t) / A.data[j][j];
        }
        return x;
   
    }

    // print matrix to standard output
    public void show() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) 
                System.out.printf("%9.4f ", data[i][j]);
            System.out.println();
        }
    }

    public Matrix inverse()
    {
    	Matrix m = new Matrix(4,4);
    	return m.inverse(this);
    }
    public Matrix inverse(Matrix original) {
    		for (int i = 0; i < N; i++)
    			data[i][i] = 1;
    	    
    	    if (M == 1)
    	    {
    	      set(0, 0, 1 / original.value(0, 0));
    	      return this;
    	    }

    	    Matrix b = new Matrix(original);

    	    int n = M;
    	    for (int i = 0; i < n; i++)
    	    {
    	      // find pivot
    	      double mag = 0;
    	      int pivot = -1;

    	      for (int j = i; j < n; j ++)
    	      {
    	        double mag2 = Math.abs(b.value(j, i));
    	        if (mag2 > mag)
    	        {
    	          mag = mag2;
    	          pivot = j;
    	        }
    	      }

    	      // no pivot (error)
    	      if (pivot == -1 || mag == 0)
    	      {
    	         return this;
    	      }

    	      // move pivot row into position
    	      if (pivot != i)
    	      {
    	        double temp;
    	        for (int j = i; j < n; j ++)
    	        {
    	          temp = b.value(i, j);
    	          set(i, j, b.value(pivot, j));
    	          b.set(pivot, j, temp);
    	        }

    	        for (int j = 0; j < n; j ++)
    	        {
    	          temp = value(i, j);
    	          set(i, j, value(pivot, j));
    	          set(pivot, j, temp);
    	        }
    	      }

    	      // normalize pivot row
    	      mag = b.value(i, i);
    	      for (int j = i; j < n; j ++) b.set(i, j, b.value(i, j) / mag);
    	      for (int j = 0; j < n; j ++) set(i, j, value(i, j) / mag);

    	      // eliminate pivot row component from other rows
    	      for (int k = 0; k < n; k ++)
    	      {
    	        if (k == i) continue;
    	        double mag2 = b.value(k, i);

    	        for (int j = i; j < n; j ++) b.set(k, j, b.value(k, j) - mag2 * b.value(i, j));
    	        for (int j = 0; j < n; j ++) set(k, j, value(k, j) - mag2 * value(i, j));
    	      }
    	    }
    	    return this;
    	  }


    // test client
    public static void main(String[] args) {
        double[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3} };
        Matrix D = new Matrix(d);
        D.show();        
        System.out.println();

        Matrix A = Matrix.random(5, 5);
        A.show(); 
        System.out.println();

        A.swap(1, 2);
        A.show(); 
        System.out.println();

        Matrix B = A.transpose();
        B.show(); 
        System.out.println();

        Matrix C = Matrix.identity(5);
        C.show(); 
        System.out.println();

        A.plus(B).show();
        System.out.println();

        B.times(A).show();
        System.out.println();

        // shouldn't be equal since AB != BA in general    
        System.out.println(A.times(B).eq(B.times(A)));
        System.out.println();

        Matrix b = Matrix.random(5, 1);
        b.show();
        System.out.println();

        Matrix x = A.solve(b);
        x.show();
        System.out.println();

        A.times(x).show();
        
        Matrix asd = new Matrix(new double[][] {
        		{10, -9, -12},
        		{7, -12, 11},
        		{-10, 10, 3},
        });
        asd.inverse().show();
        
    }
    
    public static Matrix FromVector3(Vector3 v)
    {
    	return new Matrix(new double[][] { {v.xyz[0]}, {v.xyz[1]}, {v.xyz[2]}, {1}});
    }
    
    public Vector3 ToVector3ByW() {
    	return new Vector3(value(0, 0) / value(3, 0), value(1, 0) / value(3, 0), value(2, 0) / value(3, 0));
    }
    
    public Vector3 ToVector3Col() {
    	return new Vector3(value(0, 0), value(1, 0), value(2, 0));
    }
    
    public Vector3 ToVector3Row() {
    	return new Vector3(value(0, 0), value(0, 1), value(0, 2));
    }
}