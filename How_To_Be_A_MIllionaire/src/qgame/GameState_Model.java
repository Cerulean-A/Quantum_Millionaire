package qgame;

public class GameState_Model {
	
    //---------------------------------------------------------------
    // Main Menu in Game (ESC Key Activated)  -- Model Portion
    //---------------------------------------------------------------
	
	
	private boolean paused;											// Paused T/F
	private boolean devOptionsEnabled;								// Dev menu toggle T/F
	
	// Pause Functions
	public boolean isPaused() { return paused; }					// Pause yes/no?
	public void setPaused(boolean paused) { this.paused = paused; }

	// Reveal Developer Buttons Functions
	public boolean isDevOptionsEnabled() { return devOptionsEnabled; }
	public void setDevOptionsEnabled(boolean enabled) { this.devOptionsEnabled = enabled; }


}
