package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Sobel {
	

	
	//Image map represents the RGB colors of the original image
	private RGB[][] image_map;
	private int[][] blur_map;
	
	private double stdev;
	
	//These dims must be odd and > 1
	private int blur_dim;
	private final int sobel_dim = 3;
	
	double[] G_matrix;
	
	//These variables are used for the Sobel edge generator
	private double[] Sx = new double[]{-1, 0,  1, -3, 0, 3, -1, 0, 1};
	private double[] Sy = new double[]{-1, -3, -1,  0, 0,  0, 1, 3, 1};
	private int[][] S_result;
	private boolean[][] S_threshold;
	private double[][] S_orientation;

	
	private int width;
	private int height;
	
	public Sobel(double stdev, int blur_dim){
		this.stdev = stdev;

		this.blur_dim = blur_dim;
		if(blur_dim %2 == 0){
			blur_dim ++;
		}
	}
	
	public void test(String filename, int weak, int strong, String destination){
		long startTime = System.nanoTime();
		BufferedImage im = load_image(filename);
		if(im == null)
			return;
		gaussian_blur();
		sobel();
		non_maximum_suppression(weak, strong);
		set_image(destination);		
		long endTime = System.nanoTime();
		System.out.println("Took "+(endTime - startTime) + " ns"); 
	}
	
	/**
	 * This method applies a Gaussian blur to the original image, with variable stdev and dimensions.
	 * The new 'image' is placed into blur_map
	 */
	private void gaussian_blur(){

		double g_constant = 1/ (2 * Math.PI * stdev * stdev);
		G_matrix = new double[blur_dim * blur_dim];
		int index = 0;
		//We construct the Gauss matrix
		for(int k = -blur_dim/2; k <= blur_dim/2; k++){
			for(int l = -blur_dim/2; l <= blur_dim/2; l++){
				double gauss = g_constant * Math.pow(Math.E, -1 * ((Math.pow(k, 2) + Math.pow(l, 2))/(2 * stdev * stdev)));
				G_matrix[index] = gauss;
				index++;
			}
		}
		
		//Now we apply the G_matrix to the image_map and place the luminosity values in blur_map
		for(int i = blur_dim/2; i < width-blur_dim/2; i++){
			for(int j = blur_dim/2; j < height-blur_dim/2; j++){
				//Now we construct the target matrix		
				int[] target_matrix = new int[blur_dim * blur_dim];

				index = 0;
				for(int k = i-blur_dim/2; k <= i+blur_dim/2; k++){
					for(int l = j-blur_dim/2; l <= j+blur_dim/2; l++){
						target_matrix[index]= (int) image_map[k][l].luminance();
						index++;
					}
				}
				double res = 0;

				//Image convolution
				for(int k = 0; k < blur_dim * blur_dim; k++)
					res += G_matrix[k] * target_matrix[k];
				blur_map[i][j] = (int) res;
				
			}
		}
	}
	
	
	/**
	 * This method applies the convolution of the two matrices to the result from after the blur
	 * Magnitudes are placed in S_result
	 * Angles are placed in S_orientation
	 */
	private void sobel(){
		//Constructing the Sobel matrix
	
		for(int i = blur_dim/2 + sobel_dim/2; i < width-blur_dim/2-sobel_dim/2; i++){
			for(int j = blur_dim/2 + sobel_dim/2; j < height-blur_dim/2-sobel_dim/2; j++){
				int[] target_matrix = new int[sobel_dim * sobel_dim];
				int index = 0;
				//We construct the target matrix
				for(int k = i-1; k <= i+1; k++){
					for(int l = j-1; l <= j+1; l++){
						target_matrix[index] = blur_map[k][l];
						index++;
					}
				}
				int x_res = 0;
				int y_res = 0;
				
				//This is essentially image convolution
				for(int k = 0; k < sobel_dim * sobel_dim; k++){
					x_res += Sx[k] * target_matrix[k];
					y_res += Sy[k] * target_matrix[k];
				}
				//x_res and y_res both represent the change in luminosities from the x and y directions respectively
				//Thus the total overall change is found by computing the magnitude
				//Since we also have the relative changes, by using the atan2 function, we can find the angle and thus direction of the edge
				double magnitude = magnitude(x_res, y_res);

				S_result[i][j] = (int) magnitude;
				//Conversion to degrees
				S_orientation[i][j] = Math.atan2(x_res, y_res) * 180 / Math.PI;
			}
		}
	}
	
	/**
	 * Non maximum suppression finds the local maxima and suppresses values that are not local maxima
	 * When we found the direction with atan2 in sobel(), this found the direction of greatest increase
	 * We compare the current pixel to the points in along that direction. If this pixel is greater, then
	 * we can assume it is a local max.
	 * If it is not, it is not an edge and is suppressed.
	 * Values are also filtered through a dual threshold as per the Canny Edge Detector
	 * 
	 * Resultant values are placed in S_result
	 * Blur_map is zeroed out for use later in hough()
	 * @param weak
	 * 		Weak threshold for the Canny
	 * @param strong
	 * 		Strong threshold
	 */
	private void non_maximum_suppression(int weak, int strong){
		for(int i = 1; i < width-1; i++){
			for(int j = 1; j < height-1; j++){
				//Zero out blur map for later use in Hough()
				blur_map[i][j] = 0;
				int ne = S_result[i+1][j-1];
				int e = S_result[i+1][j];
				int se = S_result[i+1][j+1];
				int s = S_result[i][j+1];
				int sw = S_result[i-1][j+1];
				int w = S_result[i-1][j];
				int nw = S_result[i-1][j-1];
				int n = S_result[i][j-1];
				
				int c = S_result[i][j];
				
				//Getting directions
				int f_target = 0;
				int f_next = 0;
				int b_target = 0;
				int b_next = 0;
				double degrees = S_orientation[i][j];
				
				int direction = (int) (degrees / 45);
				switch (direction){
				case -4:
					f_target = w;
					f_next = sw;
					b_target = e;
					b_next = ne;
					break;
				case -3:
					f_target = sw;
					f_next = s;
					b_target = ne;
					b_next = n;
					break;
				case -2:
					f_target = s;
					f_next = se;
					b_target = n;
					b_next = nw;
					break;
				case -1:
					f_target = se;
					f_next = e;
					b_target = nw;
					b_next = w;
					break;				
				case 0:
					f_target = e;
					f_next = ne;
					b_target = w;
					b_next = sw;
					break;
				case 1:
					f_target = ne;
					f_next = n;
					b_target = sw;
					b_next = s;
				case 2:
					f_target = n;
					f_next = nw;
					b_target = s;
					b_next = se;
					break;
				case 3:
					f_target = nw;
					f_next = w;
					b_target = se;
					b_next = e;
					break;
				case 4:
					f_target = w;
					f_next = sw;
					b_target = e;
					b_next = ne;
					break;
				}
				
			//	double f_res = f_target;
			//	double b_res = b_target;
			//	double overflow = degrees %45;
			//	if(overflow > 22){
			//		f_res = f_next;
			//		b_res = b_next;
			//	}
				
				//Linear interpolation to find a more accurate value
				double overflow = ((double)(degrees %45))/45;
				
				double f_res = f_target * (1-overflow) + f_next * overflow;
				double b_res = b_target * (1-overflow) + b_next * overflow;
				
				//Remove values that are less than their directions
				if(c <= f_res || c <= b_res)
					S_threshold[i][j] = true;
				//This preserves the local maxima
			}
		}
		//We assign true to all values that are not maxima
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				
				//Eliminate
				if(S_threshold[i][j]){
					S_result[i][j] = 0;
					S_threshold[i][j] = false;
				}
				//STRONG edge, 
				else if(S_result[i][j] > strong)
					S_threshold[i][j] = true;
				//NO edge. WEAK edges are not changed
				else if(S_result[i][j] < weak)
					S_result[i][j] = 0;
			}
		}
		
		//Another iteration to pick out valid weak edges
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				//WEAK or NONE
				S_result[i][j] = 0;
				if(!S_threshold[i][j]){
					//WEAK 
					if(S_result[i][j] != 0){
						//Has strong edge, preserve
						if(strong_edge(i,j))
							S_result[i][j] = 120;
						else
							S_result[i][j] = 0;
					}
					//NONE is ignored
				}
				//STRONG
				else
					S_result[i][j] = 255;
			}
		}
		
		int x = 0;
		int y = 0;
		int num = 0;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				
				if(S_result[i][j] > 0){
					x += i;
					y += j;
					num++;
				}
			}
		}
		x /= num;
		y /= num;
		S_result[x][y] = 255;
	}
	
	/**
	 * Function when given an x y coordinate sees if any surrounding pixels are 'strong edges'
	 * @param x
	 * 		X coordinate
	 * @param y
	 * 		Y coordinate
	 * @return
	 * 		Boolean if a strong edge has been detected.
	 */
	private boolean strong_edge(int x, int y){
		return S_threshold[x][y-1] ||  S_threshold[x][y+1] ||
				 S_threshold[x-1][y-1] ||  S_threshold[x-1][y] ||  S_threshold[x-1][y+1] ||
				 S_threshold[x+1][y-1] ||  S_threshold[x+1][y] ||  S_threshold[x+1][y+1];  
	}
		
	
	
	/**
	 * Sets the image using the results generated from S_result named @destination
	 */
	private void set_image(String destination) {

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int color = S_result[x][y];
				color = color << 16 | color << 8 | color;
				//Get each pixel, convert to color coordinates, and set the x, y coordinate on the image to that color
				img.setRGB(x, y, color);
			}
		}
		File f = new File(destination);
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finished");
	}
	
	/**
	 * This function loads and returns an image with the given filename
	 * @param filename
	 * @return
	 */
	private BufferedImage load_image(String filename){
		final URL resource = getClass().getResource(filename);
		BufferedImage im = null;
		try {
		    im = ImageIO.read(resource);
		} catch (IOException e) {
			System.out.println("Invalid file name");
		}
		
		if(im != null){
			width = im.getWidth();
			height = im.getHeight();
			image_map = new RGB[width][height];
			blur_map = new int[width][height];
			S_result = new int[width][height];	
			S_threshold = new boolean[width][height];
			S_orientation = new double[width][height];
			//Loading in the initial file
			for(int i = 0; i < width; i++){
				for(int j = 0; j < height; j++){
					Color c = new Color(im.getRGB(i, j));
					RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
					
					image_map[i][j] = color;
				}
			}
		}
		return im;
	}	
	
	/**
	 * This function produces the magnitude of two numbers
	 * @param x
	 * @param y
	 * @return
	 */
	public double magnitude(double x, double y){
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
}
