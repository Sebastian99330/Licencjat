public class AverageWeek extends ForecastAlgorithm{
    public ForecastAlgorithm run(String givenDate, int testSize, String fileName) {
        super.run(givenDate, testSize, fileName);
        if(this.errorNumber!=0) {
        	return this;
        }
        System.out.println("\n\nUSREDNIANIE TYGODNIA na podstawie 1 miesiaca:");
        
        //bufor na uśrednione wartości dla 7*24 godziny w tygodniu
        double[] usrednienie_dania= new double[7*24];
        
       //mamy 4 tygodnie w miesiącu
        for(int i=0; i<4; i++)
        {
        	//mamy 7*24 godziny w tygodniu
        	for(int j=0; j<7*24; j++)
        	{
        		int idx= this.startIdx+i*7*24+j;
        		double flow= this.data.instance(idx).value(this.data.attribute("flow"));
        		usrednienie_dania[j]=usrednienie_dania[j]+flow;
        	}
        }
        
    	for(int j=0; j<7*24; j++)
    	{
    		usrednienie_dania[j]=usrednienie_dania[j]/4.0;
    	}
    	 
    	//wartosci predykowanych ma byc tyle ile wynosi test_size
    	this.predictedValues=new Double[this.testSize];
    	this.absError=new Double[this.testSize];
    	double mean_squared_error =0;
        for(int i=0; i<this.testSize; i++)
        {
        	int idx=i % (7*24);
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
        System.out.println("avg Week mean squared error (MSE) = "+ mean_squared_error);
        System.out.println("avg Week root-mean-square error (RMSE) = "+ Math.sqrt(mean_squared_error));
        
        this.MSE=mean_squared_error;
        this.RMSE = Math.sqrt(mean_squared_error);
        return this; //brak bledu, wszystko pomyslnie
    }
}