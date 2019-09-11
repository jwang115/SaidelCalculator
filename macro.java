/*
 * This macro scales all ROIs in the ROI Manager
 *
 * Note: since the scaling is performed using integers,
 * upscaling leads to step artifacts.
 *
 * For technical reasons, this macro cannot handle composite
 * selections.
 */

// Dialog Box

macro "Main" {
	// Step 1: Check if Stack
	frames=nSlices;	
	if (nSlices==1) {
 		exit("Image is not a stack");
	}
	// Step 2: Divide Stack into Segments
 	run("Set Measurements...", " mean redirect=None decimal=4");
 	run("Clear Results"); 	 
    xcoord=-1;    
    Dialog.create("Saidal Calculator");
	Dialog.addMessage("Input information about files for easy video analysis");
	Dialog.addNumber("How many divisions?", 3); 	
	Dialog.addCheckbox("Do you want to save the individual images?", false);
	Dialog.show;
	n=Dialog.getNumber();
	savefiles=Dialog.getCheckbox();
  	original = getImageID; 
	title = getTitle; 
	height = getHeight; 
	width = getWidth; 
	cutWidth = width / n; 	
	imagesection = newArray(n-1); // Keeps track of the imageIDS in an array
	for(i=1; i<=n; i++){		   			    	
    	selectImage(original);     	    	
    	makeRectangle(xcoord+1, 0, cutWidth, height);  
    	run("Duplicate...", "title=section_"+i+"_"+title+" duplicate");   			
		xcoord=xcoord+cutWidth;	
		section = getImageID; 	
		addToArray(section, imagesection, i-1);			
	}
	// Step 3: Optional: Save Stack as Seperate Images	
	if (savefiles){	
		run("SaveAllImages ");
	end	
	// Step 4: Calculate Difference in Images			
	for(i=1; i<=n; i++){		
		stack=imagesection[i]; 
		for(idx=2; idx<=5; idx++){	
			selectImage(stack);
			setSlice(idx); 			
			run("Duplicate...", "title=Higher use");
			Higher=getImageID;
			selectImage(stack);
			setSlice(idx-1); 			
			run("Duplicate...", "title=Lower use"); 	
			Lower=getImageID;
			imageCalculator("Difference create", "Higher", "Lower");	
			selectWindow("Higher");	close();
			selectWindow("Lower"); close();		
			selectImage(stack);	
		}	
		i=1;
		run("Images to Stack", "name=Difference_stack_section_"+i+"title=[Result of Higher] use");	
		// Step 5: Copy the LUT and display it
		getDimensions(width, height, channels, slices, frames);
		// move through each slice of the image
		for (i=1; i<slices+1; i++) {
			setSlice(i);
		// This macro generates a 256 bin histogram and
		// displays the counts in the "Results" window.
		  	nBins = 256;
		  	row = 0;
		  	getHistogram(values, counts, nBins);
		  	for (j=0; j<nBins; j++) {
				setResult("Value", row, values[j]);
			    setResult("Count #" + i, row, counts[j]);
			    row++;
			}
		updateResults();
		}	
	}		
}	



function addToArray(value, array, position) {
    if (position<lengthOf(array)) {
        array[position]=value;
    } else {
        temparray=newArray(position+1);
        for (i=0; i<lengthOf(array); i++) {
            temparray[i]=array[i];
        }
        temparray[position]=value;
        array=temparray;
    }
    return array;
}













	halfWidth = width / 2;		
	makeRectangle(0, 0, halfWidth, height); 
	run("Duplicate...", "title=left_"+title+" duplicate"); 
	selectImage(original); 	
	makeRectangle(halfWidth+1, 0, halfWidth, height); 
	run("Crop"); 
	rename("right_"+title);
	}
}	
	 
    /* Here is a macro that chops an image into NxN tiles, where N is the number 
	of divisions you choose:*/
	n = getNumber("How many divisions (e.g., 2 means quarters)?", 10); 
	id = getImageID(); 
	title = getTitle(); 
	getLocationAndSize(locX, locY, sizeW, sizeH); 
	width = getWidth(); 
	height = getHeight(); 	
	tileWidth = width / n; 
	tileHeight = height; 
	for (y = 0; y < n; y++) { 
		offsetY = y * height; 
 		for (x = 0; x < n; x++) { 
			offsetX = x * width / n; 
			selectImage(id); 
			call("ij.gui.ImageWindow.setNextLocation", locX + offsetX, locY + offsetY); 
			tileTitle = title + " [" + x + "," + y + "]"; 
			run("Duplicate...", "title=" + tileTitle); 
			makeRectangle(offsetX, offsetY, tileWidth, tileHeight); 
			run("Crop"); 
		} 
	} 
	selectImage(id); 
	close(); 


    /* for(i=0; i<=w; i+=100) {
    	makeRectangle(xcoord, ycoord, i, h);
		run("Crop");	
		xcoord=xcoord+100;	
    }*/



    
 	for(i=0; i<frames; i++) {
 		currentslice=i+1;
 		setSlice(currentslice);
 		run("Measure");
 	}
}






macro "Measure Avg Intensity Stack" {
	frames=nSlices;
 	run("Set Measurements...", " mean redirect=None decimal=4");
 	run("Clear Results");
 	for(i=0; i<frames; i++) {
 		currentslice=i+1;
 		setSlice(currentslice);
 		run("Measure");
 	}
}

function scaleROI(factor) {
	type = selectionType();
	getSelectionCoordinates(x, y);
	for (i = 0; i < x.length; i++) {
		x[i] = x[i] * factor;
		y[i] = y[i] * factor;
	}
	makeSelection(type, x, y);
}


count = roiManager("count");
current = roiManager("index");
for (i = 0; i < count; i++) {
	roiManager("select", i);
	scaleROI(factor);
	roiManager("update");
}
if (current < 0)
	roiManager("deselect");
else
	roiManager("select", current);