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
	
	//These variables are used for the Sobel edge generator
	private Matrix3 Sx = new Matrix3(-1, 0,  1, -2, 0, 2, -1, 0, 1);
	private Matrix3 Sy = new Matrix3(-1, 2, -1,  0, 0,  0, 1, 2, 1);
	private double[][] Sx_map;
	private double[][] Sy_map;
	private double[][] S_result;
	
	private int width;
	private int height;
	
	
	public void test(String filename){
		BufferedImage im = load_image(filename);
		if(im == null)
			return;
		
		blur();
		sobel();
		set_image();
	}
	
	/**
	 * This method applies a gaussian blur to the original image
	 */
	private void gaussian_blur(){
		
	}

	private void blur(){
		for(int i = 1; i < width-1; i++){
			for(int j = 1; j < height-1; j++){
				double sum = 0;
				//We construct the target matrix
				for(int k = i-1; k <= i+1; k++){
					for(int l = j-1; l <= j+1; l++){
						sum += image_map.get(new Tuple<Integer, Integer>(k,l)).luminance();
					}
					blur_map.put(new Tuple<Integer,Integer>(i,j), sum/9);
				}
			}
		}
		for(int i = 0; i < width; i++){
			blur_map.put(new Tuple<Integer, Integer>(i, 0), blur_map.get(new Tuple<Integer, Integer>(i, 1)));
			blur_map.put(new Tuple<Integer, Integer>(i, height - 1), blur_map.get(new Tuple<Integer, Integer>(i, height - 2)));
		}
		for(int i = 0; i < height; i++){
			blur_map.put(new Tuple<Integer, Integer>(0, i), blur_map.get(new Tuple<Integer, Integer>(1, i)));
			blur_map.put(new Tuple<Integer, Integer>(width-1, i), blur_map.get(new Tuple<Integer, Integer>(width-2, i)));
		}
	}
	
	/**
	 * This method applies the convolution of the two matrices to the result from after the blur
	 */
	private void sobel(){
		//Constructing the Sobel matrix
		for(int i = 1; i < width-1; i++){
			for(int j = 1; j < height-1; j++){
				double[][] matrix = new double[3][3];
				
				//We construct the target matrix
				for(int k = i-1; k <= i+1; k++){
					for(int l = j-1; l <= j+1; l++){
						matrix[k-i+1][l-j+1] = blur_map.get(new Tuple<Integer, Integer>(k,l));
					}
				}
				
				Sx_map[i][j] = Sx.convolution(matrix);
				Sy_map[i][j] = Sy.convolution(matrix);

				double x = Sx_map[i][j];
				double y = Sy_map[i][j];
				
				double magnitude = magnitude(x,y);
				
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
