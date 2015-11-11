package main;

public class Matrix3 {

	
	public final int size = 3;
	public double[][] matrix;
	
	
	public Matrix3(int m11, int m12, int m13, int m21, int m22, int m23, int m31, int m32, int m33){
		matrix = new double[][]{ 	new double[]{m11, m12, m13},
									new double[]{m21, m22, m23},
									new double[]{m31, m32, m33}
		};
	}
	
	public Matrix3(double[][] mat){
		this.matrix = mat;
	}
		
	/**
	 * The standard image convolution 
	 * @param m
	 * @return
	 */
	public double convolution(Matrix3 m){
		return convolution(m.matrix);
	}

	
	/**
	 * Image convolution
	 * @param matrix
	 * @return
	 */
	public double convolution(double[][] matrix){
		double sum = 0; 
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				sum += this.matrix[i][j] * matrix[i][j];
			}
		}
		return sum;
	}
	
	/**
	 * Returns THIS * m
	 * @param m
	 * @return
	 */
	public Matrix3 multiply_after(Matrix3 m){
		double[] arr = new double[size * size];
		int index = 0;
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				double sum = 0;
				for(int k = 0; k < size; k++)
					sum += this.matrix[i][k] * m.matrix[k][j]; 
				arr[index] = sum;
				index++;
			}
		}
		
		double[][] ma = new double[size][size];
		for(int i = 0; i < size* size; i++){
			int row = i / size;
			int col = i % size;
			ma[row][col] = arr[i];
		}
		return new Matrix3(ma);
	}

	
	/**
	 * Prints the values of the matrix in nice row column format
	 */
	public void print(){
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++)
				System.out.print(matrix[i][j] + "\t");
			System.out.println();
		}
	}
}
