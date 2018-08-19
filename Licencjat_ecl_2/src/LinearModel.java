import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import weka.classifiers.timeseries.*;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;

public class LinearModel extends ForecastAlgorithm{
    public ForecastAlgorithm run(String givenDate, int testSize, String fileName) {
    	super.run(givenDate, testSize, fileName);
        if(this.errorNumber!=0) {
        	return this;
        }     
        System.out.println("\n\nPrzewidywanie modelem REGRESJI LINIOWEJ na podstawie zebranych "+trainSize+" danych:");
        
        this.windowSize = 4;
        
        // new forecaster
        weka.classifiers.timeseries.WekaForecaster forecaster = new weka.classifiers.timeseries.WekaForecaster();
        java.util.List<java.util.List<NumericPrediction>> forecast = null;
        try{
        	//wybieramy pole do predyckji
	        forecaster.setFieldsToForecast("flow");
	        //tworzymy klasyfikator
	        LinearRegression lin_reg=new LinearRegression();
	        //ustwiamy parametry klasyfikatora
	        forecaster.setBaseForecaster(lin_reg);
	        forecaster.getTSLagMaker().setMinLag(1);
	        forecaster.getTSLagMaker().setMaxLag(this.testSize*this.windowSize);
	        forecaster.getTSLagMaker().setTimeStampField("idx");
	        //budujemy model predykcyjny time series
	        forecaster.buildForecaster(this.train, System.out);
	        System.out.println("Po stworzeniu modelu");
	        forecaster.primeForecaster(this.train);
	        forecast = forecaster.forecast(this.testSize, System.out);
        }
        catch(Exception e){
        	e.printStackTrace();
            //JTextField textField= new JTextField("Nie udalo sie zbudowac klasyfikatora, konczymy program!",20);
            //frame.add(textField);
            this.errorNumber=4;	//blad przy budowaniu modelu regresji 
            return this;
        }
        //wartości predukowanych ma byc tyle ile wynosi test_size
        this.predictedValues=new Double[this.testSize];
    	this.absError=new Double[this.testSize];
    	double mean_squared_error =0;
        for(int i=0; i<this.testSize; i++)
        {
        	java.util.List<NumericPrediction> predsAtStep = forecast.get(i);
        	double predicted = predsAtStep.get(0).predicted();
        	this.predictedValues[i]=predicted;
        	this.absError[i]=Math.abs(this.predictedValues[i]-this.trueValues[i]);
        	mean_squared_error=mean_squared_error+this.absError[i]*this.absError[i];
        }
        System.out.println(mean_squared_error);
        mean_squared_error=mean_squared_error/this.testSize;
        
        //na koniec wypisanie statystyk
        /*System.out.println("Wypisanie przewidywan dla dat");
        System.out.println("Date \t\t\t true \t predicted \t error");
        for(int i=0; i<this.testSize; i++)
        {
        	int idx=this.currentIdx+i;
        	String readDate = this.data.instance(idx).stringValue(this.data.attribute("data"));
        	System.out.println(readDate
        					+"\t"+this.trueValues[i]
        					+"\t"+this.predictedValues[i]
                			+"\t\t"+this.absError[i]);
        }*/
        System.out.println("mean squared error (MSE) = "+ mean_squared_error);
        System.out.println("root-mean-square error (RMSE) = "+ Math.sqrt(mean_squared_error));
        
        this.MSE=mean_squared_error;
        this.RMSE = Math.sqrt(mean_squared_error);
        return this;
    }
}