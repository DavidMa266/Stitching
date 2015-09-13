package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Stitching {


	BufferedImage img;
	BufferedImage final_img;
	
	int x_length = 0;
	int y_length = 0;
	
	RGB[][] final_arr;
	String filename;
	
	int dim = 32;
	
	HashMap<Integer, Integer> tbl = new HashMap<Integer, Integer>();
	
	public void init(){
		load_images();
		set_image();
	}
	
	
	public void load_images(){
		for(int i = 0; i < 103; i+=3){
			if(i < 10) filename = "/res/5um-40x-3-2000" + i + ".png"; 
			else if(i < 100) filename = "/res/5um-40x-3-200" + i + ".png"; 
			else filename = "/res/5um-40x-3-20" + i + ".png"; 
			
			actual_loading_code();
		}
	}
	
	
	private void actual_loading_code(){
		final URL resource = getClass().getResource(filename);
		
		try {
		    img = ImageIO.read(resource);
		} catch (IOException e) {
			System.out.println("Invalid file name");
		}
		
		//Base case. First picture only.
		if(x_length == 0 || y_length == 0){
			x_length = img.getWidth();
			y_length = img.getHeight();
			
			final_img = new BufferedImage(x_length, y_length, BufferedImage.TYPE_INT_RGB);
			
			final_arr = new RGB[x_length][y_length];
			for(int y = 0; y < y_length; y++){
				for(int x = 0; x < x_length; x++){
					final_arr[x][y] = new RGB(0,0,0);
				}
			}
		}
	
		
		//Lets try to identify the center.
		int x_pos = 0;
		int y_pos = 0;
		int num = 0;
		
		for(int y = 0; y < y_length; y++){
			for(int x = 0; x < x_length; x++){
				Color c = new Color(img.getRGB(x, y));
				RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
				//if(( color.luminance() > 130)){
				if( color.luminance() > 150){
					num++;
					x_pos+=x;
					y_pos+=y;
				}
			}
		}
		
		
		//At this point, we average everything
		
		x_pos = x_pos/num;
		y_pos = y_pos/num;
		
		System.out.println("X Coordinate: " + x_pos + " Y Coordinate: " + y_pos);

		for(int y = y_pos - (dim/2); y < y_pos + (dim/2); y++){
			for(int x = x_pos - (dim/2); x < x_pos + (dim/2); x++){
				Color c = new Color(img.getRGB(x, y));
				RGB color = new RGB(c.getRed(), c.getGreen(), c.getBlue());
				
				if(color.compare(final_arr[x][y])){
					final_arr[x][y] = color;
				
				}
			}
		}
		
	}
	
	public void set_image() {
		for(int y = 0; y < y_length; y++){
			for(int x = 0; x < x_length; x++){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
