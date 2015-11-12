package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;

/****
 * @author David
 *
 */
public class Stitching {
	BufferedImage img;
	BufferedImage final_img;
	
	//Length of the image (original size)
	int image_x_length = 0;
	int image_y_length = 0;
	
	//Length of the result, current image * 2, 4 times larger
	int result_x_length = 0;
	int result_y_length = 0;
	
	RGB[][] final_arr;
	String filename;
	
	public final int dim = 40;
	public final double magnification = 1.8;
	
	HashMap<Integer, Integer> tbl = new HashMap<Integer, Integer>();
	
	public void init(){
		load_images();
		set_image();
	}
	
	public void load_sample(String filename){
		final URL resource = getClass().getResource(filename);
		BufferedImage i = null;
		try {
		    i = ImageIO.read(resource);
		} catch (IOException e) {
			System.out.println("Invalid file name");
		}
		
		
		Color c = new Color(i.getRGB(0, 0));
		RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
		System.out.println(color.luminance());
	}	
		
	public void load_images(){
		
		Tuple<Integer, Integer> prev_coordinates = new Tuple<Integer, Integer>(0,0);//just used as coordinates
		
		for(int i = 1; i < 183; i++){
			 filename = "/res/UC-3-10" + i + ".png"; 
			
			prev_coordinates = actual_loading_code(prev_coordinates);
		}
	}
	
	/* 
	 * This method loads a picture, identifies its center and uses the parameters to determine what part we want
	 * @return 
	 * 		It returns a tuple of integers containing the coordinates of the center. We use this center to compare to the previous one
	 * 		So we have an idea of what is the distance between them, so we can shift
	 */
	private Tuple<Integer, Integer> actual_loading_code(Tuple<Integer, Integer> previous){
		final URL resource = getClass().getResource(filename);
		
		try {
		    img = ImageIO.read(resource);
		} catch (IOException e) {
			System.out.println("Invalid file name");
		}
		
		//Base case. First picture only.
		if(image_x_length == 0 || image_y_length == 0){
			//Times two to account for extra length when magnifiying
			image_x_length = img.getWidth();
			image_y_length = img.getHeight();
			result_x_length = image_x_length * 2;
			result_y_length = image_y_length * 2;
			
			final_img = new BufferedImage(result_x_length, result_y_length, BufferedImage.TYPE_INT_RGB);
			
			final_arr = new RGB[result_x_length][result_y_length];
			for(int y = 0; y < result_y_length; y++){
				for(int x = 0; x < result_x_length; x++)
					final_arr[x][y] = new RGB(255,255,255);
			}
		}
		//Identify the center.
		int x_pos = 0;
		int y_pos = 0;
		int num = 0;
		
		//This for loop extracts the natural RGB values from the entire picture and records the summation of a bunch of coordinates
		//That have a luminance value above some amount
		for(int y = 0; y < image_y_length; y++){
			for(int x = 0; x < image_x_length; x++){
				
				Color c = new Color(img.getRGB(x, y));
				RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
				//if(( color.luminance() > 130)){
				
				//We only take values that are above a certain luminance value. This signifies that this is part of the thing we want. 
				//Alternatively, we could just make it above a certain average. This will have to be discussed.
				if((color.luminance() < 90)){
					num++;
					x_pos+=x;
					y_pos+=y;
				}
			}
		}
		
		//At this point, we average the values to get the center coordinate
		//This find the difference between the previous center value, We want to shift the the location of the image by 1.8 * these shifts
		if(num != 0){
			x_pos = x_pos/num;
			y_pos = y_pos/num;
		}
		else{
			x_pos = 0;
			y_pos = 0;
		}
		
		int x_shift;
		int y_shift;
		
		if(previous.get_left() == 0 && previous.get_right() == 0){
			x_shift = 0;
			y_shift = 0;
			
		}
		else{
			
			x_shift = (int) ((x_pos - previous.get_left()) * magnification);
			y_shift = (int) ((y_pos - previous.get_right()) * magnification);
			
		}
		System.out.println("X Coordinate: " + x_pos + " Y Coordinate: " + y_pos);

		
		//Now we take everything in a square around this coordinate and copy everything in the square into an array.
		for(int y = y_pos; y < y_pos + (dim/2); y++){
			for(int x = x_pos - (dim/2); x < x_pos + (dim/2); x++){
				
				//This gets the color of each pixel in the square and records it into the array, overriding previous values
				Color c = new Color(img.getRGB(x, y));
				RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
				
				if(!color.compare(final_arr[x + x_shift][y + y_shift])){
					final_arr[x+x_shift][y + y_shift] = color;
				}
			}
		}
		return new Tuple<Integer, Integer>(x_pos, y_pos);
	}
		
	/**
	 * This method takes the array we have generated and outputs it into an image we generate
	 */
	public void set_image() {
		
		for(int y = 0; y < result_y_length; y++){
			for(int x = 0; x < result_x_length; x++){

				//Get each pixel, convert to color coordinates, and set the x, y coordinate on the image to that color
				RGB pixel = final_arr[x][y];
				if(pixel != null){
					int col = (pixel.red << 16 ) | (pixel.green << 8)| pixel.blue;
					final_img.setRGB(x, y, col);
				}
				else final_img.setRGB(x,y, 0);
			}
		}
		
		File f = new File("output.png");
		try {
			ImageIO.write(final_img, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
