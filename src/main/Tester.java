package main;
		
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Tester {

	static double stdev = 2;
	static double blur_dim = 7;
	static int lower_threshold = 25;
	static int upper_threshold = 55;
	static int min_radius = 10;
	static int max_radius = 30;
	
	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String filename = "src/res/UC-3-10";
		String filetype = ".jpg";
		Mat destination = new Mat();
		
		for(int i = 3; i < 180; i++){
			String src = filename + i + filetype;
			System.out.println(src);
			Mat img = Imgcodecs.imread(src, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat gaussian = new Mat();
			Mat circle = new Mat();
			if(destination.empty()){
				System.out.println("EMPTY");
				destination = new Mat(img.rows(), img.cols(), CvType.CV_8U);
			}
			Imgproc.GaussianBlur(img, gaussian, new Size(blur_dim, blur_dim), stdev, stdev);
			Imgproc.HoughCircles(gaussian, circle, Imgproc.CV_HOUGH_GRADIENT, 1, 1, upper_threshold, lower_threshold, min_radius, max_radius);

			Mat black_img = Mat.zeros(img.rows(), img.cols(), CvType.CV_8U);
			for(int j = 0; j < circle.cols(); j++){
				double[] coordinates = circle.get(0, j);
				if(coordinates == null)
					break;
				System.out.println(j);
				Point center = new Point(Math.round(coordinates[0]), Math.round(coordinates[1]));
				int radius = (int) coordinates[2];
				
				Imgproc.circle(black_img, center, radius, new Scalar(255,255,255), -1);
			}
			Core.bitwise_and(img, black_img, destination);
		}
		
		Imgcodecs.imwrite("src/output.jpg", destination);
	}



}












