package main;

public class RGB {
	public final int red;
	public final int blue;
	public final int green;
	
	public RGB(int red, int green, int blue){
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public double luminance(){
		return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
	}
	
	public boolean compare(RGB other){
		return (this.luminance() > other.luminance());
	}
}
