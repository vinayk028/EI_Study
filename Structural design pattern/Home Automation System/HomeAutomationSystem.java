// Complex Subsystems
class LightSystem {
    public void turnOn() { System.out.println("Lights are ON"); }
    public void turnOff() { System.out.println("Lights are OFF"); }
}

class MusicSystem {
    public void playMusic() { System.out.println("Playing music"); }
    public void stopMusic() { System.out.println("Stopping music"); }
}

class SecuritySystem {
    public void activate() { System.out.println("Security system activated"); }
    public void deactivate() { System.out.println("Security system deactivated"); }
}

// Facade Class
class HomeAutomationFacade {
    private LightSystem lights;
    private MusicSystem music;
    private SecuritySystem security;

    public HomeAutomationFacade() {
        lights = new LightSystem();
        music = new MusicSystem();
        security = new SecuritySystem();
    }

    public void startEveningRoutine() {
        lights.turnOn();
        music.playMusic();
        security.deactivate();
    }

    public void endEveningRoutine() {
        lights.turnOff();
        music.stopMusic();
        security.activate();
    }
}

// Client
public class FacadePatternDemo {
    public static void main(String[] args) {
        HomeAutomationFacade facade = new HomeAutomationFacade();
        
        System.out.println("Starting evening routine:");
        facade.startEveningRoutine();
        
        System.out.println("\nEnding evening routine:");
        facade.endEveningRoutine();
    }
}
