import weka.core.Instances;
import weka.core.converters.CSVLoader;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;

import java.util.Map;
public abstract class ForecastAlgorithm {
    Instances data;
    int trainSize;
    int testSize;
    int startIdx;
    int windowSize;
    Instances train;
    Instances test;
    Map<String, Integer> dateTimeMap;
    String currentDate;
    int currentIdx=-1;
    Double[] trueValues=null;
    Double[] predictedValues;
    Double[] absError=null;
    double MSE=-1;
    double RMSE=-1;
    int errorNumber=0;
    
    public ForecastAlgorithm run(String givenDate, int givenTestSize, String fileName) {
    	this.currentDate=givenDate;
    	this.testSize=givenTestSize;
    	
        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File(fileName));
        } catch (Exception e) {
        	e.printStackTrace();
            this.errorNumber=1;	//1 to blad wczytania pliku
            return this;
        }
        
        try {
        	this.data = loader.getDataSet();

            //liczba atrybutow=3, liczba obserwacji=8733
            //dla pliku TMU Site 95102 po konwersji.csv: liczba atrybutow=3, liczba obserwacji=4607
            //System.out.println("liczba atrybutow=" + this.data.numAttributes()
            //        + ", liczba obserwacji=" + this.data.numInstances());


            // 1,'2014-01-02 03:00:00',15.5325
            // dla pliku TMU Site 95102 po konwersji.csv: 2017-06-21 01:00:00',57,1
            //System.out.println(this.data.firstInstance());

            //8733,'2014-12-31 23:00:00',11.25
            //dla pliku TMU Site 95102 po konwersji.csv: '2018-01-01 23:00:00',28.25,4607
            //System.out.println(this.data.lastInstance());

            //System.out.println(this.data.toSummaryString());



        } catch (Exception e) {
            e.printStackTrace();
        }
        
       //mapa mapujaca date i czas na nr indeksu w calym zbiorze danych
        this.dateTimeMap= new HashMap<>();
        for(int i=0; i<this.data.numInstances(); i++)
        {
        	String readDate = this.data.instance(i).stringValue(this.data.attribute("data"));
        	this.dateTimeMap.put(readDate, i);
        }
        
        //System.out.println("Wypisujemy wartosci: ");
        //System.out.println("testSize: "+testSize);
        //System.out.println("data: "+currentDate);
          
        
        //warunek na podanie niepoprawnej daty
        
        if(!this.dateTimeMap.containsKey(currentDate))
        {
        	System.out.println("Wprowadzono niepoprawna date: " +this.currentDate);
        	//System.exit(0);
        	errorNumber=2;	//2 to blad niepoprawnej daty
        	return this;
        }
        
        //indeks aktualnej daty, od ktorej przewidujeny
        this.currentIdx=this.dateTimeMap.get(this.currentDate);
        System.out.println("current_idx="+this.currentIdx);
        
        //System.out.println("ilosc wczytanych wszystkich danych z pliku numInstances(): "+this.data.numInstances());
        if(this.currentIdx+this.testSize>=this.data.numInstances())
        {
        	System.out.println("Nie mozemy porownac tylu godzin w przod."
        					+"Metoda nie moze przewidziec dla daty " +this.currentDate);
        	//System.exit(0);
        	errorNumber= 3;	//3 za duzy test size (uzytkownik chce przewidywac za duzo godzin do przodu)
        	return this;
        }
        
        //licznosc do cofniecia sie w czasie
        this.trainSize = 4 * 7 * 24;	//4*7*24 = 672. To iloœæ godzin w miesi¹cu
        
        //sprawdzenie train size
        this.startIdx=this.currentIdx-this.trainSize;
        if(this.startIdx<0)
        {
        	//System.out.println("Nie mozemy cofnac sie w czasie, poniewaz program nie moze przewidziec dla danej daty.");
        	//System.exit(0);
        	errorNumber= 6;	//3 za duzy test size (uzytkownik chce przewidywac za duzo godzin do przodu)
        	return this;
        }
        
        
        /////spr warunek na koncowa date 
        ///tutaj to mozna ogarnac tylko zamienic ten kod ponizej
        //indeks ostatniego dobrego elementu
        int idxOfEndingDate=0;
        //2017-12-18 00:00:00 to ostatnia data dla ktorej dobrze liczy. Program wyrzuca od 01 godziny czyli 2017-12-18 01:00:00
        idxOfEndingDate=this.dateTimeMap.get("2017-12-18 00:00:00");        
        if(this.currentIdx>idxOfEndingDate)
        {
        	//System.out.println("Nie mozemy cofnac sie w czasie, poniewaz program nie moze przewidziec dla danej daty.");
        	//System.exit(0);
        	errorNumber= 7;	//3 za duzy test size (uzytkownik chce przewidywac za duzo godzin do przodu)
        	return this;
        }
        
        
        
        
        trueValues=new Double[this.testSize];
        for(int i=0; i<this.testSize; i++)
        {
        	int idx=this.currentIdx+i;
        	this.trueValues[i]=this.data.instance(idx).value(this.data.attribute("flow"));
        	if(trueValues[i]==-1) {
        		errorNumber= 5;
            	return this;
        	}
        }
        
        
        this.train = new Instances(this.data,this.currentIdx-this.trainSize, this.trainSize);
        this.test = new Instances(this.data, startIdx + this.testSize, this.testSize);
        errorNumber=0;	//0 to brak bledu, wszystko ok
        return this;
    }
}