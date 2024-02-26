package com.cnlaunch.physics.utils;

public class Bridge {
	Boolean ishas = false;
	public synchronized void getData(){
		while (ishas==false) {
             try {
				wait();
             } catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void putData(){
		ishas = true;
		notify();
	}
}
