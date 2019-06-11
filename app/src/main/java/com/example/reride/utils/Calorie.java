package com.example.reride.utils;

public class Calorie {

	public final static String run( float speedAvg, long totalTime ){
		float calorie = 0;
		int unit = 0;
		
		if(speedAvg>21){
			unit = 655;
		}else if(speedAvg>16){
			unit = 415;
		}else if(speedAvg>9){
			unit = 245;
		}else{
			unit = 184;
		}
		
		calorie = ((float)unit/3600) * (totalTime/1000);		
		return String.valueOf((int)calorie);
	}

}
