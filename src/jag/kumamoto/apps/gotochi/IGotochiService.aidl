package jag.kumamoto.apps.gotochi;

interface IGotochiService{
	int getActivityNumber();
	void pause();
	void restart();
	boolean isRunning();
}