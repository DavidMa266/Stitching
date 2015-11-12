First, we find the average grayscale value of the entire image and a cut off range for pixels that are close enough to the average grayscale value. 
Since we're looking for borders, instead of going through the pixels from only one direction, we'll approach the middle from two directions. 
	If we encounter a pixel with a luminosity that's out of the acceptable range, we know we've found the border, so we stop and save the position!
	Else we keep going through while changing the color of the pixels in acceptable range to black (in essence deleting them since we don't need them anymore).
Once we finish travering, we save everything (positions of border pixels, new median, new acceptable range) about the border of the circle.

By now, we should have a mostly black picture and the circle we want, nearly isolated. 
The border should still be there, and we want to get rid of that.
But now, instead of having a huge problem, we can just focus on just getting the border out.
This is where the saved data comes in.

We'll run through the same exact steps we did earlier, except that instead of starting at the edges of the full image, we start at the positions that have been saved.
Instead of the average grayscale value/acceptable range, we'll be using the average grayscale value/acceptable range of the border.
	We'll get that with an extra step in the above thing where instead of just stopping once we encounter a border, we set the current temporary median to the value of the pixel's color.
		We keep the acceptable range (as in how much we +/- from the original average), but apply the center of this range as the median we just set.
		Then we continue until we hit something out of range again.
			This should work because jumping to the new range required us to be out of that original range. 
			Since the insides of the circle are very close to the color of the originaly average, to jump back, we just need to be out of that original range again (but shifted).

So we run it through again. Since the border's considered the "average" it will be removed. Everything else inside; however will be kept.
	The reason why we didn't just add an extra step of removing was ... wait no, we can just remove it in that step lol
Now we should have a clean image...hopefully!


//So, now, about stitching, instead of having a bunch of images (e.g. number of frames processed) saved and then mash it together at the end all at once, we can actually just save memory for the size of 2 images.
Once we're done, we keep that first image.
We open a new image and redo the process.
Now we mesh the two with bitwise OR.
	Case 1: the pixels are the same pixels that we're overlaying, A OR itself is still A, we're good, it is perfectly overlaid.
	Case 2: one pixel is black (0x000), the other is normal. A OR 0 is still A, we're good, it is perfectly overlaid.
//Only issue I'm worried about is that the pixel value changes a tiny bit over time..., but even if it's only a tiny bit, the difference should be small enough since it's tiny
Repeat for every frame.
Stitched image!?