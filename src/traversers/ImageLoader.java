package traversers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageLoader
{
	/**
	 * This function loads and returns an BufferedImage with the given filename
	 * @param filename
	 * @return
	 * @author David Ma
	 * edited extensively for and by Charlie Su
	 */
	public static BufferedImage load_image(String filename)
	{
		final URL resource = ImageLoader.class.getResource(filename);//something about implementation of this in Java being not safe unless there's an instance of the class
		BufferedImage im = null;
		try {
		    im = ImageIO.read(resource);
		} catch (IOException e) {
			System.err.println("Invalid file name");
		}
		return im;
	}
	/*
		Takes a BufferedImage, extracts each pixel's RGB values, averages the values, then saves them into a 2D array for a naive grayscale image
	*/
	public static int[][] grayscale_image(BufferedImage src)
	{
		int width = src.getWidth();
		int height = src.getHeight();
		int[][] img = new int[height][width];//will change to width/height later
		Color c = null;//declaring color
		for(int i = 0; i < height; i++)//iterating through every pixel
		{
			for(int j = 0; j < width; j++)
			{
				c = new Color(src.getRGB(j,i));//grabbing color from image
				int gray = (c.getRed() + c.getGreen() + c.getBlue())/3;//averaging them
				img[i][j] = gray;//saving to array
			}
		}
		return img;//return array
	}
	/*
		Takes an integer matrix and writes a new PNG file
	*/
	public static void save_image(int[][] mtrx, String fileName)
	{
		int height = mtrx.length;
		int width = mtrx[0].length;
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int color = mtrx[y][x];
				color = color << 16 | color << 8 | color;//not sure
				output.setRGB(x,y,color);				
			}
		}
		File f = new File(fileName+".png");
		try
		{
			ImageIO.write(output, "PNG", f);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Finished~");
	}

	public static void main(String[] args)
	{
		System.out.printf("args[0] = %s\t",args[0]);
		int[][] input = grayscale_image(load_image(args[0]));
		Traverser traverser = new Traverser(input);
		traverser.traverse();
		save_image(traverser.returnImgArray(),args[0]+"-out");
	}
}