package traversers;


/*The main class, it traverses through the 2D array representation of the image, and then modifies it*/

public class Traverser
{
	private static int[][] image;//array holding grayscale values
	private static int height;//height of image
	private static int width;//width of image
	private static AcceptableRange isIt;//the image's acceptable range, an object that returns whether or not something is out of range based on the original inputs we gave to the constructor
	private static int deviation;//the calculated mean average deviation, we need it to be global bc we'll be using it in multiple scenarios

	private static class AcceptableRange
	{
		private int lowerBound;
		private int upperBound;
		public AcceptableRange(int lowerBound, int upperBound)
		{
			this.upperBound = upperBound;
			this.lowerBound = lowerBound;
		}
		public int outOfRange(int value)
		{
			if(value<lowerBound || upperBound<value)
				return value;
			return 0;
		}
	}
	
	public Traverser(int[][] image)
	{
		this.image = image;
		height = image.length;
		width = image[0].length;
		int average = average();
		deviation = meanAverageDeviation(average)*4;
		isIt = new AcceptableRange(average-deviation, average+deviation);
	}

	public void traverse()
	{
		int start = 0;
		int end = image[0].length-1;
		//int direction = end-start > 0 ? 1:-1;//if start-end>0, it means starts from the right side, which means we decrement (-1) to reach end; vice versa for else		
		for(int i = 0; i < height; i++)
		{
			int stopPoint = traverseForwards(i, start, end);
			//System.err.printf("Row %d's stopPoint = %d\n",i,stopPoint);
			if(stopPoint!=19)
				traverseBackwards(i, end, start);
		}
	}

	private static int traverseForwards(int row, int start, int end)
	{
		for(int i = start; i <= end; i++)
		{
			int pixel = isIt.outOfRange(image[row][i]);
			if(pixel!=0)
			{
				int tempMedian = pixel;
				AcceptableRange isInner = new AcceptableRange(tempMedian-(deviation/2), tempMedian+(deviation/2));
				for(int j = i; j < end; j++)
				{//we'll continue iterating until either we exit the borders OR we reach the end
					pixel = isInner.outOfRange(image[row][j]);//checks if pixel is out of new inner rage
					if(pixel!=0)//if so, we return
						return j;
					image[row][j] = pixel;//otherwise we set the pixel value to 0
				}
			}
			image[row][i] = pixel;
		}
		return end;
	}
	private static void traverseBackwards(int row, int start, int end)
	{
		for(int i = start; i >= end; i--)
		{
			int pixel = isIt.outOfRange(image[row][i]);
			if(pixel!=0)
			{
				int tempMedian = pixel;
				//System.err.printf("tempMedian = %d", pixel);
				AcceptableRange isInner = new AcceptableRange(tempMedian-(deviation/2), tempMedian+(deviation/2));
				for(int j = i; j > end; j--)
				{//we'll continue iterating until either we exit the borders OR we reach the end
					pixel = isInner.outOfRange(image[row][j]);//checks if pixel is out of new inner rage
					//System.err.printf("[B] pixel = %d, original = %d\n", pixel, image[row][j]);
					if(pixel!=0)//if so, we return
					{
						//System.err.printf("[B] Row %d stopPoint = %d\n", row, j);
						return;						
					}
					image[row][j] = pixel;//otherwise we set the pixel value to 0
				}
			}
			image[row][i] = pixel;			
		}

	}

	private static int average()
	{
		int average = 0;//for finding average

		for(int i = 0; i < height; i++)//iterate through every single pixel
		{
			for(int j = 0; j < width; j++)
				average+=image[i][j];//sum it up
		}
		return (average/=(height*width));//divide by numPixels to get average
	}

	private static int meanAverageDeviation(int average)
	{
		//assuming a rectangular image with dimensions > 0

		int mad = 0;//(M)ean (A)verage (D)eviation

		for(int i = 0; i < height; i++)//going through every pixel
		{
			for(int j = 0; j < width; j++)
				mad+=Math.abs(image[i][j]-average);//MAD = the average of the absolute value of the difference between every pixel and the average
		}
		return (mad/=(height*width));//gotta divide by numPixels after summing to get average
	}

	public static void printImageArray()
	{
		for(int i = 0; i < 500; i++)
		{
			for(int j = 0; j < 20; j++)
				System.out.printf("%3d  ", image[i][j]);
			System.out.println();
		}
	}

	public int[][] returnImgArray()
	{
		return this.image;
	}
/*
	public static void main(String[] args)
	{
		int[][] input = new int[500][20];
		for(int i = 0; i < 500; i++)
		{
			for(int j = 0; j < 20; j++)
			{
				if((i==450 && j>5 && j<10) || (i==480 && j>5 && j<10) || (j==5 && i>450 && i<480) || (j==10 && i>450 && i<45080))
					input[i][j] = (int)(Math.random()*20)+85;
				else
					input[i][j] = (int)(Math.random()*30)+50;
				System.out.printf("%3d ", input[i][j]);
			}
			System.out.println();
		}
		Traverser traverser = new Traverser(input);
		int avg = average();
		int dev = meanAverageDeviation(avg);
		int ub = avg+dev;
		int lb = avg-dev;
		System.out.printf("%d<=%d<=%d\n", lb, avg, ub);
		//traverse a second time only if we hit an endpoint
		traverse();
		printImageArray();
	}*/

}