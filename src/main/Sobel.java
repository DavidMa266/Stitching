package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Sobel {
	

	
	//Image map represents the RGB colors of the original image
	private HashMap<Tuple<Integer, Integer>, RGB> image_map = new HashMap<Tuple<Integer, Integer>, RGB>();
	private HashMap<Tuple<Integer, Integer>, Double> blur_map = new HashMap<Tuple<Integer, Integer>, Double>();
	
	private double stdev = 1.4;
	private double g_constant = 1/ (2 * Math.PI * stdev * stdev);
	
	//These dims must be odd > 1
	private int blur_dim = 5;
	private int sobel_dim = 3;
	
	double[] G_matrix;
	
	//These variables are used for the Sobel edge generator
	private double[] Sx = new double[]{-1,  0,  1, -2, 0, 2, -1, 0, 1};
	private double[] Sy = new double[]{-1, -2, -1,  0, 0,  0, 1, 2, 1};
	private double[][] Sx_map;
	private double[][] Sy_map;
	private double[][] S_result;
	
	private int width;
	private int height;
	
	
	public void test(String filename){
		BufferedImage im = load_image(filename);
		if(im == null)
			return;
		gaussian_blur();
		sobel();
		set_image();
	}
	
	/**
	 * This method applies a gaussian blur to the original image
	 */
	private void gaussian_blur(){
	
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
				double[] target_matrix = new double[blur_dim * blur_dim];

				index = 0;
				for(int k = i-blur_dim/2; k <= i+blur_dim/2; k++){
					for(int l = j-blur_dim/2; l <= j+blur_dim/2; l++){
						target_matrix[index]= image_map.get(new Tuple<Integer, Integer>(k,l)).luminance();
						index++;
					}
				}
				double res = 0;

				//Image convolution
				for(int k = 0; k < blur_dim * blur_dim; k++)
					res += G_matrix[k] * target_matrix[k];
				blur_map.put(new Tuple<Integer, Integer>(i,j), res);
				
			}
		}
	}
	
	
	private void blur(){
		for(int i = blur_dim/2; i < width-blur_dim/2; i++){
			for(int j = blur_dim/2; j < height-blur_dim/2; j++){
				double sum = 0;
				//We construct the target matrix
				for(int k = i-blur_dim/2; k <= i+blur_dim/2; k++){
					for(int l = j-blur_dim/2; l <= j+blur_dim/2; l++){
						sum += image_map.get(new Tuple<Integer, Integer>(k,l)).luminance();
					}
					blur_map.put(new Tuple<Integer,Integer>(i,j), sum/(blur_dim * blur_dim));
				}
			}
		}
		for(int i = 0; i < width; i++){
			blur_map.put(new Tuple<Integer, Integer>(i, 0), blur_map.get(new Tuple<Integer, Integer>(i, 1)));
			blur_map.put(new Tuple<Integer, Integer>(i, height - blur_dim/2), blur_map.get(new Tuple<Integer, Integer>(i, height - 1 - blur_dim/2)));
		}
		for(int i = 0; i < height; i++){
			blur_map.put(new Tuple<Integer, Integer>(0, i), blur_map.get(new Tuple<Integer, Integer>(1, i)));
			blur_map.put(new Tuple<Integer, Integer>(width-blur_dim/2, i), blur_map.get(new Tuple<Integer, Integer>(width-blur_dim/2 - 1, i)));
		}
	}
	
	
	/**
	 * This method applies the convolution of the two matrices to the result from after the blur
	 */
	private void sobel(){
		//Constructing the Sobel matrix
		
		
		for(int i = blur_dim/2 + sobel_dim/2; i < width-blur_dim/2-sobel_dim/2; i++){
			for(int j = blur_dim/2 + sobel_dim/2; j < height-blur_dim/2-sobel_dim/2; j++){
				double[] target_matrix = new double[sobel_dim * sobel_dim];
				int index = 0;
				//We construct the target matrix
				for(int k = i-sobel_dim/2; k <= i+sobel_dim/2; k++){
					for(int l = j-sobel_dim/2; l <= j+sobel_dim/2; l++){
						target_matrix[index] = blur_map.get(new Tuple<Integer, Integer>(k,l));
						index++;
					}
				}
				double x_res = 0;
				double y_res = 0;
				
				//This is essentially image convolution
				for(int k = 0; k < sobel_dim * sobel_dim; k++){
					x_res += Sx[k] * target_matrix[k];
					y_res += Sy[k] * target_matrix[k];
				}

				Sx_map[i][j] = x_res;
				Sy_map[i][j] = y_res;
				double magnitude = magnitude(x_res, y_res);
				
				S_result[i][j] = magnitude;
			}
		}
	}
	
	/**
	 * Sets the image using the results generated from G_result
	 */
	private void set_image() {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int color = (int)S_result[x][y];
				color = color << 16 | color << 8 | color;
				//Get each pixel, convert to color coordinates, and set the x, y coordinate on the image to that color
				img.setRGB(x, y, color);
			}
		}
		File f = new File("output.png");
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
			Sx_map = new double[width][height];
			Sy_map = new double[width][height];
			S_result = new double[width][height];	
			
			//Loading in the initial file
			for(int i = 0; i < width; i++){
				for(int j = 0; j < height; j++){
					Color c = new Color(im.getRGB(i, j));
					RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
					
					image_map.put(new Tuple<Integer, Integer>(i, j), color);
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
