import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//import javax.imageio.ImageIO;


public class NaiveBayesFace {
                
	    //Likelihoods for each class (0 and 1)
		//0 = not a face,  1 = face
	    double [][][] likelihoods = new double [2][70][60];
	    //keep track of how many times a face comes up in the training labels file
	    int [] numberOfFaces = new int [2];
	    //priors P(class). The empirical frequencies of the classes
	    double [] frequencies= new double[2];
	    //total number of trials in the traininglabels 
	    int total = 451;
	    //new image from test images (new picture)
	    int [][] image = new int[70][60];
		int actualNum = 0; // Classification of face or non-face
		double correct = 0; // The counter for the number of correct classifications
		double count = 0; // A counter for the number of digits
		double smoothf =1 ;
		
		double [][] confusion = new double[2][2];
	    double [][] confusionpercent = new double[2][2];    
		public static void main(String[] args) throws IOException 
		{
			//Classify Faces
			NaiveBayesFace face_classification = new NaiveBayesFace();
			face_classification.updateLikelihoods();
			face_classification.getNewFace();
		}
		
	    //Adds a smoothing constant to all the likelihoods pixels to ensure that there are no zero counts.
	    public void initLikelihoods()
	    {
	    	for(int i =0; i < 2; i++)
	    	{
	            for(int j =0; j < 70; j++)
	            {
                    for(int k =0; k < 60; k++)
                    {
                        likelihoods[i][j][k] = smoothf;
                    }
	            }                        	
	    	}  
	    }//end of initLikelihoods
	    
	    
	    
	    //initializes the likelihoods for each class (i.e. face or not a face) from the training data
	    public NaiveBayesFace() throws IOException 
	    {
	        initLikelihoods();	            
	        FileReader inputStream = null;
	        FileReader labels = null;	        
	        try 
	        {
	            inputStream = new FileReader("facedata/facedatatrain");
	            labels = new FileReader("facedata/facedatatrainlabels");	
	            int c;
	            int face = 0;	            
	            outerloop:
	            for(int i = 0; true; i++)
	            {
                    if(i == 0)
                    {
                        face = labels.read();
                        face = face-'0';
                    }
                    for(int j =0; j < 61; j++)
                    {
                        c =  inputStream.read();
                        if(c == -1)
                        {
                            numberOfFaces[face]++;
                            break outerloop;
                        }                               
                        //foreground
                        else if(c == 35 )
                        {
                            likelihoods[face][i%70][j]++; 
                        }
                        //else its a background                                                                                                
                    }
                    
                    if(i%70 == 0 && i != 0)
                    {
                        face = labels.read();   

                        if(face == -1)
                        {
                            break outerloop;
                        }
                        if (face-'0' != 1 || face-'0' != 0)
                        {        
                            face = labels.read();
                        }
                        face = face-'0';
                        numberOfFaces[face]++;  
                    }
                }
            } 
            catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
	    }//end of Faces Constructor
	    
	    
	    
	    
	    //estimate the likelihoods P(F_ij | class), and adds smoothing. As well as calculates the frequencies 
	    public void updateLikelihoods()
	    {
            for(int i =0; i < 2; i++)
            {	
                //(Prior P(class))
            	frequencies[i]= (double)(numberOfFaces[i])/(total);
            	
	            for(int j =0; j < 70; j++)
	            {
                    for(int k =0; k < 60; k++)
                    {
                        likelihoods[i][j][k] = (double)(likelihoods[i][j][k])/( numberOfFaces[i]+(2*smoothf) );
                    }
	            }
            }
	    }//end of updateLikelihoods
	        
	    //gets new image from test images file, and determines whether the image is a face
	    public void getNewFace() throws IOException
	    {
	        FileReader inputNumber = null;
	        FileReader actualNumber = null;        
	        try {
	            inputNumber = new FileReader("facedata/facedatatest");
	            actualNumber = new FileReader("facedata/facedatatestlabels");	            
	            int c;	                    
	            outerloop:
	            for(int i = 0; true; i++)
	            {                 
            		//obtains the actual number to compare in the function call pickFace()
                    if(i%70 == 0 && i != 0)
                    {
                        actualNum =  actualNumber.read();
	                    if(actualNum  == -1)
	                    {	                            
	                            break outerloop;
	                    }
	                    if(actualNum  == 10)
	                            actualNum =  actualNumber.read();
	                    actualNum = actualNum-'0';
                    }
                    
                    for(int j =0; j < 61; j++)
                    {
                        c =  inputNumber.read();
                        if(c == -1)
                        {
                            pickFace(c);
                            break outerloop;                                                
                        }                                  
                        //foreground
                        else if(c == 35)
                        {
                            image[i%70][j] = 1;
                        }
                        //else its a background        
                        else if(c == 32)
                        {
                            image[i%70][j]= 0;
                        }
                    } 

                    if(i %70==0 && i !=0)
                        pickFace(1);    
                    
	            }	    
            }        
            catch (FileNotFoundException e) {
                    e.printStackTrace();
            }            
	    }//end of getNewFace function
	    
	    /*Determines if image is a face or not, uses the actualNum variable from getNewFace()
	      input = if we reached the end of test file (i.e -1), print statistics 
	     */
	    public void pickFace(int input)
	    {
            double [] probabilities = new double [2];
            int best = 0;
            
            for(int i =0; i < 2; i++)
            {
            	probabilities[i]= probabilities[i] + (Math.log(frequencies[i]) );
	            for(int j =0; j < 70; j++)
	            {
                    for(int k =0; k < 60; k++)
                    {
           
                        if(image[j][k]==0)  
                        	probabilities[i] = probabilities[i] + ( Math.log(1-likelihoods[i][j][k]) );
                        else 	
                        	probabilities[i] = probabilities[i] + ( Math.log(likelihoods[i][j][k]) );

                    }

	            }
	            if(probabilities[i] > probabilities[best])
	                   best = i;
            }
            confusion[best][actualNum]++;
            if(best == actualNum)
            {	            	
                correct++;	                    
            }
            
            count++;
            
            if(input ==-1)
            {
                System.out.println();
                System.out.println("Face Classification: ");
                System.out.println("Fraction of correctly classified: " +correct+" / "+count);       
                System.out.println("Percentage of correctly classified: "+ (double)(correct/count)*100);
                System.out.println();
                System.out.println("Confusion Matrix:");
                printConfusionMatrix();

                
            }
	            
	    }//end of pickFace() 
		public void printConfusionMatrix() {
			int sum = 0;
			for (int j = 0; j < 2; j++) {
				for (int i = 0; i < 2; i++) {
					sum += confusion[j][i];
				}
				for (int i = 0; i < 2; i++) {
					confusionpercent[i][j] = confusion[j][i]/sum;
				}
				sum = 0;
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					
						System.out.printf((int)confusion[j][i]+"  ");
					
				}
				System.out.println();
			}
			System.out.println();
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					if ((100*(confusion[i][j])) >= 10)
						System.out.printf("%.2f  ",(float)(100*(confusionpercent[i][j])));
					else
						System.out.printf("%.2f  ",(float)(100*(confusionpercent[i][j])));
				}
				System.out.println();
			}
			System.out.println();
		}
	    
}//end of Face class