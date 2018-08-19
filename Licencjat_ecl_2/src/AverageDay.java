import weka.core.Instances;
import weka.classifiers.timeseries.*;

public class AverageDay extends ForecastAlgorithm {
	public ForecastAlgorithm run(String givenDate, int testSize, String fileName) {
        super.run(givenDate, testSize, fileName);
        if(this.errorNumber!=0) {
        	return this;
        }
        System.out.println("\n\nUSREDNIANIE DNIA na podstawie 1 tygodnia:");
        
        //bufor na usrednione wartosci dla 24 godzin w ciagu 1 dnia
        double[] usrednienie_dania= new double[24];

        //mamy 7 dni w tygodniu
        for(int i=0; i<7; i++)
        {
        	//mamy 24 godziny w ciagu 1 dnia
        	for(int j=0; j<24; j++)
        	{
        		int idx= this.startIdx+i*7*24+j;
        		//System.out.println("this.data.size() rozmiar.."+this.data.size());
        		//System.out.println("idx: "+idx);
        		//System.out.println("iteracja, i, j: "+i*j+" "+i+" "+j);
        		double flow= this.data.instance(idx).value(this.data.attribute("flow"));
        		usrednienie_dania[j]=usrednienie_dania[j]+flow;
        	}
        }
        
    	for(int j=0; j<24; j++)
    	{
    		usrednienie_dania[j]=usrednienie_dania[j]/7.0;
    	}
    	
    	//wartosci predykowanych ma byc tyle ile wynosi test_size
    	this.predictedValues=new Double[this.testSize];
    	this.absError=new Double[this.testSize];
    	double mean_squared_error =0;
        for(int i=0; i<this.testSize; i++)
        {
        	int idx=i % 24;
        	this.predictedValues[i]=usrednienie_dania[idx];
        	this.absError[i]=Math.abs(this.predictedValues[i]-this.trueValues[i]);
        	mean_squared_error=mean_squared_error+this.absError[i]*this.absError[i];
        }
        //System.out.println(mean_squared_error);
        mean_squared_error=mean_squared_error/this.testSize;
        
        //na koniec wypisanie statystyk
        
        /*System.out.println("Wypisanie przewidywan dla dat");
        System.out.println("Date \t\t\t true \t predicted \t error");
        for(int i=0; i<this.testSize; i++)
        {
        	int idx=currentIdx+i;
        	String readDate = this.data.instance(idx).stringValue(this.data.attribute("data"));
        	System.out.println(readDate
        					+"\t"+this.trueValues[i]
        					+"\t"+this.predictedValues[i]
                			+"\t\t"+this.absError[i]);
        }*/
        
        System.out.println("avg Day mean squared error (MSE) = "+ mean_squared_error);
        System.out.println("avg Day root-mean-square error (RMSE) = "+ Math.sqrt(mean_squared_error));
        
        this.MSE=mean_squared_error;
        this.RMSE = Math.sqrt(mean_squared_error);
        return this;
    }   //koniec metody run
}