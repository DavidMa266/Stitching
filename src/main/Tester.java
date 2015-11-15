package main;



public class Tester {

	public static void main(String[] args){
	
		
		Sobel s = new Sobel(2, 5);
		s.test("/res/asdf.png", 25, 55, "Edge_detection_girl_sample.png");
		//s.load_sample("/Untitled.png");
		
	}
	
}